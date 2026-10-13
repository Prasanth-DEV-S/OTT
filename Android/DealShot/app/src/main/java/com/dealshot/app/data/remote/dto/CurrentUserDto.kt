
package com.dealshot.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CurrentUserResponseDto(
    val success: Boolean,
    val user: CurrentUserDto
)

@Serializable
data class CurrentUserDto(
    @SerialName("_id")
    val id: String,

    val name: String,
    val mobile: String,
    val email: String,
    val profileImage: String,
    val role: String,
    val isActive: Boolean,
    val isBlocked: Boolean,
    val blockedReason: String,
    val isMobileVerified: Boolean,
    val isEmailVerified: Boolean,
    val maxProfiles: Int,
    val maxDevices: Int,
    val termsAccepted: Boolean,
    val termsAcceptedAt: String?,
    val privacyPolicyAccepted: Boolean,
    val isDeleted: Boolean,
    val deletedAt: String?,
    val lastLoginAt: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val subscription: CurrentSubscriptionDto
)

@Serializable
data class CurrentSubscriptionDto(
    val isActive: Boolean,
    val planId: String?,
    val startDate: String?,
    val endDate: String?,
    val status: String
)
