package com.dealshot.app.presentation.tv.otp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dealshot.app.data.preferences.UserPreferences
import com.dealshot.app.presentation.shared.otp.OtpEffect
import com.dealshot.app.presentation.shared.otp.OtpIntent
import com.dealshot.app.presentation.shared.otp.OtpViewModel

/**
 * TV entry for the OTP screen.
 * Same ViewModel, intents and effects as the phone OtpRoute.
 * Only the UI (TvOtpScreen) is different.
 */
@Composable
fun TvOtpRoute(
    phoneNumber: String,
    mobileHint: String,
    onNavigateToHome: () -> Unit,
    onNavigateBack: () -> Unit
) {

    val context = LocalContext.current

    val userPreferences = remember {
        UserPreferences(context.applicationContext)
    }

    val viewModel: OtpViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {

            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>
            ): T {
                return OtpViewModel(
                    userPreferences = userPreferences
                ) as T
            }
        }
    )

    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(phoneNumber, mobileHint) {

        viewModel.onIntent(
            OtpIntent.PhoneNumberChanged(phoneNumber)
        )

        viewModel.onIntent(
            OtpIntent.MobileHintChanged(mobileHint)
        )
    }

    LaunchedEffect(Unit) {

        viewModel.effect.collect { effect ->

            when (effect) {

                OtpEffect.NavigateToHome -> onNavigateToHome()

                OtpEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    TvOtpScreen(
        state = state,
        onIntent = { intent ->
            viewModel.onIntent(intent)
        }
    )
}
