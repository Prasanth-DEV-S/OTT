package com.dealshot.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserAccountResponseDto(
    val success: Boolean = false,
    val data: UserAccountDataDto? = null,
    val message: String? = null
)

@Serializable
data class UserAccountDataDto(
    val name: String? = null,
    val email: String? = null,
    val gender: String? = null,
    val avatar: String? = null
)

@Serializable
data class UpdateUserAccountRequestDto(
    val name: String? = null,
    val email: String? = null,
    val gender: String? = null
)

@Serializable
data class SelectPresetAvatarRequestDto(
    val avatarId: String
)
