package com.dealshot.app.data.subscription

import kotlinx.serialization.Serializable

@Serializable
enum class SubscriptionLoadState {
    LOADING,
    LOADED,
    ERROR
}

@Serializable
data class SubscriptionState(
    val isActive: Boolean = false,
    val planId: String? = null,
    val planName: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val status: String = "inactive"
)

@Serializable
data class AuthUser(
    val id: String,
    val name: String,
    val email: String? = null,
    val mobile: String? = null,
    val gender: String? = null,
    val profileImage: String? = null,
    val activeProfileId: String? = null,
    val avatarId: String? = null,
    val subscription: SubscriptionState = SubscriptionState()
)

fun hasPremiumAccess(subscription: SubscriptionState): Boolean {
    return subscription.isActive &&
        subscription.status.equals("active", ignoreCase = true)
}
