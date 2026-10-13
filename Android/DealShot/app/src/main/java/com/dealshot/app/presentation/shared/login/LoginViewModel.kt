package com.dealshot.app.presentation.shared.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dealshot.app.core.error.AppErrorHandler
import com.dealshot.app.data.remote.ApiClient
import com.dealshot.app.data.remote.dto.SendOtpRequestDto
import kotlinx.coroutines.CancellationException
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

                // Ignore taps while a request is already running
                if (_uiState.value.isLoading) return

                val phone = _uiState.value.phoneNumber

                if (phone.length != 10) {

                    _uiState.update {
                        it.copy(
                            error = "Enter a valid 10-digit phone number",
                            message = null
                        )
                    }

                    return
                }

                sendOtp("+91$phone")
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
                val response = ApiClient.authApi.sendOtp(
                    SendOtpRequestDto(
                        mobile = mobile
                    )
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
