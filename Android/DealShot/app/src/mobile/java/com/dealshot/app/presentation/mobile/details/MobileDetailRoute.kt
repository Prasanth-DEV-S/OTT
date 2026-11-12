package com.dealshot.app.presentation.mobile.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

import com.dealshot.app.data.video.Video
import com.dealshot.app.data.remote.ApiClient.savedVideosApiService
import com.dealshot.app.data.remote.ApiClient.videoDetailsApiService
import com.dealshot.app.data.remote.ApiClient.likedVideosApiService
import com.dealshot.app.data.remote.repository.SavedVideosRepositoryImpl
import com.dealshot.app.data.remote.repository.like.LikedVideosRepositoryImpl
import com.dealshot.app.data.remote.repository.VideoDetailsRepositoryImpl
import com.dealshot.app.presentation.shared.details.DetailsEffect
import com.dealshot.app.presentation.shared.details.DetailsIntent
import com.dealshot.app.presentation.shared.details.DetailsViewModel
import com.dealshot.app.presentation.shared.details.DetailsViewModelFactory

@Composable
fun MobileDetailsRoute(
    video: Video,
    onBack: () -> Unit,
    onPlayVideo: () -> Unit,
    onMyListClick: () -> Unit = {},
    onLikeClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onLoginClick: () -> Unit = {},
    isPremiumUser: Boolean = false,
    onSubscribeClick: () -> Unit = {},
    onReportClick: () -> Unit = {}
) {
    val repository = remember {
        VideoDetailsRepositoryImpl(
            api = videoDetailsApiService
        )
    }

    val savedVideosRepository = remember {
        SavedVideosRepositoryImpl(
            api = savedVideosApiService
        )
    }
    val likedVideosRepository = remember {
        LikedVideosRepositoryImpl(
            api = likedVideosApiService
        )
    }

    val factory = remember(
        repository,
        savedVideosRepository,
        likedVideosRepository
    ) {
        DetailsViewModelFactory(
            repository = repository,
            savedVideosRepository = savedVideosRepository,
            likedVideosRepository = likedVideosRepository
        )
    }

    val viewModel: DetailsViewModel = viewModel(
        factory = factory
    )

    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(video) {
        viewModel.onIntent(
            DetailsIntent.Initialize(video)
        )
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                DetailsEffect.NavigateBack -> onBack()
                DetailsEffect.OpenPlayer -> onPlayVideo()
                DetailsEffect.OpenMyList -> onMyListClick()
                DetailsEffect.ShareVideo -> onShareClick()
                DetailsEffect.Liked -> onLikeClick()
                DetailsEffect.OpenLogin -> {
                    onLoginClick()
                }
            }
        }
    }

    MobileDetailsScreen(
        video = state.video ?: video,
        onBackClick = {
            viewModel.onIntent(DetailsIntent.BackClicked)
        },
        onPlayVideo = {
            viewModel.onIntent(DetailsIntent.PlayVideoClicked)
        },
        isInMyWatchLater = state.isInMyWatchLater,
        isUpdatingMyWatchLater = state.isUpdatingMyWatchLater,
        onMyListClick = {
            viewModel.onIntent(DetailsIntent.MyListClicked)
        },
        onLikeClick = {
            viewModel.onIntent(DetailsIntent.LikeClicked)
        },
        onShareClick = {
            viewModel.onIntent(DetailsIntent.ShareClicked)
        },
        isLiked = state.isLiked,
        isUpdatingLike = state.isUpdatingLike,
        onLoginClick = onLoginClick,
        isPremiumUser = isPremiumUser,
        onSubscribeClick = onSubscribeClick,
        onReportClick = onReportClick
    )
}
