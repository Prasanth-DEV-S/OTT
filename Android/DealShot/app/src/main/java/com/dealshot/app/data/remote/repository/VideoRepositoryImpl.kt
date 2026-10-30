package com.dealshot.app.data.remote.repository

import com.dealshot.app.data.remote.api.VideoApiService
import com.dealshot.app.data.remote.dto.VideoCategoryDto
import com.dealshot.app.data.remote.dto.VideoDto
import com.dealshot.app.data.remote.api.VideoDetailsApiService
import com.dealshot.app.data.remote.dto.VideoDetailsDto

class VideoRepositoryImpl(
    private val api: VideoApiService
) : VideoRepository {

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
        val allowedCategories = setOf("Home", "National", "International", "Motivational",
            "Inspiration", "Academic")

        return response.categories.filter { it.name in allowedCategories }
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