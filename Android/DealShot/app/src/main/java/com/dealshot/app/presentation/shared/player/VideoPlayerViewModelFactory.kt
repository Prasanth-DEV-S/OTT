package com.dealshot.app.presentation.shared.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dealshot.app.data.remote.repository.WatchProgressRepository

class VideoPlayerViewModelFactory(
    private val watchProgressRepository: WatchProgressRepository,
    private val guestPreviewRepository: com.dealshot.app.data.preferences.GuestPreviewRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(VideoPlayerViewModel::class.java)) {
            return VideoPlayerViewModel(
                watchProgressRepository = watchProgressRepository,
                guestPreviewRepository = guestPreviewRepository
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}