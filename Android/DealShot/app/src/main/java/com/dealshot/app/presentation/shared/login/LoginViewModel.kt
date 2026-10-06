package com.dealshot.app.presentation.shared.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dealshot.app.data.remote.ApiClient
import com.dealshot.app.data.remote.dto.SendOtpRequestDto
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginState())
    val uiState = _uiState.asStateFlow()

    private val _effect = Channel<LoginEffect>(
        capacity = Channel.BUFFERED
    )

    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: LoginIntent) {

        Log.d(
            "LOGIN_VM",
            "INTENT RECEIVED = $intent"
        )

        when (intent) {

            is LoginIntent.PhoneChanged -> {

                val phone = intent.phone
                    .filter { it.isDigit() }
                    .take(10)

                _uiState.update {
                    it.copy(
                        phoneNumber = phone,
                        error = null,
                        message = null
                    )
                }
            }

            LoginIntent.ContinueClicked -> {

                val phone = _uiState.value.phoneNumber

                Log.d(
                    "LOGIN_FLOW",
                    "Continue clicked: $phone"
                )

                if (phone.length != 10) {

                    _uiState.update {
                        it.copy(
                            error = "Enter a valid 10-digit phone number",
                            message = null
                        )
                    }

                    return
                }

                sendOtp(phone)
            }

            LoginIntent.ClearError -> {

                _uiState.update {
                    it.copy(
                        error = null
                    )
                }
            }
        }
    }

    private fun sendOtp(mobile: String) {

        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    isLoading = true,
                    message = null,
                    error = null
                )
            }

            try {

                Log.d(
                    "AUTH_TEST",
                    "Sending OTP for: $mobile"
                )

                val response = ApiClient.authApi.sendOtp(
                    SendOtpRequestDto(
                        mobile = mobile
                    )
                )

                Log.d(
                    "AUTH_TEST",
                    "OTP SUCCESS: ${response.message}"
                )

                Log.d(
                    "AUTH_TEST",
                    "MOBILE HINT: ${response.mobileHint}"
                )

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = response.message,
                        mobileHint = response.mobileHint,
                        error = null
                    )
                }

                // Navigate only after successful API response
                _effect.send(
                    LoginEffect.NavigateToOtp(
                        phoneNumber = mobile,
                        mobileHint = response.mobileHint
                    )
                )

            } catch (e: Exception) {

                Log.e(
                    "AUTH_TEST",
                    "SEND OTP FAILED",
                    e
                )

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = null,
                        error = e.message ?: "Failed to send OTP"
                    )
                }
            }
        }
    }
}