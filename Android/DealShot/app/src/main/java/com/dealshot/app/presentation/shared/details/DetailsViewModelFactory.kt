
package com.dealshot.app.presentation.shared.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dealshot.app.data.remote.repository.VideoDetailsRepository
import com.dealshot.app.data.remote.repository.SavedVideosRepository

class DetailsViewModelFactory(
    private val repository: VideoDetailsRepository,
    private val savedVideosRepository: SavedVideosRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailsViewModel::class.java)) {
            return DetailsViewModel(
                videoDetailsRepository = repository,
                savedVideosRepository = savedVideosRepository
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}