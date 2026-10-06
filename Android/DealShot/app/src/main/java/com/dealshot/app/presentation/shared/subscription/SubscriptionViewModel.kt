package com.dealshot.app.presentation.shared.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SubscriptionViewModel : ViewModel() {

    private val _state = MutableStateFlow(
        SubscriptionState()
    )

    val state = _state.asStateFlow()

    private val _effect =
        Channel<SubscriptionEffect>(
            capacity = Channel.BUFFERED
        )

    val effect = _effect.receiveAsFlow()

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

                _state.update {
                    it.copy(
                        isProcessing = true
                    )
                }

                sendEffect(
                    SubscriptionEffect.ShowSuccessDialog
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

    private fun sendEffect(
        effect: SubscriptionEffect
    ) {

        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}