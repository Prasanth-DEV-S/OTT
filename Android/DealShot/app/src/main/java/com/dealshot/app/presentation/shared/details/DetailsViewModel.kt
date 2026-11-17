package com.dealshot.app.presentation.shared.details

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dealshot.app.core.error.AppErrorHandler
import com.dealshot.app.data.remote.ApiClient
import com.dealshot.app.data.remote.dto.VideoDto
import com.dealshot.app.data.remote.mapper.toDomain
import com.dealshot.app.data.remote.repository.SavedVideosRepository
import com.dealshot.app.data.remote.repository.SeriesRepository
import com.dealshot.app.data.remote.repository.SeriesRepositoryImpl
import com.dealshot.app.data.remote.repository.VideoDetailsRepository
import com.dealshot.app.data.remote.repository.VideoRepository
import com.dealshot.app.data.remote.repository.VideoRepositoryImpl
import com.dealshot.app.data.remote.repository.like.LikedVideosRepository
import com.dealshot.app.data.remote.repository.report.ReportRepository
import com.dealshot.app.data.remote.repository.report.ReportRepositoryImpl
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DetailsViewModel(
    private val videoDetailsRepository: VideoDetailsRepository,
    private val savedVideosRepository: SavedVideosRepository,
    private val likedVideosRepository: LikedVideosRepository,
    seriesRepository: SeriesRepository? = null,
    reportRepository: ReportRepository? = null,
    videoRepository: VideoRepository? = null
) : ViewModel() {

    private val videoRepo: VideoRepository by lazy {
        videoRepository ?: VideoRepositoryImpl(ApiClient.videoApi)
    }

    private val seriesRepo: SeriesRepository by lazy {
        seriesRepository ?: SeriesRepositoryImpl(ApiClient.seriesApiService)
    }

    private val reportRepo: ReportRepository by lazy {
        reportRepository ?: ReportRepositoryImpl(ApiClient.reportApiService)
    }

    private val _uiState = MutableStateFlow(DetailsState())
    val uiState = _uiState.asStateFlow()
    private val _effect = Channel<DetailsEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: DetailsIntent) {
        when (intent) {
            is DetailsIntent.Initialize -> {
                val initialId = intent.video.apiId.ifBlank { intent.video.id }
                val isSeriesCategory = intent.video.category.trim().equals("Web Series", ignoreCase = true)

                _uiState.update {
                    it.copy(
                        video = intent.video,
                        videoDetails = null,
                        isLoading = true,
                        error = null,
                        isInMyWatchLater = false,
                        isLiked = false,
                        isReported = false,
                        isSeries = isSeriesCategory,
                        seasons = emptyList(),
                        episodes = emptyList(),
                        continueWatching = null,
                        activeEpisodeDetails = null,
                        episodeNavigation = null,
                        autoplayInfo = null
                    )
                }

                loadVideoDetails(initialId)
                loadVideoLikeStatus(initialId)
                loadReportStatus(initialId)
                loadMyWatchListStatus(initialId)
                loadNextVideos(category = intent.video.category, currentVideoId = initialId)

                if (isSeriesCategory) {
                    loadSeriesData(initialId)
                }
            }

            is DetailsIntent.SelectNextVideo -> {
                val domainVideo = intent.videoDto.toDomain(intent.videoDto.id)
                val targetId = domainVideo.apiId.ifBlank { domainVideo.id }
                val isSeriesCategory = domainVideo.category.trim().equals("Web Series", ignoreCase = true)

                _uiState.update {
                    it.copy(
                        video = domainVideo,
                        videoDetails = null,
                        isLoading = true,
                        error = null,
                        isInMyWatchLater = false,
                        isLiked = false,
                        isReported = false,
                        isSeries = isSeriesCategory,
                        seasons = emptyList(),
                        episodes = emptyList(),
                        continueWatching = null,
                        activeEpisodeDetails = null,
                        episodeNavigation = null,
                        autoplayInfo = null
                    )
                }

                loadVideoDetails(targetId)
                loadVideoLikeStatus(targetId)
                loadReportStatus(targetId)
                loadMyWatchListStatus(targetId)
                loadNextVideos(category = domainVideo.category, currentVideoId = targetId)

                if (isSeriesCategory) {
                    loadSeriesData(targetId)
                }
            }

            is DetailsIntent.SelectSeason -> {
                val seriesId = getVideoId() ?: return
                loadSeasonEpisodes(seriesId, intent.seasonNumber)
            }

            is DetailsIntent.SelectEpisode -> {
                loadEpisodeAndPlay(intent.episode.id)
            }

            is DetailsIntent.SelectEpisodeById -> {
                loadEpisodeAndPlay(intent.episodeId)
            }

            is DetailsIntent.SelectEpisodeQuality -> {
                selectEpisodeQuality(intent.episodeId, intent.quality)
            }

            is DetailsIntent.CheckAutoplayNext -> {
                checkAutoplayNext(intent.episodeId)
            }

            DetailsIntent.PreviousEpisodeClicked -> {
                val prevId = _uiState.value.episodeNavigation?.previous?.episodeId
                if (!prevId.isNullOrBlank()) {
                    loadEpisodeAndPlay(prevId)
                }
            }

            DetailsIntent.NextEpisodeClicked -> {
                val nextId = _uiState.value.episodeNavigation?.next?.episodeId
                if (!nextId.isNullOrBlank()) {
                    loadEpisodeAndPlay(nextId, startFromBeginning = true)
                }
            }

            DetailsIntent.WatchFirstEpisodeClicked -> {
                val continueWatching = _uiState.value.continueWatching
                val targetEpisodeId = if (continueWatching != null && !continueWatching.completed && continueWatching.episodeId.isNotBlank()) {
                    continueWatching.episodeId
                } else {
                    _uiState.value.episodes.firstOrNull()?.id
                }

                if (!targetEpisodeId.isNullOrBlank()) {
                    loadEpisodeAndPlay(targetEpisodeId)
                } else {
                    val seriesId = getVideoId()
                    if (seriesId != null) {
                        viewModelScope.launch {
                            val firstSeasonNum = _uiState.value.seasons.firstOrNull()?.seasonNumber ?: 1
                            val res = seriesRepo.getSeasonEpisodes(seriesId, firstSeasonNum)
                            res.getOrNull()?.firstOrNull()?.id?.let { epId ->
                                loadEpisodeAndPlay(epId)
                            }
                        }
                    }
                }
            }

            DetailsIntent.BackClicked ->
                sendEffect(DetailsEffect.NavigateBack)

            DetailsIntent.PlayVideoClicked ->
                sendEffect(DetailsEffect.OpenPlayer)

            DetailsIntent.MyListClicked ->
                toggleMyList()

            DetailsIntent.LikeClicked ->
                toggleLike()

            DetailsIntent.ShareClicked ->
                sendEffect(DetailsEffect.ShareVideo)

            DetailsIntent.ClearError ->
                _uiState.update { it.copy(error = null) }
        }
    }

    private fun loadSeriesData(seriesId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingSeasons = true) }

            val seasonsResult = seriesRepo.getSeriesSeasons(seriesId)
            val seasons = seasonsResult.getOrDefault(emptyList()).sortedBy { it.seasonNumber }
            val firstSeasonNumber = seasons.firstOrNull()?.seasonNumber ?: 1

            _uiState.update {
                it.copy(
                    seasons = seasons,
                    selectedSeasonNumber = firstSeasonNumber,
                    isLoadingSeasons = false
                )
            }

            if (seasons.isNotEmpty()) {
                loadSeasonEpisodes(seriesId, firstSeasonNumber)
            }

            val progressResult = seriesRepo.getSeriesProgress(seriesId)
            progressResult.getOrNull()?.let { progress ->
                _uiState.update { current ->
                    current.copy(continueWatching = progress.continueWatching)
                }
            }
        }
    }

    private fun loadSeasonEpisodes(seriesId: String, seasonNumber: Int) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingEpisodes = true,
                    selectedSeasonNumber = seasonNumber
                )
            }

            val result = seriesRepo.getSeasonEpisodes(seriesId, seasonNumber)
            val episodes = result.getOrDefault(emptyList()).sortedBy { it.episodeNumber }

            _uiState.update {
                it.copy(
                    episodes = episodes,
                    isLoadingEpisodes = false
                )
            }
        }
    }

    private fun loadEpisodeAndPlay(episodeId: String, startFromBeginning: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val result = seriesRepo.getEpisodeDetails(episodeId)
            result.fold(
                onSuccess = { details ->
                    _uiState.update {
                        it.copy(
                            activeEpisodeDetails = details,
                            isLoading = false
                        )
                    }

                    // Fetch navigation info and report status
                    loadEpisodeNavigation(episodeId)
                    loadReportStatus(episodeId)

                    val playbackUrl = details.playback?.masterUrl.orEmpty()
                    val title = details.episode?.title ?: "Episode ${details.episode?.episodeNumber ?: ""}"
                    val resumePosMs = if (startFromBeginning) 0L else (details.resumePosition * 1000L).coerceAtLeast(0L)

                    sendEffect(
                        DetailsEffect.PlayEpisode(
                            episodeId = episodeId,
                            playbackUrl = playbackUrl,
                            episodeTitle = title,
                            resumePositionMs = resumePosMs
                        )
                    )
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = AppErrorHandler.getUserMessage(error)
                        )
                    }
                }
            )
        }
    }

    private fun loadEpisodeNavigation(episodeId: String) {
        viewModelScope.launch {
            val navResult = seriesRepo.getEpisodeNavigation(episodeId)
            navResult.getOrNull()?.let { nav ->
                _uiState.update { it.copy(episodeNavigation = nav) }
            }
        }
    }

    private fun checkAutoplayNext(episodeId: String) {
        viewModelScope.launch {
            val res = seriesRepo.getAutoplayNext(episodeId)
            res.getOrNull()?.let { autoplay ->
                _uiState.update { it.copy(autoplayInfo = autoplay) }
            }
        }
    }

    private fun selectEpisodeQuality(episodeId: String, quality: String) {
        viewModelScope.launch {
            val res = seriesRepo.selectEpisodePlaybackQuality(episodeId, quality)
            res.fold(
                onSuccess = { qualityResp ->
                    if (qualityResp.playbackUrl.isNotBlank()) {
                        _uiState.update {
                            it.copy(
                                selectedEpisodeQuality = qualityResp.quality,
                                episodePlaybackUrl = qualityResp.playbackUrl
                            )
                        }
                        sendEffect(DetailsEffect.UpdateEpisodeQualityUrl(qualityResp.playbackUrl))
                    }
                },
                onFailure = { error ->
                    Log.w("DetailsVM", "Failed to select episode quality: $quality", error)
                }
            )
        }
    }

    private fun loadNextVideos(category: String, currentVideoId: String) {
        val cleanCategory = category.trim()
        if (cleanCategory.isBlank() || cleanCategory.equals("Home", ignoreCase = true)) return

        viewModelScope.launch {
            try {
                val catResult = videoRepo.getVideosByCategory(cleanCategory)
                var categoryVideos = catResult.getOrDefault(emptyList())

                if (categoryVideos.isEmpty()) {
                    val allVideos = videoRepo.getVideos()
                    categoryVideos = allVideos.filter { video ->
                        video.category?.trim()?.equals(cleanCategory, ignoreCase = true) == true
                    }
                } else {
                    categoryVideos = categoryVideos.filter { video ->
                        video.category.isNullOrBlank() || video.category.trim().equals(cleanCategory, ignoreCase = true)
                    }
                }

                val nextList = categoryVideos.filterNot {
                    it.id.trim().equals(currentVideoId.trim(), ignoreCase = true) ||
                    it.altId?.trim()?.equals(currentVideoId.trim(), ignoreCase = true) == true
                }

                _uiState.update { current ->
                    current.copy(nextVideos = nextList)
                }
            } catch (e: Exception) {
                Log.w("DetailsVM", "Failed to load next videos for category: $cleanCategory", e)
            }
        }
    }

    private fun loadMyWatchListStatus(videoId: String) {
        viewModelScope.launch {
            try {
                val videos = savedVideosRepository.getVideos()
                _uiState.update {
                    it.copy(
                        isInMyWatchLater = videos.any { saved ->
                            saved.id == videoId || saved.effectiveId == videoId
                        }
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _uiState.update { it.copy(isInMyWatchLater = false) }
            }
        }
    }

    private fun loadVideoLikeStatus(videoId: String) {
        viewModelScope.launch {
            try {
                val isLiked = likedVideosRepository.getVideoLikeStatus(videoId)
                _uiState.update {
                    it.copy(isLiked = isLiked)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _uiState.update { it.copy(isLiked = false) }
            }
        }
    }

    private fun loadReportStatus(videoId: String) {
        if (videoId.isBlank()) return
        viewModelScope.launch {
            try {
                val res = reportRepo.getReportStatus(videoId)
                val isReported = res.getOrDefault(false)
                _uiState.update { it.copy(isReported = isReported) }
            } catch (_: Exception) {
                _uiState.update { it.copy(isReported = false) }
            }
        }
    }

    private fun getVideoId(): String? {
        val state = _uiState.value
        return state.videoDetails?.mongoId
            ?: state.videoDetails?.id
            ?: state.video?.apiId?.takeIf { it.isNotBlank() }
            ?: state.video?.id
    }

    private fun toggleLike() {
        val videoId = getVideoId() ?: return
        val currentState = _uiState.value

        if (currentState.isUpdatingLike) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isUpdatingLike = true,
                    error = null
                )
            }

            try {
                val result = likedVideosRepository.toggleVideoLike(videoId)
                result.fold(
                    onSuccess = { response ->
                        _uiState.update {
                            it.copy(
                                isLiked = response.isLiked,
                                isUpdatingLike = false,
                                error = null
                            )
                        }
                    },
                    onFailure = { error ->
                        val msg = if (error.message?.contains("401") == true) "Please log in to like this video ." else AppErrorHandler.getUserMessage(error)
                        _uiState.update {
                            it.copy(
                                isUpdatingLike = false,
                                error = msg
                            )
                        }
                        if (msg.contains("log in", ignoreCase = true)) {
                            sendEffect(DetailsEffect.OpenLogin)
                        }
                    }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val msg = if (e.message?.contains("401") == true) "Please log in to like this video ." else AppErrorHandler.getUserMessage(e)
                _uiState.update {
                    it.copy(
                        isUpdatingLike = false,
                        error = msg
                    )
                }
                if (msg.contains("log in", ignoreCase = true)) {
                    sendEffect(DetailsEffect.OpenLogin)
                }
            }
        }
    }

    private fun toggleMyList() {
        val videoId = getVideoId() ?: return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isUpdatingMyWatchLater = true,
                    error = null
                )
            }

            try {
                val isSaved = _uiState.value.isInMyWatchLater

                if (isSaved) {
                    savedVideosRepository.removeVideo(videoId)
                } else {
                    savedVideosRepository.addVideo(videoId)
                }

                _uiState.update {
                    it.copy(
                        isInMyWatchLater = !isSaved,
                        isUpdatingMyWatchLater = false
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val msg = if (e.message?.contains("401") == true) "Please log in to Add this video to your list." else AppErrorHandler.getUserMessage(e)
                _uiState.update {
                    it.copy(
                        isUpdatingMyWatchLater = false,
                        error = msg
                    )
                }
                if (msg.contains("log in", ignoreCase = true)) {
                    sendEffect(DetailsEffect.OpenLogin)
                }
            }
        }
    }

    private fun loadVideoDetails(videoId: String) {
        viewModelScope.launch {
            try {
                val details = videoDetailsRepository.getVideoDetails(videoId)

                val isSeriesCat = details.category?.trim()?.equals("Web Series", ignoreCase = true) == true

                _uiState.update { current ->
                    current.copy(
                        videoDetails = details,
                        isSeries = current.isSeries || isSeriesCat,
                        isLoading = false,
                        error = null
                    )
                }

                if (isSeriesCat) {
                    loadSeriesData(videoId)
                }

                // If details category is available, refresh next videos list with exact category
                details.category?.takeIf { it.isNotBlank() }?.let { cat ->
                    loadNextVideos(category = cat, currentVideoId = videoId)
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

    private fun sendEffect(effect: DetailsEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}
