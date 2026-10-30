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
    private val guestPreviewRepository: com.dealshot.app.data.preferences.GuestPreviewRepository
) : ViewModel() {
    private val progressSaveMutex = Mutex()
    private var progressSaveJob: Job? = null
    private var previewJob: Job? = null
    private var currentPreviewTimeMs = 0L
    private val _uiState = MutableStateFlow(
        VideoPlayerState()
    )

    val uiState = _uiState.asStateFlow()

    private val _effect = Channel<VideoPlayerEffect>(
        capacity = Channel.BUFFERED
    )

    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: VideoPlayerIntent) {

        when (intent) {

            is VideoPlayerIntent.Initialize -> {
                val pendingState = guestPreviewRepository.getPendingResumeState()
                val isReturningFromLogin = pendingState?.videoId == intent.videoId

                _uiState.update {
                    it.copy(
                        videoId = intent.videoId,
                        title = intent.title,
                        description = intent.description,
                        videoUrl = intent.videoUrl,
                        isPremiumUser = intent.isPremiumUser,
                        isLoggedIn = !guestPreviewRepository.isGuest(),
                        isPreviewExhausted = guestPreviewRepository.isPreviewExhausted(intent.videoId),
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
                val pos = _uiState.value.currentPlaybackPosition.coerceAtLeast(com.dealshot.app.data.preferences.GuestPreviewRepository.GUEST_PREVIEW_LIMIT_MS)
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
                // Do not directly control ExoPlayer here.
                // ExoPlayer remains in MobilePlayerScreen/Route.
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

            // NEW: ExoPlayer reported a playback error
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
                if (guestPreviewRepository.isGuest() && guestPreviewRepository.isPreviewExhausted(_uiState.value.videoId)) {
                    _uiState.update {
                        it.copy(
                            currentPlaybackPosition = intent.currentPosition,
                            totalDuration = intent.duration
                        )
                    }
                    if (_uiState.value.isPlaying) {
                        sendEffect(VideoPlayerEffect.PauseAndShowLogin(intent.currentPosition))
                    }
                } else {
                    if (intent.currentPosition > 0L && _uiState.value.videoId.isNotBlank()) {
                        guestPreviewRepository.saveLocalWatchProgress(_uiState.value.videoId, intent.currentPosition)
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
                val clampedPosition = if (guestPreviewRepository.isGuest() && guestPreviewRepository.isPreviewExhausted(_uiState.value.videoId)) {
                    _uiState.value.currentPlaybackPosition // Cannot seek if exhausted
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
                // Actual ExoPlayer seek remains in player UI for now.
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
                val isExhausted = if (isLoggedIn) false else if (videoId.isNotBlank()) guestPreviewRepository.isPreviewExhausted(videoId) else false
                val pendingState = guestPreviewRepository.getPendingResumeState()
                val localSavedPos = if (videoId.isNotBlank()) guestPreviewRepository.getLocalWatchProgress(videoId) else 0L

                val resumePos = when {
                    pendingState?.videoId == videoId && pendingState.positionMs > 0L -> pendingState.positionMs
                    localSavedPos > 0L -> localSavedPos
                    _uiState.value.currentPlaybackPosition > 0L -> _uiState.value.currentPlaybackPosition
                    else -> 0L
                }

                if (pendingState?.videoId == videoId) {
                    guestPreviewRepository.clearPendingResumeState()
                }

                _uiState.update {
                    it.copy(
                        isLoggedIn = isLoggedIn,
                        isPreviewExhausted = isExhausted,
                        showSubscriptionPopup = if (isLoggedIn) false else it.showSubscriptionPopup,
                        position = if (isLoggedIn && resumePos > 0L) resumePos else it.position,
                        currentPlaybackPosition = if (isLoggedIn && resumePos > 0L) resumePos else it.currentPlaybackPosition
                    )
                }

                if (isLoggedIn) {
                    sendEffect(VideoPlayerEffect.ResumePlayback(resumePos))
                } else {
                    if (isExhausted || _uiState.value.currentPlaybackPosition >= GuestPreviewRepository.GUEST_PREVIEW_LIMIT_MS) {
                        _uiState.update { it.copy(showSubscriptionPopup = true) }
                        sendEffect(VideoPlayerEffect.PauseAndShowLogin(GuestPreviewRepository.GUEST_PREVIEW_LIMIT_MS))
                    }
                }
            }
        }
    }

    private fun saveWatchProgress() {
        if (!_uiState.value.isProgressLoaded) return

        // Avoid launching repeated concurrent PUT requests.
        if (progressSaveJob?.isActive == true) return

        progressSaveJob = viewModelScope.launch {
            progressSaveMutex.withLock {
                // Read the latest playback position when the save begins.
                val snapshot = _uiState.value

                if (snapshot.videoId.isBlank()) return@withLock
                if (!snapshot.isProgressLoaded) return@withLock

                val positionSeconds =
                    (snapshot.currentPlaybackPosition.coerceAtLeast(0L) / 1000L)
                        .coerceAtMost(Int.MAX_VALUE.toLong())
                        .toInt()

                val durationSeconds =
                    (snapshot.totalDuration.coerceAtLeast(0L) / 1000L)
                        .coerceAtMost(Int.MAX_VALUE.toLong())
                        .toInt()

                if (durationSeconds <= 0) return@withLock

                _uiState.update {
                    it.copy(isSavingProgress = true)
                }

                try {
                    watchProgressRepository.saveProgress(
                        videoId = snapshot.videoId,
                        positionSeconds = positionSeconds,
                        durationSeconds = durationSeconds
                    )
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: Exception) {
                    _uiState.update {
                        it.copy(
                            error = AppErrorHandler.getUserMessage(exception)
                        )
                    }
                } finally {
                    _uiState.update {
                        it.copy(isSavingProgress = false)
                    }
                }
            }
        }
    }
    private fun loadWatchProgress(
        videoId: String,
        fallbackPositionMs: Long,
        isReturningFromLogin: Boolean
    ) {
        viewModelScope.launch {
            try {
                val progress =
                    watchProgressRepository.getProgress(videoId)

                _uiState.update { current ->
                    if (current.videoId != videoId) {
                        current
                    } else {
                        // API position is in seconds.
                        // ExoPlayer positions are in milliseconds.
                        val savedPositionMs =
                            progress.positionSeconds.coerceAtLeast(0) * 1000L

                        val localSavedPos = guestPreviewRepository.getLocalWatchProgress(videoId)
                        val pendingState = guestPreviewRepository.getPendingResumeState()
                        val pendingPos = if (pendingState?.videoId == videoId) pendingState.positionMs else 0L

                        val restoredPositionMs = when {
                            pendingPos > 0L -> pendingPos
                            savedPositionMs > 0L -> savedPositionMs
                            localSavedPos > 0L -> localSavedPos
                            else -> fallbackPositionMs.coerceAtLeast(0L)
                        }

                        if (pendingState?.videoId == videoId) {
                            guestPreviewRepository.clearPendingResumeState()
                        }

                        var shouldShowPopup = false
                        val finalPositionMs = if (guestPreviewRepository.isGuest() && guestPreviewRepository.isPreviewExhausted(videoId)) {
                            shouldShowPopup = true
                            restoredPositionMs
                        } else {
                            restoredPositionMs
                        }

                        if (shouldShowPopup) {
                            sendEffect(VideoPlayerEffect.PauseAndShowLogin(finalPositionMs))
                        } else if (isReturningFromLogin) {
                            sendEffect(VideoPlayerEffect.ResumePlayback(finalPositionMs))
                        } else if (restoredPositionMs == 0L) {
                            sendEffect(VideoPlayerEffect.ResumePlayback(0L))
                        }

                        current.copy(
                            position = finalPositionMs,
                            currentPlaybackPosition = finalPositionMs,
                            showSubscriptionPopup = shouldShowPopup,
                            isPreviewExhausted = if (guestPreviewRepository.isGuest()) (guestPreviewRepository.isPreviewExhausted(videoId) || shouldShowPopup) else false,
                            isProgressLoaded = true,
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update { current ->
                    if (current.videoId != videoId) {
                        current
                    } else {
                        current.copy(
                            position = fallbackPositionMs.coerceAtLeast(0L),
                            currentPlaybackPosition =
                                fallbackPositionMs.coerceAtLeast(0L),
                            isProgressLoaded = true,
                            isLoading = false,
                            error = AppErrorHandler.getUserMessage(exception)
                        )
                    }
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
        if (!guestPreviewRepository.isGuest() || guestPreviewRepository.isPreviewExhausted(videoId)) {
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
            // Preview Expired
            guestPreviewRepository.setPreviewExhausted(videoId)
            val currentPos = _uiState.value.currentPlaybackPosition
            guestPreviewRepository.saveLocalWatchProgress(videoId, currentPos)
            guestPreviewRepository.savePendingResumeState(
                videoId = videoId,
                positionMs = currentPos,
                screen = "player/$videoId"
            )
            
            _uiState.update { it.copy(isPreviewExhausted = true, showSubscriptionPopup = true) }
            sendEffect(VideoPlayerEffect.PauseAndShowLogin(currentPos))
        }
    }

    private fun pausePreviewCountdown() {
        previewJob?.cancel()
        previewJob = null
    }

    private fun checkGuestPreviewLimit(isUserAction: Boolean = false): Boolean {
        if (!guestPreviewRepository.isGuest()) return false

        val videoId = _uiState.value.videoId
        if (videoId.isBlank()) return false

        val isExhausted = guestPreviewRepository.isPreviewExhausted(videoId)

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
                isPlaying = false,
                error = AppErrorHandler.getUserMessage(throwable)
            )
        }
    }
}