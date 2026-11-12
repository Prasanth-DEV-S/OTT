package com.dealshot.app.data.remote.repository

import com.dealshot.app.data.remote.api.WatchNextApiService
import com.dealshot.app.data.remote.dto.VideoDto
import com.dealshot.app.data.remote.dto.WatchNextAddResponseDto
import com.dealshot.app.data.remote.dto.WatchNextDeleteResponseDto
import com.dealshot.app.data.preferences.UserPreferences
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

interface WatchNextRepository {
    suspend fun getWatchNext(profileId: String? = null): Result<List<VideoDto>>
    suspend fun addToWatchNext(videoId: String, profileId: String? = null): Result<WatchNextAddResponseDto>
    suspend fun removeFromWatchNext(videoId: String, profileId: String? = null): Result<WatchNextDeleteResponseDto>
}

class WatchNextRepositoryImpl(
    private val api: WatchNextApiService,
    private val userPreferences: UserPreferences? = null
) : WatchNextRepository {

    private val json = Json { ignoreUnknownKeys = true }

    private suspend fun resolveProfileId(customProfileId: String?): String? {
        val clean = customProfileId?.trim()
        if (!clean.isNullOrBlank()) return clean
        return userPreferences?.activeProfileId?.first()?.trim()?.ifBlank { null }
    }

    override suspend fun getWatchNext(profileId: String?): Result<List<VideoDto>> {
        val activeId = resolveProfileId(profileId)
        return try {
            val response = api.getWatchNext(profileId = activeId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    Result.success(body.data)
                } else {
                    Result.success(emptyList())
                }
            } else {
                Result.failure(Exception("Failed to load Watch Next items (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addToWatchNext(videoId: String, profileId: String?): Result<WatchNextAddResponseDto> {
        val cleanVideoId = videoId.trim()
        if (cleanVideoId.isBlank()) return Result.failure(IllegalArgumentException("Video ID cannot be empty"))
        val activeId = resolveProfileId(profileId)

        return try {
            val response = api.addToWatchNext(videoId = cleanVideoId, profileId = activeId)
            if (response.isSuccessful || response.code() == 201) {
                val body = response.body()
                if (body != null && body.success) {
                    Result.success(body)
                } else {
                    Result.success(WatchNextAddResponseDto(success = true, message = "Added to Watch Next"))
                }
            } else {
                val msg = parseErrorMessage(response) ?: "Failed to add video to Watch Next (${response.code()})"
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeFromWatchNext(videoId: String, profileId: String?): Result<WatchNextDeleteResponseDto> {
        val cleanVideoId = videoId.trim()
        if (cleanVideoId.isBlank()) return Result.failure(IllegalArgumentException("Video ID cannot be empty"))
        val activeId = resolveProfileId(profileId)

        return try {
            val response = api.removeFromWatchNext(videoId = cleanVideoId, profileId = activeId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    Result.success(body)
                } else {
                    Result.success(WatchNextDeleteResponseDto(success = true, message = "Removed from Watch Next"))
                }
            } else {
                val msg = parseErrorMessage(response) ?: "Failed to remove video from Watch Next (${response.code()})"
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseErrorMessage(response: retrofit2.Response<*>): String? {
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
