package com.dealshot.app.presentation.shared.player

import com.dealshot.app.data.remote.dto.HlsQualityDto
import com.dealshot.app.data.remote.dto.WatchProgressDto

data class VideoPlayerState(
    val videoId: String = "",
    val title: String = "",
    val description: String = "",
    val videoUrl: String = "",
    val category: String = "",
    val position: Long = 0L,

    val isPremiumUser: Boolean = false,
    val isLoggedIn: Boolean = false,
    val isPreviewExhausted: Boolean = false,

    // Dynamic HLS Quality state (for International category)
    val isHlsStream: Boolean = false,
    val availableHlsQualities: List<HlsQualityDto> = emptyList(),
    val autoAvailable: Boolean = true,
    val selectedHlsQuality: String = "auto", // "auto", "720p", "1080p", etc.
    val isLoadingQualities: Boolean = false,
    val isChangingQuality: Boolean = false,
    val activePlaybackUrl: String = "",
    val qualitiesError: String? = null,

    // Player state
    val isPlaying: Boolean = true,
    val isProgressLoaded: Boolean = false,
    val isSavingProgress: Boolean = false,
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

data class WatchProgressState(
    val progress: WatchProgressDto? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null
)
