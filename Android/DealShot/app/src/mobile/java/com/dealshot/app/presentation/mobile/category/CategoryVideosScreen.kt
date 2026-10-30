package com.dealshot.app.presentation.mobile.category

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import com.dealshot.app.R
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dealshot.app.data.remote.dto.VideoDto
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.background

@Composable
fun CategoryVideosScreen(
    category: String,
    videos: List<VideoDto>,
    onBack: () -> Unit,
    onVideoClick: (VideoDto) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding().navigationBarsPadding()
    ) {

        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(
                onClick = onBack
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.chevron_left),
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Text(
                text = category,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 2.dp)
            )
        }

        // 3-column video grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp

            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                items = videos,
                key = { it.id }
            ) { video ->

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onVideoClick(video)
                        }
                ) {

                    AsyncImage(
                        model = video.thumbnailUrl ?: video.thumbnail,
                        contentDescription = video.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(9f / 16f)
                            .clip(
                                RoundedCornerShape(14.dp)
                            )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    video.title?.let {
                        Text(
                            text = it,
                            color = Color.White,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (!video.duration.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = video.duration,
                            color = Color(0xFF9E9E9E),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}