package com.dealshot.app.presentation.shared.player

sealed interface VideoPlayerIntent {

    data class Initialize(
        val videoId: String,
        val title: String,
        val description: String,
        val videoUrl: String,
        val isPremiumUser: Boolean,
        val position: Long,
        val category: String = "",
        val error: String? = null,
        val message : String? = null
    ) : VideoPlayerIntent

    data object BackClicked : VideoPlayerIntent

    data object SubscriptionClicked : VideoPlayerIntent

    data object LoginClicked : VideoPlayerIntent

    data object SettingsClicked : VideoPlayerIntent

    data object SettingsDismissed : VideoPlayerIntent

    data object FullscreenClicked : VideoPlayerIntent

    data object PlayPauseClicked : VideoPlayerIntent

    data object ClearError : VideoPlayerIntent
    data object CheckAuthStatus : VideoPlayerIntent
    data object SubscriptionPopupDismissed : VideoPlayerIntent

    data object PlaybackReady : VideoPlayerIntent

    data object SaveProgressRequested : VideoPlayerIntent

    data object LoadAvailableQualities : VideoPlayerIntent

    data class SelectHlsQuality(
        val quality: String,
        val mode: String = if (quality.lowercase() == "auto") "auto" else "manual"
    ) : VideoPlayerIntent

    data class PlaybackStateChanged(
        val isPlaying: Boolean
    ) : VideoPlayerIntent

    data class PlaybackError(
        val error: Throwable
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

sealed interface WatchProgressIntent {
    data class LoadProgress(val videoId: String) : WatchProgressIntent

    data class SaveProgress(
        val videoId: String,
        val positionSeconds: Int,
        val durationSeconds: Int
    ) : WatchProgressIntent

    data object ClearError : WatchProgressIntent
}
