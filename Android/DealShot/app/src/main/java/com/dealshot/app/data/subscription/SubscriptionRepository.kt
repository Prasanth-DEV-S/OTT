package com.dealshot.app.data.subscription

interface SubscriptionRepository {

    suspend fun getSubscriptionPlans(): List<SubscriptionPlan>

    suspend fun getSubscriptionPlanDetails(planId: String): SubscriptionPlan?

    suspend fun createSubscription(
        planId: String
    ): CreateSubscriptionResponseDto
}
