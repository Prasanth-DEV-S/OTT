package com.dealshot.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class DeleteProfileResponseDto(
    val success: Boolean = false,
    val message: String? = null
)
