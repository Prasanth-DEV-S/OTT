package com.dealshot.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CategoryVideosResponseDto(
    val success: Boolean = false,
    val category: String? = null,
    val count: Int = 0,
    val data: List<VideoDto> = emptyList(),
    val message: String? = null
)
