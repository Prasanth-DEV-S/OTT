package com.dealshot.app.presentation.mobile.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.res.painterResource
import com.dealshot.app.R
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.dealshot.app.presentation.mobile.login.LoginBackground
import com.dealshot.app.presentation.shared.profile.ProfileIntent
import com.dealshot.app.presentation.shared.profile.ProfileState
import com.dealshot.app.ui.theme.CardBgDark
import com.dealshot.app.ui.theme.CardBorderBottom
import com.dealshot.app.ui.theme.CardBorderTop
import com.dealshot.app.ui.theme.DeepBlackBg
import com.dealshot.app.ui.theme.FieldBg
import com.dealshot.app.ui.theme.FieldBorderUnfocused
import com.dealshot.app.ui.theme.FieldDivider
import com.dealshot.app.ui.theme.FieldPlaceholder
import com.dealshot.app.ui.theme.GoldPrimary
import com.dealshot.app.ui.theme.GoldTextLight
import com.dealshot.app.ui.theme.TextMuted

@Composable
fun ProfileScreen(
    state: ProfileState,
    onIntent: (ProfileIntent) -> Unit
) {
    androidx.activity.compose.BackHandler {
        onIntent(ProfileIntent.BackClicked)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlackBg)
    ) {
        // Dark gold background with bottom waves and particles
        LoginBackground()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.70f))
                .clickable { onIntent(ProfileIntent.BackClicked) },
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .clickable { /* Consume click so clicking card doesn't dismiss */ }
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "ACCOUNT",
                    color = GoldPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.5.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Text(
                    text = "My Profile",
                    color = GoldTextLight,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Manage your profile and account details",
                    color = TextMuted,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Central Profile Popup Card
                ProfilePopupCard(
                    userName = state.username.ifBlank { state.selectedProfile?.name.orEmpty() },
                    mobileNumber = state.phoneNumber,
                    profileImageUrl = state.profileImageUrl ?: state.avatar,
                    onLogout = { onIntent(ProfileIntent.LogoutClicked) },
                    onLogin = { onIntent(ProfileIntent.LogoutClicked) }
                )

                if (state.isLoading) {
                    Spacer(modifier = Modifier.height(20.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = GoldPrimary,
                        strokeWidth = 2.dp
                    )
                }
            }
        }
    }
}

@Composable
fun ProfilePopupCard(
    userName: String,
    mobileNumber: String,
    profileImageUrl: String? = null,
    onLogout: () -> Unit,
    onLogin: () -> Unit = onLogout,
    modifier: Modifier = Modifier
) {
    val isLoggedIn = mobileNumber.isNotBlank() || (userName.isNotBlank() && userName != "User Name" && userName != "Guest")

    val displayName = when {
        userName.isNotBlank() && userName != "User Name" && userName != "Guest" -> userName
        mobileNumber.length >= 4 -> "User ${mobileNumber.takeLast(4)}"
        else -> "Guest User"
    }

    val formattedMobile = if (mobileNumber.isNotBlank()) {
        if (mobileNumber.startsWith("+")) mobileNumber else "+91 $mobileNumber"
    } else {
        ""
    }

    var showLogoutConfirm by remember { mutableStateOf(false) }

    if (showLogoutConfirm) {
        LogoutConfirmDialog(
            onConfirm = {
                showLogoutConfirm = false
                onLogout()
            },
            onDismiss = {
                showLogoutConfirm = false
            }
        )
    }

    Box(
        modifier = modifier
            .widthIn(max = 440.dp)
            .fillMaxWidth()
            .shadow(
                elevation = 24.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = GoldPrimary.copy(alpha = 0.25f),
                ambientColor = GoldPrimary.copy(alpha = 0.15f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(CardBgDark.copy(alpha = 0.95f))
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        CardBorderTop,
                        CardBorderBottom
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // -------------------------------------------------
            // SECTION 1: AVATAR + USER NAME + PHONE
            // -------------------------------------------------
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.size(72.dp)) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .border(2.dp, GoldPrimary, CircleShape)
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF382B17)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!profileImageUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = profileImageUrl,
                                contentDescription = "Profile picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                painter = painterResource(id = R.drawable.person_crop_circle),
                                contentDescription = "User",
                                tint = GoldPrimary,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }

                    // Green online indicator
                    if (isLoggedIn) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = (-2).dp, y = (-2).dp)
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4CD964))
                                .border(2.5.dp, CardBgDark, CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayName,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    if (isLoggedIn && formattedMobile.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.iphone),
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = formattedMobile,
                                color = TextMuted,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        Text(
                            text = "Sign in to watch premium stories",
                            color = TextMuted,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            HorizontalDivider(
                color = FieldDivider,
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // -------------------------------------------------
            // SECTION 2: DETAILS / LOG IN OR LOG OUT ACTIONS
            // -------------------------------------------------
            if (isLoggedIn) {
                Text(
                    text = "ACCOUNT DETAILS",
                    color = FieldPlaceholder,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // ITEM 1: Mobile Number
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(FieldBg)
                        .border(1.dp, FieldBorderUnfocused.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF2B2620)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.iphone),
                                contentDescription = "Mobile Number",
                                tint = GoldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Mobile Number",
                                color = FieldPlaceholder,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = formattedMobile,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF16301C)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.checkmark),
                                contentDescription = "Verified",
                                tint = Color(0xFF4CD964),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                var isLogoutFocused by remember { mutableStateOf(false) }
                val logoutFocusRequester = remember { FocusRequester() }

                LaunchedEffect(Unit) {
                    runCatching { logoutFocusRequester.requestFocus() }
                }

                // ITEM 2: Log out
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(logoutFocusRequester)
                        .onFocusChanged { isLogoutFocused = it.isFocused }
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isLogoutFocused) Color(0xFF382F23) else FieldBg)
                        .border(
                            width = if (isLogoutFocused) 2.dp else 1.dp,
                            color = if (isLogoutFocused) GoldPrimary else CardBorderBottom,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { showLogoutConfirm = true }
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF2B2620)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.rectangle_portrait_and_arrow_right),
                                contentDescription = "Log out",
                                tint = GoldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Log out",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Sign out of your DealShot account",
                                color = TextMuted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }

                        Icon(
                            painter = painterResource(id = R.drawable.chevron_right),
                            contentDescription = "Go",
                            tint = GoldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            } else {
                // NOT LOGGED IN / GUEST STATE
                Text(
                    text = "GET STARTED",
                    color = FieldPlaceholder,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(FieldBg)
                        .border(1.dp, FieldBorderUnfocused.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = "Sign in or register now to unlock unlimited premium videos, save watch history, and sync across devices.",
                        color = TextMuted,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                var isLoginFocused by remember { mutableStateOf(false) }
                val loginFocusRequester = remember { FocusRequester() }

                LaunchedEffect(Unit) {
                    runCatching { loginFocusRequester.requestFocus() }
                }

                // Log In / Register Button
                Button(
                    onClick = onLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .focusRequester(loginFocusRequester)
                        .onFocusChanged { isLoginFocused = it.isFocused },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFFE8C575),
                                        Color(0xFFC9942C),
                                        Color(0xFF9E6B15)
                                    )
                                )
                            )
                            .border(
                                width = if (isLoginFocused) 2.5.dp else 0.dp,
                                color = if (isLoginFocused) Color.White else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Log In / Register",
                                color = Color(0xFF140E02),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                painter = painterResource(id = R.drawable.arrow_right),
                                contentDescription = null,
                                tint = Color(0xFF140E02),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LogoutConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val confirmFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        runCatching { confirmFocusRequester.requestFocus() }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 380.dp)
                .fillMaxWidth()
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = GoldPrimary.copy(alpha = 0.25f),
                    ambientColor = GoldPrimary.copy(alpha = 0.20f)
                )
                .clip(RoundedCornerShape(20.dp))
                .background(CardBgDark)
                .border(
                    width = 1.dp,
                    color = GoldPrimary,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 24.dp, vertical = 28.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Gold Circle with Logout Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .border(2.dp, GoldPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.rectangle_portrait_and_arrow_right),
                        contentDescription = "Log out",
                        tint = GoldPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Title: Log Out
                Text(
                    text = "Log Out",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.3.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Subtitle
                Text(
                    text = "Are you sure you want to log out of your DealShot account?",
                    color = TextMuted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel Button
                    var isCancelFocused by remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isCancelFocused) Color(0xFF382F23) else Color(0xFF231F1A))
                            .border(
                                width = if (isCancelFocused) 2.dp else 1.dp,
                                color = if (isCancelFocused) GoldPrimary else Color(0xFF383025),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .onFocusChanged { isCancelFocused = it.isFocused }
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Cancel",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Log Out Button
                    var isConfirmFocused by remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .focusRequester(confirmFocusRequester)
                            .onFocusChanged { isConfirmFocused = it.isFocused }
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFFE8C575),
                                        Color(0xFFC9942C),
                                        Color(0xFF9E6B15)
                                    )
                                )
                            )
                            .border(
                                width = if (isConfirmFocused) 2.5.dp else 0.dp,
                                color = if (isConfirmFocused) Color.White else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onConfirm() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Log Out",
                            color = Color(0xFF140E02),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
