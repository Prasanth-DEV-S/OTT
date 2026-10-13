package com.dealshot.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class VerifyOtpResponseDto(
    val success: Boolean,
    val message: String,
    val token: String,
    val isNewUser: Boolean,
    val user: VerifyOtpUserDto
)

@Serializable
data class VerifyOtpUserDto(
    val id: String,
    val name: String,
    val mobile: String,
    val email: String,
    val role: String,
    val isMobileVerified: Boolean,
    val subscription: VerifyOtpSubscriptionDto,
    val maxProfiles: Int,
    val maxDevices: Int,
    val lastLoginAt: String
)

@Serializable
data class VerifyOtpSubscriptionDto(
    val planId: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val isActive: Boolean,
    val status: String
)