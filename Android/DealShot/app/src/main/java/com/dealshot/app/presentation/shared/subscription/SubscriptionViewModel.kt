package com.dealshot.app.presentation.shared.subscription

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dealshot.app.DealShotApplication
import com.dealshot.app.core.error.AppErrorHandler
import com.dealshot.app.data.preferences.UserPreferences
import com.dealshot.app.data.remote.ApiClient
import com.dealshot.app.data.remote.repository.AuthRepository
import com.dealshot.app.data.remote.repository.AuthRepositoryImpl
import com.dealshot.app.data.subscription.SubscriptionPlan
import com.dealshot.app.data.subscription.SubscriptionRepository
import com.dealshot.app.data.subscription.SubscriptionRepositoryImpl
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SubscriptionViewModel(
    subscriptionRepository: SubscriptionRepository? = null,
    private val userPreferences: UserPreferences? = null,
    authRepository: AuthRepository? = null
) : ViewModel() {

    private val repository: SubscriptionRepository =
        subscriptionRepository ?: SubscriptionRepositoryImpl(ApiClient.subscriptionApi)

    private val prefs: UserPreferences by lazy {
        userPreferences ?: UserPreferences(DealShotApplication.instance.applicationContext)
    }

    private val authRepo: AuthRepository by lazy {
        authRepository ?: AuthRepositoryImpl(ApiClient.authApi, prefs)
    }

    private val _state = MutableStateFlow(
        SubscriptionState(
            isLoading = true
        )
    )

    val state = _state.asStateFlow()

    private val _effect =
        Channel<SubscriptionEffect>(
            capacity = Channel.BUFFERED
        )

    val effect = _effect.receiveAsFlow()

    init {
        loadPlans()
    }

    private fun loadPlans() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    error = null
                )
            }

            try {
                val plans = repository.getSubscriptionPlans()

                _state.update {
                    it.copy(
                        plans = plans,
                        selectedPlan = plans.firstOrNull(),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load subscription plans"
                    )
                }

                sendEffect(
                    SubscriptionEffect.ShowError(
                        e.message ?: "Failed to load subscription plans"
                    )
                )
            }
        }
    }

    fun onIntent(intent: SubscriptionIntent) {
        when (intent) {
            SubscriptionIntent.BackClicked -> {
                sendEffect(SubscriptionEffect.NavigateBack)
            }

            is SubscriptionIntent.PlanSelected -> {
                _state.update {
                    it.copy(selectedPlan = intent.plan)
                }
            }

            SubscriptionIntent.SubscribeClicked -> {
                val selectedPlan = _state.value.selectedPlan

                if (selectedPlan == null) {
                    sendEffect(
                        SubscriptionEffect.ShowError("Please select a subscription plan")
                    )
                    return
                }

                createSubscription(planId = selectedPlan.id)
            }

            SubscriptionIntent.SuccessDialogDismissed -> {
                _state.update {
                    it.copy(isProcessing = false)
                }
            }

            SubscriptionIntent.ClearError -> {
                _state.update {
                    it.copy(error = null)
                }
            }
        }
    }

    private fun createSubscription(planId: String) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isProcessing = true,
                    error = null
                )
            }

            try {
                val response = repository.createSubscription(planId = planId)

                Log.i("API_test", response.toString())

                if (response.success) {
                    val data = response.data
                    val subDto = data?.subscription
                    val planDto = data?.plan
                    val selectedPlan = _state.value.selectedPlan

                    val isActivated = if (subDto != null) {
                        subDto.isActive && subDto.status.equals("active", ignoreCase = true)
                    } else {
                        data?.subscriptionStatus?.equals("active", ignoreCase = true) == true
                    }

                    val resolvedPlanId = subDto?.planId ?: planDto?.id ?: planId
                    val resolvedPlanName = planDto?.name?.ifBlank { null } ?: selectedPlan?.name ?: "Premium"
                    val resolvedStartDate = subDto?.startDate
                    val resolvedEndDate = subDto?.endDate
                    val resolvedStatus = subDto?.status ?: "active"

                    if (isActivated) {
                        // 1. Mark user as Premium immediately in centralized preferences
                        prefs.saveSubscriptionState(
                            planId = resolvedPlanId,
                            planName = resolvedPlanName,
                            startDate = resolvedStartDate,
                            endDate = resolvedEndDate,
                            isActive = true,
                            status = resolvedStatus
                        )

                        // 2. Refresh authoritative user details from /api/auth/me
                        try {
                            val userDto = authRepo.getCurrentUser()
                            prefs.saveSubscriptionState(
                                planId = userDto.subscription.planId ?: resolvedPlanId,
                                planName = resolvedPlanName,
                                startDate = userDto.subscription.startDate ?: resolvedStartDate,
                                endDate = userDto.subscription.endDate ?: resolvedEndDate,
                                isActive = userDto.subscription.isActive,
                                status = userDto.subscription.status
                            )
                        } catch (e: Exception) {
                            Log.w(
                                "SubscriptionViewModel",
                                "Failed to refresh /auth/me after activation, keeping confirmed activation state",
                                e
                            )
                        }

                        _state.update {
                            it.copy(isProcessing = false)
                        }

                        sendEffect(SubscriptionEffect.ShowSuccessDialog)
                    } else {
                        _state.update {
                            it.copy(
                                isProcessing = false,
                                error = response.message.ifBlank { "Subscription activation failed" }
                            )
                        }

                        sendEffect(
                            SubscriptionEffect.ShowError(
                                response.message.ifBlank { "Subscription activation failed" }
                            )
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            isProcessing = false,
                            error = response.message
                        )
                    }

                    sendEffect(
                        SubscriptionEffect.ShowError(response.message)
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("SUBSCRIPTION_FLOW", "CREATE SUBSCRIPTION FAILED", e)

                _state.update {
                    it.copy(
                        isProcessing = false,
                        isLoading = false,
                        message = null,
                        error = AppErrorHandler.getUserMessage(e)
                    )
                }

                sendEffect(
                    SubscriptionEffect.ShowError(AppErrorHandler.getUserMessage(e))
                )
            }
        }
    }

    private fun sendEffect(effect: SubscriptionEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}
