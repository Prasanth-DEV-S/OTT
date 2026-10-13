package com.dealshot.app.presentation.shared.details

data class VideoPlayerState(
    val title: String = "",
    val description: String = "",
    val videoUrl: String = "",
    val position: Long = 0L,
    val isPremiumUser: Boolean = false,

    val isPlaying: Boolean = true,
    val currentPlaybackPosition: Long = 0L,
    val totalDuration: Long = 0L,

    val isFullscreen: Boolean = false,
    val isControlsVisible: Boolean = true,
    val isMuted: Boolean = false,
    val showSettings: Boolean = false,
    val showSubscriptionPopup: Boolean = false,
    val isLoading: Boolean = true,
    // Add this
    val error: String? = null
)
sealed interface VideoPlayerEffect {

    data object NavigateBack : VideoPlayerEffect

    data object OpenSubscription : VideoPlayerEffect
}