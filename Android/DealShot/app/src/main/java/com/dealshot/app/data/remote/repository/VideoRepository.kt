package com.dealshot.app.data.remote.repository

import com.dealshot.app.data.remote.dto.SharedVideoResolveResponseDto
import com.dealshot.app.data.remote.dto.VideoCategoryDto
import com.dealshot.app.data.remote.dto.VideoDetailsDto
import com.dealshot.app.data.remote.dto.VideoDto
import com.dealshot.app.data.remote.dto.VideoShareResponseDto

interface VideoRepository {

    suspend fun getVideos(): List<VideoDto>

    suspend fun getVideoCategories(): List<VideoCategoryDto>

    suspend fun getFeaturedVideo(): Result<VideoDto>

    suspend fun getVideosByCategory(category: String): Result<List<VideoDto>>

    suspend fun generateShareLink(videoId: String): Result<VideoShareResponseDto>

    suspend fun resolveSharedVideo(videoId: String): Result<SharedVideoResolveResponseDto>
}

interface VideoDetailsRepository {
    suspend fun getVideoDetails(videoId: String): VideoDetailsDto
}
