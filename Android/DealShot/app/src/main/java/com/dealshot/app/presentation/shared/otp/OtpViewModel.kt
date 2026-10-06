package com.dealshot.app.presentation.shared.otp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dealshot.app.data.preferences.UserPreferences
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.dealshot.app.data.remote.ApiClient
import com.dealshot.app.data.remote.dto.SendOtpRequestDto
import com.dealshot.app.data.remote.dto.VerifyOtpRequestDto
import kotlinx.coroutines.Job

class OtpViewModel(private val userPreferences: UserPreferences) : ViewModel() {

    private val _uiState = MutableStateFlow(
        OtpState()
    )

    val uiState = _uiState.asStateFlow()

    private val _effect = Channel<OtpEffect>(
        capacity = Channel.BUFFERED
    )

    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: OtpIntent) {

        Log.d(
            "OTP_VM",
            "INTENT RECEIVED = $intent"
        )

        when (intent) {

            is OtpIntent.MobileHintChanged -> {
                _uiState.update {
                    it.copy(
                        mobileHint = intent.mobileHint
                    )
                }
            }

            is OtpIntent.PhoneNumberChanged -> {

                _uiState.update {
                    it.copy(
                        phoneNumber = intent.phoneNumber
                    )
                }
            }

            is OtpIntent.OtpChanged -> {

                val otp = intent.otp
                    .filter { it.isDigit() }
                    .take(6)

                _uiState.update {
                    it.copy(
                        otp = otp,
                        canVerify = otp.length == 6,
                        error = null
                    )
                }

                Log.d(
                    "OTP_FLOW",
                    "OTP = $otp, canVerify = ${otp.length == 6}"
                )
            }

            OtpIntent.VerifyClicked -> {

                verifyOtp(_uiState.value.phoneNumber)
            }

            OtpIntent.BackClicked -> {

                viewModelScope.launch {

                    Log.d(
                        "OTP_FLOW",
                        "Sending NavigateBack"
                    )

                    _effect.send(
                        OtpEffect.NavigateBack
                    )
                }
            }

            OtpIntent.ResendClicked -> {

                resendOtp()
            }

            OtpIntent.ClearError -> {

                _uiState.update {
                    it.copy(
                        error = null
                    )
                }
            }
        }
    }

    private fun resendOtp() {

        val phoneNumber = _uiState.value.phoneNumber

        if (phoneNumber.isBlank()) {

            _uiState.update {
                it.copy(
                    error = "Phone number is required"
                )
            }

            return
        }

        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    message = null
                )
            }

            try {

                Log.d(
                    "OTP_FLOW",
                    "RESEND OTP API CALL"
                )

                val response = ApiClient.authApi.sendOtp(
                    SendOtpRequestDto(
                        mobile = phoneNumber
                    )
                )

                Log.d(
                    "OTP_FLOW",
                    "RESEND OTP SUCCESS"
                )

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = null,
                        message = response.message ?: "OTP sent successfully"
                    )
                }

            } catch (e: Exception) {

                Log.e(
                    "OTP_FLOW",
                    "RESEND OTP FAILED",
                    e
                )

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to resend OTP"
                    )
                }
            }
        }
    }

    private fun verifyOtp(phoneNumber: String) {

        val otp = _uiState.value.otp

        Log.d(
            "OTP_FLOW",
            "VERIFY CLICKED = $otp"
        )

        if (otp.length != 6) {

            _uiState.update {
                it.copy(
                    error = "Enter a valid 6-digit OTP",
                    canVerify = false
                )
            }

            return
        }

        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null
                )
            }

            try {

                Log.d(
                    "OTP_FLOW",
                    "VERIFY OTP API CALL"
                )

                val response = ApiClient.authApi.verifyOtp(
                    VerifyOtpRequestDto(
                        mobile = phoneNumber,
                        otp = otp
                    )
                )

                Log.d(
                    "OTP_FLOW",
                    "VERIFY OTP SUCCESS"
                )

                Log.d(
                    "OTP_FLOW",
                    "IS NEW USER = ${response.isNewUser}"
                )

                Log.d(
                    "OTP_FLOW",
                    "USER MOBILE = ${response.user}"
                )

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = null,
                        message = "OTP verified successfully"
                    )
                }

                val user = response.user

                userPreferences.saveLoginSession(
                    token = response.token.orEmpty(),
                    userId = user.id.orEmpty(),
                    mobile = user.mobile ?: phoneNumber,
                    role = user.role.orEmpty(),
                    isNewUser = response.isNewUser
                )

                _effect.send(
                    OtpEffect.NavigateToHome
                )

            } catch (e: Exception) {

                Log.e(
                    "OTP_FLOW",
                    "VERIFY OTP FAILED",
                    e
                )

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Invalid OTP"
                    )
                }
            }
        }
    }
}