package com.dealshot.app.presentation.mobile.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.dealshot.app.R
import com.dealshot.app.presentation.shared.auth.SessionIntent
import com.dealshot.app.presentation.shared.auth.SessionViewModel
import com.dealshot.app.presentation.shared.auth.SessionViewModelFactory
import com.dealshot.app.ui.theme.CardBgDark
import com.dealshot.app.ui.theme.CardBorderTop
import com.dealshot.app.ui.theme.DeepBlackBg
import com.dealshot.app.ui.theme.GoldPrimary
import kotlinx.coroutines.delay

@Composable
fun SessionRoute(
    factory: SessionViewModelFactory,
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: (phoneNumber: String) -> Unit
) {
    val viewModel: SessionViewModel = viewModel(factory = factory)
    val state by viewModel.uiState.collectAsState()
    var minTimeElapsed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.onIntent(SessionIntent.CheckSession)
        delay(3000L) // Keep splash screen for 3 seconds exactly like Instagram
        minTimeElapsed = true
    }

    LaunchedEffect(
        state.isLoading,
        state.currentUser,
        state.errorMessage,
        minTimeElapsed
    ) {
        if (minTimeElapsed && !state.isLoading && state.errorMessage == null) {
            val user = state.currentUser
            if (user == null) {
                onNavigateToLogin()
            } else {
                onNavigateToHome(user.mobile)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlackBg),
        contentAlignment = Alignment.Center
    ) {
        // Full-screen dynamic splash image adjusting across screen sizes (-swDP)
        AsyncImage(
            model = R.drawable.splash_screen,
            contentDescription = "DealShot Fullscreen Splash",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        )

        // Error Feedback & Retry Options
        if (state.errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .widthIn(max = 380.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(CardBgDark)
                        .border(1.dp, CardBorderTop, RoundedCornerShape(20.dp))
                        .padding(28.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = state.errorMessage.orEmpty(),
                            color = Color.White,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { viewModel.onIntent(SessionIntent.Retry) },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text("Retry", color = Color(0xFF140E02), fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = onNavigateToLogin,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B2620)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text("Sign in", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
