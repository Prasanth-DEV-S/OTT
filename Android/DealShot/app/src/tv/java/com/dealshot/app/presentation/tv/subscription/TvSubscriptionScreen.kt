package com.dealshot.app.presentation.tv.subscription

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dealshot.app.presentation.shared.subscription.SubscriptionIntent
import com.dealshot.app.presentation.shared.subscription.SubscriptionState

private val Purple = Color(0xFF8B5CF6)

/**
 * TV subscription screen. Same state and intents as MobileSubscriptionScreen
 * (PlanSelected, SubscribeClicked, BackClicked); landscape layout with the
 * pitch on the left and plans + Subscribe on the right.
 */
@Composable
fun TvSubscriptionScreen(
    state: SubscriptionState,
    onIntent: (SubscriptionIntent) -> Unit
) {
    BackHandler { onIntent(SubscriptionIntent.BackClicked) }

    if (state.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Purple)
        }
        return
    }

    val firstPlanFocus = remember { FocusRequester() }
    LaunchedEffect(state.plans.isNotEmpty()) {
        if (state.plans.isNotEmpty()) runCatching { firstPlanFocus.requestFocus() }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color.Black, Color(0xFF080808), Color.Black)
                )
            )
            .padding(horizontal = 64.dp, vertical = 40.dp),
        horizontalArrangement = Arrangement.spacedBy(64.dp)
    ) {
        // ── LEFT: pitch ──
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "👑", fontSize = 72.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Watch without limits",
                color = Color.White,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Choose the plan that works for you.",
                color = Color.Gray,
                fontSize = 20.sp
            )
            Spacer(Modifier.height(32.dp))

            FeatureItem(Icons.Default.PlayArrow, "Unlimited movies & TV shows")
            FeatureItem(Icons.Default.Tv, "Watch on all your devices")
            FeatureItem(Icons.Default.Download, "Download and watch offline")
            FeatureItem(Icons.Default.Star, "Ad-free experience")
        }

        // ── RIGHT: plans + subscribe ──
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                state.plans.forEachIndexed { index, plan ->
                    TvPlanCard(
                        title = plan.name,
                        subtitle = "${plan.durationDays} days",
                        price = "₹${plan.price}",
                        selected = state.selectedPlan?.id == plan.id,
                        onClick = { onIntent(SubscriptionIntent.PlanSelected(plan)) },
                        modifier = if (index == 0) {
                            Modifier.focusRequester(firstPlanFocus)
                        } else {
                            Modifier
                        }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            TvSubscribeButton(
                text = if (state.isProcessing) {
                    "Processing..."
                } else {
                    state.selectedPlan?.let { "Subscribe to ${it.name}" } ?: "Subscribe"
                },
                enabled = !state.isProcessing,
                onClick = { onIntent(SubscriptionIntent.SubscribeClicked) }
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Demo purchase only. Connect StoreKit 2 before release.",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun TvPlanCard(
    title: String,
    subtitle: String,
    price: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(24.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (focused) Color(0xFF1C1C1C) else Color(0xFF111111))
            .border(
                width = if (focused || selected) 3.dp else 1.dp,
                color = when {
                    focused -> Color.White
                    selected -> Purple
                    else -> Color.DarkGray
                },
                shape = shape
            )
            .onFocusChanged { focused = it.isFocused }
            .clickable(onClick = onClick)
            .padding(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(text = subtitle, color = Color.Gray, fontSize = 16.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                text = price,
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (selected) Purple else Color.Transparent)
                .border(2.dp, Color.LightGray, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun TvSubscribeButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(20.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(shape)
            .background(if (focused) Color(0xFFA78BFA) else Purple)
            .border(3.dp, if (focused) Color.White else Color.Transparent, shape)
            .onFocusChanged { focused = it.isFocused }
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp
        )
    }
}

@Composable
private fun FeatureItem(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier.padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0xFF181818)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(16.dp))
        Text(text = text, color = Color.Gray, fontSize = 20.sp)
    }
}
