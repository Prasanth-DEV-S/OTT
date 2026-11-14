package com.dealshot.app.data.remote.repository.like

import com.dealshot.app.data.remote.api.LikedVideosApiService
import com.dealshot.app.data.remote.dto.like.LikeMutationResponseDto
import com.dealshot.app.data.remote.dto.like.LikeVideoRequestDto
import com.dealshot.app.data.remote.dto.VideoDto
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import retrofit2.HttpException

class LikedVideosRepositoryImpl(
    private val api: LikedVideosApiService
) : LikedVideosRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getLikedVideos(): List<VideoDto> {
        return try {
            val response = api.getLikedVideos()
            if (!response.isSuccessful) {
                if (response.code() == 401) {
                    return emptyList()
                }
                throw HttpException(response)
            }
            val body = response.body() ?: return emptyList()
            if (!body.success) return emptyList()
            body.videos
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun likeVideo(
        videoId: String
    ): LikeMutationResponseDto {
        require(videoId.isNotBlank()) {
            "Video ID cannot be empty."
        }

        val response = try {
            val res = api.likeVideoPath(videoId)
            if (res.isSuccessful) res else api.likeVideo(LikeVideoRequestDto(videoId = videoId))
        } catch (e: Exception) {
            try {
                api.likeVideo(LikeVideoRequestDto(videoId = videoId))
            } catch (ex: Exception) {
                throw e
            }
        }

        if (!response.isSuccessful) {
            throw HttpException(response)
        }

        val body = response.body()
            ?: error("Unable to like video.")

        if (!body.success) {
            error("Unable to like video.")
        }

        return body
    }

    override suspend fun toggleVideoLike(
        videoId: String
    ): Result<LikeMutationResponseDto> {
        val cleanId = videoId.trim()
        if (cleanId.isBlank()) return Result.failure(IllegalArgumentException("Video ID cannot be empty"))

        return try {
            val response = api.likeVideoPath(cleanId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    Result.success(body)
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to update video like status"))
                }
            } else {
                val errorMsg = parseErrorMessage(response) ?: "Failed to update video like status (${response.code()})"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getVideoLikeStatus(
        videoId: String
    ): Boolean {
        return try {
            val response = api.getVideoLikeStatus(videoId)
            if (!response.isSuccessful) {
                if (response.code() == 401) {
                    return false
                }
                throw HttpException(response)
            }
            val body = response.body() ?: return false
            if (!body.success) return false
            body.data.myLikeStatus
        } catch (_: Exception) {
            false
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
