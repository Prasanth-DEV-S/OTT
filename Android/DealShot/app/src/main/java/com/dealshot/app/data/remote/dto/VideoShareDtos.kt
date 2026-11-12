package com.dealshot.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class VideoShareResponseDto(
    val success: Boolean = true,
    val videoId: String = "",
    val title: String = "",
    val shareUrl: String = "",
    val deepLink: String? = null,
    val message: String? = null
)
