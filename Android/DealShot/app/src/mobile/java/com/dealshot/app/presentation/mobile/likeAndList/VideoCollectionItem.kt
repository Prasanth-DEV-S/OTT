package com.dealshot.app.presentation.mobile.likeAndList

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dealshot.app.data.video.Video
import androidx.compose.foundation.layout.statusBarsPadding
@Composable
fun VideoCollectionScreen(
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
    // UI-only state: which video currently shows the "Remove" overlay
    var selectedVideoId by remember { mutableStateOf<String?>(null) }

    // Back press first closes the Remove overlay
    BackHandler(enabled = selectedVideoId != null) {
        selectedVideoId = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
    )  {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onHomeClick)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Home",
                tint = Color(0xFFFF8A00)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = title,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(
                    top = 16.dp,
                    bottom = 20.dp
                )
            )
        }



        when {
            isLoading && videos.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFFFF8A00)
                    )
                }
            }

            errorMessage != null && videos.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = errorMessage,
                            color = Color.LightGray,
                            fontSize = 16.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(onClick = onRetry) {
                            Text("Retry")
                        }
                    }
                }
            }

            videos.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No videos yet",
                        color = Color.LightGray,
                        fontSize = 17.sp
                    )
                }
            }

            else -> {
                // 3 columns x N rows
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        items = videos,
                        key = { video -> video.id }
                    ) { video ->
                        VideoCollectionItem(
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
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun VideoCollectionItem(
    video: Video,
    isSelected: Boolean,
    isRemoving: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    onDismiss: () -> Unit,
    onRemove: (() -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(2f / 4f)                       // 3:2 thumbnail
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                enabled = !isRemoving,
                onClick = {
                    // While the Remove overlay is open, a tap closes it
                    if (isSelected) onDismiss() else onClick()
                },
                onLongClick = if (onRemove != null) {
                    {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongPress()
                    }
                } else {
                    null
                }
            )
    ) {

        AsyncImage(
            model = video.thumbnailUrl ?: video.thumbnail,
            contentDescription = video.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Dim the video while Remove is shown or the request is running
        if ((isSelected && onRemove != null) || isRemoving) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
            )
        }

        // "Remove" on top of the video (after long press)
        if (isSelected && !isRemoving && onRemove != null) {
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFFE53935))
                    .clickable(onClick = onRemove)
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "Remove",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Request in progress for this video
        if (isRemoving) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(22.dp),
                color = Color(0xFFFF8A00),
                strokeWidth = 2.dp
            )
        }
    }
}