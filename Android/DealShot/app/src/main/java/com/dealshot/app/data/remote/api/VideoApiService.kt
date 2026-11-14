package com.dealshot.app.data.remote.api

import com.dealshot.app.data.remote.dto.CategoryVideosResponseDto
import com.dealshot.app.data.remote.dto.FeaturedVideoResponseDto
import com.dealshot.app.data.remote.dto.HlsPlaybackRequestDto
import com.dealshot.app.data.remote.dto.HlsPlaybackResponseDto
import com.dealshot.app.data.remote.dto.HlsQualitiesResponseDto
import com.dealshot.app.data.remote.dto.SendOtpRequestDto
import com.dealshot.app.data.remote.dto.SendOtpResponseDto
import com.dealshot.app.data.remote.dto.SharedVideoResolveResponseDto
import com.dealshot.app.data.remote.dto.VideoCategoriesResponseDto
import com.dealshot.app.data.remote.dto.VideoSearchResponseDto
import com.dealshot.app.data.remote.dto.VideoShareResponseDto
import com.dealshot.app.data.video.VideoResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface VideoApiService {

    @GET("videos")
    suspend fun getVideos(): VideoResponseDto

    @POST("auth/login/send-otp")
    suspend fun sendOtp(
        @Body request: SendOtpRequestDto
    ): SendOtpResponseDto

    @GET("videos/categories")
    suspend fun getVideoCategories(): VideoCategoriesResponseDto

    @GET("search")
    suspend fun searchVideos(
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<VideoSearchResponseDto>

    @GET("videos/{videoId}/share")
    suspend fun generateShareLink(
        @Path("videoId") videoId: String
    ): Response<VideoShareResponseDto>

    @GET("videos/share/resolve/{videoId}")
    suspend fun resolveSharedVideo(
        @Path("videoId") videoId: String
    ): Response<SharedVideoResolveResponseDto>

    @GET("videos/featured")
    suspend fun getFeaturedVideo(): Response<FeaturedVideoResponseDto>

    @GET("videos/category/{category}")
    suspend fun getVideosByCategory(
        @Path("category") category: String
    ): Response<CategoryVideosResponseDto>

    @GET("videos/{videoId}/playback/hls/qualities")
    suspend fun getHlsQualities(
        @Path("videoId") videoId: String,
        @Header("X-Profile-Id") profileId: String? = null
    ): Response<HlsQualitiesResponseDto>

    @POST("videos/{videoId}/playback/hls")
    suspend fun requestHlsPlayback(
        @Path("videoId") videoId: String,
        @Body request: HlsPlaybackRequestDto,
        @Header("X-Profile-Id") profileId: String? = null
    ): Response<HlsPlaybackResponseDto>
}
