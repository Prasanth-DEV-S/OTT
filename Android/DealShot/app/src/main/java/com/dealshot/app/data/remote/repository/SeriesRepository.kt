package com.dealshot.app.data.remote.repository

import com.dealshot.app.data.remote.dto.EpisodeAutoplayNextResponseDto
import com.dealshot.app.data.remote.dto.EpisodeDetailsResponseDto
import com.dealshot.app.data.remote.dto.EpisodeDto
import com.dealshot.app.data.remote.dto.EpisodeNavigationResponseDto
import com.dealshot.app.data.remote.dto.EpisodePlaybackQualityResponseDto
import com.dealshot.app.data.remote.dto.EpisodeProgressResponseDto
import com.dealshot.app.data.remote.dto.NextEpisodeResponseDto
import com.dealshot.app.data.remote.dto.SeasonDto
import com.dealshot.app.data.remote.dto.SeriesProgressResponseDto

interface SeriesRepository {
    suspend fun getSeriesSeasons(seriesId: String): Result<List<SeasonDto>>
    suspend fun getSeasonEpisodes(seriesId: String, seasonNumber: Int): Result<List<EpisodeDto>>
    suspend fun getSeriesProgress(seriesId: String): Result<SeriesProgressResponseDto>
    suspend fun getEpisodeDetails(episodeId: String): Result<EpisodeDetailsResponseDto>
    suspend fun getEpisodeProgress(episodeId: String): Result<EpisodeProgressResponseDto?>
    suspend fun saveEpisodeProgress(episodeId: String, positionSeconds: Int, durationSeconds: Int): Result<EpisodeProgressResponseDto>

    suspend fun getNextEpisode(episodeId: String): Result<NextEpisodeResponseDto>
    suspend fun getEpisodeNavigation(episodeId: String): Result<EpisodeNavigationResponseDto>
    suspend fun getAutoplayNext(episodeId: String): Result<EpisodeAutoplayNextResponseDto>
    suspend fun getEpisodePlaybackQuality(episodeId: String): Result<EpisodePlaybackQualityResponseDto>
    suspend fun selectEpisodePlaybackQuality(episodeId: String, quality: String): Result<EpisodePlaybackQualityResponseDto>
}
