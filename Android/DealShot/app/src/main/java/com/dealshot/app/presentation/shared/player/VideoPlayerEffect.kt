package com.dealshot.app.presentation.shared.player

sealed interface VideoPlayerEffect {

    data object NavigateBack : VideoPlayerEffect

    data object OpenSubscription : VideoPlayerEffect
    
    data class PauseAndShowLogin(val limitMs: Long) : VideoPlayerEffect
    data class ResumePlayback(val positionMs: Long = 0L) : VideoPlayerEffect
}

sealed interface WatchProgressEffect {
    data class ProgressLoaded(
        val positionSeconds: Int
    ) : WatchProgressEffect
}