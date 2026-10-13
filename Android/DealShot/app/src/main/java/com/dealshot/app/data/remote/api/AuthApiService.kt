package com.dealshot.app.data.remote.api

import com.dealshot.app.data.remote.dto.CurrentUserDto
import com.dealshot.app.data.remote.dto.CurrentUserResponseDto
import com.dealshot.app.data.remote.dto.SendOtpRequestDto
import com.dealshot.app.data.remote.dto.SendOtpResponseDto
import com.dealshot.app.data.remote.dto.VerifyOtpRequestDto
import com.dealshot.app.data.remote.dto.VerifyOtpResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApiService {

    @POST("auth/login/send-otp")
    suspend fun sendOtp(
        @Body request: SendOtpRequestDto
    ): SendOtpResponseDto

    @POST("auth/verify-otp")
    suspend fun verifyOtp(
        @Body request: VerifyOtpRequestDto
    ): VerifyOtpResponseDto

    @GET("auth/me")
    suspend fun getCurrentUser(): Response<CurrentUserResponseDto>

}