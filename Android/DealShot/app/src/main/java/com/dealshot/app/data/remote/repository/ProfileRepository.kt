package com.dealshot.app.data.remote.repository

import android.content.Context
import android.net.Uri
import com.dealshot.app.data.remote.ApiClient
import com.dealshot.app.data.remote.api.ProfileApiService
import com.dealshot.app.data.remote.api.UserApiService
import com.dealshot.app.data.remote.dto.AvatarDto
import com.dealshot.app.data.remote.dto.CreateProfileRequestDto
import com.dealshot.app.data.remote.dto.DeleteProfileResponseDto
import com.dealshot.app.data.remote.dto.ProfileDto
import com.dealshot.app.data.remote.dto.ProfileResponseDto
import com.dealshot.app.data.remote.dto.SelectPresetAvatarRequestDto
import com.dealshot.app.data.remote.dto.UpdateProfileRequestDto
import com.dealshot.app.data.remote.dto.UpdateAvatarRequestDto
import com.dealshot.app.data.remote.dto.UpdateUserAccountRequestDto
import com.dealshot.app.data.remote.dto.UserAccountDataDto
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import retrofit2.Response

interface ProfileRepository {
    suspend fun createProfile(request: CreateProfileRequestDto): ProfileDto
    suspend fun getProfiles(): List<ProfileDto>
    suspend fun getProfileDetails(profileId: String): ProfileDto
    suspend fun updateProfile(profileId: String, request: UpdateProfileRequestDto): ProfileDto
    suspend fun updateProfileAvatar(profileId: String, request: UpdateAvatarRequestDto): Result<ProfileDto>
    suspend fun deleteProfile(profileId: String): Result<DeleteProfileResponseDto>
    suspend fun getPresetAvatars(): Result<List<AvatarDto>>
    suspend fun selectProfile(profileId: String): Result<ProfileDto>

    // Account level profile APIs (US-05)
    suspend fun getUserAccountProfile(): Result<UserAccountDataDto>
    suspend fun updateUserAccountProfile(name: String?, email: String?, gender: String?): Result<UserAccountDataDto>
    suspend fun selectAccountPresetAvatar(avatarId: String): Result<UserAccountDataDto>
    suspend fun uploadCustomAvatar(context: Context, uri: Uri): Result<UserAccountDataDto>
    suspend fun deleteUserAccount(): Result<UserAccountDataDto>
}

class ProfileRepositoryImpl(
    private val api: ProfileApiService,
    private val userApi: UserApiService = ApiClient.userApi
) : ProfileRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun createProfile(request: CreateProfileRequestDto): ProfileDto {
        val response = api.createProfile(request)
        return handleProfileResponse(response)
    }

    override suspend fun getProfiles(): List<ProfileDto> {
        val response = api.getProfiles()

        if (!response.isSuccessful) {
            handleErrorResponse(response)
        }

        val body = response.body()
            ?: throw IllegalStateException("Unable to load profiles.")

        if (!body.success) {
            throw IllegalStateException("Unable to load profiles.")
        }

        return body.data
    }

    override suspend fun getProfileDetails(profileId: String): ProfileDto {
        val response = api.getProfileDetails(profileId)
        return handleProfileResponse(response)
    }

    override suspend fun updateProfile(
        profileId: String,
        request: UpdateProfileRequestDto
    ): ProfileDto {
        val response = api.updateProfile(profileId, request)
        return handleProfileResponse(response)
    }

    override suspend fun updateProfileAvatar(
        profileId: String,
        request: UpdateAvatarRequestDto
    ): Result<ProfileDto> {
        val cleanId = profileId.trim()
        if (cleanId.isBlank()) return Result.failure(IllegalArgumentException("Profile ID cannot be empty"))

        return try {
            val response = api.updateProfileAvatar(cleanId, request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to update profile avatar"))
                }
            } else {
                val errorMsg = parseErrorMessage(response)
                    ?: when (response.code()) {
                        400 -> "Invalid avatar request"
                        401 -> "Session expired. Please log in again."
                        404 -> "Profile not found"
                        else -> "Failed to update profile avatar (${response.code()})"
                    }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteProfile(profileId: String): Result<DeleteProfileResponseDto> {
        val cleanId = profileId.trim()
        if (cleanId.isBlank()) return Result.failure(IllegalArgumentException("Profile ID cannot be empty"))

        return try {
            val response = api.deleteProfile(cleanId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    Result.success(body)
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to delete profile"))
                }
            } else {
                val errorMsg = parseErrorMessage(response)
                val exception = when (response.code()) {
                    401 -> Exception(errorMsg ?: "Session expired. Please log in again.")
                    403 -> Exception(errorMsg ?: "You are not authorized to delete this profile.")
                    404 -> Exception(errorMsg ?: "Profile not found.")
                    409 -> Exception(errorMsg ?: "The last remaining profile cannot be deleted.")
                    else -> Exception(errorMsg ?: "Failed to delete profile (${response.code()})")
                }
                Result.failure(exception)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPresetAvatars(): Result<List<AvatarDto>> {
        return try {
            val response = api.getPresetAvatars()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    val sorted = body.data.sortedBy { it.sortOrder }
                    Result.success(sorted)
                } else {
                    Result.success(emptyList())
                }
            } else {
                Result.failure(Exception("Failed to load avatars (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun selectProfile(profileId: String): Result<ProfileDto> {
        val cleanId = profileId.trim()
        if (cleanId.isBlank()) return Result.failure(IllegalArgumentException("Profile ID cannot be empty"))

        return try {
            val response = api.selectProfile(cleanId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to select profile"))
                }
            } else {
                val errorMsg = parseErrorMessage(response)
                    ?: when (response.code()) {
                        401 -> "Session expired. Please log in again."
                        403 -> "Permission denied."
                        404 -> "Profile not found."
                        else -> "Failed to select profile (${response.code()})"
                    }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserAccountProfile(): Result<UserAccountDataDto> {
        return try {
            val response = userApi.getUserAccountProfile()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to load account profile"))
                }
            } else {
                Result.failure(Exception("Failed to load account profile (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserAccountProfile(
        name: String?,
        email: String?,
        gender: String?
    ): Result<UserAccountDataDto> {
        val cleanName = name?.trim()?.takeIf { it.isNotBlank() }
        val cleanEmail = email?.trim()?.lowercase()?.takeIf { it.isNotBlank() }

        val validGenders = setOf("male", "female", "other", "prefer_not_to_say")
        val cleanGender = gender?.trim()?.lowercase()
            ?.replace(" ", "_")
            ?.takeIf { it in validGenders }

        if (cleanName != null && (cleanName.length < 2 || cleanName.length > 50)) {
            return Result.failure(IllegalArgumentException("Name must be between 2 and 50 characters."))
        }

        val request = UpdateUserAccountRequestDto(
            name = cleanName,
            email = cleanEmail,
            gender = cleanGender
        )

        return try {
            var response = userApi.updateUserAccountProfile(request)
            if (response.code() == 405 || response.code() == 404) {
                response = userApi.updateUserAccountProfilePatch(request)
            }

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    Result.success(body.data)
                } else if (body != null && body.success) {
                    Result.success(
                        UserAccountDataDto(
                            name = cleanName,
                            email = cleanEmail,
                            gender = cleanGender
                        )
                    )
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to update account profile"))
                }
            } else {
                val errorMsg = parseErrorMessage(response)
                val exception = when (response.code()) {
                    400 -> Exception(errorMsg ?: "Validation error in profile update")
                    401 -> Exception("Session expired. Please log in again.")
                    409 -> Exception(errorMsg ?: "Email is already in use by another account.")
                    else -> Exception(errorMsg ?: "Failed to update profile (${response.code()})")
                }
                Result.failure(exception)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun selectAccountPresetAvatar(avatarId: String): Result<UserAccountDataDto> {
        val cleanId = avatarId.trim()
        if (cleanId.isBlank()) return Result.failure(IllegalArgumentException("Avatar ID cannot be empty"))

        return try {
            val response = userApi.selectAccountPresetAvatar(SelectPresetAvatarRequestDto(avatarId = cleanId))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to select preset avatar"))
                }
            } else {
                val errorMsg = parseErrorMessage(response)
                    ?: when (response.code()) {
                        400 -> "Invalid avatar ID"
                        404 -> "Avatar not found"
                        else -> "Failed to select preset avatar (${response.code()})"
                    }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadCustomAvatar(context: Context, uri: Uri): Result<UserAccountDataDto> {
        return try {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
            val validTypes = setOf("image/jpeg", "image/png", "image/webp")
            if (mimeType !in validTypes) {
                return Result.failure(IllegalArgumentException("Unsupported image format. Please select JPEG, PNG, or WebP."))
            }

            val inputStream = contentResolver.openInputStream(uri)
                ?: return Result.failure(IllegalStateException("Could not open image file"))

            val bytes = inputStream.use { it.readBytes() }
            if (bytes.size > 2 * 1024 * 1024) {
                return Result.failure(IllegalArgumentException("Image is too large. Maximum size is 2 MB."))
            }

            val extension = when (mimeType) {
                "image/png" -> "png"
                "image/webp" -> "webp"
                else -> "jpg"
            }

            val requestFile = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            val bodyPart = MultipartBody.Part.createFormData("avatar", "avatar.$extension", requestFile)

            val response = userApi.uploadCustomAvatar(bodyPart)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to upload custom avatar"))
                }
            } else {
                val errorMsg = parseErrorMessage(response)
                val exception = when (response.code()) {
                    400 -> Exception(errorMsg ?: "Invalid avatar upload request")
                    401 -> Exception("Session expired. Please log in again.")
                    413 -> Exception("Avatar image too large. Maximum size is 2 MB.")
                    415 -> Exception("Unsupported image format. Please select JPEG, PNG, or WebP.")
                    else -> Exception(errorMsg ?: "Failed to upload custom avatar (${response.code()})")
                }
                Result.failure(exception)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteUserAccount(): Result<UserAccountDataDto> {
        return try {
            val response = userApi.deleteUserAccount()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    Result.success(body.data ?: UserAccountDataDto())
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to delete account"))
                }
            } else {
                val errorMsg = parseErrorMessage(response)
                val exception = when (response.code()) {
                    401 -> Exception("Session expired. Please log in again.")
                    403 -> Exception("Permission denied.")
                    else -> Exception(errorMsg ?: "Failed to delete account (${response.code()})")
                }
                Result.failure(exception)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun handleProfileResponse(response: Response<ProfileResponseDto>): ProfileDto {
        if (!response.isSuccessful) {
            handleErrorResponse(response)
        }

        val body = response.body()
            ?: throw IllegalStateException("Unable to process profile request.")

        if (!body.success || body.data == null) {
            val message = body.message.takeIf { !it.isNullOrBlank() }
                ?: "Unable to process profile request."
            throw IllegalStateException(message)
        }

        return body.data
    }

    private fun handleErrorResponse(response: Response<*>): Nothing {
        val errorMsg = parseErrorMessage(response)
        if (!errorMsg.isNullOrBlank()) {
            throw IllegalStateException(errorMsg)
        }
        throw HttpException(response)
    }

    private fun parseErrorMessage(response: Response<*>): String? {
        return try {
            val errorBody = response.errorBody()?.string()
            if (!errorBody.isNullOrBlank()) {
                val element = json.parseToJsonElement(errorBody)
                if (element is JsonObject) {
                    element["message"]?.let {
                        if (it is JsonPrimitive) it.content else null
                    }
                } else null
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
