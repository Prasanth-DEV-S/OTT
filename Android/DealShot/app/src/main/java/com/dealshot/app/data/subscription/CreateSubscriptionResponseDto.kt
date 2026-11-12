package com.dealshot.app.data.subscription

import kotlinx.serialization.Serializable

@Serializable
data class CreateSubscriptionResponseDto(
    val success: Boolean = false,
    val message: String = "",
    val data: CreateSubscriptionDataDto? = null
)

@Serializable
data class CreateSubscriptionDataDto(
    val subscriptionId: String = "",
    val plan: CreatedSubscriptionPlanDto? = null,
    val amount: Int = 0,
    val currency: String = "INR",
    val subscriptionStatus: String = "pending",
    val paymentStatus: String = "pending",
    val subscription: CreatedSubscriptionInfoDto? = null
)

@Serializable
data class CreatedSubscriptionPlanDto(
    val id: String = "",
    val name: String = "",
    val code: String = "",
    val durationDays: Int = 30
)

@Serializable
data class CreatedSubscriptionInfoDto(
    val isActive: Boolean = false,
    val planId: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val status: String = "inactive"
)
