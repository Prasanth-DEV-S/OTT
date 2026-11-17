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
import com.dealshot.app.data.remote.ApiClient.seriesApiService
import com.dealshot.app.data.remote.repository.SavedVideosRepositoryImpl
import com.dealshot.app.data.remote.repository.like.LikedVideosRepositoryImpl
import com.dealshot.app.data.remote.repository.VideoDetailsRepositoryImpl
import com.dealshot.app.data.remote.repository.SeriesRepositoryImpl
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
    val seriesRepository = remember {
        SeriesRepositoryImpl(
            api = seriesApiService
        )
    }

    val factory = remember(
        repository,
        savedVideosRepository,
        likedVideosRepository,
        seriesRepository
    ) {
        DetailsViewModelFactory(
            repository = repository,
            savedVideosRepository = savedVideosRepository,
            likedVideosRepository = likedVideosRepository,
            seriesRepository = seriesRepository
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
                is DetailsEffect.PlayEpisode -> {
                    // Handled inside MobileDetailsScreen via activeEpisodeDetails / effect
                }
                is DetailsEffect.UpdateEpisodeQualityUrl -> {
                    // Handled in player
                }
                DetailsEffect.OpenMyList -> onMyListClick()
                DetailsEffect.ShareVideo -> onShareClick()
                DetailsEffect.Liked -> onLikeClick()
                DetailsEffect.OpenLogin -> {
                    onLoginClick()
                }
            }
        }
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    val preferenceManager = remember { com.dealshot.app.data.preferences.PreferenceManager(context) }
    val isLoggedIn = preferenceManager.isLoggedIn()
    MobileDetailsScreen(
        video = state.video ?: video,
        nextVideos = state.nextVideos,
        onSelectNextVideo = { nextVideoDto ->
            viewModel.onIntent(DetailsIntent.SelectNextVideo(nextVideoDto))
        },
        onBackClick = {
            viewModel.onIntent(DetailsIntent.BackClicked)
        },
        onPlayVideo = {
            viewModel.onIntent(DetailsIntent.PlayVideoClicked)
        },
        isInMyWatchLater = state.isInMyWatchLater,
        isUpdatingMyWatchLater = state.isUpdatingMyWatchLater,
        onMyListClick = {

            if (!isLoggedIn) {
                android.widget.Toast.makeText(context, "Please log in to Add this video to your list.", android.widget.Toast.LENGTH_SHORT).show()
                onLoginClick()
            } else {
                viewModel.onIntent(DetailsIntent.MyListClicked)
            }
        },
        onLikeClick = {
            if (!isLoggedIn) {
                android.widget.Toast.makeText(context, "Please log in to like this video .", android.widget.Toast.LENGTH_SHORT).show()
                onLoginClick()
            } else {
                viewModel.onIntent(DetailsIntent.LikeClicked)
            }
        },
        onShareClick = {
            viewModel.onIntent(DetailsIntent.ShareClicked)
        },
        isLiked = state.isLiked,
        isUpdatingLike = state.isUpdatingLike,
        onLoginClick = onLoginClick,
        isPremiumUser = isPremiumUser,
        onSubscribeClick = onSubscribeClick,
        onReportClick = {
            if (!isLoggedIn) {
                android.widget.Toast.makeText(
                    context,
                    "Please log in to Report this video .",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                onLoginClick()
            } else {
                onReportClick()
            }
        },
        isReported = state.isReported,
        isSeries = state.isSeries,
        seasons = state.seasons,
        selectedSeasonNumber = state.selectedSeasonNumber,
        episodes = state.episodes,
        isLoadingEpisodes = state.isLoadingEpisodes,
        continueWatching = state.continueWatching,
        activeEpisodeDetails = state.activeEpisodeDetails,
        episodeNavigation = state.episodeNavigation,
        autoplayInfo = state.autoplayInfo,
        onSeasonSelect = { seasonNum ->
            viewModel.onIntent(DetailsIntent.SelectSeason(seasonNum))
        },
        onEpisodeSelect = { episode ->
            viewModel.onIntent(DetailsIntent.SelectEpisode(episode))
        },
        onWatchFirstEpisodeClick = {
            viewModel.onIntent(DetailsIntent.WatchFirstEpisodeClicked)
        },
        onPreviousEpisodeClick = {
            viewModel.onIntent(DetailsIntent.PreviousEpisodeClicked)
        },
        onNextEpisodeClick = {
            viewModel.onIntent(DetailsIntent.NextEpisodeClicked)
        },
        onCheckAutoplayNext = { epId ->
            viewModel.onIntent(DetailsIntent.CheckAutoplayNext(epId))
        },
        onSelectEpisodeQuality = { epId, qual ->
            viewModel.onIntent(DetailsIntent.SelectEpisodeQuality(epId, qual))
        }
    )
}
