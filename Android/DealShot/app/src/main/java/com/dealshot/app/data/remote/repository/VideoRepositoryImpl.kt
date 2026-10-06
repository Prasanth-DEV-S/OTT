package com.dealshot.app.data.remote.repository

import com.dealshot.app.data.remote.api.VideoApiService
import com.dealshot.app.data.remote.dto.VideoDto

class VideoRepositoryImpl(
    private val api: VideoApiService
) : VideoRepository {

    override suspend fun getVideos(): List<VideoDto> {
        val response = api.getVideos()

        if (!response.success) {
            throw Exception("Failed to load videos")
        }

        return response.data
    }
}