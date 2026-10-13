package com.dealshot.app.presentation.shared.otp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dealshot.app.core.error.AppErrorHandler
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
import kotlinx.coroutines.CancellationException
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
            }

            OtpIntent.VerifyClicked -> {

                verifyOtp(_uiState.value.phoneNumber)
            }

            OtpIntent.BackClicked -> {

                viewModelScope.launch {

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

                val response = ApiClient.authApi.sendOtp(
                    SendOtpRequestDto(
                        mobile = phoneNumber
                    )
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

                val response = ApiClient.authApi.verifyOtp(
                    VerifyOtpRequestDto(
                        mobile = phoneNumber,
                        otp = otp,
                        name = "User Name"
                    )
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
                    token = response.token,
                    userId = user.id,
                    name = user.name,
                    mobile = user.mobile.ifBlank { phoneNumber },
                    email = user.email,
                    role = user.role,
                    isMobileVerified = user.isMobileVerified,
                    isNewUser = response.isNewUser,
                    maxProfiles = user.maxProfiles,
                    maxDevices = user.maxDevices,
                    subscriptionPlanId = user.subscription.planId,
                    subscriptionStartDate = user.subscription.startDate,
                    subscriptionEndDate = user.subscription.endDate,
                    subscriptionIsActive = user.subscription.isActive,
                    subscriptionStatus = user.subscription.status,
                    lastLoginAt = user.lastLoginAt
                )

                _effect.send(
                    OtpEffect.NavigateToHome
                )

            } catch (e: CancellationException) {

                // Never swallow cancellation (screen closed / scope cancelled)
                throw e

            } catch (e: Exception) {

                Log.e("AUTH_TEST", "SEND OTP FAILED", e)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = null,
                        error = AppErrorHandler.getUserMessage(e)
                    )
                }
            }
        }
    }
}