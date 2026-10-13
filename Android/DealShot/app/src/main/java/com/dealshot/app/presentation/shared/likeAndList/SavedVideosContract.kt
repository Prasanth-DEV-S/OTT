package com.dealshot.app.presentation.shared.likeAndList

import com.dealshot.app.data.remote.dto.VideoDto

data class SavedVideosState(
    val videos: List<VideoDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val addingIds: Set<String> = emptySet(),
    val removingIds: Set<String> = emptySet(),
    val addedIds: Set<String> = emptySet()
)

sealed interface SavedVideosIntent {
    data object Load : SavedVideosIntent

    data class Add(
        val videoId: String,
        val video: VideoDto? = null
    ) : SavedVideosIntent

    data class Remove(
        val videoId: String
    ) : SavedVideosIntent

    data object Retry : SavedVideosIntent

    data object ClearError : SavedVideosIntent

    data object ClearMessage : SavedVideosIntent

    data class VideoClicked(
        val video: VideoDto
    ) : SavedVideosIntent
}

sealed interface SavedVideosEffect {
    data class ShowMessage(
        val text: String
    ) : SavedVideosEffect

    data class OpenPlayer(
        val video: VideoDto
    ) : SavedVideosEffect

    data object NavigateBack : SavedVideosEffect
}