package com.dealshot.app.presentation.shared.home

import androidx.lifecycle.ViewModel
//import com.DealShot.stream.data.video.VideoData

class TvOtpScreen : ViewModel() {

//    private val _state = MutableStateFlow(
//        HomeState()
//    )
//
//    val state = _state.asStateFlow()
//
//    private val _effect = MutableSharedFlow<HomeEffect>()
//
//    val effect = _effect.asSharedFlow()
//
//    init {
//        onIntent(HomeIntent.LoadVideos)
//    }
//
//    fun onIntent(intent: HomeIntent) {
//
//        when (intent) {
//
//            HomeIntent.LoadVideos -> {
//                loadVideos()
//            }
//
//            is HomeIntent.VideoClicked -> {
//
//                viewModelScope.launch {
//
//                    _effect.emit(
//                        HomeEffect.OpenPlayer(
//                            video = intent.video
//                        )
//                    )
//                }
//            }
//
//            HomeIntent.ProfileClicked -> {
//
//                viewModelScope.launch {
//
//                    _effect.emit(
//                        HomeEffect.OpenProfile
//                    )
//                }
//            }
//        }
//    }
//
//    private fun loadVideos() {
//
//        _state.value = _state.value.copy(
//            isLoading = true,
//            error = null
//        )
//
//        try {
//
//            /*
//             * Currently using VideoData directly.
//             *
//             * Later replace this with:
//             *
//             * repository.getFeaturedVideos()
//             *
//             * or:
//             *
//             * getFeaturedVideosUseCase()
//             */
//
//            val videos = VideoData.featuredVideo
//
//            _state.value = _state.value.copy(
//                isLoading = false,
//                videos = videos
//            )
//
//        } catch (exception: Exception) {
//
//            _state.value = _state.value.copy(
//                isLoading = false,
//                error = exception.message
//                    ?: "Unable to load videos"
//            )
//        }
//    }
}
