package com.dealshot.app.data.remote.repository

import com.dealshot.app.data.remote.api.VideoApiService
import com.dealshot.app.data.remote.api.VideoDetailsApiService
import com.dealshot.app.data.remote.dto.HlsPlaybackDataDto
import com.dealshot.app.data.remote.dto.HlsPlaybackRequestDto
import com.dealshot.app.data.remote.dto.HlsQualitiesDataDto
import com.dealshot.app.data.remote.dto.SharedVideoResolveResponseDto
import com.dealshot.app.data.remote.dto.VideoCategoryDto
import com.dealshot.app.data.remote.dto.VideoDetailsDto
import com.dealshot.app.data.remote.dto.VideoDto
import com.dealshot.app.data.remote.dto.VideoShareResponseDto
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class VideoRepositoryImpl(
    private val api: VideoApiService
) : VideoRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getVideos(): List<VideoDto> {
        val response = api.getVideos()

        if (!response.success) {
            throw Exception("Failed to load videos")
        }

        return response.videos
    }

    override suspend fun getVideoCategories(): List<VideoCategoryDto> {
        val response = api.getVideoCategories()

        if (!response.success) {
            throw Exception("Failed to load video categories")
        }

        val excludedCategories = setOf("banner", "images")

        return response.categories.filter { cat ->
            val name = cat.name.trim()
            name.isNotBlank() && name.lowercase() !in excludedCategories
        }
    }

    override suspend fun getFeaturedVideo(): Result<VideoDto> {
        return try {
            val response = api.getFeaturedVideo()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.video != null) {
                    Result.success(body.video)
                } else {
                    Result.failure(Exception(body?.message ?: "Featured video unavailable"))
                }
            } else {
                val errorMsg = parseErrorMessage(response)
                    ?: "Failed to fetch featured video (${response.code()})"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getVideosByCategory(category: String): Result<List<VideoDto>> {
        val cleanCat = category.trim()
        if (cleanCat.isBlank()) return Result.success(emptyList())

        return try {
            val response = api.getVideosByCategory(cleanCat)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    val filtered = body.data
                        .filter { it.isAvailable && it.isPublished }
                        .distinctBy { it.id.ifBlank { it.altId.orEmpty() } }
                    Result.success(filtered)
                } else {
                    Result.success(emptyList())
                }
            } else {
                val errorMsg = parseErrorMessage(response)
                    ?: "Failed to load category videos (${response.code()})"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun generateShareLink(videoId: String): Result<VideoShareResponseDto> {
        val cleanId = videoId.trim()
        if (cleanId.isBlank()) return Result.failure(Exception("Invalid video ID"))

        return try {
            val response = api.generateShareLink(cleanId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    Result.success(body)
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to generate share link"))
                }
            } else {
                val errorMsg = parseErrorMessage(response)
                    ?: "Failed to generate share link (${response.code()})"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resolveSharedVideo(videoId: String): Result<SharedVideoResolveResponseDto> {
        val cleanId = videoId.trim()
        if (cleanId.isBlank()) return Result.failure(Exception("Invalid video ID"))

        return try {
            val response = api.resolveSharedVideo(cleanId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && (body.id != null || body.mongoId != null || body.title != null || body.success)) {
                    Result.success(body)
                } else {
                    val msg = body?.message ?: "This video is not available"
                    Result.failure(Exception(msg))
                }
            } else if (response.code() == 404) {
                val errorMsg = parseErrorMessage(response) ?: "This video is not available"
                Result.failure(Exception(errorMsg))
            } else {
                val errorMsg = parseErrorMessage(response)
                    ?: "Failed to resolve shared video (${response.code()})"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getHlsQualities(videoId: String, profileId: String?): Result<HlsQualitiesDataDto> {
        val cleanId = videoId.trim()
        if (cleanId.isBlank()) return Result.failure(Exception("Invalid video ID"))

        return try {
            val response = api.getHlsQualities(cleanId, profileId?.trim()?.ifBlank { null })
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    Result.success(body.data)
                } else {
                    val msg = body?.message?.ifBlank { null } ?: "Failed to fetch HLS qualities"
                    Result.failure(Exception(msg))
                }
            } else {
                val errorMsg = parseErrorMessage(response)
                    ?: "Failed to fetch HLS qualities (${response.code()})"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun requestHlsPlayback(
        videoId: String,
        mode: String,
        quality: String?,
        profileId: String?
    ): Result<HlsPlaybackDataDto> {
        val cleanId = videoId.trim()
        if (cleanId.isBlank()) return Result.failure(Exception("Invalid video ID"))

        val request = HlsPlaybackRequestDto(
            mode = mode.lowercase(),
            quality = quality?.trim()?.ifBlank { null }
        )

        return try {
            val response = api.requestHlsPlayback(cleanId, request, profileId?.trim()?.ifBlank { null })
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    Result.success(body.data)
                } else {
                    val msg = body?.message?.ifBlank { null } ?: "Failed to request HLS playback URL"
                    Result.failure(Exception(msg))
                }
            } else {
                val errorMsg = parseErrorMessage(response)
                    ?: "Failed to request HLS playback (${response.code()})"
                Result.failure(Exception(errorMsg))
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

class VideoDetailsRepositoryImpl(
    private val api: VideoDetailsApiService
) : VideoDetailsRepository {

    override suspend fun getVideoDetails(videoId: String): VideoDetailsDto {
        val response = api.getVideoDetails(videoId)

        if (!response.isSuccessful) {
            throw retrofit2.HttpException(response)
        }

        val body = response.body()
            ?: throw IllegalStateException("Video details are unavailable.")

        if (!body.success) {
            throw IllegalStateException("Unable to load video details.")
        }

        return body.video
    }
}
