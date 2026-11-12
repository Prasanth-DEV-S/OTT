package com.dealshot.app.data.remote.api

import com.dealshot.app.data.remote.dto.WatchNextAddResponseDto
import com.dealshot.app.data.remote.dto.WatchNextDeleteResponseDto
import com.dealshot.app.data.remote.dto.WatchNextListResponseDto
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Path

interface WatchNextApiService {

    @GET("watch-next")
    suspend fun getWatchNext(
        @Header("X-Profile-Id") profileId: String? = null
    ): Response<WatchNextListResponseDto>

    @PUT("watch-next/{videoId}")
    suspend fun addToWatchNext(
        @Path("videoId") videoId: String,
        @Header("X-Profile-Id") profileId: String? = null
    ): Response<WatchNextAddResponseDto>

    @DELETE("watch-next/{videoId}")
    suspend fun removeFromWatchNext(
        @Path("videoId") videoId: String,
        @Header("X-Profile-Id") profileId: String? = null
    ): Response<WatchNextDeleteResponseDto>
}
