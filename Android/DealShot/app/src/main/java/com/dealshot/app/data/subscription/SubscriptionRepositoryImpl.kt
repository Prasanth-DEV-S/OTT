package com.dealshot.app.data.subscription

import com.dealshot.app.data.remote.api.SubscriptionApiService

class SubscriptionRepositoryImpl(
    private val api: SubscriptionApiService
) : SubscriptionRepository {

    override suspend fun getSubscriptionPlans(): List<SubscriptionPlan> {
        return api
            .getSubscriptionPlans()
            .data
            .map { it.toDomain() }
    }

    override suspend fun createSubscription(
        planId: String
    ): CreateSubscriptionResponseDto {

        return api.createSubscription(planId)
    }
}