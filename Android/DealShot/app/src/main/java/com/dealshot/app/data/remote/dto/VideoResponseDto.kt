package com.dealshot.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class VideoResponseDto(

    val success: Boolean,

    val count: Int,

    val data: List<VideoDto>
)