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

@Serializable
data class VideoCategoriesResponseDto(
    val success: Boolean = false,
    val categories: List<VideoCategoryDto> = emptyList()
)

@Serializable
data class VideoCategoryDto(
    val name: String,
    val count: Int = 0
)


@Serializable
data class VideoDetailsResponseDto(
    val success: Boolean = false,
    val video: VideoDetailsDto
)

@Serializable
data class VideoDetailsDto(
    val id: String,
    @SerialName("_id")
    val mongoId: String,
    val slug: String,
    val title: String,
    val description: String,
    val subdescription: String? = null,
    val promoVideoUrl: String? = null,
    val thumbnail: String? = null,
    val thumbnailUrl: String? = null,
    val videoUrl: String? = null,
    val duration: String? = null,
    val durationSeconds: Int? = null,
    val isPremium: Boolean = false,
    val category: String? = null,
    val featured: Boolean = false,
    val views: Int = 0,
    val likesCount: Int = 0,
    val createdAt: String? = null
)