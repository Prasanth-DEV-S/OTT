
package com.dealshot.app.presentation.shared.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dealshot.app.data.remote.repository.AuthRepository

class SessionViewModelFactory(
    private val authRepository: AuthRepository,
    private val hasSavedToken: suspend () -> Boolean,
    private val clearSession: suspend () -> Unit
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(SessionViewModel::class.java)) {
            return SessionViewModel(
                authRepository = authRepository,
                hasSavedToken = hasSavedToken,
                clearSession = clearSession
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}
