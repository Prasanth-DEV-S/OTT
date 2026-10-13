package com.dealshot.app.presentation.shared.subscription

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dealshot.app.core.error.AppErrorHandler
import com.dealshot.app.data.remote.ApiClient
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
class SubscriptionViewModel : ViewModel() {

    private val repository: SubscriptionRepository =
        SubscriptionRepositoryImpl(
            ApiClient.subscriptionApi
        )

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

    fun onIntent(
        intent: SubscriptionIntent
    ) {

        when (intent) {

            SubscriptionIntent.BackClicked -> {

                sendEffect(
                    SubscriptionEffect.NavigateBack
                )
            }

            is SubscriptionIntent.PlanSelected -> {

                _state.update {
                    it.copy(
                        selectedPlan = intent.plan
                    )
                }
            }

            SubscriptionIntent.SubscribeClicked -> {

                val selectedPlan =
                    _state.value.selectedPlan

                if (selectedPlan == null) {

                    sendEffect(
                        SubscriptionEffect.ShowError(
                            "Please select a subscription plan"
                        )
                    )

                    return
                }

                createSubscription(
                    planId = selectedPlan.id
                )
            }

            SubscriptionIntent.SuccessDialogDismissed -> {

                _state.update {
                    it.copy(
                        isProcessing = false
                    )
                }
            }

            SubscriptionIntent.ClearError -> {

                _state.update {
                    it.copy(
                        error = null
                    )
                }
            }
        }
    }
    private fun createSubscription(
        planId: String
    ) {

        viewModelScope.launch {

            _state.update {
                it.copy(
                    isProcessing = true,
                    error = null
                )
            }

            try {

                val response =
                    repository.createSubscription(
                        planId = planId
                    )

                Log.i("API_test",response.toString())

                if (response.success) {

                    _state.update {
                        it.copy(
                            isProcessing = false
                        )
                    }

                    sendEffect(
                        SubscriptionEffect.ShowSuccessDialog
                    )

                } else {

                    _state.update {
                        it.copy(
                            isProcessing = false,
                            error = response.message
                        )
                    }

                    sendEffect(
                        SubscriptionEffect.ShowError(
                            response.message
                        )
                    )
                }

            } catch (e: CancellationException) {

                // Never swallow cancellation (screen closed / scope cancelled)
                throw e

            } catch (e: Exception) {

                Log.e("AUTH_TEST", "SEND OTP FAILED", e)

                _state.update {
                    it.copy(
                        isLoading = false,
                        message = null,
                        error = AppErrorHandler.getUserMessage(e)
                    )
                }
            }
        }
    }
    private fun sendEffect(
        effect: SubscriptionEffect
    ) {

        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}