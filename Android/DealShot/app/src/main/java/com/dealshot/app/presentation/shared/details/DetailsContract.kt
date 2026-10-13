package com.dealshot.app.presentation.shared.details

import com.dealshot.app.data.remote.dto.VideoDetailsDto
import com.dealshot.app.data.video.Video


data class DetailsState(
    val video: Video? = null,
    val videoDetails: VideoDetailsDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLiked: Boolean = false,
    val isInMyWatchLater: Boolean = false,
    val isUpdatingMyWatchLater: Boolean = false,
    val isUpdatingLike: Boolean = false
)

sealed interface DetailsIntent {

    data class Initialize(val video: Video) : DetailsIntent

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

    data object OpenMyList : DetailsEffect

    data object Liked : DetailsEffect

    data object OpenLogin : DetailsEffect

    data object ShareVideo : DetailsEffect
}