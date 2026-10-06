package com.dealshot.app.presentation.mobile.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dealshot.app.data.preferences.UserPreferences
import com.dealshot.app.data.remote.ApiClient
import com.dealshot.app.data.remote.dto.VideoDto
import com.dealshot.app.data.remote.repository.VideoRepositoryImpl
import com.dealshot.app.presentation.shared.home.HomeEffect
import com.dealshot.app.presentation.shared.home.HomeViewModel

@Composable
fun HomeRoute(
    onOpenPlayer: (VideoDto) -> Unit,
    onOpenProfile: () -> Unit,
    onOpenSubscription: (VideoDto) -> Unit,
    onNavigateToLogin: () -> Unit,
    onOpenLiked: () -> Unit,
    onOpenDetails: (String) -> Unit,
    onOpenMyList: () -> Unit,
    onOpenCategory: (String) -> Unit,
    mobileNumber: String,
) {

    val context = LocalContext.current

    val userPreferences = remember {
        UserPreferences(context.applicationContext)
    }

    val viewModel: HomeViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {

            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>
            ): T {
                return HomeViewModel(
                    userPreferences = userPreferences,
                    videoRepository = VideoRepositoryImpl(
                        ApiClient.videoApi
                    )
                ) as T
            }
        }
    )

    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {

        viewModel.effect.collect { effect ->
            when (effect) {
                is HomeEffect.OpenDetails -> {
                    onOpenDetails(effect.video.id)
                }
                is HomeEffect.OpenCategory -> {
                    onOpenCategory(effect.category)
                }

                is HomeEffect.OpenPlayer -> {
                    onOpenPlayer(effect.video)
                }
                HomeEffect.OpenProfile -> {
                    onOpenProfile()
                }

                is HomeEffect.OpenSubscription -> {
                    onOpenSubscription(effect.video)
                }

                HomeEffect.NavigateToLogin -> {
                    onNavigateToLogin()
                }

                HomeEffect.OpenLikedVideos -> {
                    onOpenLiked()
                }

                HomeEffect.OpenMyList -> {
                    onOpenMyList()
                }
            }
        }
    }

    HomeScreen(
        state = state,
        onIntent = viewModel::onIntent
    )
}