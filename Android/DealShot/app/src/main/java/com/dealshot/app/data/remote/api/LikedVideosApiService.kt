package com.dealshot.app.data.remote.api

import com.dealshot.app.data.remote.dto.like.LikeMutationResponseDto
import com.dealshot.app.data.remote.dto.like.LikeVideoRequestDto
import com.dealshot.app.data.remote.dto.like.LikedVideosResponseDto
import com.dealshot.app.data.remote.dto.like.VideoLikeStatusResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface LikedVideosApiService {

    @GET("videos/my-liked-videos")
    suspend fun getLikedVideos(): Response<LikedVideosResponseDto>

    @POST("videos/my-liked-videos")
    suspend fun likeVideo(
        @Body request: LikeVideoRequestDto
    ): Response<LikeMutationResponseDto>

    @POST("videos/{videoId}/like")
    suspend fun likeVideoPath(
        @Path("videoId") videoId: String
    ): Response<LikeMutationResponseDto>

    @GET("videos/status/{videoId}")
    suspend fun getVideoLikeStatus(
        @Path("videoId") videoId: String
    ): Response<VideoLikeStatusResponseDto>
}
