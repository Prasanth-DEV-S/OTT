
package com.dealshot.app.presentation.shared.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dealshot.app.data.remote.repository.AuthRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

class SessionViewModel(
    private val authRepository: AuthRepository,
    private val hasSavedToken: suspend () -> Boolean,
    private val clearSession: suspend () -> Unit
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionUiState())
    val uiState = _uiState.asStateFlow()

    private var hasCheckedSession = false

    fun onIntent(intent: SessionIntent) {
        when (intent) {
            SessionIntent.CheckSession -> {
                if (!hasCheckedSession) {
                    hasCheckedSession = true
                    checkSession()
                }
            }

            SessionIntent.Retry -> checkSession()
        }
    }

    private fun checkSession() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            try {
                if (!hasSavedToken()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentUser = null
                        )
                    }
                    return@launch
                }

                val user = authRepository.getCurrentUser()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentUser = user,
                        errorMessage = null
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: HttpException) {
                if (e.code() == 401 || e.code() == 403) {
                    clearSession()

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentUser = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Unable to verify your session. Please retry."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Network or server error. Please try again."
                    )
                }
            }
        }
    }
}
