package com.dealshot.app.data.remote.repository

import com.dealshot.app.data.remote.api.ProfileApiService
import com.dealshot.app.data.remote.dto.CreateProfileRequestDto
import com.dealshot.app.data.remote.dto.ProfileDto
import com.dealshot.app.data.remote.dto.ProfileResponseDto
import com.dealshot.app.data.remote.dto.UpdateProfileRequestDto
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import retrofit2.Response

interface ProfileRepository {
    suspend fun createProfile(request: CreateProfileRequestDto): ProfileDto
    suspend fun getProfiles(): List<ProfileDto>
    suspend fun getProfileDetails(profileId: String): ProfileDto
    suspend fun updateProfile(profileId: String, request: UpdateProfileRequestDto): ProfileDto
}

class ProfileRepositoryImpl(
    private val api: ProfileApiService
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
        val errorJson = response.errorBody()?.string()
        if (!errorJson.isNullOrBlank()) {
            try {
                val errorDto = json.decodeFromString<ProfileResponseDto>(errorJson)
                if (!errorDto.message.isNullOrBlank()) {
                    throw IllegalStateException(errorDto.message)
                }
            } catch (e: Exception) {
                if (e is IllegalStateException) throw e
            }
        }
        throw HttpException(response)
    }
}
