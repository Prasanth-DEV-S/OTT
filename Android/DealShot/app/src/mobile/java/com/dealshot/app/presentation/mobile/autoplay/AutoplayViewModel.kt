package com.dealshot.app.presentation.mobile.autoplay

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AutoplayUiState(
    val selectedCategoryId: String = "national",
    val selectedCategoryName: String = "National",
    val availableCategories: List<AutoplayCategory> = AutoplayDemoRepository.categories,
    val categoryVideos: List<AutoplayVideo> = emptyList(),
    val currentIndex: Int = 0,
    val showNextOverlay: Boolean = false,
    val countdownSeconds: Int = 5,
    val showCategoryCompletedOverlay: Boolean = false,
    val isAutoplayEnabled: Boolean = true,
    val isFullScreen: Boolean = false,
    val isPlaying: Boolean = false,
    val hideWatchedVideos: Boolean = false
) {
    val currentVideo: AutoplayVideo?
        get() = categoryVideos.getOrNull(currentIndex)

    val nextVideo: AutoplayVideo?
        get() = categoryVideos.getOrNull(currentIndex + 1)

    val hasNextVideo: Boolean
        get() = currentIndex < categoryVideos.lastIndex
}

class AutoplayViewModel(
    private val savedStateHandle: SavedStateHandle = SavedStateHandle()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AutoplayUiState())
    val uiState: StateFlow<AutoplayUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null

    init {
        val savedCategoryId = savedStateHandle.get<String>("saved_category_id") ?: "national"
        val savedVideoId = savedStateHandle.get<String>("saved_video_id")
        val savedAutoplay = savedStateHandle.get<Boolean>("saved_autoplay") ?: true

        loadCategory(savedCategoryId, initialVideoId = savedVideoId, isAutoplayEnabled = savedAutoplay)
    }

    fun loadCategory(categoryId: String, initialVideoId: String? = null, isAutoplayEnabled: Boolean = true) {
        cancelCountdown()

        val category = AutoplayDemoRepository.categories.firstOrNull { it.id == categoryId }
            ?: AutoplayDemoRepository.categories.first()
        val videos = AutoplayDemoRepository.videosByCategory[category.id] ?: emptyList()

        val targetIndex = if (!initialVideoId.isNullOrBlank()) {
            videos.indexOfFirst { it.id == initialVideoId }.coerceAtLeast(0)
        } else {
            0
        }

        _uiState.update {
            it.copy(
                selectedCategoryId = category.id,
                selectedCategoryName = category.name,
                categoryVideos = videos,
                currentIndex = targetIndex,
                showNextOverlay = false,
                showCategoryCompletedOverlay = false,
                isAutoplayEnabled = isAutoplayEnabled
            )
        }

        saveState()
    }

    /**
     * Called when the current video reaches Player.STATE_ENDED.
     */
    fun onVideoEnded() {
        val state = _uiState.value
        if (!state.isAutoplayEnabled) return

        if (state.hasNextVideo) {
            startCountdown()
        } else {
            // Reached last video in category - show category completed overlay
            _uiState.update {
                it.copy(
                    showCategoryCompletedOverlay = true,
                    showNextOverlay = false
                )
            }
        }
    }

    private fun startCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    showNextOverlay = true,
                    countdownSeconds = 5,
                    showCategoryCompletedOverlay = false
                )
            }

            for (sec in 5 downTo 1) {
                _uiState.update { it.copy(countdownSeconds = sec) }
                delay(1000L)
            }

            // 5 seconds finished -> automatically play next video in same category
            playNextVideoNow()
        }
    }

    fun cancelCountdown() {
        countdownJob?.cancel()
        countdownJob = null
        _uiState.update {
            it.copy(
                showNextOverlay = false,
                countdownSeconds = 5
            )
        }
    }

    fun playNextVideoNow() {
        cancelCountdown()
        val state = _uiState.value
        if (state.hasNextVideo) {
            val nextIdx = state.currentIndex + 1
            _uiState.update {
                it.copy(
                    currentIndex = nextIdx,
                    showNextOverlay = false,
                    showCategoryCompletedOverlay = false
                )
            }
            saveState()
        }
    }

    fun selectVideo(video: AutoplayVideo) {
        cancelCountdown()
        val index = _uiState.value.categoryVideos.indexOfFirst { it.id == video.id }
        if (index != -1) {
            _uiState.update {
                it.copy(
                    currentIndex = index,
                    showNextOverlay = false,
                    showCategoryCompletedOverlay = false
                )
            }
            saveState()
        }
    }

    fun replayCategory() {
        cancelCountdown()
        _uiState.update {
            it.copy(
                currentIndex = 0,
                showNextOverlay = false,
                showCategoryCompletedOverlay = false
            )
        }
        saveState()
    }

    fun toggleAutoplay(enabled: Boolean) {
        if (!enabled) {
            cancelCountdown()
        }
        _uiState.update { it.copy(isAutoplayEnabled = enabled) }
        saveState()
    }

    fun toggleFullScreen() {
        _uiState.update { it.copy(isFullScreen = !it.isFullScreen) }
    }

    fun setFullScreen(fullScreen: Boolean) {
        _uiState.update { it.copy(isFullScreen = fullScreen) }
    }

    fun updateIsPlaying(isPlaying: Boolean) {
        _uiState.update { it.copy(isPlaying = isPlaying) }
    }

    fun dismissCategoryCompletedOverlay() {
        _uiState.update { it.copy(showCategoryCompletedOverlay = false) }
    }

    private fun saveState() {
        val state = _uiState.value
        savedStateHandle["saved_category_id"] = state.selectedCategoryId
        savedStateHandle["saved_video_id"] = state.currentVideo?.id
        savedStateHandle["saved_autoplay"] = state.isAutoplayEnabled
    }
}
