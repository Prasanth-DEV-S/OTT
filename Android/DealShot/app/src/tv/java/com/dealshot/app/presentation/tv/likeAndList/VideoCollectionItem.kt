package com.dealshot.app.presentation.tv.likeAndList

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dealshot.app.data.video.Video

private val FocusOrange = Color(0xFFFF9800)
private val RemoveRed = Color(0xFFE53935)

/**
 * TV version of VideoCollectionScreen.
 * - OK/Enter opens the video.
 * - Long-press OK on a card shows a "Remove" button (same as mobile long-press).
 * - Back closes the Remove overlay first, otherwise goes back.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TvVideoCollectionScreen(
    title: String,
    videos: List<Video>,
    onHomeClick: () -> Unit,
    onVideoClick: (Video) -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onRetry: () -> Unit = {},
    onRemoveVideo: ((Video) -> Unit)? = null,
    removingVideoIds: Set<String> = emptySet()
) {
    var selectedVideoId by remember { mutableStateOf<String?>(null) }

    // Back closes the Remove overlay first.
    BackHandler(enabled = selectedVideoId != null) {
        selectedVideoId = null
    }
    BackHandler(enabled = selectedVideoId == null) {
        onHomeClick()
    }

    val firstItemFocus = remember { FocusRequester() }
    val retryFocus = remember { FocusRequester() }

    LaunchedEffect(videos.isNotEmpty()) {
        if (videos.isNotEmpty()) runCatching { firstItemFocus.requestFocus() }
    }

    val showError = errorMessage != null && videos.isEmpty()
    LaunchedEffect(showError) {
        if (showError) runCatching { retryFocus.requestFocus() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 48.dp, vertical = 24.dp)
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        when {
            isLoading && videos.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = FocusOrange)
            }

            showError -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = errorMessage.orEmpty(),
                        color = Color.LightGray,
                        fontSize = 20.sp
                    )
                    Spacer(Modifier.height(16.dp))
                    TvActionButton(
                        label = "Retry",
                        onClick = onRetry,
                        modifier = Modifier.focusRequester(retryFocus)
                    )
                }
            }

            videos.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No videos yet", color = Color.LightGray, fontSize = 22.sp)
            }

            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 48.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                items(videos, key = { it.id }) { video ->
                    TvCollectionItem(
                        video = video,
                        isSelected = selectedVideoId == video.id,
                        isRemoving = video.id in removingVideoIds,
                        onClick = { onVideoClick(video) },
                        onLongPress = { selectedVideoId = video.id },
                        onDismiss = { selectedVideoId = null },
                        onRemove = onRemoveVideo?.let {
                            {
                                selectedVideoId = null
                                it(video)
                            }
                        },
                        modifier = if (video.id == videos.first().id) {
                            Modifier.focusRequester(firstItemFocus)
                        } else {
                            Modifier
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TvCollectionItem(
    video: Video,
    isSelected: Boolean,
    isRemoving: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    onDismiss: () -> Unit,
    onRemove: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1.06f else 1f, label = "collectionScale")
    val shape = RoundedCornerShape(14.dp)
    val removeFocus = remember { FocusRequester() }
    val showRemove = isSelected && !isRemoving && onRemove != null

    LaunchedEffect(showRemove) {
        if (showRemove) runCatching { removeFocus.requestFocus() }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(shape)
                .border(3.dp, if (focused) FocusOrange else Color.Transparent, shape)
                .onFocusChanged { focused = it.isFocused }
                .combinedClickable(
                    enabled = !isRemoving,
                    onClick = { if (isSelected) onDismiss() else onClick() },
                    onLongClick = if (onRemove != null) onLongPress else null
                )
        ) {
            AsyncImage(
                model = video.thumbnailUrl ?: video.thumbnail,
                contentDescription = video.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            if (showRemove || isRemoving) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.55f))
                )
            }

            if (showRemove) {
                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .focusRequester(removeFocus)
                        .clip(RoundedCornerShape(50))
                        .background(RemoveRed)
                        .clickable { onRemove?.invoke() }
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Remove",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (isRemoving) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(32.dp),
                    color = FocusOrange,
                    strokeWidth = 3.dp
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = video.title,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (video.duration.isNotBlank()) {
            Text(text = video.duration, color = Color(0xFF9E9E9E), fontSize = 14.sp)
        }
    }
}

@Composable
private fun TvActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(if (focused) FocusOrange else Color(0xFF252525))
            .onFocusChanged { focused = it.isFocused }
            .clickable(onClick = onClick)
            .padding(horizontal = 32.dp, vertical = 14.dp)
    ) {
        Text(
            text = label,
            color = if (focused) Color.Black else Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}