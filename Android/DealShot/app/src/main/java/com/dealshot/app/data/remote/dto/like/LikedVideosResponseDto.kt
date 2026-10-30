package com.dealshot.app.data.remote.dto.like

import com.dealshot.app.data.remote.dto.VideoDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LikedVideosResponseDto(
    val success: Boolean = false,
    val count: Int = 0,
    val videos: List<VideoDto> = emptyList(),
    val pagination: LikedVideosPaginationDto? = null
)

@Serializable
data class LikedVideosPaginationDto(
    val total: Int = 0,
    val page: Int = 0,
    val limit: Int = 0,
    val totalPages: Int = 0
)

@Serializable
data class LikeMutationResponseDto(
    val success: Boolean = false,
    val message: String = "",
    val isLiked: Boolean = false,
    val likesCount: Int = 0
)

@Serializable
data class VideoLikeStatusResponseDto(
    val success: Boolean = false,
    val data: VideoLikeStatusDataDto
)

@Serializable
data class VideoLikeStatusDataDto(
    val videoId: String = "",
    val video: VideoDto? = null,
    @SerialName("myLikeStatus")
    val myLikeStatus: Boolean = false
)

@Serializable
data class LikeVideoRequestDto(
    @SerialName("videoId")
    val videoId: String = ""
)