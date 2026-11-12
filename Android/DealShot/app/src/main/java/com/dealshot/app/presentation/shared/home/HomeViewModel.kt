package com.dealshot.app.presentation.shared.home

import android.util.Log
import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.dealshot.app.core.error.AppErrorHandler
import com.dealshot.app.data.preferences.UserPreferences
import com.dealshot.app.data.remote.ApiClient
import com.dealshot.app.data.remote.dto.VideoDto
import com.dealshot.app.data.remote.repository.SavedVideosRepository
import com.dealshot.app.data.remote.repository.VideoRepository
import com.dealshot.app.data.remote.repository.WatchNextRepository
import com.dealshot.app.data.remote.repository.WatchNextRepositoryImpl
import com.dealshot.app.data.subscription.SubscriptionLoadState
import com.dealshot.app.data.subscription.hasPremiumAccess
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
    private val savedVideosRepository: SavedVideosRepository,
    watchNextRepository: WatchNextRepository? = null
) : ViewModel() {

    private val watchNextRepo: WatchNextRepository by lazy {
        watchNextRepository ?: WatchNextRepositoryImpl(ApiClient.watchNextApiService, userPreferences)
    }

    private val _uiState = MutableStateFlow(HomeState())
    val uiState = _uiState.asStateFlow()

    private val _effect = Channel<HomeEffect>(
        capacity = Channel.BUFFERED
    )

    val effect = _effect.receiveAsFlow()

    init {
        viewModelScope.launch {
            userPreferences.subscriptionState.collect { subState ->
                _uiState.update { current ->
                    current.copy(
                        subscriptionState = subState,
                        subscriptionLoadState = SubscriptionLoadState.LOADED,
                        continueWatching = if (hasPremiumAccess(subState)) emptyList() else current.continueWatching
                    )
                }
            }
        }
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
        _uiState.update {
            it.copy(
                selectedSection = section,
                isLoading = false,
                error = null
            )
        }

        if (!section.equals("Home", ignoreCase = true) && !section.equals("Premium", ignoreCase = true)) {
            viewModelScope.launch {
                try {
                    val catResult = videoRepository.getVideosByCategory(section)
                    val catVideos = catResult.getOrNull()
                    if (!catVideos.isNullOrEmpty()) {
                        _uiState.update { current ->
                            val combined = (current.allVideos + catVideos)
                                .distinctBy { it.id.ifBlank { it.altId.orEmpty() } }
                            current.copy(allVideos = combined)
                        }
                    }
                } catch (_: Exception) {
                }
            }
        }
    }

    private fun buildSections(
        categories: List<VideoCategory>,
        allVideos: List<VideoDto>
    ): List<String> {
        return buildList {
            add("Home")
            add("National")
            add("International")
            add("Motivational")
            add("Inspiration")
            add("Academic")
        }.distinct()
    }

    private suspend fun loadMyWatchLaterIds() {
        try {
            val ids = savedVideosRepository
                .getVideos()
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

            try {
                val videos = videoRepository.getVideos()

                // Fetch featured video dynamically from GET /videos/featured
                val featuredResult = videoRepository.getFeaturedVideo()
                val featuredVideo = featuredResult.getOrNull()

                // Fetch category videos for Banner from GET /videos/category/Banner
                val bannerResult = videoRepository.getVideosByCategory("Banner")
                val bannerCategoryVideos = bannerResult.getOrDefault(emptyList())

                val bannerVideos = if (bannerCategoryVideos.isNotEmpty()) {
                    if (featuredVideo != null) {
                        (listOf(featuredVideo) + bannerCategoryVideos).distinctBy { it.id.ifBlank { it.altId.orEmpty() } }
                    } else {
                        bannerCategoryVideos
                    }
                } else if (featuredVideo != null) {
                    listOf(featuredVideo)
                } else {
                    videos.filter { it.category.equals("Banner", ignoreCase = true) }
                }

                val latestReleases = videos.drop(1).take(6)
                val popularVideos = videos.takeLast(6)

                val savedVideos = try {
                    savedVideosRepository.getVideos()
                } catch (e: Exception) {
                    emptyList()
                }
                val savedIds = savedVideos.map { it.id }.toSet()

                val currentSubState = _uiState.value.subscriptionState
                val continueWatchingVideos = if (!hasPremiumAccess(currentSubState)) {
                    try {
                        watchNextRepo.getWatchNext().getOrDefault(emptyList())
                    } catch (_: Exception) {
                        emptyList()
                    }
                } else {
                    emptyList()
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        allVideos = videos,
                        bannerVideos = bannerVideos,
                        featuredVideo = featuredVideo,
                        continueWatching = continueWatchingVideos,
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
                Log.e("HomeViewModel", "Error loading home videos", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = AppErrorHandler.getUserMessage(e)
                    )
                }
            }
        }
    }

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
                if (isSaved) {
                    savedVideosRepository.removeVideo(videoId)
                } else {
                    savedVideosRepository.addVideo(videoId)
                }

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
                _uiState.update { it.copy(error = AppErrorHandler.getUserMessage(e)) }
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
