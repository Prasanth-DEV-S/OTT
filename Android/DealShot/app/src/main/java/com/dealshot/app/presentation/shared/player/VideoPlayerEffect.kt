package com.dealshot.app.presentation.shared.player

sealed interface VideoPlayerEffect {

    data object NavigateBack : VideoPlayerEffect

    data object OpenSubscription : VideoPlayerEffect
}

sealed interface WatchProgressEffect {
    data class ProgressLoaded(
        val positionSeconds: Int
    ) : WatchProgressEffect
}