package com.dealshot.app.presentation.shared.home

import com.dealshot.app.data.remote.dto.VideoDto
import com.dealshot.app.data.video.Video

data class HomeState(
    val isLoading: Boolean = false,

    // Complete API data
    val allVideos: List<VideoDto> = emptyList(),

    // Currently selected section
    val bannerVideos: List<VideoDto> = emptyList(),
    val inspirationVideos: List<VideoDto> = emptyList(),
    val internationalVideos: List<VideoDto> = emptyList(),
    val eventsVideos: List<VideoDto> = emptyList(),
    val premiumVideos: List<VideoDto> = emptyList(),

    val featuredVideo: VideoDto? = null,

    val continueWatching: List<VideoDto> = emptyList(),
    val latestReleases: List<VideoDto> = emptyList(),
    val comedyVideos: List<VideoDto> = emptyList(),
    val actionVideos: List<VideoDto> = emptyList(),
    val popularVideos: List<VideoDto> = emptyList(),

    val error: String? = null,

    val categories: List<VideoCategory> = emptyList(),

    val likedVideos: List<VideoDto> = emptyList(),
    val myListVideos: List<VideoDto> = emptyList(),

    val selectedSection: String = "Home"

    )


data class VideoCategory(
    val name: String,
    val videos: List<Video>
)
/*
enum class HomeSection {
    HOME,
    EVENTS,
    INTERNATIONAL,
    INSPIRATION,
    BANNER,
    PREMIUM,
    NATIONAL,
    MOTIVATIONAL,
    ACADEMIC

}
*/

sealed interface HomeIntent {

    data object LoadHome : HomeIntent

    data class VideoClicked(
        val video: VideoDto
    ) : HomeIntent

    data class SectionClicked(
        val section: String
    ) : HomeIntent
    data class FeaturedVideoClicked(val video: VideoDto) : HomeIntent

    data object ProfileClicked : HomeIntent

    data class SubscriptionClicked(
        val video: VideoDto
    ) : HomeIntent
    data object RetryClicked : HomeIntent

    data object LogoutClicked : HomeIntent

    data object MyListClicked : HomeIntent
    data object LikedVideosClicked  : HomeIntent

    data object ClearError : HomeIntent
    data class SeeAllClicked(
        val category: String
    ) : HomeIntent
}

sealed interface HomeEffect {

    data class OpenPlayer(
        val video: VideoDto
    ) : HomeEffect

    data class OpenDetails(
        val video: VideoDto
    ) : HomeEffect
    data object OpenProfile : HomeEffect

    data class OpenSubscription(
        val video: VideoDto
    ) : HomeEffect

    data class OpenCategory(
        val category: String
    ) : HomeEffect
    data object NavigateToLogin : HomeEffect
    data object OpenMyList : HomeEffect
    data object OpenLikedVideos  : HomeEffect

}
