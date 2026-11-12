package com.dealshot.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SharedVideoResolveResponseDto(
    val success: Boolean = true,
    @SerialName("_id")
    val mongoId: String? = null,
    val id: String? = null,
    val title: String? = null,
    val thumbnail: String? = null,
    val thumbnailUrl: String? = null,
    val type: String? = null,
    val description: String? = null,
    val videoUrl: String? = null,
    val message: String? = null
) {
    fun getEffectiveId(): String {
        return id?.ifBlank { null } ?: mongoId.orEmpty()
    }

    fun toVideoDto(): VideoDto {
        val remoteThumb = listOfNotNull(thumbnailUrl, thumbnail)
            .firstOrNull { it.isNotBlank() && (it.startsWith("http://") || it.startsWith("https://")) }
        val effectiveThumb = remoteThumb ?: thumbnailUrl?.ifBlank { null } ?: thumbnail
        return VideoDto(
            id = getEffectiveId(),
            altId = id,
            title = title,
            description = description,
            thumbnail = effectiveThumb,
            thumbnailUrl = effectiveThumb,
            videoUrl = videoUrl
        )
    }
}
