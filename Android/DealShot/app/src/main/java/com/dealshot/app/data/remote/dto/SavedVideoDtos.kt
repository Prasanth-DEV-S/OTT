package com.dealshot.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SavedVideosListResponseDto(
    val success: Boolean = false,
    val count: Int = 0,
    val data: List<SavedVideoItemDto> = emptyList()
)

@Serializable
data class SavedVideoMutationResponseDto(
    val success: Boolean = false,
    val message: String = "",
    val data: VideoDto? = null
)

@Serializable
data class SavedVideoRemoveResponseDto(
    val success: Boolean = false,
    val message: String = ""
)

@Serializable
data class LikedVideosListResponseDto(
    val success: Boolean = false,
    val count: Int = 0,
    val videos: List<VideoDto> = emptyList()
)