package com.dealshot.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionPlansResponseDto(
    val success: Boolean = false,
    val message: String = "",
    val count: Int = 0,
    val data: List<SubscriptionPlanDto> = emptyList()
)

@Serializable
data class SingleSubscriptionPlanResponseDto(
    val success: Boolean = false,
    val message: String = "",
    val data: SubscriptionPlanDto? = null
)

@Serializable
data class SubscriptionPlanDto(
    @SerialName("_id")
    val id: String = "",
    val name: String = "",
    val code: String = "",
    val description: String = "",
    val price: Int = 0,
    val currency: String = "INR",
    @SerialName("durationInDays")
    val durationInDays: Int = 0,
    val hdAvailable: Boolean = false,
    val hdr4kAvailable: Boolean = false,
    val watchOnLaptopAndTV: Boolean = false,
    val watchOnMobileAndTablet: Boolean = false,
    val simultaneousStreams: Int = 1,
    val isActive: Boolean = true,
    val sortOrder: Int = 0
)
