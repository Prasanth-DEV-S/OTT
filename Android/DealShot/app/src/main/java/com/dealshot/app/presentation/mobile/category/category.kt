package com.dealshot.app.presentation.mobile.category

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.dealshot.app.data.remote.ApiClient
import com.dealshot.app.data.remote.dto.VideoDto
import com.dealshot.app.data.remote.repository.VideoRepositoryImpl

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
        try {
            val allVideos = repository.getVideos()

            videos = if (category.equals("Premium", ignoreCase = true)) {
                allVideos.filter {
                    it.isPremium &&
                            !it.videoUrl.isNullOrBlank()
                }
            } else {
                allVideos.filter {
                    it.category
                        ?.trim()
                        ?.equals(category.trim(), ignoreCase = true) == true &&
                            !it.videoUrl.isNullOrBlank()
                }
            }
        } finally {
            isLoading = false
        }
    }

    when {
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "Loading...",
                    color = Color.White
                )
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