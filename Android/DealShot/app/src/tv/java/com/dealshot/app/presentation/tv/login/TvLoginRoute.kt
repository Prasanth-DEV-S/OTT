package com.dealshot.app.presentation.tv.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dealshot.app.presentation.shared.login.LoginEffect
import com.dealshot.app.presentation.shared.login.LoginViewModel

/**
 * Same wiring as the mobile LoginRoute. The ViewModel, state, intents and effects are
 * shared untouched; only the UI (TvLoginScreen) differs.
 */
@Composable
fun TvLoginRoute(
    viewModel: LoginViewModel = viewModel(),
    onNavigateToOtp: (String, String) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is LoginEffect.NavigateToOtp ->
                    onNavigateToOtp(effect.phoneNumber, effect.mobileHint)

                // Errors are already rendered from state.error in the screen.
                is LoginEffect.ShowError -> Unit
            }
        }
    }

    TvLoginScreen(
        state = state,
        onIntent = viewModel::onIntent
    )
}
