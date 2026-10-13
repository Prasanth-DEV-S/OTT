package com.dealshot.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class VerifyOtpRequestDto(
    val mobile: String,
    val otp: String,
    val name: String
)