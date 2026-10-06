package com.dealshot.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SendOtpResponseDto(
    val message: String,
    val mobileHint: String
)