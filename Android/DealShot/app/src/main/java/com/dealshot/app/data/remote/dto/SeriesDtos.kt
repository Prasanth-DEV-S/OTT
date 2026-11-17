package com.dealshot.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SeriesSeasonsResponseDto(
    val seasons: List<SeasonDto> = emptyList()
)

@Serializable
data class SeasonDto(
    @SerialName("_id")
    val id: String = "",
    val seasonNumber: Int = 1,
    val title: String = "",
    val episodeCount: Int = 0
)

@Serializable
data class SeasonEpisodesResponseDto(
    val episodes: List<EpisodeDto> = emptyList()
)

@Serializable
data class EpisodeDto(
    @SerialName("_id")
    val id: String = "",
    val episodeNumber: Int = 1,
    val title: String = "",
    val thumbnailUrl: String? = null,
    val durationSeconds: Int = 0,
    val isLocked: Boolean = false,
    val progress: EpisodeProgressDto? = null
)

@Serializable
data class EpisodeProgressDto(
    val positionSeconds: Int = 0,
    val completed: Boolean = false
)

@Serializable
data class SeriesProgressResponseDto(
    val episodes: List<SeriesEpisodeProgressDto> = emptyList(),
    val continueWatching: ContinueWatchingDto? = null
)

@Serializable
data class SeriesEpisodeProgressDto(
    val episodeId: String = "",
    val seasonNumber: Int = 1,
    val episodeNumber: Int = 1,
    val title: String = "",
    val thumbnailUrl: String? = null,
    val positionSeconds: Int = 0,
    val durationSeconds: Int = 0,
    val completed: Boolean = false,
    val updatedAt: String? = null
)

@Serializable
data class ContinueWatchingDto(
    val episodeId: String = "",
    val seasonNumber: Int = 1,
    val episodeNumber: Int = 1,
    val title: String = "",
    val thumbnailUrl: String? = null,
    val positionSeconds: Int = 0,
    val durationSeconds: Int = 0,
    val completed: Boolean = false,
    val updatedAt: String? = null
)

@Serializable
data class EpisodeDetailsResponseDto(
    val episode: EpisodeInfoDto? = null,
    val season: EpisodeSeasonInfoDto? = null,
    val series: EpisodeSeriesInfoDto? = null,
    val playback: EpisodePlaybackDto? = null,
    val resumePosition: Int = 0
)

@Serializable
data class EpisodeInfoDto(
    @SerialName("_id")
    val id: String = "",
    val episodeNumber: Int = 1,
    val title: String = "",
    val thumbnailUrl: String? = null,
    val durationSeconds: Int = 0
)

@Serializable
data class EpisodeSeasonInfoDto(
    @SerialName("_id")
    val id: String = "",
    val seasonNumber: Int = 1,
    val title: String = ""
)

@Serializable
data class EpisodeSeriesInfoDto(
    @SerialName("_id")
    val id: String = "",
    val slug: String? = null,
    val title: String = "",
    val thumbnailUrl: String? = null
)

@Serializable
data class EpisodePlaybackDto(
    val masterUrl: String = "",
    val qualities: List<PlaybackQualityDto> = emptyList()
)

@Serializable
data class PlaybackQualityDto(
    val quality: String = "",
    val label: String = ""
)

@Serializable
data class EpisodeProgressResponseDto(
    val episodeId: String = "",
    val positionSeconds: Int = 0,
    val durationSeconds: Int = 0,
    val completed: Boolean = false,
    val updatedAt: String? = null
)

@Serializable
data class SaveEpisodeProgressRequestDto(
    val positionSeconds: Int = 0,
    val durationSeconds: Int = 0
)

@Serializable
data class EpisodeNavigationItemDto(
    val episodeId: String = "",
    val seasonNumber: Int = 1,
    val episodeNumber: Int = 1,
    val title: String = "",
    val thumbnailUrl: String? = null
)

@Serializable
data class NextEpisodeResponseDto(
    val next: EpisodeNavigationItemDto? = null,
    val reason: String? = null
)

@Serializable
data class EpisodeNavigationResponseDto(
    val previous: EpisodeNavigationItemDto? = null,
    val current: EpisodeNavigationItemDto? = null,
    val next: EpisodeNavigationItemDto? = null
)

@Serializable
data class EpisodeAutoplayNextResponseDto(
    val eligible: Boolean = false,
    val next: EpisodeNavigationItemDto? = null,
    val countdownSeconds: Int = 5,
    val reason: String? = null
)

@Serializable
data class EpisodePlaybackQualityResponseDto(
    val quality: String = "",
    val label: String = "",
    val playbackUrl: String = "",
    val expiresAt: String? = null
)

@Serializable
data class SelectEpisodeQualityRequestDto(
    val quality: String = ""
)
