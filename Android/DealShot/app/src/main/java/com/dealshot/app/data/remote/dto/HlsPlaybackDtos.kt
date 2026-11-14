package com.dealshot.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class HlsQualityDto(
    val quality: String = "",
    val label: String = "",
    val resolution: String = "",
    val bandwidthBps: Long = 0L,
    val averageBandwidthBps: Long = 0L
)

@Serializable
data class HlsQualitiesDataDto(
    val videoId: String = "",
    val streamingType: String = "hls",
    val autoAvailable: Boolean = true,
    val qualities: List<HlsQualityDto> = emptyList()
)

@Serializable
data class HlsQualitiesResponseDto(
    val success: Boolean = false,
    val message: String = "",
    val data: HlsQualitiesDataDto? = null
)

@Serializable
data class HlsPlaybackRequestDto(
    val mode: String, // "auto" or "manual"
    val quality: String? = null // e.g. "720p", "1080p"
)

@Serializable
data class HlsPlaybackDataDto(
    val mode: String = "auto",
    val selectedQuality: String = "auto",
    val resolution: String? = null,
    val bandwidthBps: Long? = null,
    val averageBandwidthBps: Long? = null,
    val availableQualities: List<String> = emptyList(),
    val streamingType: String = "hls",
    val playbackUrl: String = "",
    val expiresIn: Long = 300L
)

@Serializable
data class HlsPlaybackResponseDto(
    val success: Boolean = false,
    val message: String = "",
    val data: HlsPlaybackDataDto? = null
)

/** Helper extension to resolve relative playback URLs into full staging/production URLs. */
fun String.toAbsolutePlaybackUrl(baseUrl: String = "https://stg-api.dealshot.in"): String {
    val cleanUrl = this.trim()
    if (cleanUrl.isBlank()) return ""
    if (cleanUrl.startsWith("http://") || cleanUrl.startsWith("https://")) {
        return cleanUrl
    }
    val cleanBase = baseUrl.trimEnd('/')
    val cleanPath = if (cleanUrl.startsWith("/")) cleanUrl else "/$cleanUrl"
    return "$cleanBase$cleanPath"
}
