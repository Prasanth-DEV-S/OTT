package com.dealshot.app.presentation.mobile.category

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
@Composable
fun CategoryVideosScreen(
    category: String,
    videos: List<VideoDto>,
    onBack: () -> Unit,
    onVideoClick: (VideoDto) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 50.dp,
                    start = 8.dp,
                    end = 16.dp,
                    bottom = 16.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(
                onClick = onBack
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Text(
                text = category,
                color = Color.White,
                fontSize = 24.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // 3-column video grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 10.dp,
                bottom = 20.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
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

                    Spacer(modifier = Modifier.height(8.dp))

/*                    Text(
                        text = video.title.orEmpty(),
                        color = Color.White,
                        fontSize = 15.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = video.duration.orEmpty(),
                        color = Color.Gray,
                        fontSize = 12.sp
                    )*/
                }
            }
        }
    }
}