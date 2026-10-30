package com.dealshot.app.presentation.shared.like

import com.dealshot.app.data.remote.repository.like.LikedVideosRepository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dealshot.app.core.error.AppErrorHandler
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LikedVideosViewModel(
    private val repository: LikedVideosRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LikedVideosState())
    val uiState = _uiState.asStateFlow()

    private val _effect = Channel<LikedVideosEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: LikedVideosIntent) {
        when (intent) {
            LikedVideosIntent.Load,
            LikedVideosIntent.Retry -> loadVideos()

            is LikedVideosIntent.LikeVideo ->
                likeVideo(intent.videoId)

            is LikedVideosIntent.CheckLikeStatus ->
                checkLikeStatus(intent.videoId)

            LikedVideosIntent.ClearError ->
                _uiState.update { it.copy(error = null) }

            LikedVideosIntent.ClearMessage ->
                _uiState.update { it.copy(message = null) }

            is LikedVideosIntent.VideoClicked -> {
                viewModelScope.launch {
                    _effect.send(
                        LikedVideosEffect.OpenPlayer(intent.video)
                    )
                }
            }
        }

    }

    private fun loadVideos() {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, error = null)
            }

            try {
                val videos = repository.getLikedVideos()

                _uiState.update {
                    it.copy(
                        videos = videos,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = AppErrorHandler.getUserMessage(e)
                    )
                }
            }
        }
    }

    private fun likeVideo(videoId: String) {
        if (videoId.isBlank()) return

        if (videoId in _uiState.value.likingIds) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    likingIds = it.likingIds + videoId,
                    error = null,
                    message = null
                )
            }

            try {
                val result = repository.likeVideo(videoId)

                _uiState.update {
                    it.copy(
                        likingIds = it.likingIds - videoId,
                        message = result.message
                    )
                }

                sendEffect(
                    LikedVideosEffect.ShowMessage(result.message)
                )

                // Refresh from the server to keep the list authoritative.
                refreshAfterMutation()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        likingIds = it.likingIds - videoId,
                        error = AppErrorHandler.getUserMessage(e)
                    )
                }
            }
        }
    }

    private fun checkLikeStatus(videoId: String) {
        if (videoId.isBlank()) return

        viewModelScope.launch {
            try {
                val isLiked = repository.getVideoLikeStatus(videoId)

                _uiState.update {
                    it.copy(
                        message = if (isLiked) {
                            "Video is liked"
                        } else {
                            "Video is not liked"
                        },
                        error = null
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = AppErrorHandler.getUserMessage(e))
                }
            }
        }
    }

    private suspend fun refreshAfterMutation() {
        try {
            val videos = repository.getLikedVideos()

            _uiState.update {
                it.copy(videos = videos)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Keep the successful mutation message; report refresh failure.
            _uiState.update {
                it.copy(error = AppErrorHandler.getUserMessage(e))
            }
        }
    }

    private fun sendEffect(effect: LikedVideosEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}