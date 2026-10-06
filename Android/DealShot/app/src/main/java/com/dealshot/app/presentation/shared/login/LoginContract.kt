package com.dealshot.app.presentation.shared.login

data class LoginState(
    val phoneNumber: String = "",
    val message: String? = null,
    val mobileHint: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface LoginIntent {
    data class PhoneChanged(val phone: String) : LoginIntent
    data object ContinueClicked : LoginIntent
    data object ClearError : LoginIntent
}

sealed interface LoginEffect {
    data class NavigateToOtp(val phoneNumber: String, val mobileHint: String) : LoginEffect
    data class ShowError(val message: String) : LoginEffect
}