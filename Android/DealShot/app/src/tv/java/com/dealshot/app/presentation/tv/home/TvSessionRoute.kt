package com.dealshot.app.presentation.tv.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dealshot.app.R
import com.dealshot.app.presentation.shared.auth.SessionIntent
import com.dealshot.app.presentation.shared.auth.SessionViewModel
import com.dealshot.app.presentation.shared.auth.SessionViewModelFactory
import com.dealshot.app.ui.theme.CardBgDark
import com.dealshot.app.ui.theme.CardBorderTop
import com.dealshot.app.ui.theme.DeepBlackBg
import com.dealshot.app.ui.theme.GoldPrimary
import com.dealshot.app.ui.theme.GoldTextLight
import com.dealshot.app.ui.theme.TextMuted

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlackBg)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {

            // Left & Right Video Collage Side Columns for TV (Landscape 16:9)
            Row(modifier = Modifier.fillMaxSize()) {
                // Left Collage Column
                TvSplashCollageColumn(
                    isLeft = true,
                    modifier = Modifier
                        .weight(0.30f)
                        .fillMaxHeight()
                )

                // Center Spacer
                Spacer(modifier = Modifier.weight(0.40f))

                // Right Collage Column
                TvSplashCollageColumn(
                    isLeft = false,
                    modifier = Modifier
                        .weight(0.30f)
                        .fillMaxHeight()
                )
            }

            // Center Branding & Loading Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 32.dp, horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // Center Content Block
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Title: DealShot
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .background(
                                    Brush.radialGradient(
                                        listOf(
                                            GoldPrimary.copy(alpha = 0.35f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(color = GoldTextLight)) {
                                    append("Deal")
                                }
                                withStyle(SpanStyle(color = GoldPrimary)) {
                                    append("Shot")
                                }
                            },
                            fontSize = 52.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    // Tagline 1
                    Text(
                        text = "WATCH   •   DISCOVER   •   EXPERIENCE",
                        color = GoldPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.5.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Hero Heading: Your Next Favourite Video Starts Here
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = GoldPrimary)) {
                                append("Your Next ")
                            }
                            withStyle(SpanStyle(color = Color.White)) {
                                append("Favourite Video")
                            }
                        },
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Starts Here",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Gold Accent Bar
                    Box(
                        modifier = Modifier
                            .width(52.dp)
                            .height(3.5.dp)
                            .clip(CircleShape)
                            .background(GoldPrimary)
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    // Spinner & Subtext
                    if (state.isLoading) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(60.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(
                                                GoldPrimary.copy(alpha = 0.30f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )

                            CircularProgressIndicator(
                                color = GoldPrimary,
                                strokeWidth = 3.5.dp,
                                modifier = Modifier.size(44.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "LOADING AMAZING CONTENT...",
                            color = TextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 2.5.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Footer
                Text(
                    text = "ENTERTAINMENT   •   INSPIRATION   •   KNOWLEDGE",
                    color = GoldPrimary.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.5.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
        }

        // TV Error & Retry Modal
        if (state.errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .widthIn(max = 440.dp)
                        .fillMaxWidth()
                        .shadow(
                            elevation = 24.dp,
                            shape = RoundedCornerShape(20.dp),
                            spotColor = GoldPrimary.copy(alpha = 0.25f)
                        )
                        .clip(RoundedCornerShape(20.dp))
                        .background(CardBgDark)
                        .border(1.dp, CardBorderTop, RoundedCornerShape(20.dp))
                        .padding(32.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = state.errorMessage.orEmpty(),
                            color = Color.White,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        TvSessionButton(
                            label = "Retry",
                            onClick = { viewModel.onIntent(SessionIntent.Retry) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(retryFocus)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        TvSessionButton(
                            label = "Sign in",
                            onClick = onNavigateToLogin,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TvSplashCollageColumn(
    isLeft: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxHeight()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            repeat(6) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.splash_screen),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // Gradient fade mask into dark center background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        if (isLeft) {
                            listOf(Color.Transparent, DeepBlackBg.copy(alpha = 0.95f), DeepBlackBg)
                        } else {
                            listOf(DeepBlackBg, DeepBlackBg.copy(alpha = 0.95f), Color.Transparent)
                        }
                    )
                )
        )
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
            .height(52.dp)
            .clip(shape)
            .background(
                if (focused) {
                    Brush.horizontalGradient(listOf(Color(0xFF7A1515), Color(0xFF3B0B0B)))
                } else {
                    Brush.horizontalGradient(listOf(Color(0xFF25211B), Color(0xFF25211B)))
                }
            )
            .border(
                width = if (focused) 2.5.dp else 1.dp,
                color = if (focused) GoldPrimary else Color(0xFF3A3226),
                shape = shape
            )
            .onFocusChanged { focused = it.isFocused }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (focused) Color.White else Color(0xFFE5E5E5),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
