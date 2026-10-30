package com.dealshot.app.data.remote.api

import com.dealshot.app.data.remote.dto.SingleSubscriptionPlanResponseDto
import com.dealshot.app.data.remote.dto.SubscriptionPlansResponseDto
import com.dealshot.app.data.subscription.CreateSubscriptionResponseDto
import retrofit2.http.GET
import retrofit2.http.Path

interface SubscriptionApiService {

    @GET("subscription/plans")
    suspend fun getSubscriptionPlans(): SubscriptionPlansResponseDto

    @GET("subscription/plans/{planId}")
    suspend fun getSubscriptionPlanDetails(
        @Path("planId") planId: String
    ): SingleSubscriptionPlanResponseDto

    @GET("subscription/plans/{planId}")
    suspend fun createSubscription(
        @Path("planId") planId: String
    ): CreateSubscriptionResponseDto
}
