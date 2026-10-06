package com.dealshot.app.presentation.shared.player

data class VideoPlayerState(
    val title: String = "",
    val description: String = "",
    val videoUrl: String = "",
    val position: Long = 0L,

    val isPremiumUser: Boolean = false,

    // Player state
    val isPlaying: Boolean = true,
    val currentPlaybackPosition: Long = 0L,
    val totalDuration: Long = 0L,

    // UI state
    val isFullscreen: Boolean = false,
    val isControlsVisible: Boolean = true,
    val isMuted: Boolean = false,

    // Settings / subscription
    var showSettings: Boolean = false,
    val showSubscriptionPopup: Boolean = false,

    // Loading / error
    val isLoading: Boolean = true,
    val error: String? = null
)