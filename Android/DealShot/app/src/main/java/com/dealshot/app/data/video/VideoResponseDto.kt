// path: app/src/main/java/com/dealshot/app/data/video/VideoResponseDto.kt

package com.dealshot.app.data.video

import com.dealshot.app.data.remote.dto.VideoDto
import kotlinx.serialization.Serializable

@Serializable
data class VideoResponseDto(
    val success: Boolean = false,
    val videos: List<VideoDto> = emptyList(),
    val page: Int = 1,
    val limit: Int = 0,
    val total: Int = 0,
    val pages: Int = 0
)