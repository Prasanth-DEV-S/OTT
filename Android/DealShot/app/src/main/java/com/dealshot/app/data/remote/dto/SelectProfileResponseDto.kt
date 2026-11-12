package com.dealshot.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SelectProfileResponseDto(
    val success: Boolean = false,
    val message: String? = null,
    val data: ProfileDto? = null
)
