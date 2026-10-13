package com.dealshot.app.presentation.shared.likeAndList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dealshot.app.core.error.AppErrorHandler
import com.dealshot.app.data.remote.dto.VideoDto
import com.dealshot.app.data.remote.repository.SavedVideoCollection
import com.dealshot.app.data.remote.repository.SavedVideosRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import kotlin.text.contains

class SavedVideosViewModel(
    private val repository: SavedVideosRepository,
    private val collection: SavedVideoCollection
) : ViewModel() {

    private val _uiState = MutableStateFlow(SavedVideosState())
    val uiState = _uiState.asStateFlow()

    private val _effect = Channel<SavedVideosEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: SavedVideosIntent) {
        when (intent) {
            SavedVideosIntent.Load,
            SavedVideosIntent.Retry -> loadVideos()

            is SavedVideosIntent.Add -> addVideo(
                videoId = intent.videoId,
                video = intent.video
            )

            is SavedVideosIntent.Remove -> removeVideo(intent.videoId)

            SavedVideosIntent.ClearError ->
                _uiState.update { it.copy(error = null) }

            SavedVideosIntent.ClearMessage ->
                _uiState.update { it.copy(message = null) }

            is SavedVideosIntent.VideoClicked ->
                sendEffect(SavedVideosEffect.OpenPlayer(intent.video))
        }
    }

    private fun loadVideos() {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, error = null)
            }

            try {
                val videos = repository.getVideos(collection)

                _uiState.update {
                    it.copy(
                        videos = videos,
                        addedIds = videos.map { video -> video.id }.toSet(),
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleError(e)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun addVideo(
        videoId: String,
        video: VideoDto?
    ) {
        val current = _uiState.value

        // Ignore repeated taps while this video is being processed.
        if (videoId in current.addingIds || videoId in current.removingIds) {
            return
        }

        val wasAlreadyAdded = videoId in current.addedIds
        val previousVideos = current.videos
        val previousAddedIds = current.addedIds

        _uiState.update { state ->
            state.copy(
                addingIds = state.addingIds + videoId,
                error = null,
                message = null,
                addedIds = state.addedIds + videoId,
                videos = if (
                    video != null && state.videos.none { it.id == videoId }
                ) {
                    listOf(video) + state.videos
                } else {
                    state.videos
                }
            )
        }

        viewModelScope.launch {
            try {
                val addedVideo = repository.addVideo(collection, videoId)

                _uiState.update { state ->
                    val resolvedVideo = addedVideo ?: video

                    state.copy(
                        videos = if (
                            resolvedVideo != null &&
                            state.videos.none { it.id == videoId }
                        ) {
                            listOf(resolvedVideo) + state.videos
                        } else {
                            state.videos
                        },
                        addedIds = state.addedIds + videoId,
                        addingIds = state.addingIds - videoId,
                        message = addedMessage(),
                        error = null
                    )
                }

                sendEffect(SavedVideosEffect.ShowMessage(addedMessage()))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val status = (e as? HttpException)?.code()

                if (status == 409) {
                    // Already saved: keep it marked as added and refresh.
                    _uiState.update {
                        it.copy(
                            addingIds = it.addingIds - videoId,
                            addedIds = it.addedIds + videoId,
                            message = alreadyAddedMessage(),
                            error = null
                        )
                    }

                    sendEffect(
                        SavedVideosEffect.ShowMessage(alreadyAddedMessage())
                    )
                    loadVideos()
                } else {
                    _uiState.update {
                        it.copy(
                            videos = previousVideos,
                            addedIds = previousAddedIds,
                            addingIds = it.addingIds - videoId
                        )
                    }
                    handleError(e)
                }
            }
        }
    }

    private fun removeVideo(videoId: String) {
        val current = _uiState.value

        if (videoId in current.removingIds || videoId in current.addingIds) {
            return
        }

        val previousVideos = current.videos
        val previousAddedIds = current.addedIds

        _uiState.update { state ->
            state.copy(
                videos = state.videos.filterNot { it.id == videoId },
                addedIds = state.addedIds - videoId,
                removingIds = state.removingIds + videoId,
                error = null,
                message = null
            )
        }

        viewModelScope.launch {
            try {
                val message = repository.removeVideo(collection, videoId)

                _uiState.update {
                    it.copy(
                        removingIds = it.removingIds - videoId,
                        message = message,
                        error = null
                    )
                }

                sendEffect(SavedVideosEffect.ShowMessage(message))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val status = (e as? HttpException)?.code()

                if (status == 404) {
                    // Already removed on the server; keep it removed locally.
                    _uiState.update {
                        it.copy(
                            removingIds = it.removingIds - videoId,
                            addedIds = it.addedIds - videoId,
                            message = removedMessage(),
                            error = null
                        )
                    }

                    sendEffect(
                        SavedVideosEffect.ShowMessage(removedMessage())
                    )
                    loadVideos()
                } else {
                    _uiState.update {
                        it.copy(
                            videos = previousVideos,
                            addedIds = previousAddedIds,
                            removingIds = it.removingIds - videoId
                        )
                    }
                    handleError(e)
                }
            }
        }
    }

    private fun handleError(throwable: Throwable) {
        val message = AppErrorHandler.getUserMessage(throwable)

        _uiState.update {
            it.copy(error = message)
        }
    }

    private fun addedMessage(): String =
        when (collection) {
            SavedVideoCollection.MY_LIST -> "Video added to My List"
            SavedVideoCollection.WATCH_LATER -> "Video added to Watch Later"
        }

    private fun alreadyAddedMessage(): String =
        when (collection) {
            SavedVideoCollection.MY_LIST -> "Already in My List"
            SavedVideoCollection.WATCH_LATER -> "Already in Watch Later"
        }

    private fun removedMessage(): String =
        when (collection) {
            SavedVideoCollection.MY_LIST -> "Video removed from My List"
            SavedVideoCollection.WATCH_LATER -> "Video removed from Watch Later"
        }

    private fun sendEffect(effect: SavedVideosEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}