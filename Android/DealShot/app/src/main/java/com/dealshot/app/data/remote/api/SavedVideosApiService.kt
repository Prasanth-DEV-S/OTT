package com.dealshot.app.data.remote.api

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

}
