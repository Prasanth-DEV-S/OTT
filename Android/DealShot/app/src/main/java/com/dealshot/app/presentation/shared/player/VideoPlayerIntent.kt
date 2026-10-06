package com.dealshot.app.presentation.shared.player

sealed interface VideoPlayerIntent {

    data class Initialize(
        val title: String,
        val description: String,
        val videoUrl: String,
        val isPremiumUser: Boolean,
        val position: Long
    ) : VideoPlayerIntent

    data object BackClicked : VideoPlayerIntent

    data object SubscriptionClicked : VideoPlayerIntent

    data object SettingsClicked : VideoPlayerIntent

    data object SettingsDismissed : VideoPlayerIntent

    data object FullscreenClicked : VideoPlayerIntent

    data object PlayPauseClicked : VideoPlayerIntent

    data object ClearError : VideoPlayerIntent

    data object PlaybackReady : VideoPlayerIntent

    data class PlaybackStateChanged(
        val isPlaying: Boolean
    ) : VideoPlayerIntent

    data class PlaybackProgressChanged(
        val currentPosition: Long,
        val duration: Long
    ) : VideoPlayerIntent

    data class SliderChanged(
        val position: Long
    ) : VideoPlayerIntent

    data object SliderChangeFinished : VideoPlayerIntent
}