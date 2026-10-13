package com.dealshot.app.presentation.mobile.profile

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dealshot.app.presentation.shared.profile.ProfileIntent
import com.dealshot.app.presentation.shared.profile.ProfileState
import com.dealshot.app.ui.theme.orange

import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions

// ───────────── Design tokens (matched to the iOS "My Profile" screen) ─────────────
private val ScreenBg = Color(0xFF080808)
private val CardBg = Color(0xFF0F0F0F)
private val CardBorder = Color(0xFF3D341C)          // dim gold hairline around the card
private val Gold = Color(0xFFE9D7A0)                // titles, avatar ring, icons
private val GoldDeep = Color(0xFFD8B64A)            // "ACCOUNT" eyebrow, underline
private val GoldBorder = Color(0xFF5C4D24)          // button borders
private val AvatarInner = Color(0xFF5B4421)         // dark brown inside avatar ring
private val Divider = Color(0xFF242424)
private val IconTile = Color(0xFF1A1A1A)
private val ButtonWarmBg = Color(0xFF1B1812)
private val TextSecondary = Color(0xFF9A9A9A)
private val TextMuted = Color(0xFF6E6E6E)
private val OnlineGreen = Color(0xFF4CD964)
private val VerifiedBg = Color(0xFF16301C)

@Composable
fun ProfileScreen(
    state: ProfileState,
    onIntent: (ProfileIntent) -> Unit,
    email: String? = null,                 // null/blank -> "Not available"
    language: String = "English",
    onLanguageClick: () -> Unit = {}
) {
    val displayName = state.username
        .ifBlank { state.selectedProfile?.name.orEmpty() }
        .ifBlank { "Guest" }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Top bar: back button + eyebrow ──
            Box(modifier = Modifier.fillMaxWidth().height(48.dp)) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1C1C1C))
                        .clickable { onIntent(ProfileIntent.BackClicked) }
                        .align(Alignment.CenterStart),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Text(
                    text = "ACCOUNT",
                    color = GoldDeep,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 3.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Title block: left-aligned and sized like the Home screen headings
            Text(
                text = "My Profile",
                color = Gold,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Manage your profile and account details",
                color = TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            // ── Main card ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(CardBg)
                    .border(1.dp, CardBorder, RoundedCornerShape(32.dp))
                    .padding(horizontal = 20.dp, vertical = 22.dp)
            ) {
                // Avatar + name + phone
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProfileAvatar(
                        imageUrl = state.profileImageUrl,
                        editable = state.isEditingProfile,
                        onClick = { onIntent(ProfileIntent.ChangeProfilePictureClicked) }
                    )

                    Spacer(Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        if (state.isEditingProfile) {
                            BasicTextField(
                                value = state.username,
                                onValueChange = { onIntent(ProfileIntent.UsernameChanged(it)) },
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                cursorBrush = SolidColor(GoldDeep),
                                modifier = Modifier.fillMaxWidth(),
                                decorationBox = { innerTextField ->
                                    Box {
                                        if (state.username.isEmpty()) {
                                            Text(
                                                text = "Guest",
                                                color = TextMuted,
                                                fontSize = 22.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                            Spacer(Modifier.height(6.dp))
                            Box(
                                Modifier
                                    .fillMaxWidth(0.92f)
                                    .height(1.dp)
                                    .background(GoldDeep.copy(alpha = 0.6f))
                            )
                        } else {
                            Text(
                                text = displayName,
                                color = Color.White,
                                fontSize = 22.sp,
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
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = state.phoneNumber,
                                    color = TextSecondary,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))
                HorizontalDivider(color = Divider)
                Spacer(Modifier.height(18.dp))

                // Edit / Cancel button
                OutlinedActionButton(
                    text = if (state.isEditingProfile) "Cancel Editing" else "Edit Profile",
                    icon = if (state.isEditingProfile) Icons.Default.Close else Icons.Default.Edit,
                    onClick = {
                        onIntent(
                            (if (state.isEditingProfile) {
                                ProfileIntent.CancelEditClicked}
                            else
                                (ProfileIntent.EditProfileClicked)) as ProfileIntent

                        )
                    }
                )

                Spacer(Modifier.height(22.dp))

                Text(
                    text = "ACCOUNT DETAILS",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 2.5.sp
                )

                Spacer(Modifier.height(10.dp))

                // Mobile number (verified)
                DetailRow(
                    icon = Icons.Default.PhoneIphone,
                    label = "Mobile Number",
                    value = state.phoneNumber.ifBlank { "Not available" },
                    trailing = {
                        if (state.phoneNumber.isNotBlank()) VerifiedBadge()
                    }
                )

                // Email
                // Inside ProfileScreen(), replace the current Email section:
                val displayEmail = state.email.ifBlank { email.orEmpty() }

                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconTile(Icons.Default.Email)
                        Spacer(Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Email",
                                color = TextMuted,
                                fontSize = 12.sp
                            )

                            Spacer(Modifier.height(4.dp))

                            if (state.isEditingProfile) {
                                BasicTextField(
                                    value = state.email,
                                    onValueChange = {
                                        onIntent(ProfileIntent.EmailChanged(it))
                                    },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        color = Color.White,
                                        fontSize = 15.sp
                                    ),
                                    cursorBrush = SolidColor(GoldDeep),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Email
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    decorationBox = { innerTextField ->
                                        Box {
                                            if (state.email.isBlank()) {
                                                Text(
                                                    text = "Enter email address",
                                                    color = TextMuted,
                                                    fontSize = 14.sp
                                                )
                                            }
                                            innerTextField()
                                        }
                                    }
                                )

                                Spacer(Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(GoldDeep.copy(alpha = 0.6f))
                                )
                            } else {
                                Text(
                                    text = displayEmail.ifBlank { "Not available" },
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        if (!state.isEditingProfile) {
                            // Keep the existing non-editing appearance.
                            Spacer(Modifier.width(8.dp))
                        }
                    }

                    HorizontalDivider(color = Divider)
                }


                // Language
                LanguageRow(
                    language = language,
                    onClick = onLanguageClick
                )

                // Save Profile (only while editing)
                if (state.isEditingProfile) {
                    Spacer(Modifier.height(20.dp))
                    SaveProfileButton(
                        isSaving = state.isSaving,
                        onClick = { onIntent(ProfileIntent.SaveProfileClicked) }
                    )
                }

                Spacer(Modifier.height(20.dp))

                // Logout
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onIntent(ProfileIntent.LogoutClicked) }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(text = "Logout", color = TextSecondary, fontSize = 14.sp)
                }
            }

            // Loading / error feedback (kept from the previous screen)
            if (state.isLoading) {
                Spacer(Modifier.height(20.dp))
                CircularProgressIndicator(color = orange)
            }

            state.error?.let { message ->
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFF4D4D).copy(alpha = 0.12f))
                        .border(1.dp, Color(0xFFFF4D4D).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = message,
                        color = Color(0xFFFF6B6B),
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onIntent(ProfileIntent.RetryClicked) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Retry",
                            tint = orange,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Retry", color = orange, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

// ───────────────────────────── Components ─────────────────────────────

@Composable
private fun ProfileAvatar(
    imageUrl: String?,
    editable: Boolean,
    onClick: () -> Unit
) {
    Box(modifier = Modifier.size(84.dp)) {
        Box(
            modifier = Modifier
                .size(84.dp)
                .clip(CircleShape)
                .background(Gold)
                .padding(5.dp)
                .clip(CircleShape)
                .background(AvatarInner)
                .then(if (editable) Modifier.clickable(onClick = onClick) else Modifier),
            contentAlignment = Alignment.Center
        ) {
            if (imageUrl.isNullOrBlank()) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile picture",
                    tint = Gold,
                    modifier = Modifier.size(52.dp)
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

        // Online indicator
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 2.dp, y = 2.dp)
                .size(18.dp)
                .clip(CircleShape)
                .background(OnlineGreen)
                .border(3.dp, CardBg, CircleShape)
        )
    }
}

@Composable
private fun OutlinedActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(shape)
            .background(ButtonWarmBg)
            .border(1.dp, GoldBorder, shape)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            color = Color.White,
            fontSize = 15.sp,
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
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconTile(icon)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = label, color = TextMuted, fontSize = 12.sp)
                Spacer(Modifier.height(2.dp))
                Text(
                    text = value,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            trailing?.invoke()
        }
        HorizontalDivider(color = Divider)
    }
}

@Composable
private fun LanguageRow(
    language: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconTile(Icons.Default.Language)
        Spacer(Modifier.width(14.dp))
        Text(
            text = "Language",
            color = TextMuted,
            fontSize = 13.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = language,
            color = orange,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.width(4.dp))
        Icon(
            imageVector = Icons.Default.UnfoldMore,
            contentDescription = null,
            tint = orange,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
    }
}

@Composable
private fun IconTile(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(IconTile)
            .border(1.dp, Color(0xFF222222), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Gold,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun VerifiedBadge() {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(VerifiedBg),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Verified",
            tint = OnlineGreen,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun SaveProfileButton(
    isSaving: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(shape)
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF3A2E17), Color(0xFF1A1509), Color(0xFF110E07))
                )
            )
            .border(BorderStroke(1.dp, GoldBorder), shape)
            .clickable(enabled = !isSaving, onClick = onClick)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = Gold,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = "Save Profile",
            color = Color(0xFFF6EBC4),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        if (isSaving) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Gold,
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}