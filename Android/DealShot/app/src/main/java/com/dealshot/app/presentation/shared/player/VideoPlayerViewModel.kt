package com.dealshot.app.presentation.shared.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dealshot.app.core.error.AppErrorHandler
import com.dealshot.app.data.remote.repository.WatchProgressRepository
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
    private val watchProgressRepository: WatchProgressRepository
) : ViewModel() {
    private val progressSaveMutex = Mutex()
    private var progressSaveJob: Job? = null
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
                _uiState.update {
                    it.copy(
                        videoId = intent.videoId,
                        title = intent.title,
                        description = intent.description,
                        videoUrl = intent.videoUrl,
                        isPremiumUser = intent.isPremiumUser,
                        position = intent.position,
                        currentPlaybackPosition = intent.position,
                        isLoading = true,
                        isProgressLoaded = false,
                        error = null
                    )
                }
                loadWatchProgress(intent.videoId, intent.position)

            }

            VideoPlayerIntent.BackClicked -> {
                sendEffect(
                    VideoPlayerEffect.NavigateBack
                )
            }

            VideoPlayerIntent.SubscriptionClicked -> {
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
            }

            is VideoPlayerIntent.PlaybackProgressChanged -> {
                _uiState.update {
                    it.copy(
                        currentPlaybackPosition =
                            intent.currentPosition,
                        totalDuration =
                            intent.duration
                    )
                }
            }

            is VideoPlayerIntent.SliderChanged -> {
                _uiState.update {
                    it.copy(
                        currentPlaybackPosition =
                            intent.position
                    )
                }
            }

            VideoPlayerIntent.SliderChangeFinished -> {
                // Actual ExoPlayer seek remains in player UI for now.
            }

            VideoPlayerIntent.ClearError -> {
                _uiState.update {
                    it.copy(error = null)
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
        fallbackPositionMs: Long
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

                        val restoredPositionMs =
                            if (savedPositionMs > 0L) {
                                savedPositionMs
                            } else {
                                fallbackPositionMs.coerceAtLeast(0L)
                            }

                        current.copy(
                            position = restoredPositionMs,
                            currentPlaybackPosition = restoredPositionMs,
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

    /**
     * Single place that turns any playback / network exception
     * into a user-friendly message.
     */
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