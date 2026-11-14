package com.dealshot.app.presentation.shared.subscription

import com.dealshot.app.data.subscription.SubscriptionPlan
import com.dealshot.app.data.subscription.SubscriptionState as UserSubscriptionState

data class SubscriptionState(
    val plans: List<SubscriptionPlan> = emptyList(),
    val selectedPlan: SubscriptionPlan? = null,
    val isLoading: Boolean = false,
    val isProcessing: Boolean = false,
    val error: String? = null,
    val message: String? = null,

    // User Subscription Status & Dynamic Plan Details
    val userSubscription: UserSubscriptionState = UserSubscriptionState(),
    val featuredPlanDetails: SubscriptionPlan? = null,
    val activePlanDetails: SubscriptionPlan? = null,
    val showNoSubscriptionPopup: Boolean = false,
    val isLoadingPlanDetails: Boolean = false,
    val planDetailsError: String? = null
)

sealed interface SubscriptionIntent {

    data object BackClicked : SubscriptionIntent

    data class PlanSelected(
        val plan: SubscriptionPlan
    ) : SubscriptionIntent

    data object SubscribeClicked : SubscriptionIntent

    data object SuccessDialogDismissed : SubscriptionIntent

    data object ClearError : SubscriptionIntent

    data object DismissNoSubscriptionPopup : SubscriptionIntent

    data object UpgradePlanClicked : SubscriptionIntent

    data object RetryLoadClicked : SubscriptionIntent
}

sealed interface SubscriptionEffect {

    data object NavigateBack : SubscriptionEffect

    data object ShowSuccessDialog : SubscriptionEffect

    data class ShowError(
        val message: String
    ) : SubscriptionEffect
}
