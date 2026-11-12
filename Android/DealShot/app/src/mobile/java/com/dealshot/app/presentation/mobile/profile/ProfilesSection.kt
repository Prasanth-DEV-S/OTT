package com.dealshot.app.presentation.mobile.profile

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.SvgDecoder

/** UI model for one entry in the PROFILES row. Map your real profile model to this. */
data class ProfileListItem(
    val id: String,
    val name: String,
    val avatarUrl: String?,
    val isPrimary: Boolean = false
)

private val DangerRed = Color(0xFFD65C52)
private val ChipBg = Color(0xFF211E17)
private val ChipBorder = Color(0xFF302B20)
private val GoldPrimary = Color(0xFFD8AD55)
private val GoldLight = Color(0xFFF2DDA6)
private val AvatarCream = Color(0xFFF1E3B5)
private val TextMuted = Color(0xFFA6A29A)

@Composable
internal fun ProfilesRow(
    profiles: List<ProfileListItem>,
    selectedProfileId: String?,
    onSelect: (ProfileListItem) -> Unit,
    onRemoveRequest: (ProfileListItem) -> Unit,
    onAddProfile: () -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(profiles, key = { it.id }) { profile ->
            ProfileChip(
                profile = profile,
                selected = profile.id == selectedProfileId,
                removable = !profile.isPrimary,
                onClick = { onSelect(profile) },
                onRemove = { onRemoveRequest(profile) }
            )
        }
        item(key = "add_profile") {
            AddProfileChip(
                onClick = onAddProfile
            )
        }
    }
}

@Composable
private fun ProfileChip(
    profile: ProfileListItem,
    selected: Boolean,
    removable: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    val borderColor by animateColorAsState(
        targetValue = if (selected) GoldPrimary else ChipBorder,
        animationSpec = tween(200),
        label = "chipBorder"
    )
    val bgColor by animateColorAsState(
        targetValue = if (selected) Color(0xFF2C2417) else ChipBg,
        animationSpec = tween(200),
        label = "chipBg"
    )

    Box(modifier = Modifier.width(76.dp).height(88.dp)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .background(bgColor)
                .border(if (selected) 1.5.dp else 1.dp, borderColor, shape)
                .clickable(onClick = onClick)
                .padding(horizontal = 4.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ProfileAvatar(url = profile.avatarUrl, size = 44.dp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = profile.name,
                color = if (selected) GoldLight else Color.White,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (removable) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(24.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onRemove),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color(0xCC000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove ${profile.name}",
                        tint = DangerRed,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AddProfileChip(onClick: () -> Unit) {
    val dashColor = GoldPrimary.copy(alpha = 0.5f)
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = Modifier
            .width(76.dp)
            .height(88.dp)
            .clip(shape)
            .background(ChipBg)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRoundRect(
                        color = dashColor,
                        cornerRadius = CornerRadius(14.dp.toPx()),
                        style = Stroke(
                            width = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
                        )
                    )
                }
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2B2620)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add profile",
                    tint = GoldPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Add Profile",
                color = TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

/** Circular cream avatar. Shows [url] if present, otherwise a person icon. */
@Composable
internal fun ProfileAvatar(url: String?, size: Dp, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val imageLoader = remember(context) {
        ImageLoader.Builder(context)
            .components {
                add(SvgDecoder.Factory())
            }
            .build()
    }
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(AvatarCream),
        contentAlignment = Alignment.Center
    ) {
        if (!url.isNullOrBlank()) {
            AsyncImage(
                model = url,
                contentDescription = null,
                imageLoader = imageLoader,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color(0xFF8A6A25),
                modifier = Modifier.size(size * 0.55f)
            )
        }
    }
}

/** Plain detail row (icon tile, label, value, optional trailing content). */
@Composable
internal fun AccountDetailRow(
    label: String,
    value: String,
    icon: @Composable () -> Unit,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF211E17)),
            contentAlignment = Alignment.Center
        ) { icon() }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, color = TextMuted, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        trailing?.invoke()
    }
}

/** Gold-outlined action button (Edit Profile / Unlock Premium). */
@Composable
internal fun GoldOutlineButton(
    label: String,
    onClick: () -> Unit,
    leading: @Composable () -> Unit,
    trailing: (@Composable () -> Unit)? = null,
    centered: Boolean = true
) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(shape)
            .background(Color(0xFF211E17))
            .border(1.dp, GoldPrimary, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (centered) Arrangement.Center else Arrangement.Start
    ) {
        leading()
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            color = GoldLight,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = if (centered) Modifier else Modifier.weight(1f)
        )
        trailing?.invoke()
    }
}

/** Confirmation before a profile is removed. */
@Composable
internal fun RemoveProfileConfirmDialog(
    profileName: String,
    isDeleting: Boolean = false,
    errorMessage: String? = null,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = { if (!isDeleting) onDismiss() },
        properties = androidx.compose.ui.window.DialogProperties(
            dismissOnBackPress = !isDeleting,
            dismissOnClickOutside = !isDeleting
        )
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 380.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF151411))
                .border(1.dp, GoldPrimary, RoundedCornerShape(20.dp))
                .padding(horizontal = 24.dp, vertical = 28.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Remove Profile",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Remove \"$profileName\" from your account? This can't be undone.",
                    color = TextMuted,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
                if (!errorMessage.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMessage,
                        color = DangerRed,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF211E17))
                            .border(1.dp, ChipBorder, RoundedCornerShape(12.dp))
                            .clickable(enabled = !isDeleting) { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Cancel", color = if (isDeleting) TextMuted else Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDeleting) DangerRed.copy(alpha = 0.55f) else DangerRed)
                            .clickable(enabled = !isDeleting) { onConfirm() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isDeleting) {
                            androidx.compose.material3.CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Remove", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
