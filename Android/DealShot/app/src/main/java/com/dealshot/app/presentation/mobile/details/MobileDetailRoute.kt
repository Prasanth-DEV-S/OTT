package com.dealshot.app.presentation.mobile.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import com.dealshot.app.data.video.Video
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dealshot.app.presentation.shared.details.DetailsEffect
import com.dealshot.app.presentation.shared.details.DetailsIntent
import com.dealshot.app.presentation.shared.details.DetailsViewModel
@Composable
fun MobileDetailsRoute(
    video: Video,
    onBack: () -> Unit,
    onPlayVideo: () -> Unit,
    onMyListClick: () -> Unit = {},
    onLikeClick: () -> Unit = {},
    onShareClick: () -> Unit = {}
) {
    val viewModel: DetailsViewModel = viewModel()

    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(video) {
        viewModel.onIntent(
            DetailsIntent.Initialize(video)
        )
    }
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->

            when (effect) {

                DetailsEffect.NavigateBack -> {
                    onBack()
                }

                DetailsEffect.OpenPlayer -> {
                    onPlayVideo()
                }

                DetailsEffect.OpenMyList -> {
                    onMyListClick()
                }

                DetailsEffect.ShareVideo -> {
                    onShareClick()
                }

                DetailsEffect.OpenLogin -> {
                    // Handle login navigation if needed
                }
            }
        }
    }

    MobileDetailsScreen(
        video = state.video ?: video,
        onBackClick = {
            viewModel.onIntent(
                DetailsIntent.BackClicked
            )
        },
        onPlayVideo = {
            viewModel.onIntent(
                DetailsIntent.PlayVideoClicked
            )
        },
        onMyListClick = {
            viewModel.onIntent(
                DetailsIntent.MyListClicked
            )
        },
        onLikeClick = {
            viewModel.onIntent(
                DetailsIntent.LikeClicked
            )
        },
        onShareClick = {
            viewModel.onIntent(
                DetailsIntent.ShareClicked
            )
        }
    )
}