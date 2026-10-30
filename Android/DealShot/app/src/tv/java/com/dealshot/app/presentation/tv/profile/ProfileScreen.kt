package com.dealshot.app.presentation.tv.profile

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.res.painterResource
import com.dealshot.app.R
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.dealshot.app.ui.theme.CardBgDark
import com.dealshot.app.ui.theme.GoldPrimary
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dealshot.app.presentation.shared.profile.ProfileIntent
import com.dealshot.app.presentation.shared.profile.ProfileState
import com.dealshot.app.ui.theme.orange

// Same palette as the mobile ProfileScreen.
private val ScreenBg = Color(0xFF080808)
private val CardBg = Color(0xFF0F0F0F)
private val CardBorder = Color(0xFF3D341C)
private val Gold = Color(0xFFE9D7A0)
private val GoldDeep = Color(0xFFD8B64A)
private val GoldBorder = Color(0xFF5C4D24)
private val AvatarInner = Color(0xFF5B4421)
private val Divider = Color(0xFF242424)
private val IconTileBg = Color(0xFF1A1A1A)
private val ButtonWarmBg = Color(0xFF1B1812)
private val TextSecondary = Color(0xFF9A9A9A)
private val TextMuted = Color(0xFF6E6E6E)
private val OnlineGreen = Color(0xFF4CD964)
private val VerifiedBg = Color(0xFF16301C)
private val FocusOrange = Color(0xFFFF9800)

/**
 * TV profile: landscape two-column layout.
 * Left: avatar, name, phone and actions. Right: account details.
 * OK on a text field opens the on-screen keyboard.
 */
@Composable
fun TvProfileScreen(
    state: ProfileState,
    onIntent: (ProfileIntent) -> Unit,
    email: String? = null,
    language: String = "English"
) {
    val displayName = state.username
        .ifBlank { state.selectedProfile?.name.orEmpty() }
        .ifBlank { "Guest" }

    BackHandler { onIntent(ProfileIntent.BackClicked) }

    var showLogoutConfirm by remember { mutableStateOf(false) }
    val editFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { editFocus.requestFocus() } }

    if (showLogoutConfirm) {
        TvLogoutConfirmDialog(
            onConfirm = {
                showLogoutConfirm = false
                onIntent(ProfileIntent.LogoutClicked)
            },
            onDismiss = {
                showLogoutConfirm = false
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
            .padding(horizontal = 56.dp, vertical = 32.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "ACCOUNT",
                color = GoldDeep,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 3.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "My Profile",
                color = Gold,
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Manage your profile and account details",
                color = TextSecondary,
                fontSize = 18.sp
            )

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // ── LEFT: identity + actions ──
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(32.dp))
                        .background(CardBg)
                        .border(1.dp, CardBorder, RoundedCornerShape(32.dp))
                        .padding(28.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TvAvatar(
                            imageUrl = state.profileImageUrl,
                            editable = state.isEditingProfile,
                            onClick = { onIntent(ProfileIntent.ChangeProfilePictureClicked) }
                        )
                        Spacer(Modifier.width(20.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            if (state.isEditingProfile) {
                                TvTextField(
                                    value = state.username,
                                    placeholder = "Guest",
                                    fontSize = 28.sp,
                                    bold = true,
                                    onValueChange = { onIntent(ProfileIntent.UsernameChanged(it)) }
                                )
                            } else {
                                Text(
                                    text = displayName,
                                    color = Color.White,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                            if (state.phoneNumber.isNotBlank()) {
                                Spacer(Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.iphone),
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = state.phoneNumber,
                                        color = TextSecondary,
                                        fontSize = 18.sp
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = Divider)

                    TvActionButton(
                        text = if (state.isEditingProfile) "Cancel Editing" else "Edit Profile",
                        iconResId = if (state.isEditingProfile) R.drawable.xmark else R.drawable.pencil,
                        onClick = {
                            if (state.isEditingProfile) {
                                onIntent(ProfileIntent.CancelEditClicked)
                            } else {
                                onIntent(ProfileIntent.EditProfileClicked)
                            }
                        },
                        modifier = Modifier.focusRequester(editFocus)
                    )

                    if (state.isEditingProfile) {
                        TvActionButton(
                            text = "Save Profile",
                            iconResId = R.drawable.ic_save,
                            enabled = !state.isSaving,
                            showSpinner = state.isSaving,
                            onClick = { onIntent(ProfileIntent.SaveProfileClicked) }
                        )
                    }

                    TvActionButton(
                        text = "Logout",
                        iconResId = R.drawable.rectangle_portrait_and_arrow_right,
                        onClick = { showLogoutConfirm = true }
                    )
                }

                // ── RIGHT: account details ──
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(32.dp))
                        .background(CardBg)
                        .border(1.dp, CardBorder, RoundedCornerShape(32.dp))
                        .padding(28.dp)
                ) {
                    Text(
                        text = "ACCOUNT DETAILS",
                        color = TextMuted,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 2.5.sp
                    )
                    Spacer(Modifier.height(8.dp))

                    DetailRow(
                        iconResId = R.drawable.iphone,
                        label = "Mobile Number",
                        value = state.phoneNumber.ifBlank { "Not available" },
                        trailing = {
                            if (state.phoneNumber.isNotBlank()) VerifiedBadge()
                        }
                    )

                    val displayEmail = state.email.ifBlank { email.orEmpty() }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconTile(R.drawable.envelope)
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Email", color = TextMuted, fontSize = 15.sp)
                            Spacer(Modifier.height(4.dp))
                            if (state.isEditingProfile) {
                                TvTextField(
                                    value = state.email,
                                    placeholder = "Enter email address",
                                    fontSize = 18.sp,
                                    bold = false,
                                    keyboardType = KeyboardType.Email,
                                    onValueChange = { onIntent(ProfileIntent.EmailChanged(it)) }
                                )
                            } else {
                                Text(
                                    text = displayEmail.ifBlank { "Not available" },
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = Divider)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconTile(R.drawable.globe)
                        Spacer(Modifier.width(16.dp))
                        Text(
                            text = "Language",
                            color = TextMuted,
                            fontSize = 16.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = language,
                            color = orange,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (state.isLoading) {
                        Spacer(Modifier.height(16.dp))
                        CircularProgressIndicator(color = orange)
                    }

                    state.error?.let { message ->
                        Spacer(Modifier.height(16.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFFF4D4D).copy(alpha = 0.12f))
                                .border(
                                    1.dp,
                                    Color(0xFFFF4D4D).copy(alpha = 0.4f),
                                    RoundedCornerShape(16.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = message,
                                color = Color(0xFFFF6B6B),
                                fontSize = 16.sp,
                                modifier = Modifier.weight(1f)
                            )
                            TvActionButton(
                                text = "Retry",
                                iconResId = R.drawable.arrow_right,
                                fillWidth = false,
                                onClick = { onIntent(ProfileIntent.RetryClicked) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TvAvatar(
    imageUrl: String?,
    editable: Boolean,
    onClick: () -> Unit
) {
    var focused by remember { mutableStateOf(false) }

    Box(modifier = Modifier.size(96.dp)) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(if (focused) FocusOrange else Gold)
                .padding(5.dp)
                .clip(CircleShape)
                .background(AvatarInner)
                .then(
                    if (editable) {
                        Modifier
                            .onFocusChanged { focused = it.isFocused }
                            .clickable(onClick = onClick)
                    } else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            if (imageUrl.isNullOrBlank()) {
                Icon(
                    painter = painterResource(id = R.drawable.person_crop_circle),
                    contentDescription = "Profile picture",
                    tint = Gold,
                    modifier = Modifier.size(58.dp)
                )
            } else {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Profile picture",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 2.dp, y = 2.dp)
                .size(20.dp)
                .clip(CircleShape)
                .background(OnlineGreen)
                .border(3.dp, CardBg, CircleShape)
        )
    }
}

@Composable
private fun TvTextField(
    value: String,
    placeholder: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    bold: Boolean,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    var focused by remember { mutableStateOf(false) }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(
            color = Color.White,
            fontSize = fontSize,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
        ),
        cursorBrush = SolidColor(GoldDeep),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { focused = it.isFocused },
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .border(
                        2.dp,
                        if (focused) FocusOrange else GoldDeep.copy(alpha = 0.5f),
                        RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                if (value.isEmpty()) {
                    Text(text = placeholder, color = TextMuted, fontSize = fontSize)
                }
                innerTextField()
            }
        }
    )
}

@Composable
private fun TvActionButton(
    text: String,
    iconResId: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    showSpinner: Boolean = false,
    fillWidth: Boolean = true
) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(18.dp)
    val contentColor = if (focused) Color.Black else Color.White

    Row(
        modifier = modifier
            .then(if (fillWidth) Modifier.fillMaxWidth() else Modifier)
            .height(58.dp)
            .clip(shape)
            .background(if (focused) FocusOrange else ButtonWarmBg)
            .border(1.dp, if (focused) Color.White else GoldBorder, shape)
            .onFocusChanged { focused = it.isFocused }
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (showSpinner) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = contentColor,
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = text,
            color = contentColor,
            fontSize = 19.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun DetailRow(
    iconResId: Int,
    label: String,
    value: String,
    trailing: @Composable (() -> Unit)? = null
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconTile(iconResId)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = label, color = TextMuted, fontSize = 15.sp)
                Spacer(Modifier.height(2.dp))
                Text(
                    text = value,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            trailing?.invoke()
        }
        HorizontalDivider(color = Divider)
    }
}

@Composable
private fun IconTile(iconResId: Int) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(IconTileBg)
            .border(1.dp, Color(0xFF222222), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            tint = Gold,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun VerifiedBadge() {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(VerifiedBg),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.checkmark),
            contentDescription = "Verified",
            tint = OnlineGreen,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun TvLogoutConfirmDialog(
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
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = GoldPrimary.copy(alpha = 0.35f),
                    ambientColor = GoldPrimary.copy(alpha = 0.20f)
                )
                .clip(RoundedCornerShape(20.dp))
                .background(CardBgDark)
                .border(
                    width = 1.dp,
                    color = GoldPrimary,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 28.dp, vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
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

                Text(
                    text = "Log Out",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.3.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Are you sure you want to log out of your DealShot account?",
                    color = TextMuted,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    var isCancelFocused by remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isCancelFocused) Color(0xFF7A1515) else Color(0xFF231F1A))
                            .border(
                                width = if (isCancelFocused) 2.5.dp else 1.dp,
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
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    var isConfirmFocused by remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .focusRequester(confirmFocusRequester)
                            .onFocusChanged { isConfirmFocused = it.isFocused }
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isConfirmFocused) {
                                    Brush.horizontalGradient(listOf(Color(0xFF7A1515), Color(0xFF3B0B0B)))
                                } else {
                                    Brush.horizontalGradient(listOf(Color(0xFFE8C575), Color(0xFFC9942C), Color(0xFF9E6B15)))
                                }
                            )
                            .border(
                                width = if (isConfirmFocused) 2.5.dp else 0.dp,
                                color = if (isConfirmFocused) GoldPrimary else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onConfirm() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Log Out",
                            color = if (isConfirmFocused) Color.White else Color(0xFF140E02),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}