package com.dealshot.app.presentation.shared.subscription

import com.dealshot.app.data.subscription.SubscriptionPlan

data class SubscriptionState(
    val plans: List<SubscriptionPlan> = emptyList(),
    val selectedPlan: SubscriptionPlan? = null,
    val isLoading: Boolean = false,
    val isProcessing: Boolean = false,
    val error: String? = null,
    val message : String? = null,
)

sealed interface SubscriptionIntent {

    data object BackClicked : SubscriptionIntent

    data class PlanSelected(
        val plan: SubscriptionPlan
    ) : SubscriptionIntent

    data object SubscribeClicked : SubscriptionIntent

    data object SuccessDialogDismissed : SubscriptionIntent

    data object ClearError : SubscriptionIntent
}

sealed interface SubscriptionEffect {

    data object NavigateBack : SubscriptionEffect

    data object ShowSuccessDialog : SubscriptionEffect

    data class ShowError(
        val message: String
    ) : SubscriptionEffect
}