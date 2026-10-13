package com.dealshot.app.data.remote.api

import com.dealshot.app.data.remote.dto.SendOtpRequestDto
import com.dealshot.app.data.remote.dto.SendOtpResponseDto
import com.dealshot.app.data.remote.dto.VideoCategoriesResponseDto
import com.dealshot.app.data.video.VideoResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface VideoApiService {

    @GET("videos")
    suspend fun getVideos(): VideoResponseDto

    @POST("auth/login/send-otp")
    suspend fun sendOtp(
        @Body request: SendOtpRequestDto
    ): SendOtpResponseDto

    @GET("videos/categories")
    suspend fun getVideoCategories(): VideoCategoriesResponseDto
}