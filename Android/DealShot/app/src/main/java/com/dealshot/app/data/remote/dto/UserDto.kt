package com.dealshot.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    val mobile: String,
    val role: String
)