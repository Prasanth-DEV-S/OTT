package com.dealshot.app.data.subscription

import com.dealshot.app.data.remote.dto.SubscriptionPlanDto

data class SubscriptionPlan(
    val id: String,
    val name: String,
    val code: String,
    val description: String,
    val price: Int,
    val currency: String,
    val durationDays: Int,
    val hdAvailable: Boolean = false,
    val hdr4kAvailable: Boolean = false,
    val watchOnLaptopAndTV: Boolean = false,
    val watchOnMobileAndTablet: Boolean = false,
    val simultaneousStreams: Int = 1,
    val isActive: Boolean = true,
    val sortOrder: Int = 0
)

fun SubscriptionPlanDto.toDomain(): SubscriptionPlan {
    return SubscriptionPlan(
        id = id,
        name = name,
        code = code,
        description = description,
        price = price,
        currency = currency,
        durationDays = durationInDays,
        hdAvailable = hdAvailable,
        hdr4kAvailable = hdr4kAvailable,
        watchOnLaptopAndTV = watchOnLaptopAndTV,
        watchOnMobileAndTablet = watchOnMobileAndTablet,
        simultaneousStreams = simultaneousStreams,
        isActive = isActive,
        sortOrder = sortOrder
    )
}
