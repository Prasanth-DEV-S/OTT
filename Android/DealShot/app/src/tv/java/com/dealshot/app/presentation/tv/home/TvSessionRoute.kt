package com.dealshot.app.presentation.tv.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dealshot.app.presentation.shared.auth.SessionIntent
import com.dealshot.app.presentation.shared.auth.SessionViewModel
import com.dealshot.app.presentation.shared.auth.SessionViewModelFactory

/**
 * TV copy of the mobile SessionRoute (the tv source set cannot see the
 * mobile package). Same SessionViewModel, intents and navigation rules.
 */
@Composable
fun TvSessionRoute(
    factory: SessionViewModelFactory,
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: (phoneNumber: String) -> Unit
) {
    val viewModel: SessionViewModel = viewModel(factory = factory)

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

    val retryFocus = remember { FocusRequester() }

    LaunchedEffect(state.errorMessage) {
        if (state.errorMessage != null) {
            runCatching { retryFocus.requestFocus() }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when {
            state.isLoading -> {
                CircularProgressIndicator()
                Text(
                    text = "Checking your session...",
                    color = Color.White,
                    fontSize = 22.sp,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            state.errorMessage != null -> {
                Text(
                    text = state.errorMessage.orEmpty(),
                    color = Color.White,
                    fontSize = 22.sp
                )

                TvSessionButton(
                    label = "Retry",
                    onClick = { viewModel.onIntent(SessionIntent.Retry) },
                    modifier = Modifier
                        .padding(top = 24.dp)
                        .focusRequester(retryFocus)
                )

                TvSessionButton(
                    label = "Sign in",
                    onClick = onNavigateToLogin,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun TvSessionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(14.dp)

    Box(
        modifier = modifier
            .clip(shape)
            .background(if (focused) Color(0xFFFF9800) else Color(0xFF252525))
            .border(2.dp, if (focused) Color.White else Color.Transparent, shape)
            .onFocusChanged { focused = it.isFocused }
            .clickable(onClick = onClick)
            .padding(horizontal = 36.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (focused) Color.Black else Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
