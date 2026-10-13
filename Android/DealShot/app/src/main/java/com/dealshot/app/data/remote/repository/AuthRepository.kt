
package com.dealshot.app.data.remote.repository

import com.dealshot.app.data.remote.dto.CurrentUserDto
import com.dealshot.app.data.remote.dto.SendOtpRequestDto
import com.dealshot.app.data.remote.dto.SendOtpResponseDto
import com.dealshot.app.data.remote.dto.VerifyOtpRequestDto
import com.dealshot.app.data.remote.dto.VerifyOtpResponseDto

interface AuthRepository {
    suspend fun sendOtp(
        request: SendOtpRequestDto
    ): SendOtpResponseDto

    suspend fun verifyOtp(
        request: VerifyOtpRequestDto
    ): VerifyOtpResponseDto

    suspend fun getCurrentUser(): CurrentUserDto
}
