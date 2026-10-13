package com.dealshot.app.presentation.tv.category

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import com.dealshot.app.data.remote.dto.VideoDto

private val FocusOrange = Color(0xFFFF9800)

@Composable
fun TvCategoryScreen(
    category: String,
    videos: List<VideoDto>,
    isLoading: Boolean,
    hasError: Boolean,
    onBack: () -> Unit,
    onVideoClick: (VideoDto) -> Unit
) {
    // The remote's Back key returns to the previous screen.
    BackHandler(onBack = onBack)

    val firstItemFocus = remember { FocusRequester() }

    LaunchedEffect(videos) {
        if (videos.isNotEmpty()) {
            runCatching { firstItemFocus.requestFocus() }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Text(
            text = category,
            color = Color.White,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 48.dp, top = 32.dp, bottom = 16.dp)
        )

        when {
            isLoading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            hasError -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Couldn't load videos. Press Back and try again.",
                    color = Color.White,
                    fontSize = 22.sp
                )
            }

            videos.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No videos in $category",
                    color = Color.White,
                    fontSize = 22.sp
                )
            }

            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 48.dp,
                    end = 48.dp,
                    top = 12.dp,
                    bottom = 48.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                items(videos, key = { it.id }) { video ->
                    val isFirst = video.id == videos.first().id
                    TvCategoryCard(
                        video = video,
                        onClick = { onVideoClick(video) },
                        modifier = if (isFirst) {
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

@Composable
private fun TvCategoryCard(
    video: VideoDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1.06f else 1f, label = "categoryCardScale")
    val shape = RoundedCornerShape(14.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .onFocusChanged { focused = it.isFocused }
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = video.thumbnailUrl ?: video.thumbnail,
            contentDescription = video.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(shape)
                .border(3.dp, if (focused) FocusOrange else Color.Transparent, shape)
        )

        Spacer(Modifier.height(8.dp))

        video.title?.let {
            Text(
                text = it,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        video.duration?.let {
            Text(text = it, color = Color(0xFF9E9E9E), fontSize = 14.sp)
        }
    }
}