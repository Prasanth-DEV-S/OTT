package com.dealshot.app.presentation.shared.subscription

data class SubscriptionState(
    val selectedPlan: SubscriptionPlan = SubscriptionPlan.YEARLY,
    val isProcessing: Boolean = false,
    val error: String? = null
)

enum class SubscriptionPlan {
    MONTHLY,
    YEARLY
}

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