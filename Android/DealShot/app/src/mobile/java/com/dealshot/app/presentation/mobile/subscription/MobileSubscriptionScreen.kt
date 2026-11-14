package com.dealshot.app.presentation.mobile.subscription

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.dealshot.app.R
import com.dealshot.app.data.subscription.SubscriptionPlan
import com.dealshot.app.data.subscription.hasPremiumAccess
import com.dealshot.app.presentation.shared.subscription.SubscriptionIntent
import com.dealshot.app.presentation.shared.subscription.SubscriptionState
import java.text.NumberFormat
import java.util.Locale

// ---------- Theme colors ----------
private val BgDark = Color(0xFF0B0812)
private val CardBg = Color(0xFF120E1A)
private val CardBgSelected = Color(0xFF16111F)
private val Gold = Color(0xFFF5C85A)
private val GoldDark = Color(0xFFD4A030)
private val PurpleBadge = Color(0xFF7B5EA7)
private val TextMuted = Color(0xFFB8B2C4)
private val BorderMuted = Color(0xFF2A2433)
private val GreenActive = Color(0xFF4CAF50)

// ---------- Per-plan UI config ----------
private data class PlanUi(
    val description: String,
    val badge: String?,
    val badgeColor: Color,
    val badgeTextColor: Color,
    val hd: Boolean,
    val uhd: Boolean,
    val watchOnLaptopAndTV: Boolean,
    val watchOnMobileAndTablet: Boolean,
    val screens: Int
)

private fun SubscriptionPlan.toUi(): PlanUi = when (name.trim().lowercase()) {
    "basic" -> PlanUi(
        description = description.ifBlank { "Enjoy entertainment every month" },
        badge = null,
        badgeColor = Gold,
        badgeTextColor = Color.Black,
        hd = hdAvailable,
        uhd = hdr4kAvailable,
        watchOnLaptopAndTV = watchOnLaptopAndTV,
        watchOnMobileAndTablet = watchOnMobileAndTablet,
        screens = simultaneousStreams
    )
    "standard" -> PlanUi(
        description = description.ifBlank { "Enjoy HD entertainment on two screens" },
        badge = "Best Value",
        badgeColor = Gold,
        badgeTextColor = Color.Black,
        hd = hdAvailable,
        uhd = hdr4kAvailable,
        watchOnLaptopAndTV = watchOnLaptopAndTV,
        watchOnMobileAndTablet = watchOnMobileAndTablet,
        screens = simultaneousStreams
    )
    else -> PlanUi(
        description = description.ifBlank { "Your favourite entertainment all year" },
        badge = "Popular",
        badgeColor = PurpleBadge,
        badgeTextColor = Color.White,
        hd = hdAvailable,
        uhd = hdr4kAvailable,
        watchOnLaptopAndTV = watchOnLaptopAndTV,
        watchOnMobileAndTablet = watchOnMobileAndTablet,
        screens = simultaneousStreams
    )
}

private fun SubscriptionPlan.periodText(): String =
    if (durationDays >= 365) "/year" else "/$durationDays days"

private fun formatPrice(price: Number, currency: String = "INR"): String {
    val symbol = if (currency.equals("INR", ignoreCase = true)) "₹" else "$currency "
    return symbol + NumberFormat.getNumberInstance(Locale("en", "IN")).format(price)
}

private fun formatDateString(dateIso: String?): String {
    if (dateIso.isNullOrBlank()) return "N/A"
    return try {
        dateIso.take(10)
    } catch (_: Exception) {
        dateIso
    }
}

// ---------- Screen ----------
@Composable
fun MobileSubscriptionScreen(
    state: SubscriptionState,
    onIntent: (SubscriptionIntent) -> Unit
) {
    if (state.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BgDark),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Gold)
        }
        return
    }

    val isUserSubscribed = hasPremiumAccess(state.userSubscription)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF1A1226), BgDark, Color.Black)
                    )
                )
                .statusBarsPadding()
        ) {

            // ---------- Scrollable content ----------
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {

                Spacer(Modifier.height(20.dp))

                // Error banner with retry if API failed
                if (state.error != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF381F19))
                            .border(1.dp, Color(0xFFFF5252).copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = state.error,
                            color = Color.White,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { onIntent(SubscriptionIntent.RetryLoadClicked) }) {
                            Text(text = "Retry", color = Gold, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // Active Subscription Card (Case 2: Active subscriber)
                if (isUserSubscribed) {
                    ActiveSubscriptionHeaderCard(
                        userSub = state.userSubscription,
                        activePlan = state.activePlanDetails ?: state.selectedPlan
                    )
                    Spacer(Modifier.height(24.dp))
                }

                Text(
                    text = "PREMIUM MEMBERSHIP",
                    color = Gold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = if (isUserSubscribed) "Your current membership & plans" else "Choose the plan that's right for you",
                    color = Color.White,
                    fontSize = 26.sp,
                    lineHeight = 32.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(Modifier.height(14.dp))

                HeaderBullet("Unlimited movies and TV shows, watch anytime.")
                Spacer(Modifier.height(8.dp))
                HeaderBullet("Change or cancel your plan anytime.")

                Spacer(Modifier.height(20.dp))

                // Logo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DealShotLogo(modifier = Modifier.weight(1f))
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CardBg)
                        .border(1.dp, BorderMuted, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🎁", fontSize = 28.sp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Watch without limits",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isUserSubscribed) "Manage or upgrade your plan" else "Unlock your premium experience",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(Modifier.height(28.dp))

                // Plans list
                state.plans.forEach { plan ->
                    PlanCard(
                        plan = plan,
                        selected = state.selectedPlan?.id == plan.id,
                        onClick = {
                            onIntent(SubscriptionIntent.PlanSelected(plan))
                        }
                    )
                    Spacer(Modifier.height(24.dp))
                }

                Spacer(Modifier.height(4.dp))
            }

            // ---------- Fixed bottom bar ----------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(BorderMuted)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgDark)
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Button(
                    onClick = { onIntent(SubscriptionIntent.SubscribeClicked) },
                    enabled = !state.isProcessing && state.selectedPlan != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.horizontalGradient(listOf(GoldDark, Gold))
                        ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        disabledContainerColor = Color.Gray.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = when {
                            state.isProcessing -> "Processing..."
                            state.selectedPlan != null ->
                                "Subscribe to ${state.selectedPlan.name}"
                            else -> "Subscribe"
                        },
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    if (!state.isProcessing) {
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    text = "Secure subscription · Cancel anytime",
                    color = TextMuted,
                    fontSize = 11.sp
                )

                Spacer(Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onIntent(SubscriptionIntent.BackClicked) }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.chevron_left),
                        contentDescription = null,
                        tint = Gold,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Back to Home",
                        color = Gold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Case 1: No active subscription popup overlay
        if (state.showNoSubscriptionPopup && !isUserSubscribed) {
            NoSubscriptionPopup(
                featuredPlan = state.featuredPlanDetails ?: state.plans.firstOrNull(),
                onUpgradeClick = {
                    onIntent(SubscriptionIntent.UpgradePlanClicked)
                },
                onDismiss = {
                    onIntent(SubscriptionIntent.DismissNoSubscriptionPopup)
                }
            )
        }
    }
}

// ---------- Active Subscription Card (Case 2) ----------
@Composable
private fun ActiveSubscriptionHeaderCard(
    userSub: com.dealshot.app.data.subscription.SubscriptionState,
    activePlan: SubscriptionPlan?
) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF1F182C), Color(0xFF140F1E))
                )
            )
            .border(1.5.dp, Gold.copy(alpha = 0.6f), shape)
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "YOUR ACTIVE SUBSCRIPTION",
                color = Gold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(GreenActive.copy(alpha = 0.2f))
                    .border(1.dp, GreenActive, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "ACTIVE",
                    color = GreenActive,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = activePlan?.name ?: userSub.planName ?: "Premium Subscription",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold
        )

        if (activePlan != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = activePlan.description,
                color = TextMuted,
                fontSize = 13.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "${formatPrice(activePlan.price, activePlan.currency)} / ${activePlan.durationDays} days",
                color = Gold,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(14.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(BorderMuted)
        )
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = "Start Date", color = TextMuted, fontSize = 11.sp)
                Text(
                    text = formatDateString(userSub.startDate),
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "End Date", color = TextMuted, fontSize = 11.sp)
                Text(
                    text = formatDateString(userSub.endDate),
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ---------- Popup (Case 1: No Active Subscription) ----------
@Composable
private fun NoSubscriptionPopup(
    featuredPlan: SubscriptionPlan?,
    onUpgradeClick: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            val shape = RoundedCornerShape(24.dp)
            Column(
                modifier = Modifier
                    .widthIn(max = 380.dp)
                    .fillMaxWidth(0.9f)
                    .clip(shape)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1E172B), Color(0xFF120E1A))
                        )
                    )
                    .border(1.5.dp, Gold.copy(alpha = 0.6f), shape)
                    .clickable(enabled = false) {}
                    .padding(22.dp)
            ) {

                // Top bar: Close X icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Gold.copy(alpha = 0.15f))
                            .border(1.dp, Gold.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "👑", fontSize = 18.sp)
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                Text(
                    text = "No Active Subscription",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "You are currently using the free plan. Upgrade your subscription to enjoy premium benefits.",
                    color = TextMuted,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Spacer(Modifier.height(18.dp))

                // Dynamic Plan Details Card from API
                if (featuredPlan != null) {
                    val ui = featuredPlan.toUi()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF181324))
                            .border(1.dp, BorderMuted, RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = featuredPlan.name,
                                color = Gold,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${formatPrice(featuredPlan.price, featuredPlan.currency)} / ${featuredPlan.durationDays} days",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        if (featuredPlan.description.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = featuredPlan.description,
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(BorderMuted)
                        )
                        Spacer(Modifier.height(8.dp))

                        PopupFeatureItem("HD Resolution", enabled = ui.hd)
                        PopupFeatureItem("4K + HDR Quality", enabled = ui.uhd)
                        PopupFeatureItem("Watch on Laptop & TV", enabled = ui.watchOnLaptopAndTV)
                        PopupFeatureItem("Watch on Phone & Tablet", enabled = ui.watchOnMobileAndTablet)
                        PopupFeatureItem("Simultaneous Streams", value = "${featuredPlan.simultaneousStreams} screen")
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Upgrade Plan Button
                Button(
                    onClick = onUpgradeClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.horizontalGradient(listOf(GoldDark, Gold))
                        ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Upgrade Plan",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(Modifier.height(8.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Maybe Later", color = TextMuted, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun PopupFeatureItem(
    label: String,
    enabled: Boolean = true,
    value: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = if (enabled) Icons.Default.Check else Icons.Default.Close,
                contentDescription = null,
                tint = if (enabled) Gold else Color.Gray,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = label,
                color = Color.White,
                fontSize = 12.sp
            )
        }
        if (value != null) {
            Text(
                text = value,
                color = Gold,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ---------- Header pieces ----------
@Composable
private fun HeaderBullet(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = null,
            tint = Gold,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(text = text, color = TextMuted, fontSize = 13.sp)
    }
}

@Composable
private fun DealShotLogo(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.play_fill),
            contentDescription = null,
            tint = Gold,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = Color.White)) { append("DEAL") }
                withStyle(SpanStyle(color = GoldDark)) { append("SHOT") }
                withStyle(SpanStyle(color = Gold)) { append(" +") }
            },
            fontSize = 26.sp,
            fontWeight = FontWeight.Black
        )
    }
}

// ---------- Plan card ----------
@Composable
private fun PlanCard(
    plan: SubscriptionPlan,
    selected: Boolean,
    onClick: () -> Unit
) {
    val ui = plan.toUi()
    val shape = RoundedCornerShape(20.dp)

    Box(modifier = Modifier.fillMaxWidth()) {

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .clickable { onClick() },
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = if (selected) CardBgSelected else CardBg
            ),
            border = BorderStroke(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) Gold else BorderMuted
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                // Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = plan.name,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Price + radio
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = formatPrice(plan.price, plan.currency),
                            color = Gold,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = plan.periodText(),
                            color = TextMuted,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 5.dp)
                        )
                    }
                    RadioDot(selected = selected)
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = ui.description,
                    color = TextMuted,
                    fontSize = 12.sp
                )

                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(BorderMuted)
                )
                Spacer(Modifier.height(8.dp))

                // Features
                FeatureRow(R.drawable.play_fill, "HD available", enabled = ui.hd)
                FeatureRow(R.drawable.crown_fill, "4K + HDR available", enabled = ui.uhd)
                FeatureRow(R.drawable.play_fill, "Watch on your laptop and TV", enabled = plan.watchOnLaptopAndTV)
                FeatureRow(R.drawable.iphone, "Watch on your mobile phone and tablet", enabled = plan.watchOnMobileAndTablet)
                FeatureRow(
                    R.drawable.arrow_right,
                    "Screens you can watch on at the same time",
                    value = plan.simultaneousStreams.toString()
                )

                Spacer(Modifier.height(16.dp))

                // Choose / Selected button
                if (selected) {
                    Button(
                        onClick = onClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Gold),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Selected Plan",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = onClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        border = BorderStroke(1.dp, GoldDark.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Choose Plan",
                            color = Gold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // Badge (Best Value / Popular) overlapping the top-right corner
        if (ui.badge != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-16).dp, y = (-10).dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(ui.badgeColor)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = ui.badge,
                    color = ui.badgeTextColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun RadioDot(selected: Boolean, size: Dp = 24.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(if (selected) Gold else Color.Transparent)
            .border(1.5.dp, if (selected) Gold else Color.Gray, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

// ---------- Feature row ----------
@Composable
private fun FeatureRow(
    iconResId: Int,
    label: String,
    enabled: Boolean = true,
    value: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            tint = Gold,
            modifier = Modifier.size(14.dp)
        )

        Spacer(Modifier.width(12.dp))

        Text(
            text = label,
            color = Color.White,
            fontSize = 13.sp,
            modifier = Modifier.weight(1f)
        )

        Spacer(Modifier.width(8.dp))

        if (value != null) {
            Text(
                text = value,
                color = Gold,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        } else {
            StatusIcon(enabled)
        }
    }
}

@Composable
private fun StatusIcon(enabled: Boolean) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(if (enabled) Gold else Color.Transparent)
            .then(
                if (enabled) Modifier
                else Modifier.border(1.dp, Color.Gray, CircleShape)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (enabled) Icons.Filled.Check else Icons.Filled.Close,
            contentDescription = null,
            tint = if (enabled) Color.Black else Color.Gray,
            modifier = Modifier.size(if (enabled) 14.dp else 11.dp)
        )
    }
}
