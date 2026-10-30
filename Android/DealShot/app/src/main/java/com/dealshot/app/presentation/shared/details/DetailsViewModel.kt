package com.dealshot.app.presentation.shared.details

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dealshot.app.core.error.AppErrorHandler
import com.dealshot.app.data.remote.repository.VideoDetailsRepository
import com.dealshot.app.data.remote.repository.SavedVideosRepository
import com.dealshot.app.data.remote.repository.like.LikedVideosRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

class DetailsViewModel(
    private val videoDetailsRepository: VideoDetailsRepository,
    private val savedVideosRepository: SavedVideosRepository,
    private val likedVideosRepository: LikedVideosRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailsState())
    val uiState = _uiState.asStateFlow()
    private val _effect = Channel<DetailsEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()


    fun onIntent(intent: DetailsIntent) {

        when (intent) {
            is DetailsIntent.Initialize -> {
                _uiState.update {
                    it.copy(
                        video = intent.video,
                        videoDetails = null,
                        isLoading = true,
                        error = null,
                        isInMyWatchLater = false,
                        isLiked = false
                    )
                }

                val initialId = intent.video.apiId.ifBlank { intent.video.id }
                loadVideoDetails(initialId)
                loadVideoLikeStatus(initialId)
                loadMyWatchListStatus(initialId)
            }

            DetailsIntent.BackClicked ->
                sendEffect(DetailsEffect.NavigateBack)

            DetailsIntent.PlayVideoClicked ->
                sendEffect(DetailsEffect.OpenPlayer)

            DetailsIntent.MyListClicked ->
                toggleMyList()

            DetailsIntent.LikeClicked -> {
                /*add condition for without Login user 14102026 */
                toggleLike()
            }

            DetailsIntent.ShareClicked ->
                sendEffect(DetailsEffect.ShareVideo)

            DetailsIntent.ClearError ->
                _uiState.update { it.copy(error = null) }
        }
    }

    private fun loadMyWatchListStatus(videoId: String) {
        viewModelScope.launch {
            try {
                val videos = savedVideosRepository.getVideos()

                _uiState.update {
                    it.copy(
                        isInMyWatchLater = videos.any { saved ->
                            saved.id == videoId
                        }
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Keep the details screen usable if My List status fails.
                Log.e(
                    "DetailsVM",
                    "Failed to load My List status",
                    e
                )
            }
        }
    }

    private fun loadVideoLikeStatus(videoId: String) {
        viewModelScope.launch {
            try {
                val isLiked = likedVideosRepository.getVideoLikeStatus(videoId)
                _uiState.update {
                    it.copy(isLiked = isLiked)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(
                    "DetailsVM",
                    "Failed to load video like status",
                    e
                )
            }
        }
    }

    private fun getVideoId(): String? {
        val state = _uiState.value
        return state.videoDetails?.mongoId
            ?: state.videoDetails?.id
            ?: state.video?.apiId?.takeIf { it.isNotBlank() }
            ?: state.video?.id
    }

    private fun toggleLike() {
        val videoId = getVideoId() ?: return
        val currentState = _uiState.value

        if (currentState.isUpdatingLike) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isUpdatingLike = true,
                    error = null
                )
            }

            try {
                val response = likedVideosRepository.likeVideo(videoId)

                _uiState.update {
                    it.copy(
                        isLiked = response.isLiked,
                        isUpdatingLike = false,
                        error = null
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isUpdatingLike = false,
                        error = AppErrorHandler.getUserMessage(e)
                    )
                }
            }
        }
    }

    private fun toggleMyList() {
        val videoId = getVideoId() ?: return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isUpdatingMyWatchLater = true,
                    error = null
                )
            }

            try {
                val isSaved = _uiState.value.isInMyWatchLater

                if (isSaved) {
                    savedVideosRepository.removeVideo(videoId)
                } else {
                    savedVideosRepository.addVideo(videoId)
                }

                _uiState.update {
                    it.copy(
                        isInMyWatchLater = !isSaved,
                        isUpdatingMyWatchLater = false
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isUpdatingMyWatchLater = false,
                        error = AppErrorHandler.getUserMessage(e)
                    )
                }
            }
        }
    }

    private fun loadVideoDetails(videoId: String) {
        viewModelScope.launch {
            try {
                val details =
                    videoDetailsRepository.getVideoDetails(videoId)

                _uiState.update {
                    it.copy(
                        videoDetails = details,
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

    private fun sendEffect(effect: DetailsEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}
