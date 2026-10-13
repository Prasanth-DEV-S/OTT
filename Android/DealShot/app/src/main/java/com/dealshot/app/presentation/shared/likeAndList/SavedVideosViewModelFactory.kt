package com.dealshot.app.presentation.shared.likeAndList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dealshot.app.data.remote.repository.SavedVideoCollection
import com.dealshot.app.data.remote.repository.SavedVideosRepository
import com.dealshot.app.presentation.shared.likeAndList.SavedVideosViewModel

class SavedVideosViewModelFactory(
    private val repository: SavedVideosRepository,
    private val collection: SavedVideoCollection
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SavedVideosViewModel::class.java)) {
            return SavedVideosViewModel(repository, collection) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}
