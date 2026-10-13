
package com.dealshot.app.data.remote.api

import com.dealshot.app.data.remote.dto.SaveWatchProgressRequestDto
import com.dealshot.app.data.remote.dto.SaveWatchProgressResponseDto
import com.dealshot.app.data.remote.dto.WatchProgressResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface WatchProgressApiService {

    @GET("videos/{id}/progress")
    suspend fun getWatchProgress(
        @Path("id") videoId: String
    ): Response<WatchProgressResponseDto>

    @PUT("videos/{id}/progress")
    suspend fun saveWatchProgress(
        @Path("id") videoId: String,
        @Body request: SaveWatchProgressRequestDto
    ): Response<SaveWatchProgressResponseDto>
}