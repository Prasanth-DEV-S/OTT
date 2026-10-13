
package com.dealshot.app.data.remote.api

import com.dealshot.app.data.remote.dto.VideoDetailsResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface VideoDetailsApiService {

    @GET("videos/{id}")
    suspend fun getVideoDetails(
        @Path("id") videoId: String
    ): Response<VideoDetailsResponseDto>
}