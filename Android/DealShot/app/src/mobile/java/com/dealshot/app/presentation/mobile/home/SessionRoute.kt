package com.dealshot.app.presentation.mobile.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dealshot.app.presentation.shared.auth.SessionIntent
import com.dealshot.app.presentation.shared.auth.SessionViewModel
import com.dealshot.app.presentation.shared.auth.SessionViewModelFactory

@Composable
fun SessionRoute(
    factory: SessionViewModelFactory,
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: (phoneNumber: String) -> Unit
) {
    val viewModel: SessionViewModel = viewModel(
        factory = factory
    )

    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onIntent(SessionIntent.CheckSession)
    }

    LaunchedEffect(
        state.isLoading,
        state.currentUser,
        state.errorMessage
    ) {
        if (!state.isLoading && state.errorMessage == null) {
            val user = state.currentUser

            if (user == null) {
                onNavigateToLogin()
            } else {
                onNavigateToHome(user.mobile)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when {
            state.isLoading -> {
                CircularProgressIndicator()
                Text(
                    text = "Checking your session...",
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            state.errorMessage != null -> {
                Text(
                    text = state.errorMessage.orEmpty(),
                    style = MaterialTheme.typography.bodyLarge
                )

                Button(
                    onClick = {
                        viewModel.onIntent(SessionIntent.Retry)
                    },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Retry")
                }

                Button(
                    onClick = onNavigateToLogin,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Sign in")
                }
            }
        }
    }
}
