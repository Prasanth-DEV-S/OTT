package com.dealshot.app.presentation.tv.category

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.dealshot.app.data.remote.ApiClient
import com.dealshot.app.data.remote.dto.VideoDto
import com.dealshot.app.data.remote.repository.VideoRepositoryImpl

/**
 * TV entry point for a category / "See all" screen.
 * Uses the same repository call and filtering rules as the mobile
 * CategoryVideosRoute.
 */
@Composable
fun TvCategoryRoute(
    category: String,
    onBack: () -> Unit,
    onVideoClick: (VideoDto) -> Unit
) {
    val repository = remember {
        VideoRepositoryImpl(ApiClient.videoApi)
    }

    var videos by remember { mutableStateOf<List<VideoDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }

    LaunchedEffect(category) {
        isLoading = true
        hasError = false
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
                            hasError = true
                        }
                    }
                )
            }
        } catch (e: Exception) {
            hasError = true
        } finally {
            isLoading = false
        }
    }

    TvCategoryScreen(
        category = category,
        videos = videos,
        isLoading = isLoading,
        hasError = hasError,
        onBack = onBack,
        onVideoClick = onVideoClick
    )
}
