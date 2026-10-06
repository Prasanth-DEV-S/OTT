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
) {
//    val currentPosition: Any
//    val duration: Any
}
/*
sealed interface VideoPlayerIntent {

    data object BackClicked : VideoPlayerIntent

    data object VideoClicked : VideoPlayerIntent

    data object PipClicked : VideoPlayerIntent
    data object CastClicked : VideoPlayerIntent

    data object PlayPauseClicked : VideoPlayerIntent

    data object RewindClicked : VideoPlayerIntent

    data object ForwardClicked : VideoPlayerIntent

    data object FullscreenClicked : VideoPlayerIntent

    data object SettingsClicked : VideoPlayerIntent

    data object SettingsDismissed : VideoPlayerIntent
    data object ClearError : VideoPlayerIntent
    data object SubscriptionClicked : VideoPlayerIntent

    data class PlaybackSpeedSelected(
        val speed: Float
    ) : VideoPlayerIntent

    data class SliderChanged(
        val position: Float
    ) : VideoPlayerIntent

    data object SliderChangeFinished : VideoPlayerIntent

    data class PlaybackStateChanged(
        val isPlaying: Boolean
    ) : VideoPlayerIntent

    data class PlaybackProgressChanged(
        val currentPosition: Long,
        val duration: Long
    ) : VideoPlayerIntent

    data object PlaybackReady : VideoPlayerIntent

    data object SubscriptionDismissed : VideoPlayerIntent

    data object SubscribeClicked : VideoPlayerIntent

    data object PlayerError : VideoPlayerIntent

    data class Initialize(
        val title: String,
        val description: String,
        val videoUrl: String,
        val isPremiumUser: Boolean,
        val position: Long
    ) : VideoPlayerIntent
}*/

sealed interface VideoPlayerEffect {

    data object NavigateBack : VideoPlayerEffect

    data object OpenSubscription : VideoPlayerEffect
}