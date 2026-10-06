package com.dealshot.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VideoDto(

    @SerialName("_id")
    val id: String,

    val slug: String? = null,
    val category: String? = null,
    val description: String? = null,
    val duration: String? = null,
    val durationSeconds: Int? = null,
    val featured: Boolean = false,
    val isPremium: Boolean = false,
    val subdescription: String? = null,
    val thumbnail: String? = null,
    val thumbnailUrl: String? = null,
    val title: String? = null,
    val updatedAt: String? = null,
    val createdAt: String? = null,
    val videoUrl: String? = null,
    val videos: String? = null,
    val promoVideoUrl: String? = null
)