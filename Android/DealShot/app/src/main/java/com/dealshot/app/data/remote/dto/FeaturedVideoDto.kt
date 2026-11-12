package com.dealshot.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class FeaturedVideoResponseDto(
    val success: Boolean = false,
    val video: VideoDto? = null,
    val message: String? = null
)
