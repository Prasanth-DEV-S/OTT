package com.dealshot.app.presentation.shared.details

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dealshot.app.core.error.AppErrorHandler
import com.dealshot.app.data.remote.ApiClient
import com.dealshot.app.data.remote.dto.VideoDto
import com.dealshot.app.data.remote.mapper.toDomain
import com.dealshot.app.data.remote.repository.SavedVideosRepository
import com.dealshot.app.data.remote.repository.VideoDetailsRepository
import com.dealshot.app.data.remote.repository.VideoRepository
import com.dealshot.app.data.remote.repository.VideoRepositoryImpl
import com.dealshot.app.data.remote.repository.like.LikedVideosRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DetailsViewModel(
    private val videoDetailsRepository: VideoDetailsRepository,
    private val savedVideosRepository: SavedVideosRepository,
    private val likedVideosRepository: LikedVideosRepository,
    videoRepository: VideoRepository? = null
) : ViewModel() {

    private val videoRepo: VideoRepository by lazy {
        videoRepository ?: VideoRepositoryImpl(ApiClient.videoApi)
    }

    private val _uiState = MutableStateFlow(DetailsState())
    val uiState = _uiState.asStateFlow()
    private val _effect = Channel<DetailsEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: DetailsIntent) {
        when (intent) {
            is DetailsIntent.Initialize -> {
                val initialId = intent.video.apiId.ifBlank { intent.video.id }
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

                loadVideoDetails(initialId)
                loadVideoLikeStatus(initialId)
                loadMyWatchListStatus(initialId)
                loadNextVideos(category = intent.video.category, currentVideoId = initialId)
            }

            is DetailsIntent.SelectNextVideo -> {
                val domainVideo = intent.videoDto.toDomain(intent.videoDto.id)
                val targetId = domainVideo.apiId.ifBlank { domainVideo.id }

                _uiState.update {
                    it.copy(
                        video = domainVideo,
                        videoDetails = null,
                        isLoading = true,
                        error = null,
                        isInMyWatchLater = false,
                        isLiked = false
                    )
                }

                loadVideoDetails(targetId)
                loadVideoLikeStatus(targetId)
                loadMyWatchListStatus(targetId)
                loadNextVideos(category = domainVideo.category, currentVideoId = targetId)
            }

            DetailsIntent.BackClicked ->
                sendEffect(DetailsEffect.NavigateBack)

            DetailsIntent.PlayVideoClicked ->
                sendEffect(DetailsEffect.OpenPlayer)

            DetailsIntent.MyListClicked ->
                toggleMyList()

            DetailsIntent.LikeClicked ->
                toggleLike()

            DetailsIntent.ShareClicked ->
                sendEffect(DetailsEffect.ShareVideo)

            DetailsIntent.ClearError ->
                _uiState.update { it.copy(error = null) }
        }
    }

    private fun loadNextVideos(category: String, currentVideoId: String) {
        val cleanCategory = category.trim()
        if (cleanCategory.isBlank() || cleanCategory.equals("Home", ignoreCase = true)) return

        viewModelScope.launch {
            try {
                val catResult = videoRepo.getVideosByCategory(cleanCategory)
                var categoryVideos = catResult.getOrDefault(emptyList())

                if (categoryVideos.isEmpty()) {
                    val allVideos = videoRepo.getVideos()
                    categoryVideos = allVideos.filter { video ->
                        video.category?.trim()?.equals(cleanCategory, ignoreCase = true) == true
                    }
                } else {
                    categoryVideos = categoryVideos.filter { video ->
                        video.category.isNullOrBlank() || video.category.trim().equals(cleanCategory, ignoreCase = true)
                    }
                }

                val nextList = categoryVideos.filterNot {
                    it.id.trim().equals(currentVideoId.trim(), ignoreCase = true) ||
                    it.altId?.trim()?.equals(currentVideoId.trim(), ignoreCase = true) == true
                }

                _uiState.update { current ->
                    current.copy(nextVideos = nextList)
                }
            } catch (e: Exception) {
                Log.w("DetailsVM", "Failed to load next videos for category: $cleanCategory", e)
            }
        }
    }

    private fun loadMyWatchListStatus(videoId: String) {
        viewModelScope.launch {
            try {
                val videos = savedVideosRepository.getVideos()
                _uiState.update {
                    it.copy(
                        isInMyWatchLater = videos.any { saved ->
                            saved.id == videoId || saved.effectiveId == videoId
                        }
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _uiState.update { it.copy(isInMyWatchLater = false) }
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
            } catch (_: Exception) {
                _uiState.update { it.copy(isLiked = false) }
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
                val result = likedVideosRepository.toggleVideoLike(videoId)
                result.fold(
                    onSuccess = { response ->
                        _uiState.update {
                            it.copy(
                                isLiked = response.isLiked,
                                isUpdatingLike = false,
                                error = null
                            )
                        }
                    },
                    onFailure = { error ->
                        val msg = if (error.message?.contains("401") == true) "Please log in to like this video ." else AppErrorHandler.getUserMessage(error)
                        _uiState.update {
                            it.copy(
                                isUpdatingLike = false,
                                error = msg
                            )
                        }
                        if (msg.contains("log in", ignoreCase = true)) {
                            sendEffect(DetailsEffect.OpenLogin)
                        }
                    }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val msg = if (e.message?.contains("401") == true) "Please log in to like this video ." else AppErrorHandler.getUserMessage(e)
                _uiState.update {
                    it.copy(
                        isUpdatingLike = false,
                        error = msg
                    )
                }
                if (msg.contains("log in", ignoreCase = true)) {
                    sendEffect(DetailsEffect.OpenLogin)
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
                val msg = if (e.message?.contains("401") == true) "Please log in to Add this video to your list." else AppErrorHandler.getUserMessage(e)
                _uiState.update {
                    it.copy(
                        isUpdatingMyWatchLater = false,
                        error = msg
                    )
                }
                if (msg.contains("log in", ignoreCase = true)) {
                    sendEffect(DetailsEffect.OpenLogin)
                }
            }
        }
    }

    private fun loadVideoDetails(videoId: String) {
        viewModelScope.launch {
            try {
                val details = videoDetailsRepository.getVideoDetails(videoId)

                _uiState.update { current ->
                    current.copy(
                        videoDetails = details,
                        isLoading = false,
                        error = null
                    )
                }

                // If details category is available, refresh next videos list with exact category
                details.category?.takeIf { it.isNotBlank() }?.let { cat ->
                    loadNextVideos(category = cat, currentVideoId = videoId)
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
