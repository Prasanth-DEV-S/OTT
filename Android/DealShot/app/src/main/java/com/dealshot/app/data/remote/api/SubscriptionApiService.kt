package com.dealshot.app.data.remote.api

import com.dealshot.app.data.remote.dto.CreateSubscriptionRequestDto
import com.dealshot.app.data.remote.dto.SingleSubscriptionPlanResponseDto
import com.dealshot.app.data.remote.dto.SubscriptionPlansResponseDto
import com.dealshot.app.data.subscription.CreateSubscriptionResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface SubscriptionApiService {

    @GET("subscription/plans")
    suspend fun getSubscriptionPlans(): SubscriptionPlansResponseDto

    @GET("subscription/plans/{planId}")
    suspend fun getSubscriptionPlanDetails(
        @Path("planId") planId: String
    ): SingleSubscriptionPlanResponseDto

    @POST("subscription/create")
    suspend fun createSubscription(
        @Body request: CreateSubscriptionRequestDto
    ): Response<CreateSubscriptionResponseDto>
}
