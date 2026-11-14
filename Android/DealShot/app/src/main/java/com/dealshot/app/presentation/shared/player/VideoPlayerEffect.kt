package com.dealshot.app.presentation.shared.player

sealed interface VideoPlayerEffect {

    data object NavigateBack : VideoPlayerEffect

    data object OpenSubscription : VideoPlayerEffect

    data object OpenLogin : VideoPlayerEffect

    data class PauseAndShowLogin(val limitMs: Long) : VideoPlayerEffect
    data class ResumePlayback(val positionMs: Long = 0L) : VideoPlayerEffect

    data class UpdatePlaybackUrl(
        val playbackUrl: String,
        val positionMs: Long = 0L,
        val shouldPlay: Boolean = true
    ) : VideoPlayerEffect
}

sealed interface WatchProgressEffect {
    data class ProgressLoaded(
        val positionSeconds: Int
    ) : WatchProgressEffect
}
