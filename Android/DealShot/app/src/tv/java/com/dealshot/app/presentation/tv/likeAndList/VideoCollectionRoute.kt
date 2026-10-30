package com.dealshot.app.presentation.tv.myList

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.dealshot.app.data.remote.dto.VideoDto
import com.dealshot.app.data.video.Video
import com.dealshot.app.presentation.shared.myList.SavedVideosEffect
import com.dealshot.app.presentation.shared.myList.SavedVideosIntent
import com.dealshot.app.presentation.shared.myList.SavedVideosViewModel

/**
 * TV entry point for Liked Videos / My List. Uses the same shared
 * SavedVideosViewModel, intents and effects as the mobile
 * VideoCollectionRoute.
 */
@Composable
fun TvVideoCollectionRoute(
    viewModel: SavedVideosViewModel,
    title: String,
    onHomeClick: () -> Unit,
    onVideoClick: (VideoDto) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.onIntent(SavedVideosIntent.Load)
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SavedVideosEffect.OpenPlayer -> onVideoClick(effect.video)
                is SavedVideosEffect.ShowMessage -> Unit // available in state
                is SavedVideosEffect.NavigateBack -> Unit // same as mobile
            }
        }
    }

    TvVideoCollectionScreen(
        title = title,
        videos = state.videos.map { it.toTvVideo() },
        onHomeClick = onHomeClick,
        onVideoClick = { video ->
            state.videos.firstOrNull { it.id == video.id }?.let { dto ->
                viewModel.onIntent(SavedVideosIntent.VideoClicked(dto))
            }
        },
        isLoading = state.isLoading,
        errorMessage = state.error,
        onRetry = { viewModel.onIntent(SavedVideosIntent.Retry) },
        onRemoveVideo = { video ->
            viewModel.onIntent(SavedVideosIntent.Remove(video.id))
        },
        removingVideoIds = state.removingIds
    )
}

/** Same field mapping as the mobile VideoDto.toVideo(). */
private fun VideoDto.toTvVideo(): Video {
    return Video(
        id = id.orEmpty(),
        title = title.orEmpty(),
        description = description.orEmpty(),
        duration = duration.orEmpty(),
        thumbnail = thumbnail.orEmpty(),
        url = videoUrl.orEmpty(),
        category = category.orEmpty(),
        apiId = id.orEmpty(),
        thumbnailUrl = thumbnailUrl.orEmpty(),
        isPremium = isPremium ?: false,
        featured = featured ?: false
    )
}
