package com.dealshot.app.presentation.tv.details

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import com.dealshot.app.R
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dealshot.app.data.video.Video
import com.dealshot.app.ui.theme.orange

private val FocusOrange = Color(0xFFFF9800)

@Composable
fun TvDetailsScreen(
    video: Video,
    isLiked: Boolean,
    isUpdatingLike: Boolean,
    isInMyWatchLater: Boolean,
    isUpdatingMyWatchLater: Boolean,
    onPlayVideo: () -> Unit,
    onMyListClick: () -> Unit,
    onLikeClick: () -> Unit
) {
    val context = LocalContext.current
    val playFocus = remember { FocusRequester() }

    // D-pad focus starts on Play.
    LaunchedEffect(Unit) {
        runCatching { playFocus.requestFocus() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Backdrop on the right, fading into black on the left.
        AsyncImage(
            model = video.thumbnailUrl ?: video.thumbnail,
            contentDescription = video.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.65f)
                .align(Alignment.CenterEnd)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        0.0f to Color.Black,
                        0.40f to Color.Black,
                        0.70f to Color(0xCC000000),
                        1.0f to Color.Transparent
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.5f)
                .padding(start = 56.dp, end = 24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = video.title,
                color = Color.White,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                val hasDuration = !video.duration.isNullOrBlank()
                val hasCategory = !video.category.isNullOrBlank()

                if (hasDuration) {
                    Text(video.duration!!, color = Color.LightGray, fontSize = 18.sp)
                }
                if (hasDuration && hasCategory) {
                    Text("  •  ", color = Color.Gray, fontSize = 18.sp)
                }
                if (hasCategory) {
                    Text(video.category!!, color = Color.LightGray, fontSize = 18.sp)
                }
                if (video.isPremium) {
                    Text(
                        text = "  •  Premium",
                        color = orange,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            if (video.description.isNotBlank()) {
                Text(
                    text = video.description,
                    color = Color(0xFFBDBDBD),
                    fontSize = 18.sp,
                    lineHeight = 27.sp,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(28.dp))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                TvDetailsButton(
                    label = "Play",
                    iconResId = R.drawable.play_fill,
                    primary = true,
                    onClick = onPlayVideo,
                    modifier = Modifier.focusRequester(playFocus)
                )

                TvDetailsButton(
                    label = if (isInMyWatchLater) "Added" else "My List",
                    iconResId = if (isInMyWatchLater) R.drawable.checkmark else R.drawable.plus,
                    enabled = !isUpdatingMyWatchLater,
                    onClick = {
                        if (!isInMyWatchLater) {
                            val title = video.title.ifBlank { "Video" }
                            Toast.makeText(context, "$title is added to the list", Toast.LENGTH_SHORT).show()
                        }
                        onMyListClick()
                    }
                )

                TvDetailsButton(
                    label = if (isLiked) "Liked" else "Like",
                    iconResId = if (isLiked) R.drawable.heart_fill else R.drawable.heart,
                    iconTint = if (isLiked) Color.Red else null,
                    enabled = !isUpdatingLike,
                    onClick = {
                        if (!isLiked) {
                            val title = video.title.ifBlank { "Video" }
                            Toast.makeText(context, "$title is added to the list", Toast.LENGTH_SHORT).show()
                        }
                        onLikeClick()
                    }
                )
            }
        }
    }
}

@Composable
private fun TvDetailsButton(
    label: String,
    iconResId: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
    enabled: Boolean = true,
    iconTint: Color? = null
) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(14.dp)

    val container = when {
        focused -> FocusOrange
        primary -> Color.White
        else -> Color(0xCC252525)
    }
    val content = if (focused || primary) Color.Black else Color.White

    Row(
        modifier = modifier
            .clip(shape)
            .background(container)
            .border(2.dp, if (focused) Color.White else Color.Transparent, shape)
            .onFocusChanged { focused = it.isFocused }
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 26.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            tint = if (focused) content else (iconTint ?: content),
            modifier = Modifier.size(26.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            color = content,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
