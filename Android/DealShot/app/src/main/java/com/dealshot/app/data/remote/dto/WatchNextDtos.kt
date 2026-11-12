package com.dealshot.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class WatchNextListResponseDto(
    val success: Boolean = false,
    val count: Int = 0,
    val data: List<VideoDto> = emptyList(),
    val message: String? = null
)

@Serializable
data class WatchNextAddResponseDto(
    val success: Boolean = false,
    val message: String? = null,
    val data: WatchNextItemDataDto? = null
)

@Serializable
data class WatchNextItemDataDto(
    val id: String? = null,
    val videoId: String? = null
)

@Serializable
data class WatchNextDeleteResponseDto(
    val success: Boolean = false,
    val message: String? = null
)
