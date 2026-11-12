package com.dealshot.app.presentation.mobile.category

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.dealshot.app.data.remote.ApiClient
import com.dealshot.app.data.remote.dto.VideoDto
import com.dealshot.app.data.remote.repository.VideoRepositoryImpl
import com.dealshot.app.ui.theme.GoldPrimary

@Composable
fun CategoryVideosRoute(
    category: String,
    onBack: () -> Unit,
    onVideoClick: (VideoDto) -> Unit
) {
    val repository = remember {
        VideoRepositoryImpl(ApiClient.videoApi)
    }

    var videos by remember {
        mutableStateOf<List<VideoDto>>(emptyList())
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    LaunchedEffect(category) {
        isLoading = true
        try {
            if (category.equals("Premium", ignoreCase = true)) {
                val allVideos = repository.getVideos()
                videos = allVideos.filter {
                    it.isPremium && !it.videoUrl.isNullOrBlank()
                }
            } else {
                val result = repository.getVideosByCategory(category)
                result.fold(
                    onSuccess = { catVideos ->
                        if (catVideos.isNotEmpty()) {
                            videos = catVideos
                        } else {
                            val allVideos = repository.getVideos()
                            videos = allVideos.filter {
                                it.category?.trim()?.equals(category.trim(), ignoreCase = true) == true &&
                                        !it.videoUrl.isNullOrBlank()
                            }
                        }
                    },
                    onFailure = {
                        try {
                            val allVideos = repository.getVideos()
                            videos = allVideos.filter {
                                it.category?.trim()?.equals(category.trim(), ignoreCase = true) == true &&
                                        !it.videoUrl.isNullOrBlank()
                            }
                        } catch (_: Exception) {
                            videos = emptyList()
                        }
                    }
                )
            }
        } finally {
            isLoading = false
        }
    }

    when {
        isLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GoldPrimary)
            }
        }

        else -> {
            CategoryVideosScreen(
                category = category,
                videos = videos,
                onBack = onBack,
                onVideoClick = onVideoClick
            )
        }
    }
}
