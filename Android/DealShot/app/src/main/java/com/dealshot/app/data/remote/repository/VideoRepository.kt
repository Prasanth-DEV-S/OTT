package com.dealshot.app.data.remote.repository

import com.dealshot.app.data.remote.dto.HlsPlaybackDataDto
import com.dealshot.app.data.remote.dto.HlsQualitiesDataDto
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

    suspend fun getHlsQualities(videoId: String, profileId: String? = null): Result<HlsQualitiesDataDto>

    suspend fun requestHlsPlayback(videoId: String, mode: String, quality: String? = null, profileId: String? = null): Result<HlsPlaybackDataDto>
}

interface VideoDetailsRepository {
    suspend fun getVideoDetails(videoId: String): VideoDetailsDto
}
