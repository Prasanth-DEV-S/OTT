package com.dealshot.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AvatarsResponseDto(
    val success: Boolean = false,
    val data: List<AvatarDto> = emptyList(),
    val message: String? = null
)

@Serializable
data class AvatarDto(
    val id: String = "",
    val name: String = "",
    val imageUrl: String = "",
    val sortOrder: Int = 0
)
