package com.dealshot.app.presentation.shared.home

import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.dealshot.app.data.preferences.UserPreferences
import com.dealshot.app.data.remote.dto.VideoDto
import com.dealshot.app.data.video.Video
import com.dealshot.app.data.remote.repository.VideoRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val userPreferences: UserPreferences,
    private val videoRepository: VideoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeState())
    val uiState = _uiState.asStateFlow()

    private val _effect = Channel<HomeEffect>(
        capacity = Channel.BUFFERED
    )

    val effect = _effect.receiveAsFlow()

    init {
        onIntent(HomeIntent.LoadHome)
    }

    fun onIntent(intent: HomeIntent) {

        when (intent) {

            HomeIntent.LoadHome -> {
                loadHome()
            }

            is HomeIntent.VideoClicked -> {
                openPlayer(intent.video)
            }

            is HomeIntent.FeaturedVideoClicked -> {
                sendEffect(HomeEffect.OpenDetails(intent.video))
//                _uiState.value.featuredVideo?.let { video ->
//                    openPlayer(video)
//                }
            }

            HomeIntent.ProfileClicked -> {
                sendEffect(HomeEffect.OpenProfile)
            }

            is HomeIntent.SubscriptionClicked -> {
                sendEffect(
                    HomeEffect.OpenSubscription(
                        intent.video
                    )
                )
            }
            is HomeIntent.SeeAllClicked -> {
                sendEffect(
                    HomeEffect.OpenCategory(intent.category)
                )
            }

            HomeIntent.RetryClicked -> {
                loadHome()
            }
            is HomeIntent.SectionClicked -> {
                loadSection(intent.section)
            }

            HomeIntent.LogoutClicked -> {
                viewModelScope.launch {
                    userPreferences.clearUser()
                    _effect.send(
                        HomeEffect.NavigateToLogin
                    )
                }
            }

            HomeIntent.ClearError -> {
                _uiState.update {
                    it.copy(error = null)
                }
            }

            is HomeIntent.LikedVideosClicked -> {
                sendEffect(HomeEffect.OpenLikedVideos)
            }
            is HomeIntent.MyListClicked -> {
                sendEffect(HomeEffect.OpenLikedVideos)
            }
        }
    }

   /* private fun loadSection(section: HomeSection) {

        val allVideos = _uiState.value.allVideos

        val sectionVideos  = when (section) {

            HomeSection.HOME ->
                allVideos

            HomeSection.EVENTS ->
                allVideos.filter {
                    it.category.equals("Events", ignoreCase = true)
                }

            HomeSection.INTERNATIONAL ->
                allVideos.filter {
                    it.category.equals("International", ignoreCase = true)
                }

            HomeSection.INSPIRATION ->
                allVideos.filter {
                    it.category.equals("Inspiration", ignoreCase = true)
                }

            HomeSection.BANNER ->
                allVideos.filter {
                    it.category.equals("Banner", ignoreCase = true)
                }

            HomeSection.PREMIUM ->
                allVideos.filter {
                    it.isPremium
                }
        }

        _uiState.update {
            it.copy(
                selectedSection = section,
                isLoading = false,
                error = null
            )
        }
    }*/

    private fun loadSection(section: String) {

        val allVideos = _uiState.value.allVideos

        val sectionVideos = when {
            section.equals("Home", ignoreCase = true) -> {
                allVideos
            }

            section.equals("Premium", ignoreCase = true) -> {
                allVideos.filter { it.isPremium }
            }

            else -> {
                allVideos.filter {
                    it.category?.trim()
                        ?.equals(section.trim(), ignoreCase = true) == true
                }
            }
        }

        _uiState.update {
            it.copy(
                selectedSection = section,
                isLoading = false,
                error = null
            )
        }
    }

    @OptIn(UnstableApi::class)
    private fun loadHome() {
        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null
                )
            }

            viewModelScope.launch {

                try {

                    val videos = videoRepository.getVideos()

                    val featuredVideo = videos.firstOrNull { it.featured }

                    val bannerVideos = videos.filter {
                        it.category.equals("Banner", ignoreCase = true)
                    }

                    val inspirationVideos = videos.filter {
                        it.category.equals("Inspiration", ignoreCase = true)
                    }

                    val internationalVideos = videos.filter {
                        it.category.equals("International", ignoreCase = true)
                    }

                    val eventsVideos = videos.filter {
                        it.category.equals("Events", ignoreCase = true)
                    }

                    val premiumVideos = videos.filter {
                        it.isPremium
                    }

                    val premium =
                        videos.filter {
                            it.isPremium
                        }

                    val continueWatching =
                        videos.take(4)

                    val latestReleases =
                        videos.drop(1).take(6)

                    val popularVideos =
                        videos.takeLast(6)

                    _uiState.update {
                        it.copy(
                            isLoading = false,

                            // Complete API response
                            allVideos = videos,

                            // Category-specific videos
                            bannerVideos = bannerVideos,
                            inspirationVideos = inspirationVideos,
                            internationalVideos = internationalVideos,
                            eventsVideos = eventsVideos,
                            premiumVideos = premiumVideos,

                            featuredVideo = featuredVideo,

                            continueWatching = emptyList(),
                            latestReleases = latestReleases,
                            comedyVideos = emptyList(),
                            actionVideos = emptyList(),
                            popularVideos = popularVideos,

                            likedVideos = emptyList(),
                            myListVideos = emptyList(),

                            error = null
                        )
                    }

                } catch (exception: Exception) {

                    _uiState.update {

                        it.copy(
                            isLoading = false,
                            error = exception.message
                                ?: "Something went wrong"
                        )
                    }
                }
            }
        }
    }

    private fun openPlayer(video: VideoDto) {

        viewModelScope.launch {

            _effect.send(
                HomeEffect.OpenPlayer(video)
            )
        }
    }

    private fun sendEffect(effect: HomeEffect) {

        viewModelScope.launch {

            _effect.send(effect)
        }
    }

    @OptIn(UnstableApi::class)
    private fun createDemoVideos(): List<Video> {
        return listOf(
            Video(
                id = "1",
                title = "The Color Journey",
                description = "A one-minute stream demo",
                duration = "1 min",
                thumbnail = "https://dealshot-video-bucket.s3.ap-south-2.amazonaws.com/thumbnails/jewelry_thumbnail.jpg",
                url = "https://d2mt5pdqyed9m3.cloudfront.net/videos/kalyan_jewelry.mp4",
                "premium"
            ),

            Video(
                id = "2",
                title = "The Color Journey",
                description = "A one-minute stream demo",
                duration = "1 min",
                thumbnail = "https://dealshot-video-bucket.s3.ap-south-2.amazonaws.com/thumbnails/jewelry_thumbnail.jpg",
                url = "https://d2mt5pdqyed9m3.cloudfront.net/videos/jewelry.mp4",
                "premium"
            ),

            Video(
                id = "3",
                title = "Chuttamalle",
                description = "CloudFront streaming demo",
                duration = "1 min",
                thumbnail = "https://dealshot-video-bucket.s3.ap-south-2.amazonaws.com/thumbnails/jewelry_thumbnail.jpg",
                url = "https://d2mt5pdqyed9m3.cloudfront.net/videos/gold-video.mp4",
                "sports"
            ),

            Video(
                id = "4",
                title = "Video 4",
                description = "StreamBox demo video",
                duration = "1 min",
                thumbnail = "https://dealshot-video-bucket.s3.ap-south-2.amazonaws.com/thumbnails/Hightech farming.png",
                url = "https://d2mt5pdqyed9m3.cloudfront.net/videos/Why the Future of Farming is in Cities - The Big Money in Vertical Farming.mp4",
                "home"
            ),

            Video(
                id = "5",
                title = "Video 5",
                description = "StreamBox demo video",
                duration = "1 min",
                thumbnail = "https://dealshot-video-bucket.s3.ap-south-2.amazonaws.com/thumbnails/MIG-29s.png",
                url = "https://d2mt5pdqyed9m3.cloudfront.net/videos/When MiG-29s Ambushed F-15 Eagles.mp4",
                "home"
            ),

            Video(
                id = "6",
                title = "Video 6",
                description = "StreamBox demo video",
                duration = "1 min",
                thumbnail = "https://dealshot-video-bucket.s3.ap-south-2.amazonaws.com/thumbnails/Monaco.png",
                url = "https://d2mt5pdqyed9m3.cloudfront.net/videos/Monaco  Where the Rich Come to Live 4k video_1440p.mp4",

                "home"
            ),

            Video(
                id =" 7",
                title = "Video 7",
                description = "StreamBox demo video",
                duration = "1 min",
                thumbnail = "https://dealshot-video-bucket.s3.ap-south-2.amazonaws.com/thumbnails/GOLD.png",
                url = "https://d2mt5pdqyed9m3.cloudfront.net/videos/Inside the 24K Gold Factory From Raw Ore to Pure Gold Bars.mp4",
                "home"
            ),

            Video(
                id = "8",
                title = "Video 8",
                description = "StreamBox demo video",
                duration = "1 min",
                thumbnail = "https://dealshot-video-bucket.s3.ap-south-2.amazonaws.com/thumbnails/GOLD.png",
                url = "https://d2mt5pdqyed9m3.cloudfront.net/videos/Inside the 24K Gold Factory From Raw Ore to Pure Gold Bars.mp4",

                "movies"
            )
        )
    }
}