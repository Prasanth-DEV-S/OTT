package com.dealshot.app.presentation.shared.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dealshot.app.data.preferences.GuestPreviewRepository
import com.dealshot.app.data.preferences.UserPreferences
import com.dealshot.app.data.remote.repository.VideoRepository
import com.dealshot.app.data.remote.repository.WatchProgressRepository

class VideoPlayerViewModelFactory(
    private val watchProgressRepository: WatchProgressRepository,
    private val guestPreviewRepository: GuestPreviewRepository,
    private val userPreferences: UserPreferences? = null,
    private val videoRepository: VideoRepository? = null
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(VideoPlayerViewModel::class.java)) {
            return VideoPlayerViewModel(
                watchProgressRepository = watchProgressRepository,
                guestPreviewRepository = guestPreviewRepository,
                userPreferences = userPreferences,
                videoRepository = videoRepository
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}
