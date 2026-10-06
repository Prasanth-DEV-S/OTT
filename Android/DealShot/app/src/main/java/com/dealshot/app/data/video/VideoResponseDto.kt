package com.dealshot.app.data.video

import com.dealshot.app.data.remote.dto.VideoDto
import kotlinx.serialization.Serializable

@Serializable
data class VideoResponseDto(
    val success: Boolean,
    val count: Int,
    val data: List<VideoDto>
)