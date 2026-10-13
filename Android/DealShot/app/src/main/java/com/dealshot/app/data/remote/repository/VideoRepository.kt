package com.dealshot.app.data.remote.repository

import com.dealshot.app.data.remote.dto.VideoCategoryDto
import com.dealshot.app.data.remote.dto.VideoDto
import com.dealshot.app.data.remote.dto.VideoDetailsDto

interface VideoRepository {

    suspend fun getVideos(): List<VideoDto>

    suspend fun getVideoCategories(): List<VideoCategoryDto>

}
interface VideoDetailsRepository {
    suspend fun getVideoDetails(videoId: String): VideoDetailsDto
}