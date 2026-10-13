package com.dealshot.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

object MaturityLevel {
    const val KIDS = "kids"
    const val ADULT = "adult"
}

@Serializable
data class ProfileDto(
    @SerialName("_id") val _id: String? = null,
    val id: String? = null,
    val userId: String? = null,
    val name: String = "",
    val email: String? = null,
    val avatar: String? = null,
    val isKidsProfile: Boolean = false,
    val isDefault: Boolean = false,
    val language: String? = null,
    val maturityLevel: String = MaturityLevel.ADULT,
    val autoplayNext: Boolean = true,
    val autoplayPreviews: Boolean = true,
    val isPinEnabled: Boolean = false,
    val isActive: Boolean = true,
    val isDeleted: Boolean = false,
    val deletedAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    val profileId: String
        get() = _id.takeIf { !it.isNullOrBlank() }
            ?: id.takeIf { !it.isNullOrBlank() }
            ?: ""
}

@Serializable
data class CreateProfileRequestDto(
    val name: String,
    val avatar: String = "",
    val isKidsProfile: Boolean = false,
    val language: String = "en",
    val maturityLevel: String = MaturityLevel.ADULT,
    val autoplayNext: Boolean = true,
    val autoplayPreviews: Boolean = true
)

@Serializable
data class UpdateProfileRequestDto(
    val name: String? = null,
    val email: String? = null,
    val avatar: String? = null,
    val isKidsProfile: Boolean? = null,
    val language: String? = null,
    val maturityLevel: String? = null,
    val autoplayNext: Boolean? = null,
    val autoplayPreviews: Boolean? = null
)

@Serializable
data class ProfileResponseDto(
    val success: Boolean = false,
    val message: String? = null,
    val data: ProfileDto? = null
)

@Serializable
data class ProfilesListResponseDto(
    val success: Boolean = false,
    val count: Int? = null,
    val data: List<ProfileDto> = emptyList()
)
