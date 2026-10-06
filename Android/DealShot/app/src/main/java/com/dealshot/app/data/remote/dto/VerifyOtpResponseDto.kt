package com.dealshot.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class VerifyOtpResponseDto(
    val token: String,
    val isNewUser: Boolean,
    val user: UserDto
)