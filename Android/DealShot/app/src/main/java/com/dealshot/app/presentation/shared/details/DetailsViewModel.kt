package com.dealshot.app.presentation.shared.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dealshot.app.core.error.AppErrorHandler
import com.dealshot.app.data.remote.repository.VideoDetailsRepository
import com.dealshot.app.data.remote.dto.VideoDto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.dealshot.app.data.remote.repository.SavedVideosRepository
import com.dealshot.app.data.remote.repository.SavedVideoCollection
import retrofit2.HttpException

class DetailsViewModel(
    private val videoDetailsRepository: VideoDetailsRepository,
    private val savedVideosRepository: SavedVideosRepository
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

                loadVideoDetails(intent.video.id)
                loadMyListStatus(intent.video.id)
                loadMyWatchListStatus(intent.video.id)
            }

            DetailsIntent.BackClicked ->
                sendEffect(DetailsEffect.NavigateBack)

            DetailsIntent.PlayVideoClicked ->
                sendEffect(DetailsEffect.OpenPlayer)

            DetailsIntent.MyListClicked ->
                toggleMyList()

            DetailsIntent.LikeClicked -> {
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
                val videos = savedVideosRepository.getVideos(
                    SavedVideoCollection.WATCH_LATER
                )

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
                android.util.Log.e(
                    "DetailsVM",
                    "Failed to load My List status",
                    e
                )
            }
        }
    }

    private fun loadMyListStatus(videoId: String) {
        viewModelScope.launch {
            try {
                val videos = savedVideosRepository.getVideos(
                    SavedVideoCollection.MY_LIST
                )

                _uiState.update {
                    it.copy(
                        isLiked = videos.any { saved ->
                            saved.id == videoId
                        }
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Keep the details screen usable if My List status fails.
                android.util.Log.e(
                    "DetailsVM",
                    "Failed to load My List status",
                    e
                )
            }
        }
    }

    private fun toggleLike() {
        val currentState = _uiState.value
        val videoId = currentState.video?.id ?: return

        if (currentState.isUpdatingLike) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isUpdatingLike = true,
                    error = null
                )
            }

            try {
                val isLiked = _uiState.value.isLiked

                /*if (isLiked) {
                    savedVideosRepository.removeVideo(
                        SavedVideoCollection.MY_LIST,
                        videoId
                    )
                } else {*/
                    savedVideosRepository.addVideo(
                        SavedVideoCollection.MY_LIST,
                        videoId
                    )
//                }

                _uiState.update {
                    it.copy(
                        isLiked = !isLiked,
                        isUpdatingLike = false
                    )
                }
                sendEffect(DetailsEffect.Liked)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val statusCode = (e as? HttpException)?.code()
                if (statusCode == 409) {
                    // 409 Conflict: Video is already liked on the server
                    _uiState.update {
                        it.copy(
                            isLiked = true,
                            isUpdatingLike = false,
                            error = null
                        )
                    }
                    sendEffect(DetailsEffect.Liked)
                } else if (statusCode == 404) {
                    // 404 Not Found: Video is already unliked on the server
                    _uiState.update {
                        it.copy(
                            isLiked = false,
                            isUpdatingLike = false,
                            error = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isUpdatingLike = false,
                            error = AppErrorHandler.getUserMessage(e)
                        )
                    }
                }
            }
        }
    }

    private fun toggleMyList() {
        val currentState = _uiState.value
        val videoId = currentState.video?.id ?: return

        if (currentState.isUpdatingMyWatchLater) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isUpdatingMyWatchLater = true,
                    error = null
                )
            }

            try {
                val isSaved = _uiState.value.isInMyWatchLater

                /*if (isSaved) {
                    savedVideosRepository.removeVideo(
                        SavedVideoCollection.WATCH_LATER,
                        videoId
                    )
                } else {*/
                    savedVideosRepository.addVideo(
                        SavedVideoCollection.WATCH_LATER,
                        videoId
                    )
//                }

                _uiState.update {
                    it.copy(
                        isInMyWatchLater = !isSaved,
                        isUpdatingMyWatchLater = false
                    )
                }
                sendEffect(DetailsEffect.OpenMyList)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val statusCode = (e as? HttpException)?.code()
                if (statusCode == 409) {
                    _uiState.update {
                        it.copy(
                            isInMyWatchLater = true,
                            isUpdatingMyWatchLater = false,
                            error = null
                        )
                    }
                    sendEffect(DetailsEffect.OpenMyList)
                } else if (statusCode == 404) {
                    _uiState.update {
                        it.copy(
                            isInMyWatchLater = false,
                            isUpdatingMyWatchLater = false,
                            error = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isUpdatingMyWatchLater = false,
                            error = AppErrorHandler.getUserMessage(e)
                        )
                    }
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