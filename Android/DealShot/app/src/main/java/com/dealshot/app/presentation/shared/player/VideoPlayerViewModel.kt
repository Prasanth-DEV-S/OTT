package com.dealshot.app.presentation.shared.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VideoPlayerViewModel : ViewModel() {

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
                        title = intent.title,
                        description = intent.description,
                        videoUrl = intent.videoUrl,
                        isPremiumUser = intent.isPremiumUser,
                        position = intent.position,
                        isLoading = false,
                        error = null
                    )
                }
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

    private fun sendEffect(
        effect: VideoPlayerEffect
    ) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}