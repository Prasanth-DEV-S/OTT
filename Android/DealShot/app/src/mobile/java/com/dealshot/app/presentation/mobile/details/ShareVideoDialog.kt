package com.dealshot.app.presentation.mobile.details

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.dealshot.app.R
import com.dealshot.app.ui.theme.CardBgDark
import com.dealshot.app.ui.theme.FieldBg
import com.dealshot.app.ui.theme.FieldBorderUnfocused
import com.dealshot.app.ui.theme.GoldPrimary
import com.dealshot.app.ui.theme.GoldTextLight
import com.dealshot.app.ui.theme.TextMuted

private data class InstalledShareApp(
    val name: String,
    val packageName: String = "",
    val activityName: String = "",
    val iconDrawable: Drawable? = null,
    val iconVector: ImageVector? = null,
    val iconResId: Int? = null,
    val isMore: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareBottomSheetMenu(
    videoTitle: String,
    videoUrl: String,
    shareText: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isCopied by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }

    // Dynamically query installed apps on device that can handle text sharing
    val installedApps = remember(context) {
        val pm = context.packageManager
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
        }

        val flags = PackageManager.MATCH_ALL

        val resolveInfoList: List<ResolveInfo> = try {
            pm.queryIntentActivities(shareIntent, flags)
        } catch (_: Exception) {
            emptyList()
        }

        val myPackage = context.packageName

        val apps = resolveInfoList
            .filter { it.activityInfo.packageName != myPackage }
            .map { resolveInfo ->
                val appName = resolveInfo.loadLabel(pm).toString()
                val appIcon = try {
                    resolveInfo.loadIcon(pm)
                } catch (_: Exception) {
                    null
                }
                InstalledShareApp(
                    name = appName,
                    packageName = resolveInfo.activityInfo.packageName,
                    activityName = resolveInfo.activityInfo.name,
                    iconDrawable = appIcon
                )
            }
            .distinctBy { it.packageName }

        // Preferred ordering for common messaging/social apps if installed
        val priorityPackages = listOf(
            "com.whatsapp",
            "org.telegram.messenger",
            "com.google.android.apps.messaging",
            "com.samsung.android.messaging",
            "com.google.android.gm",
            "com.twitter.android",
            "com.facebook.katana",
            "com.instagram.android"
        )

        apps.sortedWith { a, b ->
            val indexA = priorityPackages.indexOf(a.packageName).let { if (it == -1) 999 else it }
            val indexB = priorityPackages.indexOf(b.packageName).let { if (it == -1) 999 else it }
            if (indexA != indexB) indexA.compareTo(indexB) else a.name.compareTo(b.name, ignoreCase = true)
        }
    }

    // Determine initial row targets vs expanded grid targets
    val initialTargets = remember(installedApps) {
        if (installedApps.size > 4) {
            installedApps.take(4) + InstalledShareApp(
                name = "More",
                iconVector = Icons.Default.MoreHoriz,
                isMore = true
            )
        } else if (installedApps.isNotEmpty()) {
            installedApps
        } else {
            // Fallback if no specific apps resolved
            listOf(
                InstalledShareApp("Email", iconVector = Icons.Default.Email),
                InstalledShareApp("Share", iconVector = Icons.Default.Share)
            )
        }
    }

    val handleTargetClick: (InstalledShareApp) -> Unit = { target ->
        if (target.isMore) {
            isExpanded = true
        } else if (target.packageName.isNotBlank()) {
            onDismiss()
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
                setClassName(target.packageName, target.activityName)
            }
            try {
                context.startActivity(intent)
            } catch (_: Exception) {
                val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    setPackage(target.packageName)
                }
                try {
                    context.startActivity(fallbackIntent)
                } catch (_: Exception) {
                    Toast.makeText(context, "Could not open ${target.name}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            onDismiss()
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, videoTitle)
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            try {
                context.startActivity(Intent.createChooser(shareIntent, "Share video"))
            } catch (_: Exception) {
                Toast.makeText(context, "Could not share video", Toast.LENGTH_SHORT).show()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CardBgDark,
        contentColor = Color.White,
        scrimColor = Color.Black.copy(alpha = 0.70f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(38.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF383025))
            )
        },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .animateContentSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, GoldPrimary, CircleShape)
                        .background(Color(0xFF2B2216)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.square_and_arrow_up),
                        contentDescription = "Share",
                        tint = GoldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Share Video",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = videoTitle.ifBlank { "DealShot Video" },
                        color = GoldTextLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        painter = painterResource(id = R.drawable.xmark),
                        contentDescription = "Close",
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // URL Copy Row
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(FieldBg)
                    .border(1.dp, FieldBorderUnfocused.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = videoUrl,
                        color = TextMuted,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isCopied) Color(0xFF16301C) else Color(0xFF2B2620))
                            .border(1.dp, if (isCopied) Color(0xFF4CD964) else GoldPrimary, RoundedCornerShape(8.dp))
                            .clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("DealShot Video", videoUrl)
                                clipboard.setPrimaryClip(clip)
                                isCopied = true
                                Toast.makeText(context, "Link copied to clipboard!", Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isCopied) "Copied! ✓" else "Copy",
                            color = if (isCopied) Color(0xFF4CD964) else GoldPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isExpanded) "AVAILABLE APPS" else "SHARE VIA",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.weight(1f)
                )

                if (isExpanded) {
                    Text(
                        text = "Show Less",
                        color = GoldPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { isExpanded = false }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (!isExpanded) {
                // Initial Single Row view
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(initialTargets) { target ->
                        ShareTargetItem(target = target, onClick = { handleTargetClick(target) })
                    }
                }
            } else {
                // Expanded Grid view of all installed apps on device
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    installedApps.chunked(4).forEach { rowTargets ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            rowTargets.forEach { target ->
                                ShareTargetItem(target = target, onClick = { handleTargetClick(target) })
                            }
                            repeat(4 - rowTargets.size) {
                                Spacer(modifier = Modifier.width(56.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Cancel Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF231F1A))
                    .border(1.dp, Color(0xFF383025), RoundedCornerShape(14.dp))
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

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ShareTargetItem(
    target: InstalledShareApp,
    onClick: () -> Unit
) {
    val imageBitmap = remember(target.iconDrawable) {
        target.iconDrawable?.let { drawable ->
            try {
                drawable.toBitmap(width = 112, height = 112).asImageBitmap()
            } catch (_: Exception) {
                null
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(64.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF231F1A))
                .border(1.dp, Color(0xFF383025), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            when {
                imageBitmap != null -> {
                    Image(
                        bitmap = imageBitmap,
                        contentDescription = target.name,
                        modifier = Modifier.size(28.dp)
                    )
                }
                target.iconResId != null -> {
                    Icon(
                        painter = painterResource(id = target.iconResId),
                        contentDescription = target.name,
                        tint = GoldPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                target.iconVector != null -> {
                    Icon(
                        imageVector = target.iconVector,
                        contentDescription = target.name,
                        tint = GoldPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                else -> {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = target.name,
                        tint = GoldPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = target.name,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
