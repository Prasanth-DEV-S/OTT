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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
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

    val editFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { editFocus.requestFocus() } }

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
                                        imageVector = Icons.Default.PhoneIphone,
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
                        icon = if (state.isEditingProfile) Icons.Default.Close else Icons.Default.Edit,
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
                            icon = Icons.Default.Save,
                            enabled = !state.isSaving,
                            showSpinner = state.isSaving,
                            onClick = { onIntent(ProfileIntent.SaveProfileClicked) }
                        )
                    }

                    TvActionButton(
                        text = "Logout",
                        icon = Icons.AutoMirrored.Filled.Logout,
                        onClick = { onIntent(ProfileIntent.LogoutClicked) }
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
                        icon = Icons.Default.PhoneIphone,
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
                        IconTile(Icons.Default.Email)
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
                        IconTile(Icons.Default.Language)
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
                                icon = Icons.Default.Refresh,
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
                    imageVector = Icons.Default.Person,
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
    icon: ImageVector,
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
                imageVector = icon,
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
    icon: ImageVector,
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
            IconTile(icon)
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
private fun IconTile(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(IconTileBg)
            .border(1.dp, Color(0xFF222222), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
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
            imageVector = Icons.Default.Check,
            contentDescription = "Verified",
            tint = OnlineGreen,
            modifier = Modifier.size(20.dp)
        )
    }
}