package com.dealshot.app.presentation.mobile.myList

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.ui.res.painterResource
import com.dealshot.app.R
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dealshot.app.data.video.Video
import com.dealshot.app.ui.theme.GoldPrimary
import com.dealshot.app.ui.theme.TextMuted
import androidx.compose.material.icons.filled.FavoriteBorder

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
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                modifier = Modifier.size(24.dp),
                onClick = onHomeClick
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.chevron_left),
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        when {
            isLoading && videos.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GoldPrimary)
                }
            }

            errorMessage != null && videos.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Sign in required",
                        tint = Color(0xFFFF8A00),
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Sign in to continue",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = errorMessage.ifBlank {
                            "Please sign in to access your videos and continue watching."
                        },
                        color = Color.LightGray,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onRetry,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF8A00),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Sign in",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }


            videos.isEmpty() -> {
                val isMyList = title.equals("My List", ignoreCase = true)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isMyList) {
                            Icons.Default.BookmarkBorder
                        } else {
                            Icons.Default.FavoriteBorder
                        },
                        contentDescription = null,
                        tint = Color(0xFFFF8A00),
                        modifier = Modifier.size(76.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = if (isMyList) "My List" else "Liked Videos",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isMyList) {
                            "Use the plus button to build your watchlist"
                        } else {
                            "Videos you like will appear here."
                        },
                        color = Color.LightGray,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
            }


            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(
                        items = videos,
                        key = { it.id }
                    ) { video ->
                        VideoCollectionRow(
                            video = video,
                            onClick = { onVideoClick(video) },
                            onRemove = onRemoveVideo?.let {
                                { it(video) }
                            },
                            isRemoving = video.id in removingVideoIds
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VideoCollectionRow(
    video: Video,
    onClick: () -> Unit,
    onRemove: (() -> Unit)?,
    isRemoving: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF181818))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail with 16:9 Aspect Ratio
        AsyncImage(
            model = video.thumbnailUrl ?: video.thumbnail,
            contentDescription = video.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(130.dp)
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onClick)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Title and Category Details
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onClick),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = video.title,
                color = Color.White,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                modifier = Modifier.basicMarquee()
            )

            if (video.category.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = video.category,
                    color = TextMuted,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1
                )
            }

            if (video.duration.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = video.duration,
                    color = GoldPrimary,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Remove Button
        if (onRemove != null) {
            IconButton(
                onClick = onRemove,
                enabled = !isRemoving,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF252525))
            ) {
                if (isRemoving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = GoldPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.trash),
                        contentDescription = "Remove from list",
                        tint = GoldPrimary,
                        modifier = Modifier.size(25.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
    }
}
