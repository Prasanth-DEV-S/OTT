package com.dealshot.app.presentation.shared.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DetailsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        DetailsState()
    )

    val uiState = _uiState.asStateFlow()

    private val _effect = Channel<DetailsEffect>(
        capacity = Channel.BUFFERED
    )
    val effect = _effect.receiveAsFlow()

/*
    fun setVideo(video: Video) {
        _uiState.update {
            it.copy(
                video = video,
                isLoading = false,
                error = null
            )
        }
    }*/

    fun onIntent(intent: DetailsIntent) {

        when (intent) {

            is DetailsIntent.Initialize -> {
                _uiState.update {
                    it.copy(
                        video = intent.video,
                        isLoading = false,
                        error = null
                    )
                }
            }

            DetailsIntent.BackClicked -> {
                sendEffect(
                    DetailsEffect.NavigateBack
                )
            }

            DetailsIntent.PlayVideoClicked -> {
                sendEffect(
                    DetailsEffect.OpenPlayer
                )
            }

            DetailsIntent.MyListClicked -> {
                sendEffect(
                    DetailsEffect.OpenMyList
                )
            }

            DetailsIntent.LikeClicked -> {

            }

            DetailsIntent.ShareClicked -> {
                sendEffect(
                    DetailsEffect.ShareVideo
                )
            }

            DetailsIntent.ClearError -> {
                _uiState.update {
                    it.copy(error = null)
                }
            }
        }
    }

    private fun sendEffect(effect: DetailsEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}