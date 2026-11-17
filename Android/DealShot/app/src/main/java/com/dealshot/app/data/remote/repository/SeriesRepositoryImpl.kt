package com.dealshot.app.data.remote.repository

import com.dealshot.app.data.remote.api.SeriesApiService
import com.dealshot.app.data.remote.dto.EpisodeAutoplayNextResponseDto
import com.dealshot.app.data.remote.dto.EpisodeDetailsResponseDto
import com.dealshot.app.data.remote.dto.EpisodeDto
import com.dealshot.app.data.remote.dto.EpisodeNavigationResponseDto
import com.dealshot.app.data.remote.dto.EpisodePlaybackQualityResponseDto
import com.dealshot.app.data.remote.dto.EpisodeProgressResponseDto
import com.dealshot.app.data.remote.dto.NextEpisodeResponseDto
import com.dealshot.app.data.remote.dto.SaveEpisodeProgressRequestDto
import com.dealshot.app.data.remote.dto.SeasonDto
import com.dealshot.app.data.remote.dto.SelectEpisodeQualityRequestDto
import com.dealshot.app.data.remote.dto.SeriesProgressResponseDto

class SeriesRepositoryImpl(
    private val api: SeriesApiService
) : SeriesRepository {

    override suspend fun getSeriesSeasons(seriesId: String): Result<List<SeasonDto>> {
        return runCatching {
            val response = api.getSeriesSeasons(seriesId)
            if (response.isSuccessful) {
                response.body()?.seasons ?: emptyList()
            } else {
                throw Exception("Failed to fetch seasons: ${response.code()} ${response.message()}")
            }
        }
    }

    override suspend fun getSeasonEpisodes(seriesId: String, seasonNumber: Int): Result<List<EpisodeDto>> {
        return runCatching {
            val response = api.getSeasonEpisodes(seriesId, seasonNumber)
            if (response.isSuccessful) {
                response.body()?.episodes ?: emptyList()
            } else {
                throw Exception("Failed to fetch episodes: ${response.code()} ${response.message()}")
            }
        }
    }

    override suspend fun getSeriesProgress(seriesId: String): Result<SeriesProgressResponseDto> {
        return runCatching {
            val response = api.getSeriesProgress(seriesId)
            if (response.isSuccessful) {
                response.body() ?: SeriesProgressResponseDto()
            } else {
                throw Exception("Failed to fetch series progress: ${response.code()} ${response.message()}")
            }
        }
    }

    override suspend fun getEpisodeDetails(episodeId: String): Result<EpisodeDetailsResponseDto> {
        return runCatching {
            val response = api.getEpisodeDetails(episodeId)
            if (response.isSuccessful) {
                response.body() ?: throw Exception("Episode details empty body")
            } else {
                throw Exception("Failed to fetch episode details: ${response.code()} ${response.message()}")
            }
        }
    }

    override suspend fun getEpisodeProgress(episodeId: String): Result<EpisodeProgressResponseDto?> {
        return runCatching {
            val response = api.getEpisodeProgress(episodeId)
            if (response.isSuccessful) {
                response.body()
            } else if (response.code() == 404) {
                null
            } else {
                throw Exception("Failed to fetch episode progress: ${response.code()} ${response.message()}")
            }
        }
    }

    override suspend fun saveEpisodeProgress(
        episodeId: String,
        positionSeconds: Int,
        durationSeconds: Int
    ): Result<EpisodeProgressResponseDto> {
        return runCatching {
            val request = SaveEpisodeProgressRequestDto(
                positionSeconds = positionSeconds,
                durationSeconds = durationSeconds
            )
            val response = api.saveEpisodeProgress(episodeId, request)
            if (response.isSuccessful) {
                response.body() ?: throw Exception("Save episode progress empty body")
            } else {
                throw Exception("Failed to save episode progress: ${response.code()} ${response.message()}")
            }
        }
    }

    override suspend fun getNextEpisode(episodeId: String): Result<NextEpisodeResponseDto> {
        return runCatching {
            val response = api.getNextEpisode(episodeId)
            if (response.isSuccessful) {
                response.body() ?: NextEpisodeResponseDto()
            } else {
                throw Exception("Failed to fetch next episode: ${response.code()} ${response.message()}")
            }
        }
    }

    override suspend fun getEpisodeNavigation(episodeId: String): Result<EpisodeNavigationResponseDto> {
        return runCatching {
            val response = api.getEpisodeNavigation(episodeId)
            if (response.isSuccessful) {
                response.body() ?: EpisodeNavigationResponseDto()
            } else {
                throw Exception("Failed to fetch episode navigation: ${response.code()} ${response.message()}")
            }
        }
    }

    override suspend fun getAutoplayNext(episodeId: String): Result<EpisodeAutoplayNextResponseDto> {
        return runCatching {
            val response = api.getAutoplayNext(episodeId)
            if (response.isSuccessful) {
                response.body() ?: EpisodeAutoplayNextResponseDto()
            } else {
                throw Exception("Failed to fetch autoplay next: ${response.code()} ${response.message()}")
            }
        }
    }

    override suspend fun getEpisodePlaybackQuality(episodeId: String): Result<EpisodePlaybackQualityResponseDto> {
        return runCatching {
            val response = api.getEpisodePlaybackQuality(episodeId)
            if (response.isSuccessful) {
                response.body() ?: EpisodePlaybackQualityResponseDto()
            } else {
                throw Exception("Failed to fetch episode quality: ${response.code()} ${response.message()}")
            }
        }
    }

    override suspend fun selectEpisodePlaybackQuality(
        episodeId: String,
        quality: String
    ): Result<EpisodePlaybackQualityResponseDto> {
        return runCatching {
            val request = SelectEpisodeQualityRequestDto(quality = quality)
            val response = api.selectEpisodePlaybackQuality(episodeId, request)
            if (response.isSuccessful) {
                response.body() ?: EpisodePlaybackQualityResponseDto()
            } else {
                throw Exception("Failed to select episode quality: ${response.code()} ${response.message()}")
            }
        }
    }
}
