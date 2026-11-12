package com.dealshot.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VideoSearchResponseDto(
    val success: Boolean = false,
    val items: List<VideoSearchItemDto> = emptyList(),
    val pagination: PaginationDto? = null,
    val message: String? = null
)

@Serializable
data class VideoSearchItemDto(
    @SerialName("_id")
    val mongoId: String? = null,
    val id: String? = null,
    val title: String? = null,
    val thumbnail: String? = null,
    val thumbnailUrl: String? = null,
    val shortDescription: String? = null,
    val description: String? = null,
    val category: String? = null,
    val language: String? = null,
    val duration: String? = null,
    val durationSeconds: Double? = null,
    val isPremium: Boolean = false,
    val featured: Boolean = false,
    val videoUrl: String? = null,
    val promoVideoUrl: String? = null
) {
    fun toVideoDto(): VideoDto {
        val effectiveId = mongoId?.ifBlank { null }
            ?: id?.ifBlank { null }
            ?: title?.hashCode()?.toString()
            ?: "search_item"

        val remoteThumb = listOfNotNull(thumbnailUrl, thumbnail)
            .firstOrNull { it.isNotBlank() && (it.startsWith("http://") || it.startsWith("https://")) }
        val effectiveThumb = remoteThumb ?: thumbnailUrl?.ifBlank { null } ?: thumbnail

        return VideoDto(
            id = effectiveId,
            altId = id,
            title = title,
            description = description ?: shortDescription,
            shortDescription = shortDescription,
            category = category,
            language = language,
            duration = duration,
            durationSeconds = durationSeconds,
            thumbnail = effectiveThumb,
            thumbnailUrl = effectiveThumb,
            isPremium = isPremium,
            featured = featured,
            videoUrl = videoUrl,
            promoVideoUrl = promoVideoUrl
        )
    }
}

@Serializable
data class PaginationDto(
    val page: Int = 1,
    val limit: Int = 20,
    val total: Int = 0,
    val totalPages: Int = 0,
    val hasNext: Boolean = false
)
