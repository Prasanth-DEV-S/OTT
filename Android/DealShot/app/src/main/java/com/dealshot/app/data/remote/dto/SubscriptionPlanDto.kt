package com.dealshot.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionPlansResponseDto(
    val success: Boolean,
    val message: String,
    val count: Int,
    val data: List<SubscriptionPlanDto>
)

@Serializable
data class SubscriptionPlanDto(
    @SerialName("_id")
    val id: String,
    val name: String,
    val code: String,
    val description: String,
    val price: Int,
    val currency: String,
    val durationDays: Int,
    val maxDevices: Int,
    val maxProfiles: Int,
    val simultaneousStreams: Int,
    val videoQuality: String,
    val hasPremiumContent: Boolean,
    val sortOrder: Int
)

