package com.dealshot.app.presentation.shared.otp

data class OtpState(
    val phoneNumber: String = "",
    val otp: String = "",
    val canVerify: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val message : String? = null,
    val mobileHint : String? = null
)

sealed interface OtpIntent {

    data class PhoneNumberChanged(
        val phoneNumber: String
    ) : OtpIntent

    data class MobileHintChanged(
        val mobileHint: String
    ) : OtpIntent

    data class OtpChanged(
        val otp: String
    ) : OtpIntent

    data object VerifyClicked : OtpIntent

    data object BackClicked : OtpIntent

    data object ResendClicked : OtpIntent

    data object ClearError : OtpIntent
}

sealed interface OtpEffect {

    data object NavigateToHome : OtpEffect

    data object NavigateBack : OtpEffect
}