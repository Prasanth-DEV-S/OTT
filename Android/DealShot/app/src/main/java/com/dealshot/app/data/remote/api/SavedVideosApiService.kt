package com.dealshot.app.data.remote.api

import com.dealshot.app.data.remote.dto.LikedVideosListResponseDto
import com.dealshot.app.data.remote.dto.SavedVideoMutationResponseDto
import com.dealshot.app.data.remote.dto.SavedVideoRemoveResponseDto
import com.dealshot.app.data.remote.dto.SavedVideosListResponseDto
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface SavedVideosApiService {

    @GET("{collection}")
    suspend fun getSavedVideos(
        @Path("collection") collection: String
    ): Response<SavedVideosListResponseDto>

    @POST("{collection}/{videoId}")
    suspend fun addVideo(
        @Path("collection") collection: String,
        @Path("videoId") videoId: String
    ): Response<SavedVideoMutationResponseDto>

    @DELETE("{collection}/{videoId}")
    suspend fun removeVideo(
        @Path("collection") collection: String,
        @Path("videoId") videoId: String
    ): Response<SavedVideoRemoveResponseDto>

    // Dedicated Liked Videos endpoints
    @GET("videos/my-liked-videos")
    suspend fun getLikedVideos(): Response<LikedVideosListResponseDto>

    @POST("videos/{videoId}/like")
    suspend fun likeVideo(
        @Path("videoId") videoId: String
    ): Response<SavedVideoMutationResponseDto>

    @DELETE("videos/{videoId}/unlike")
    suspend fun unlikeVideo(
        @Path("videoId") videoId: String
    ): Response<SavedVideoRemoveResponseDto>
}
