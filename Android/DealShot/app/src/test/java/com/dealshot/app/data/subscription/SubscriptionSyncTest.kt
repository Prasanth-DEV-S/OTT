package com.dealshot.app.data.subscription

import com.dealshot.app.data.remote.dto.CurrentUserResponseDto
import com.dealshot.app.data.remote.dto.SingleSubscriptionPlanResponseDto
import com.dealshot.app.data.remote.dto.toAuthUser
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SubscriptionSyncTest {

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        isLenient = true
    }

    @Test
    fun scenarioA_activeSubscription_hasPremiumAccess() {
        val state = SubscriptionState(
            isActive = true,
            planId = "6acca570964aec80e86e0d52",
            startDate = "2026-11-11T12:12:29.443Z",
            endDate = "2027-11-11T12:12:29.443Z",
            status = "active"
        )

        assertTrue(hasPremiumAccess(state))
    }

    @Test
    fun scenarioB_inactiveSubscription_deniesPremiumAccess() {
        val state = SubscriptionState(
            isActive = false,
            planId = "6acca570964aec80e86e0d52",
            status = "inactive"
        )

        assertFalse(hasPremiumAccess(state))
    }

    @Test
    fun scenarioE_expiredSubscription_deniesPremiumAccess() {
        val state = SubscriptionState(
            isActive = false,
            planId = "6acca570964aec80e86e0d52",
            status = "expired"
        )

        assertFalse(hasPremiumAccess(state))
    }

    @Test
    fun parseSingleSubscriptionPlanResponse() {
        val sampleJson = """
            {
              "success": true,
              "message": "Subscription plan fetched successfully",
              "data": {
                "_id": "6acca54a964aec80e86e0d4e",
                "name": "Basic",
                "code": "BASIC",
                "description": "Enjoy entertainment every month",
                "price": 199,
                "currency": "INR",
                "durationInDays": 30,
                "hdAvailable": false,
                "hdr4kAvailable": false,
                "watchOnLaptopAndTV": true,
                "watchOnMobileAndTablet": true,
                "simultaneousStreams": 1,
                "isActive": true,
                "sortOrder": 1
              }
            }
        """.trimIndent()

        val response = json.decodeFromString<SingleSubscriptionPlanResponseDto>(sampleJson)

        assertTrue(response.success)
        assertEquals("Subscription plan fetched successfully", response.message)
        assertNotNull(response.data)

        val planDto = response.data!!
        assertEquals("6acca54a964aec80e86e0d4e", planDto.id)
        assertEquals("Basic", planDto.name)
        assertEquals("BASIC", planDto.code)
        assertEquals("Enjoy entertainment every month", planDto.description)
        assertEquals(199, planDto.price)
        assertEquals("INR", planDto.currency)
        assertEquals(30, planDto.durationInDays)
        assertFalse(planDto.hdAvailable)
        assertFalse(planDto.hdr4kAvailable)
        assertTrue(planDto.watchOnLaptopAndTV)
        assertTrue(planDto.watchOnMobileAndTablet)
        assertEquals(1, planDto.simultaneousStreams)
        assertTrue(planDto.isActive)

        val domainPlan = planDto.toDomain()
        assertEquals("Basic", domainPlan.name)
        assertEquals(30, domainPlan.durationDays)
        assertTrue(domainPlan.watchOnLaptopAndTV)
        assertTrue(domainPlan.watchOnMobileAndTablet)
    }

    @Test
    fun parseCreateSubscriptionResponse() {
        val sampleJson = """
            {
              "success": true,
              "message": "Subscription activated successfully",
              "data": {
                "subscriptionId": "6af2a5cd078edef2a470ad86",
                "plan": {
                  "id": "6acca570964aec80e86e0d52",
                  "name": "Premium",
                  "code": "PREMIUM",
                  "durationDays": 365
                },
                "subscription": {
                  "isActive": true,
                  "planId": "6acca570964aec80e86e0d52",
                  "startDate": "2026-11-11T12:12:29.443Z",
                  "endDate": "2027-11-11T12:12:29.443Z",
                  "status": "active"
                }
              }
            }
        """.trimIndent()

        val response = json.decodeFromString<CreateSubscriptionResponseDto>(sampleJson)

        assertTrue(response.success)
        assertEquals("Subscription activated successfully", response.message)
        assertNotNull(response.data)

        val data = response.data!!
        assertEquals("6af2a5cd078edef2a470ad86", data.subscriptionId)

        val plan = data.plan
        assertNotNull(plan)
        assertEquals("6acca570964aec80e86e0d52", plan!!.id)
        assertEquals("Premium", plan.name)
        assertEquals("PREMIUM", plan.code)
        assertEquals(365, plan.durationDays)

        val sub = data.subscription
        assertNotNull(sub)
        assertTrue(sub!!.isActive)
        assertEquals("6acca570964aec80e86e0d52", sub.planId)
        assertEquals("2026-11-11T12:12:29.443Z", sub.startDate)
        assertEquals("2027-11-11T12:12:29.443Z", sub.endDate)
        assertEquals("active", sub.status)
    }

    @Test
    fun parseAuthMeResponse_mapsToAuthUser() {
        val sampleJson = """
            {
              "success": true,
              "user": {
                "subscription": {
                  "isActive": true,
                  "planId": "6acca570964aec80e86e0d52",
                  "startDate": "2026-11-11T12:12:29.443Z",
                  "endDate": "2027-11-11T12:12:29.443Z",
                  "status": "active"
                },
                "_id": "6ae597ed7c61f1d1da15c233",
                "name": "Casey",
                "mobile": "919345540171",
                "email": "hdhd@hdhd.com",
                "profileImage": "https://d2mt5pdqyed9m3.cloudfront.net/avatars/presets/avatar1.svg",
                "role": "user",
                "isActive": true,
                "isBlocked": false,
                "blockedReason": "",
                "isMobileVerified": true,
                "isEmailVerified": false,
                "maxProfiles": 5,
                "lastLoginAt": "2026-11-11T07:43:36.593Z",
                "maxDevices": 3,
                "termsAccepted": false,
                "privacyPolicyAccepted": false,
                "isDeleted": false,
                "gender": "male",
                "activeProfileId": "6af2b7cf078edef2a470ae79",
                "avatarId": "6aec5c98194650def7b18b14"
              }
            }
        """.trimIndent()

        val response = json.decodeFromString<CurrentUserResponseDto>(sampleJson)

        assertTrue(response.success)
        val userDto = response.user
        assertEquals("6ae597ed7c61f1d1da15c233", userDto.id)
        assertEquals("Casey", userDto.name)
        assertEquals("919345540171", userDto.mobile)
        assertEquals("hdhd@hdhd.com", userDto.email)
        assertEquals("male", userDto.gender)
        assertEquals("6af2b7cf078edef2a470ae79", userDto.activeProfileId)
        assertEquals("6aec5c98194650def7b18b14", userDto.avatarId)
        assertTrue(userDto.subscription.isActive)
        assertEquals("active", userDto.subscription.status)

        val authUser = userDto.toAuthUser(planName = "Premium")
        assertEquals("6ae597ed7c61f1d1da15c233", authUser.id)
        assertEquals("Casey", authUser.name)
        assertEquals("Premium", authUser.subscription.planName)
        assertTrue(hasPremiumAccess(authUser.subscription))
    }
}
