
package com.dealshot.app.data.remote.repository

import com.dealshot.app.data.remote.api.AuthApiService
import com.dealshot.app.data.remote.dto.CurrentUserDto
import com.dealshot.app.data.remote.dto.SendOtpRequestDto
import com.dealshot.app.data.remote.dto.SendOtpResponseDto
import com.dealshot.app.data.remote.dto.VerifyOtpRequestDto
import com.dealshot.app.data.remote.dto.VerifyOtpResponseDto
import retrofit2.HttpException

class AuthRepositoryImpl(
    private val authApiService: AuthApiService
) : AuthRepository {

    override suspend fun sendOtp(
        request: SendOtpRequestDto
    ): SendOtpResponseDto {
        return authApiService.sendOtp(request)
    }

    override suspend fun verifyOtp(
        request: VerifyOtpRequestDto
    ): VerifyOtpResponseDto {
        return authApiService.verifyOtp(request)
    }

    override suspend fun getCurrentUser(): CurrentUserDto {
        val response = authApiService.getCurrentUser()

        if (!response.isSuccessful) {
            throw HttpException(response)
        }

        val body = response.body()
            ?: throw IllegalStateException(
                "Unable to load current user."
            )

        if (!body.success) {
            throw IllegalStateException(
                "Unable to load current user."
            )
        }

        if (body.user.isBlocked || body.user.isDeleted ||
            !body.user.isActive
        ) {
            throw IllegalStateException(
                "This account is not available."
            )
        }

        return body.user
    }
}
