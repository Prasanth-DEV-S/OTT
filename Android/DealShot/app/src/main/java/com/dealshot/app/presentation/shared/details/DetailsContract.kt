package com.dealshot.app.presentation.shared.details

import com.dealshot.app.data.remote.dto.ContinueWatchingDto
import com.dealshot.app.data.remote.dto.EpisodeAutoplayNextResponseDto
import com.dealshot.app.data.remote.dto.EpisodeDetailsResponseDto
import com.dealshot.app.data.remote.dto.EpisodeDto
import com.dealshot.app.data.remote.dto.EpisodeNavigationResponseDto
import com.dealshot.app.data.remote.dto.SeasonDto
import com.dealshot.app.data.remote.dto.VideoDto
import com.dealshot.app.data.remote.dto.VideoDetailsDto
import com.dealshot.app.data.video.Video

data class DetailsState(
    val video: Video? = null,
    val videoDetails: VideoDetailsDto? = null,
    val nextVideos: List<VideoDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLiked: Boolean = false,
    val isReported: Boolean = false,
    val isInMyWatchLater: Boolean = false,
    val isUpdatingMyWatchLater: Boolean = false,
    val isUpdatingLike: Boolean = false,

    // Series attributes
    val isSeries: Boolean = false,
    val seasons: List<SeasonDto> = emptyList(),
    val selectedSeasonNumber: Int = 1,
    val episodes: List<EpisodeDto> = emptyList(),
    val isLoadingSeasons: Boolean = false,
    val isLoadingEpisodes: Boolean = false,
    val continueWatching: ContinueWatchingDto? = null,
    val activeEpisodeDetails: EpisodeDetailsResponseDto? = null,
    val episodeNavigation: EpisodeNavigationResponseDto? = null,
    val autoplayInfo: EpisodeAutoplayNextResponseDto? = null,
    val selectedEpisodeQuality: String? = null,
    val episodePlaybackUrl: String? = null
)

sealed interface DetailsIntent {

    data class Initialize(val video: Video) : DetailsIntent

    data class SelectNextVideo(val videoDto: VideoDto) : DetailsIntent

    data class SelectSeason(val seasonNumber: Int) : DetailsIntent

    data class SelectEpisode(val episode: EpisodeDto) : DetailsIntent

    data class SelectEpisodeById(val episodeId: String) : DetailsIntent

    data class SelectEpisodeQuality(val episodeId: String, val quality: String) : DetailsIntent

    data class CheckAutoplayNext(val episodeId: String) : DetailsIntent

    data object WatchFirstEpisodeClicked : DetailsIntent

    data object PreviousEpisodeClicked : DetailsIntent

    data object NextEpisodeClicked : DetailsIntent

    data object BackClicked : DetailsIntent

    data object PlayVideoClicked : DetailsIntent

    data object MyListClicked : DetailsIntent

    data object LikeClicked : DetailsIntent

    data object ShareClicked : DetailsIntent

    data object ClearError : DetailsIntent
}

sealed interface DetailsEffect {

    data object NavigateBack : DetailsEffect

    data object OpenPlayer : DetailsEffect

    data class PlayEpisode(
        val episodeId: String,
        val playbackUrl: String,
        val episodeTitle: String,
        val resumePositionMs: Long
    ) : DetailsEffect

    data class UpdateEpisodeQualityUrl(
        val playbackUrl: String
    ) : DetailsEffect

    data object OpenMyList : DetailsEffect

    data object Liked : DetailsEffect

    data object OpenLogin : DetailsEffect

    data object ShareVideo : DetailsEffect
}
