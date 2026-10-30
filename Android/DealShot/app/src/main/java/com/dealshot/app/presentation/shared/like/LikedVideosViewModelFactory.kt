package com.dealshot.app.presentation.shared.like

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dealshot.app.data.remote.repository.like.LikedVideosRepository

class LikedVideosViewModelFactory(
    private val repository: LikedVideosRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(LikedVideosViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LikedVideosViewModel(repository) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}