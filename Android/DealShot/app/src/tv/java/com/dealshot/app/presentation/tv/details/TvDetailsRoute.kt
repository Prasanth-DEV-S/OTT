package com.dealshot.app.presentation.tv.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dealshot.app.data.remote.ApiClient.savedVideosApiService
import com.dealshot.app.data.remote.ApiClient.videoDetailsApiService
import com.dealshot.app.data.remote.ApiClient.likedVideosApiService
import com.dealshot.app.data.remote.repository.SavedVideosRepositoryImpl
import com.dealshot.app.data.remote.repository.like.LikedVideosRepositoryImpl
import com.dealshot.app.data.remote.repository.VideoDetailsRepositoryImpl
import com.dealshot.app.data.video.Video
import com.dealshot.app.presentation.shared.details.DetailsEffect
import com.dealshot.app.presentation.shared.details.DetailsIntent
import com.dealshot.app.presentation.shared.details.DetailsViewModel
import com.dealshot.app.presentation.shared.details.DetailsViewModelFactory

/**
 * TV entry point for Details. Uses the same shared DetailsViewModel,
 * repositories and effects as MobileDetailsRoute.
 */
@Composable
fun TvDetailsRoute(
    video: Video,
    onBack: () -> Unit,
    onPlayVideo: () -> Unit,
    onMyListClick: () -> Unit = {},
    onLikeClick: () -> Unit = {},
    onOpenLogin: () -> Unit = {}
) {
    val repository = remember {
        VideoDetailsRepositoryImpl(api = videoDetailsApiService)
    }

    val savedVideosRepository = remember {
        SavedVideosRepositoryImpl(api = savedVideosApiService)
    }

    val likedVideosRepository = remember {
        LikedVideosRepositoryImpl(api = likedVideosApiService)
    }

    val factory = remember(repository, savedVideosRepository, likedVideosRepository) {
        DetailsViewModelFactory(
            repository = repository,
            savedVideosRepository = savedVideosRepository,
            likedVideosRepository = likedVideosRepository
        )
    }

    val viewModel: DetailsViewModel = viewModel(factory = factory)
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(video) {
        viewModel.onIntent(DetailsIntent.Initialize(video))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                DetailsEffect.NavigateBack -> onBack()
                DetailsEffect.OpenPlayer -> onPlayVideo()
                DetailsEffect.OpenMyList -> onMyListClick()
                DetailsEffect.Liked -> onLikeClick()
                DetailsEffect.OpenLogin -> onOpenLogin()
                // Sharing is not supported on TV.
                DetailsEffect.ShareVideo -> Unit
            }
        }
    }

    TvDetailsScreen(
        video = state.video ?: video,
        isLiked = state.isLiked,
        isUpdatingLike = state.isUpdatingLike,
        isInMyWatchLater = state.isInMyWatchLater,
        isUpdatingMyWatchLater = state.isUpdatingMyWatchLater,
        onPlayVideo = { viewModel.onIntent(DetailsIntent.PlayVideoClicked) },
        onMyListClick = { viewModel.onIntent(DetailsIntent.MyListClicked) },
        onLikeClick = { viewModel.onIntent(DetailsIntent.LikeClicked) }
    )
}
