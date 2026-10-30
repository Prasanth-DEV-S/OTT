package com.dealshot.app.presentation.mobile.subscription

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import com.dealshot.app.R
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dealshot.app.data.subscription.SubscriptionPlan
import com.dealshot.app.presentation.shared.subscription.SubscriptionIntent
import com.dealshot.app.presentation.shared.subscription.SubscriptionState

@Composable
fun MobileSubscriptionScreen(
    state: SubscriptionState,
    onIntent: (SubscriptionIntent) -> Unit
) {
    if (state.isLoading) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {

            CircularProgressIndicator(
                color = Color(0xFF8B5CF6)
            )
        }

    } else {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black,
                            Color(0xFF080808),
                            Color.Black
                        )
                    )
                )
        )
        {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                // TOP BAR
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    IconButton(
                        onClick = {
                            onIntent(
                                SubscriptionIntent.BackClicked
                            )
                        }
                    ) {

                        Icon(
                            painter = painterResource(id = R.drawable.chevron_left),
                            contentDescription = null,
                            tint = Color(0xFF8B5CF6)
                        )
                    }

                    Spacer(
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = "Subscription",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                // CROWN
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = "👑",
                        fontSize = 60.sp
                    )
                }

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                Text(
                    text = "Watch without limits",
                    color = Color.White,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(
                        Alignment.CenterHorizontally
                    )
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "Choose the plan that works for you.",
                    color = Color.Gray,
                    fontSize = 13.sp,
                    modifier = Modifier.align(
                        Alignment.CenterHorizontally
                    )
                )

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                state.plans.forEach { plan ->

                    SubscriptionCard(
                        title = plan.name,
                        subtitle = "${plan.durationDays} days",
                        price = "₹${plan.price}",
                        selected = state.selectedPlan?.id == plan.id,
                        saveText = null,
                        onClick = {

                            onIntent(
                                SubscriptionIntent.PlanSelected(plan)
                            )
                        }
                    )

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )
                }

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                FeatureItem(
                    R.drawable.play_fill,
                    "Unlimited movies & TV shows"
                )

                FeatureItem(
                    R.drawable.iphone,
                    "Watch on all your devices"
                )

                FeatureItem(
                    R.drawable.arrow_right,
                    "Download and watch offline"
                )

                FeatureItem(
                    R.drawable.crown_fill,
                    "Ad-free experience"
                )

                Spacer(
                    modifier = Modifier.weight(1f)
                )

                // SUBSCRIBE BUTTON
                Button(
                    onClick = {

                        onIntent(
                            SubscriptionIntent.SubscribeClicked
                        )
                    },
                    enabled = !state.isProcessing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8B5CF6)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {

                    Text(
                        text = if (state.isProcessing) {
                            "Processing..."
                        } else {
                            state.selectedPlan?.let {
                                "Subscribe to ${it.name}"
                            } ?: "Subscribe"
                        },
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Text(
                    text =
                        "Demo purchase only. Connect StoreKit 2 before release.",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.align(
                        Alignment.CenterHorizontally
                    )
                )

                Spacer(
                    modifier = Modifier.height(20.dp)
                )
            }
        }
    }
}

@Composable
fun SubscriptionCard(
    title: String,
    subtitle: String,
    price: String,
    selected: Boolean,
    saveText: String?,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) {
                    Color(0xFF8B5CF6)
                } else {
                    Color.DarkGray
                },
                shape = RoundedCornerShape(24.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF111111)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.Top
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Text(
                        title,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (saveText != null) {

                        Spacer(
                            modifier = Modifier.width(8.dp)
                        )

                        Box(
                            modifier = Modifier
                                .clip(
                                    RoundedCornerShape(50)
                                )
                                .background(
                                    Color(0xFF1F5F3B)
                                )
                                .padding(
                                    horizontal = 12.dp,
                                    vertical = 4.dp
                                )
                        ) {

                            Text(
                                saveText,
                                color = Color(0xFF6FFF9C),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    subtitle,
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    price,
                    color = Color.White,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        if (selected) {
                            Color(0xFF8B5CF6)
                        } else {
                            Color.Transparent
                        }
                    )
                    .border(
                        2.dp,
                        Color.LightGray,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {

                if (selected) {

                    Icon(
                        painter = painterResource(id = R.drawable.checkmark),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FeatureItem(
    iconResId: Int,
    text: String
) {

    Row(
        modifier = Modifier.padding(
            vertical = 10.dp
        ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(
                    Color(0xFF181818)
                ),
            contentAlignment =
                Alignment.Center
        ) {

            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(14.dp)
            )
        }

        Spacer(
            modifier = Modifier.width(16.dp)
        )

        Text(
            text = text,
            color = Color.Gray,
            fontSize = 18.sp
        )
    }
}