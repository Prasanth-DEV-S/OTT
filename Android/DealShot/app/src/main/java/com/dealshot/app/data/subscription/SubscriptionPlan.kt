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
    val maxDevices: Int,
    val maxProfiles: Int,
    val simultaneousStreams: Int,
    val videoQuality: String,
    val hasPremiumContent: Boolean
)

fun SubscriptionPlanDto.toDomain(): SubscriptionPlan {
    return SubscriptionPlan(
        id = id,
        name = name,
        code = code,
        description = description,
        price = price,
        currency = currency,
        durationDays = durationDays,
        maxDevices = maxDevices,
        maxProfiles = maxProfiles,
        simultaneousStreams = simultaneousStreams,
        videoQuality = videoQuality,
        hasPremiumContent = hasPremiumContent
    )
}