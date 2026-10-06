package com.dealshot.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SendOtpRequestDto(
    val mobile: String
)