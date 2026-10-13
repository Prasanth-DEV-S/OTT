package com.dealshot.app.presentation.shared.home

import android.util.Log
import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.dealshot.app.core.error.AppErrorHandler
import com.dealshot.app.data.preferences.UserPreferences
import com.dealshot.app.data.remote.dto.VideoDto
import com.dealshot.app.data.remote.repository.VideoRepository
import com.dealshot.app.data.remote.repository.SavedVideosRepository
import com.dealshot.app.data.remote.repository.SavedVideoCollection
import retrofit2.HttpException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class HomeViewModel(
    private val userPreferences: UserPreferences,
    private val videoRepository: VideoRepository,
    private val savedVideosRepository: SavedVideosRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeState(    ))
    val uiState = _uiState.asStateFlow()

    private val _effect = Channel<HomeEffect>(
        capacity = Channel.BUFFERED
    )

    val effect = _effect.receiveAsFlow()

    init {
        onIntent(HomeIntent.LoadHome)
        onIntent(HomeIntent.LoadCategories)
        viewModelScope.launch {
            loadMyWatchLaterIds()
        }
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
            is HomeIntent.LoadCategories -> {
                loadCategories()
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
            is HomeIntent.RefreshMyWatchLater -> {
                viewModelScope.launch { loadMyWatchLaterIds() }
            }

            HomeIntent.LogoutClicked -> {
                logout()
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
                sendEffect(HomeEffect.OpenMyList)
            }
            is HomeIntent.ToggleMyList -> {
                toggleMyList(intent.video)
            }
        }
    }

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

    private fun buildSections(
        categories: List<VideoCategory>,
        allVideos: List<VideoDto>
    ): List<String> {
        return buildList {
            add("Home")

            addAll(
                categories
                    .map { it.name.trim() }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .sorted()
            )

            // Uncomment if you want Premium to appear only when premium videos exist.
            // if (allVideos.any { it.isPremium }) {
            //     add("Premium")
            // }
        }.distinct()
    }

    private suspend fun loadMyWatchLaterIds() {
        try {
            val ids = savedVideosRepository
                .getVideos(SavedVideoCollection.WATCH_LATER)
                .map { it.id }
                .toSet()
            _uiState.update { it.copy(myWatchLaterVideoIds = ids) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Keep the Home screen usable even if this list fails to load
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
                    val bannerVideos = videos.filter {
                        it.category.equals("Banner", ignoreCase = true)
                    }
                   /* val featuredVideo = videos.firstOrNull { it.featured }



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
                    }*/

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

                    val savedVideos = try {
                        savedVideosRepository.getVideos(SavedVideoCollection.WATCH_LATER)
                    } catch (e: Exception) {
                        emptyList()
                    }
                    val savedIds = savedVideos.map { it.id }.toSet()

                    _uiState.update {
                        it.copy(
                            isLoading = false,

                            // Complete API response
                            allVideos = videos,

                            // Category-specific videos
                            bannerVideos = bannerVideos,

                            continueWatching = emptyList(),
                            latestReleases = latestReleases,
                            comedyVideos = emptyList(),
                            actionVideos = emptyList(),
                            popularVideos = popularVideos,

                            likedVideos = emptyList(),
                            myListVideos = emptyList(),
                            myWatchLaterVideoIds = savedIds,

                            error = null
                        )
                    }

                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = AppErrorHandler.getUserMessage(e)
                        )
                    }
                }
            }
        }
    }

  /*  private fun toggleMyList(video: VideoDto) {
        val videoId = video.id
        val current = _uiState.value

        if (videoId in current.updatingMyWatchLaterVideoIds) return
        val isSaved = videoId in state.myWatchLaterVideoIds
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    updatingMyWatchLaterVideoIds = it.updatingMyWatchLaterVideoIds + videoId,
                    error = null
                )
            }

            try {
                val isSaved = videoId in _uiState.value.myWatchLaterVideoIds

                if (isSaved) {
                    savedVideosRepository.removeVideo(
                        SavedVideoCollection.WATCH_LATER,
                        videoId
                    )
                } else {
                    savedVideosRepository.addVideo(
                        SavedVideoCollection.WATCH_LATER,
                        videoId
                    )
                }

                _uiState.update { state ->
                    val newSavedIds = if (isSaved) {
                        state.myWatchLaterVideoIds - videoId
                    } else {
                        state.myWatchLaterVideoIds + videoId
                    }

                    state.copy(
                        myWatchLaterVideoIds = newSavedIds,
                        updatingMyWatchLaterVideoIds = state.updatingMyWatchLaterVideoIds - videoId
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val statusCode = (e as? HttpException)?.code()
                if (statusCode == 409) {
                    _uiState.update { state ->
                        state.copy(
                            myWatchLaterVideoIds = state.myWatchLaterVideoIds + videoId,
                            updatingMyWatchLaterVideoIds = state.updatingMyWatchLaterVideoIds - videoId,
                            error = null
                        )
                    }
                } else if (statusCode == 404) {
                    _uiState.update { state ->
                        state.copy(
                            myWatchLaterVideoIds = state.myWatchLaterVideoIds - videoId,
                            updatingMyWatchLaterVideoIds = state.updatingMyWatchLaterVideoIds - videoId,
                            error = null
                        )
                    }
                } else {
                    _uiState.update { state ->
                        state.copy(
                            updatingMyWatchLaterVideoIds = state.updatingMyWatchLaterVideoIds - videoId,
                            error = AppErrorHandler.getUserMessage(e)
                        )
                    }
                }
            }
        }
    }*/

    private fun toggleMyList(video: VideoDto) {
        val videoId = video.id
        val state = _uiState.value

        // Ignore taps while this video's request is still running
        if (videoId in state.updatingMyWatchLaterVideoIds) return

        val isSaved = videoId in state.myWatchLaterVideoIds

        _uiState.update {
            it.copy(updatingMyWatchLaterVideoIds = it.updatingMyWatchLaterVideoIds + videoId)
        }

        viewModelScope.launch {
            try {
                /*if (isSaved) {
                    savedVideosRepository.removeVideo(SavedVideoCollection.WATCH_LATER, videoId)
                } else {*/
                    savedVideosRepository.addVideo(SavedVideoCollection.WATCH_LATER, videoId)
//                }

                _uiState.update {
                    it.copy(
                        myWatchLaterVideoIds = if (isSaved) {
                            it.myWatchLaterVideoIds - videoId
                        } else {
                            it.myWatchLaterVideoIds + videoId
                        }
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Unable to update My List.") }
            } finally {
                _uiState.update {
                    it.copy(updatingMyWatchLaterVideoIds = it.updatingMyWatchLaterVideoIds - videoId)
                }
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isCategoriesLoading = true)
            }

            try {
                val response = videoRepository.getVideoCategories()

                val categories = response
                    .mapNotNull { category ->
                        category.name.trim()
                            .takeIf { name -> name.isNotBlank() }
                            ?.let { name ->
                                VideoCategory(
                                    name = name,
                                    count = category.count
                                )
                            }
                    }
                    .distinctBy { it.name.lowercase() }
                    .sortedBy { it.name }

                _uiState.update { currentState ->
                    currentState.copy(
                        categories = categories,
                        sections = buildSections(
                            categories = categories,
                            allVideos = currentState.allVideos
                        ),
                        isCategoriesLoading = false
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        isCategoriesLoading = false,
                        error = AppErrorHandler.getUserMessage(e)
                    )
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


    private fun logout() {
        viewModelScope.launch {
            try {
                userPreferences.clearUser()
                _effect.send(HomeEffect.NavigateToLogin)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    /**
     * Single place that turns any exception into a user-friendly message.
     */
    private fun handleError(throwable: Throwable) {
        if (throwable is CancellationException) {
            throw throwable
        }

        _uiState.update {
            it.copy(
                isLoading = false,
                error = AppErrorHandler.getUserMessage(throwable)
            )
        }
    }
}