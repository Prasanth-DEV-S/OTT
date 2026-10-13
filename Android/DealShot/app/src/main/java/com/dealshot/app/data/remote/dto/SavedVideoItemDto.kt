package com.dealshot.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SavedVideoItemDto(
    @SerialName("_id")
    val savedItemId: String,

    val userId: String? = null,

    val videoId: VideoDto,

    val createdAt: String? = null,
    val updatedAt: String? = null
)