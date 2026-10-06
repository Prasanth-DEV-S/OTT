package com.dealshot.app.data.remote.repository

import com.dealshot.app.data.remote.dto.VideoDto

interface VideoRepository {

    suspend fun getVideos(): List<VideoDto>
}