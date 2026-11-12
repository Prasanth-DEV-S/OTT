package com.dealshot.app.data.subscription

import com.dealshot.app.data.remote.api.SubscriptionApiService
import com.dealshot.app.data.remote.dto.CreateSubscriptionRequestDto
import retrofit2.HttpException

class SubscriptionRepositoryImpl(
    private val api: SubscriptionApiService
) : SubscriptionRepository {

    override suspend fun getSubscriptionPlans(): List<SubscriptionPlan> {
        return api
            .getSubscriptionPlans()
            .data
            .map { it.toDomain() }
    }

    override suspend fun getSubscriptionPlanDetails(planId: String): SubscriptionPlan? {
        return api
            .getSubscriptionPlanDetails(planId)
            .data
            ?.toDomain()
    }

    override suspend fun createSubscription(
        planId: String
    ): CreateSubscriptionResponseDto {
        val cleanId = planId.trim()
        require(cleanId.isNotBlank()) { "Plan ID cannot be empty" }

        val response = api.createSubscription(CreateSubscriptionRequestDto(planId = cleanId))
        if (!response.isSuccessful) {
            throw HttpException(response)
        }

        val body = response.body()
            ?: throw IllegalStateException("Subscription creation response empty")

        if (!body.success) {
            throw IllegalStateException(body.message.ifBlank { "Failed to initiate subscription" })
        }

        return body
    }
}
