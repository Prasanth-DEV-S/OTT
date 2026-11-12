package com.dealshot.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateSubscriptionRequestDto(
    val planId: String
)
