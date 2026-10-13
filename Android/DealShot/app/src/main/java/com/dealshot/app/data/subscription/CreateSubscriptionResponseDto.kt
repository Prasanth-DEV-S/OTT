package com.dealshot.app.data.subscription

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateSubscriptionResponseDto(
    val success: Boolean,
    val message: String,
    val data: CreateSubscriptionDataDto
)

@Serializable
data class CreateSubscriptionDataDto(
    val subscriptionId: String,
    val plan: CreatedSubscriptionPlanDto,
    val amount: Int,
    val currency: String,
    val subscriptionStatus: String,
    val paymentStatus: String
)

@Serializable
data class CreatedSubscriptionPlanDto(
    val id: String,
    val name: String,
    val code: String,
    val durationDays: Int
)

