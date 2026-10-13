package com.dealshot.app.presentation.mobile.login

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dealshot.app.presentation.shared.login.LoginEffect
import com.dealshot.app.presentation.shared.login.LoginViewModel

@Composable
fun LoginRoute(
    viewModel: LoginViewModel = viewModel(),
    onNavigateToOtp: (String, String) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {

        viewModel.effect.collect { effect ->
            when (effect) {

                is LoginEffect.NavigateToOtp -> {

                    onNavigateToOtp(
                        effect.phoneNumber,
                        effect.mobileHint
                    )
                }

                is LoginEffect.ShowError -> {

                }
            }
        }
    }

    LoginScreen(
        state = state,
        onIntent = { intent ->
            viewModel.onIntent(intent)
        }
    )
}