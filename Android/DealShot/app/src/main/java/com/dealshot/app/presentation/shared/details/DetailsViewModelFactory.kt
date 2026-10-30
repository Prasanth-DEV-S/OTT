
package com.dealshot.app.presentation.shared.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dealshot.app.data.remote.repository.SavedVideosRepository
import com.dealshot.app.data.remote.repository.VideoDetailsRepository
import com.dealshot.app.data.remote.repository.like.LikedVideosRepository

class DetailsViewModelFactory(
    private val repository: VideoDetailsRepository,
    private val savedVideosRepository: SavedVideosRepository,
    private val likedVideosRepository: LikedVideosRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailsViewModel::class.java)) {
            return DetailsViewModel(
                videoDetailsRepository = repository,
                savedVideosRepository = savedVideosRepository,
                likedVideosRepository = likedVideosRepository
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}