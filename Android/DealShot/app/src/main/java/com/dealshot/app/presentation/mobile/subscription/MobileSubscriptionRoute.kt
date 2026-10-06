package com.dealshot.app.presentation.mobile.subscription

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dealshot.app.presentation.shared.subscription.SubscriptionEffect
import com.dealshot.app.presentation.shared.subscription.SubscriptionViewModel

@Composable
fun MobileSubscriptionRoute(
    title: String,
    description: String,
    url: String,
    onBack: () -> Unit,
    onOpenPlayer: (
        title: String,
        description: String,
        url: String
    ) -> Unit,
    viewModel: SubscriptionViewModel = viewModel()
) {

    val state = viewModel.state.collectAsStateWithLifecycle().value

    LaunchedEffect(Unit) {

        viewModel.effect.collect { effect ->

            when (effect) {

                SubscriptionEffect.NavigateBack -> {
                    onBack()
                }

                SubscriptionEffect.ShowSuccessDialog -> {

                    onOpenPlayer(
                        title,
                        description,
                        url
                    )
                }

                else -> {}
            }
        }
    }

    MobileSubscriptionScreen(
        state = state,
        onIntent = viewModel::onIntent
    )
}