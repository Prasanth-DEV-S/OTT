package com.dealshot.app.presentation.shared.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dealshot.app.core.error.AppErrorHandler
import com.dealshot.app.data.remote.repository.WatchProgressRepository
import com.dealshot.app.data.preferences.GuestPreviewRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class VideoPlayerViewModel(
    private val watchProgressRepository: WatchProgressRepository,
    private val guestPreviewRepository: com.dealshot.app.data.preferences.GuestPreviewRepository,
    private val userPreferences: com.dealshot.app.data.preferences.UserPreferences? = null
) : ViewModel() {
    private val progressSaveMutex = Mutex()
    private var progressSaveJob: Job? = null
    private var previewJob: Job? = null
    private var currentPreviewTimeMs = 0L
    private var hasActiveSubscription = false

    private val _uiState = MutableStateFlow(
        VideoPlayerState()
    )

    val uiState = _uiState.asStateFlow()

    private val _effect = Channel<VideoPlayerEffect>(
        capacity = Channel.BUFFERED
    )

    val effect = _effect.receiveAsFlow()

    init {
        userPreferences?.let { prefs ->
            viewModelScope.launch {
                prefs.subscriptionState.collect { subState ->
                    hasActiveSubscription = com.dealshot.app.data.subscription.hasPremiumAccess(subState)
                }
            }
        }
    }

    fun onIntent(intent: VideoPlayerIntent) {

        when (intent) {

            is VideoPlayerIntent.Initialize -> {
                val pendingState = guestPreviewRepository.getPendingResumeState()
                val isReturningFromLogin = pendingState?.videoId == intent.videoId
                val videoId = intent.videoId
                val isRestricted = !hasActiveSubscription
                val isExhausted = (isRestricted && intent.position >= GuestPreviewRepository.GUEST_PREVIEW_LIMIT_MS) ||
                        (videoId.isNotBlank() && guestPreviewRepository.isPreviewExhausted(videoId))

                currentPreviewTimeMs = 0L

                _uiState.update {
                    it.copy(
                        videoId = intent.videoId,
                        title = intent.title,
                        description = intent.description,
                        videoUrl = intent.videoUrl,
                        isPremiumUser = intent.isPremiumUser,
                        isLoggedIn = !guestPreviewRepository.isGuest(),
                        isPreviewExhausted = isExhausted,
                        showSubscriptionPopup = if (isRestricted && isExhausted) true else it.showSubscriptionPopup,
                        position = intent.position,
                        currentPlaybackPosition = intent.position,
                        isLoading = true,
                        isProgressLoaded = false,
                        error = null
                    )
                }
                loadWatchProgress(intent.videoId, intent.position, isReturningFromLogin)
            }

            VideoPlayerIntent.BackClicked -> {
                sendEffect(
                    VideoPlayerEffect.NavigateBack
                )
            }

            VideoPlayerIntent.SubscriptionClicked -> {
                val videoId = _uiState.value.videoId
                val pos = _uiState.value.currentPlaybackPosition.coerceAtLeast(GuestPreviewRepository.GUEST_PREVIEW_LIMIT_MS)
                guestPreviewRepository.savePendingResumeState(
                    videoId = videoId,
                    positionMs = pos,
                    screen = "player/$videoId"
                )
                guestPreviewRepository.saveLocalWatchProgress(videoId, pos)
                sendEffect(
                    VideoPlayerEffect.OpenSubscription
                )
            }

            VideoPlayerIntent.SaveProgressRequested -> {
                saveWatchProgress()
            }

            VideoPlayerIntent.SettingsClicked -> {
                _uiState.update {
                    it.copy(
                        showSettings = true
                    )
                }
            }

            VideoPlayerIntent.SettingsDismissed -> {
                _uiState.update {
                    it.copy(
                        showSettings = false
                    )
                }
            }

            VideoPlayerIntent.FullscreenClicked -> {
                _uiState.update {
                    it.copy(
                        isFullscreen = !it.isFullscreen
                    )
                }
            }

            VideoPlayerIntent.PlayPauseClicked -> {
                checkGuestPreviewLimit(isUserAction = true)
            }

            VideoPlayerIntent.PlaybackReady -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = null
                    )
                }
            }

            is VideoPlayerIntent.PlaybackError -> {
                handleError(intent.error)
            }

            is VideoPlayerIntent.PlaybackStateChanged -> {
                _uiState.update {
                    it.copy(
                        isPlaying = intent.isPlaying
                    )
                }
                onPlaybackStateChanged(intent.isPlaying, _uiState.value.videoId)
            }

            is VideoPlayerIntent.PlaybackProgressChanged -> {
                val videoId = _uiState.value.videoId
                val isRestricted = !hasActiveSubscription
                val isExhausted = isRestricted && (intent.currentPosition >= GuestPreviewRepository.GUEST_PREVIEW_LIMIT_MS ||
                        (videoId.isNotBlank() && guestPreviewRepository.isPreviewExhausted(videoId)))

                if (isRestricted && isExhausted) {
                    if (videoId.isNotBlank()) {
                        guestPreviewRepository.setPreviewExhausted(videoId)
                    }
                    _uiState.update {
                        it.copy(
                            currentPlaybackPosition = intent.currentPosition,
                            totalDuration = intent.duration,
                            isPreviewExhausted = true,
                            showSubscriptionPopup = true
                        )
                    }
                    sendEffect(VideoPlayerEffect.PauseAndShowLogin(intent.currentPosition))
                } else {
                    if (intent.currentPosition > 0L && videoId.isNotBlank()) {
                        guestPreviewRepository.saveLocalWatchProgress(videoId, intent.currentPosition)
                    }
                    _uiState.update {
                        it.copy(
                            currentPlaybackPosition = intent.currentPosition,
                            totalDuration = intent.duration
                        )
                    }
                }
            }

            is VideoPlayerIntent.SliderChanged -> {
                val isRestricted = !hasActiveSubscription
                val clampedPosition = if (isRestricted && _uiState.value.isPreviewExhausted) {
                    _uiState.value.currentPlaybackPosition
                } else {
                    intent.position
                }
                _uiState.update {
                    it.copy(
                        currentPlaybackPosition = clampedPosition
                    )
                }
            }

            VideoPlayerIntent.SliderChangeFinished -> {
                checkGuestPreviewLimit(isUserAction = true)
            }

            VideoPlayerIntent.ClearError -> {
                _uiState.update {
                    it.copy(error = null)
                }
            }

            VideoPlayerIntent.SubscriptionPopupDismissed -> {
                _uiState.update {
                    it.copy(showSubscriptionPopup = false)
                }
            }

            VideoPlayerIntent.CheckAuthStatus -> {
                val isLoggedIn = !guestPreviewRepository.isGuest()
                val videoId = _uiState.value.videoId
                val isRestricted = !hasActiveSubscription
                val isExhausted = if (isRestricted && videoId.isNotBlank()) guestPreviewRepository.isPreviewExhausted(videoId) else false
                val pendingState = guestPreviewRepository.getPendingResumeState()
                val localSavedPos = if (videoId.isNotBlank()) guestPreviewRepository.getLocalWatchProgress(videoId) else 0L

                val resumePos = when {
                    pendingState?.videoId == videoId && pendingState.positionMs > 0L -> pendingState.positionMs
                    localSavedPos > 0L -> localSavedPos
                    _uiState.value.currentPlaybackPosition > 0L -> _uiState.value.currentPlaybackPosition
                    else -> 0L
                }

                _uiState.update {
                    it.copy(
                        isLoggedIn = isLoggedIn,
                        isPreviewExhausted = isExhausted,
                        showSubscriptionPopup = if (isRestricted && isExhausted) true else it.showSubscriptionPopup,
                        position = resumePos,
                        currentPlaybackPosition = resumePos
                    )
                }

                if (pendingState?.videoId == videoId) {
                    guestPreviewRepository.clearPendingResumeState()
                }
            }
        }
    }

    private fun saveWatchProgress() {
        val state = _uiState.value
        val videoId = state.videoId
        val positionMs = state.currentPlaybackPosition
        val durationMs = state.totalDuration

        if (videoId.isBlank() || positionMs <= 0L) return

        if (!hasActiveSubscription && guestPreviewRepository.isPreviewExhausted(videoId)) {
            return
        }

        progressSaveJob?.cancel()
        progressSaveJob = viewModelScope.launch {
            progressSaveMutex.withLock {
                try {
                    val posSec = (positionMs / 1000L).toInt()
                    val durSec = (durationMs / 1000L).toInt()
                    watchProgressRepository.saveProgress(
                        videoId = videoId,
                        positionSeconds = posSec,
                        durationSeconds = durSec
                    )
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                }
            }
        }
    }

    private fun loadWatchProgress(
        videoId: String,
        initialPosition: Long,
        isReturningFromLogin: Boolean
    ) {
        viewModelScope.launch {
            try {
                val localSavedPos = guestPreviewRepository.getLocalWatchProgress(videoId)
                val pendingState = guestPreviewRepository.getPendingResumeState()

                val resolvedPosition: Long = when {
                    pendingState?.videoId == videoId && pendingState.positionMs > 0L -> {
                        val pos = pendingState.positionMs
                        guestPreviewRepository.clearPendingResumeState()
                        pos
                    }
                    localSavedPos > 0L -> localSavedPos
                    initialPosition > 0L -> initialPosition
                    else -> {
                        try {
                            val dto = watchProgressRepository.getProgress(videoId)
                            (dto.positionSeconds * 1000L)
                        } catch (_: Exception) {
                            0L
                        }
                    }
                }

                val isExhausted = !hasActiveSubscription && (guestPreviewRepository.isPreviewExhausted(videoId) || resolvedPosition >= GuestPreviewRepository.GUEST_PREVIEW_LIMIT_MS)
                val finalPositionMs = if (isExhausted) {
                    resolvedPosition.coerceAtMost(GuestPreviewRepository.GUEST_PREVIEW_LIMIT_MS)
                } else {
                    resolvedPosition
                }

                val shouldShowPopup = !hasActiveSubscription && isExhausted

                _uiState.update {
                    it.copy(
                        position = finalPositionMs,
                        currentPlaybackPosition = finalPositionMs,
                        showSubscriptionPopup = shouldShowPopup,
                        isPreviewExhausted = isExhausted,
                        isProgressLoaded = true,
                        isLoading = false
                    )
                }

                if (finalPositionMs > 0L) {
                    sendEffect(VideoPlayerEffect.ResumePlayback(finalPositionMs))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        position = initialPosition,
                        currentPlaybackPosition = initialPosition,
                        isProgressLoaded = true,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun sendEffect(
        effect: VideoPlayerEffect
    ) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }

    private fun onPlaybackStateChanged(isPlaying: Boolean, videoId: String) {
        if (hasActiveSubscription) return

        val isExhausted = (videoId.isNotBlank() && guestPreviewRepository.isPreviewExhausted(videoId)) ||
                _uiState.value.currentPlaybackPosition >= GuestPreviewRepository.GUEST_PREVIEW_LIMIT_MS

        if (isExhausted) {
            _uiState.update { it.copy(isPreviewExhausted = true, showSubscriptionPopup = true) }
            sendEffect(VideoPlayerEffect.PauseAndShowLogin(_uiState.value.currentPlaybackPosition))
            return
        }

        if (isPlaying) {
            startPreviewCountdown(videoId)
        } else {
            pausePreviewCountdown()
        }
    }

    private fun startPreviewCountdown(videoId: String) {
        if (previewJob?.isActive == true) return
        previewJob = viewModelScope.launch {
            while (currentPreviewTimeMs < GuestPreviewRepository.GUEST_PREVIEW_LIMIT_MS) {
                kotlinx.coroutines.delay(1000L)
                currentPreviewTimeMs += 1000L
            }
            if (videoId.isNotBlank()) {
                guestPreviewRepository.setPreviewExhausted(videoId)
            }
            val currentPos = _uiState.value.currentPlaybackPosition.coerceAtLeast(GuestPreviewRepository.GUEST_PREVIEW_LIMIT_MS)
            if (videoId.isNotBlank()) {
                guestPreviewRepository.saveLocalWatchProgress(videoId, currentPos)
                guestPreviewRepository.savePendingResumeState(
                    videoId = videoId,
                    positionMs = currentPos,
                    screen = "player/$videoId"
                )
            }

            _uiState.update { it.copy(isPreviewExhausted = true, showSubscriptionPopup = true) }
            sendEffect(VideoPlayerEffect.PauseAndShowLogin(currentPos))
        }
    }

    private fun pausePreviewCountdown() {
        previewJob?.cancel()
        previewJob = null
    }

    private fun checkGuestPreviewLimit(isUserAction: Boolean = false): Boolean {
        if (hasActiveSubscription) return false

        val videoId = _uiState.value.videoId
        val isExhausted = (videoId.isNotBlank() && guestPreviewRepository.isPreviewExhausted(videoId)) ||
                _uiState.value.currentPlaybackPosition >= GuestPreviewRepository.GUEST_PREVIEW_LIMIT_MS

        if (isExhausted) {
            val shouldShowPopup = isUserAction || _uiState.value.isPlaying

            _uiState.update {
                it.copy(
                    isPreviewExhausted = true,
                    showSubscriptionPopup = if (shouldShowPopup) true else it.showSubscriptionPopup
                )
            }

            if (shouldShowPopup) {
                sendEffect(VideoPlayerEffect.PauseAndShowLogin(_uiState.value.currentPlaybackPosition))
            }
            return true
        }
        return false
    }

    private fun handleError(throwable: Throwable) {
        if (throwable is CancellationException) {
            throw throwable
        }

        _uiState.update {
            it.copy(
                isLoading = false,
                error = AppErrorHandler.getUserMessage(throwable)
            )
        }
    }
}
