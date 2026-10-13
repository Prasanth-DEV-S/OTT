
package com.dealshot.app.presentation.shared.auth

import com.dealshot.app.data.remote.dto.CurrentUserDto

data class SessionUiState(
    val isLoading: Boolean = true,
    val currentUser: CurrentUserDto? = null,
    val errorMessage: String? = null
)

sealed interface SessionIntent {
    data object CheckSession : SessionIntent
    data object Retry : SessionIntent
}
