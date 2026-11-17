package com.dealshot.app.data.remote.api

import com.dealshot.app.data.remote.dto.EpisodeAutoplayNextResponseDto
import com.dealshot.app.data.remote.dto.EpisodeDetailsResponseDto
import com.dealshot.app.data.remote.dto.EpisodeNavigationResponseDto
import com.dealshot.app.data.remote.dto.EpisodePlaybackQualityResponseDto
import com.dealshot.app.data.remote.dto.EpisodeProgressResponseDto
import com.dealshot.app.data.remote.dto.NextEpisodeResponseDto
import com.dealshot.app.data.remote.dto.SaveEpisodeProgressRequestDto
import com.dealshot.app.data.remote.dto.SeasonEpisodesResponseDto
import com.dealshot.app.data.remote.dto.SelectEpisodeQualityRequestDto
import com.dealshot.app.data.remote.dto.SeriesProgressResponseDto
import com.dealshot.app.data.remote.dto.SeriesSeasonsResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface SeriesApiService {

    @GET("series/{seriesId}/seasons")
    suspend fun getSeriesSeasons(
        @Path("seriesId") seriesId: String
    ): Response<SeriesSeasonsResponseDto>

    @GET("series/{seriesId}/seasons/{seasonNumber}/episodes")
    suspend fun getSeasonEpisodes(
        @Path("seriesId") seriesId: String,
        @Path("seasonNumber") seasonNumber: Int
    ): Response<SeasonEpisodesResponseDto>

    @GET("series/{seriesId}/progress")
    suspend fun getSeriesProgress(
        @Path("seriesId") seriesId: String
    ): Response<SeriesProgressResponseDto>

    @GET("episodes/{episodeId}")
    suspend fun getEpisodeDetails(
        @Path("episodeId") episodeId: String
    ): Response<EpisodeDetailsResponseDto>

    @GET("episodes/{episodeId}/progress")
    suspend fun getEpisodeProgress(
        @Path("episodeId") episodeId: String
    ): Response<EpisodeProgressResponseDto>

    @POST("episodes/{episodeId}/progress")
    suspend fun saveEpisodeProgress(
        @Path("episodeId") episodeId: String,
        @Body request: SaveEpisodeProgressRequestDto
    ): Response<EpisodeProgressResponseDto>

    @GET("episodes/{episodeId}/next")
    suspend fun getNextEpisode(
        @Path("episodeId") episodeId: String
    ): Response<NextEpisodeResponseDto>

    @GET("episodes/{episodeId}/navigation")
    suspend fun getEpisodeNavigation(
        @Path("episodeId") episodeId: String
    ): Response<EpisodeNavigationResponseDto>

    @GET("episodes/{episodeId}/autoplay-next")
    suspend fun getAutoplayNext(
        @Path("episodeId") episodeId: String
    ): Response<EpisodeAutoplayNextResponseDto>

    @GET("episodes/{episodeId}/playback/quality")
    suspend fun getEpisodePlaybackQuality(
        @Path("episodeId") episodeId: String
    ): Response<EpisodePlaybackQualityResponseDto>

    @POST("episodes/{episodeId}/playback/quality")
    suspend fun selectEpisodePlaybackQuality(
        @Path("episodeId") episodeId: String,
        @Body request: SelectEpisodeQualityRequestDto
    ): Response<EpisodePlaybackQualityResponseDto>
}
