package com.dealshot.app.presentation.mobile.likeAndList

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dealshot.app.data.remote.dto.VideoDto
import com.dealshot.app.data.remote.repository.SavedVideoCollection
import com.dealshot.app.data.remote.repository.SavedVideosRepository
import com.dealshot.app.data.video.Video
import com.dealshot.app.presentation.shared.likeAndList.SavedVideosEffect
import com.dealshot.app.presentation.shared.likeAndList.SavedVideosIntent
import com.dealshot.app.presentation.shared.likeAndList.SavedVideosViewModel
@Composable
fun VideoCollectionRoute(
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
                is SavedVideosEffect.OpenPlayer -> {
                    onVideoClick(effect.video)
                }

                is SavedVideosEffect.ShowMessage -> {
                    // Message is already available in state.
                }
                is SavedVideosEffect.NavigateBack -> {
                    // Message is already available in state.
                }

            }
        }
    }

    VideoCollectionScreen(
        title = title,
        videos = state.videos.map { it.toVideo() },
        onHomeClick = onHomeClick,
        onVideoClick = { video ->
            state.videos.firstOrNull { it.id == video.id }?.let { dto ->
                viewModel.onIntent(
                    SavedVideosIntent.VideoClicked(dto)
                )
            }
        },
        isLoading = state.isLoading,
        errorMessage = state.error,
        onRetry = {
            viewModel.onIntent(SavedVideosIntent.Retry)
        },
        onRemoveVideo = { video ->
            viewModel.onIntent(
                SavedVideosIntent.Remove(video.id)
            )
        },
        removingVideoIds = state.removingIds
    )
}

/**
 * Adapt this mapping to the exact fields in your VideoDto and Video model.
 */
fun VideoDto.toVideo(): Video {
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