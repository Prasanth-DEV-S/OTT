package com.dealshot.app.data.remote.dto

import com.dealshot.app.data.subscription.AuthUser
import com.dealshot.app.data.subscription.SubscriptionState
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
    val email: String? = null,
    val profileImage: String? = null,
    val role: String? = null,
    val isActive: Boolean = true,
    val isBlocked: Boolean = false,
    val blockedReason: String = "",
    val isMobileVerified: Boolean = false,
    val isEmailVerified: Boolean = false,
    val maxProfiles: Int = 1,
    val maxDevices: Int = 1,
    val termsAccepted: Boolean = false,
    val termsAcceptedAt: String? = null,
    val privacyPolicyAccepted: Boolean = false,
    val isDeleted: Boolean = false,
    val deletedAt: String? = null,
    val lastLoginAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val gender: String? = null,
    val activeProfileId: String? = null,
    val avatarId: String? = null,
    val subscription: CurrentSubscriptionDto
)

@Serializable
data class CurrentSubscriptionDto(
    val isActive: Boolean = false,
    val planId: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val status: String = "inactive"
)

fun CurrentUserDto.toAuthUser(planName: String? = null): AuthUser {
    return AuthUser(
        id = id,
        name = name,
        email = email,
        mobile = mobile,
        gender = gender,
        profileImage = profileImage,
        activeProfileId = activeProfileId,
        avatarId = avatarId,
        subscription = SubscriptionState(
            isActive = subscription.isActive,
            planId = subscription.planId,
            planName = planName,
            startDate = subscription.startDate,
            endDate = subscription.endDate,
            status = subscription.status
        )
    )
}
