package com.dealshot.app.presentation.mobile.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.dealshot.app.data.video.Video
import com.dealshot.app.ui.theme.orange
import android.content.Intent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext

@Composable
fun MobileDetailsScreen(
    video: Video,
    onBackClick: () -> Unit,
    onPlayVideo: () -> Unit,
    isLiked: Boolean,
    isInMyWatchLater: Boolean,
    onMyListClick: () -> Unit = {},
    onLikeClick: () -> Unit = {},
    isUpdatingMyWatchLater: Boolean = false,
    isUpdatingLike: Boolean = false,
    onShareClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val heartScale by animateFloatAsState(
        targetValue = if (isLiked) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "heartScale"
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
    ) {

        // ------------------------------------------------------------
        // TOP BAR
        // ------------------------------------------------------------

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(56.dp)
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 4.dp)
                    .zIndex(10f)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // ------------------------------------------------------------
        // HERO IMAGE
        // ------------------------------------------------------------

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(18.dp))
        ) {

            AsyncImage(
                model = video.thumbnailUrl ?: video.thumbnail,
                contentDescription = video.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .aspectRatio(16f / 20f),
                contentScale = ContentScale.Crop
            )

            // Dark gradient at bottom
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.75f)
                            )
                        )
                    )
            )

            // Play button
            /*            Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(
                                    orange.copy(alpha = 0.95f)
                                )
                                .clickable(
                                    onClick = onPlayVideo
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.White,
                                modifier = Modifier.size(38.dp)
                            )
                        }*/
        }

        // ------------------------------------------------------------
        // DETAILS
        // ------------------------------------------------------------

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 32.dp
                )
        ) {

            // TITLE
            Text(
                text = video.title,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            // METADATA
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                video.duration?.takeIf {
                    it.isNotBlank()
                }?.let {

                    Text(
                        text = it,
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )
                }

                if (
                    !video.duration.isNullOrBlank() &&
                    !video.category.isNullOrBlank()
                ) {
                    Text(
                        text = "  •  ",
                        color = Color.Gray
                    )
                }

                video.category?.takeIf {
                    it.isNotBlank()
                }?.let {

                    Text(
                        text = it,
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )
                }

                if (video.isPremium) {

                    Text(
                        text = "  •  Premium",
                        color = orange,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )



            // --------------------------------------------------------
            // PLAY BUTTON
            // --------------------------------------------------------

            Button(
                onClick = onPlayVideo,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {

                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null
                )

                Spacer(
                    modifier = Modifier.width(6.dp)
                )

                Text(
                    text = "Play",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(
                modifier = Modifier.height(16.dp)
            )
            // --------------------------------------------------------
            // DESCRIPTION
            // --------------------------------------------------------

            if (video.description.isNotBlank()) {

                Text(
                    text = video.description,
                    color = Color(0xFFBDBDBD),
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            // --------------------------------------------------------
            // ACTIONS
            // --------------------------------------------------------

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DetailsActionItem(
                    icon = if (isInMyWatchLater) {
                        Icons.Default.Check
                    } else {
                        Icons.Default.Add
                    },
                    label = if (isInMyWatchLater) "Added" else "My List",
                    onClick = onMyListClick
                )

                Column(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .clickable {
                            onLikeClick()
                        },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (isLiked) {
                            Icons.Filled.Favorite
                        } else {
                            Icons.Outlined.FavoriteBorder
                        },
                        contentDescription = if (isLiked) "Unlike" else "Like",
                        tint = if (isLiked) {
                            Color.Red
                        } else {
                            Color(0xFFFF7A00)
                        },
                        modifier = Modifier
                            .size(28.dp)
                            .scale(heartScale)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Like",
                        color = Color(0xFFFF7A00),
                        fontSize = 13.sp
                    )
                }

                DetailsActionItem(
                    icon = Icons.Default.Share,
                    label = "Share",
                    onClick = {
                        val shareText = buildString {
                            append(video.title)

                            if (video.description.isNotBlank()) {
                                append("\n\n")
                                append(video.description)
                            }

                            if (video.url.isNotBlank()) {
                                append("\n\n")
                                append(video.url)
                            }
                        }

                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, video.title)
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }

                        context.startActivity(
                            Intent.createChooser(shareIntent, "Share video")
                        )
                    }
                )
            }

            Spacer(
                modifier = Modifier.height(10.dp)
            )

        }
    }
}

@Composable
private fun DetailsActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(
                horizontal = 12.dp,
                vertical = 6.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color(0xFFFF7A00),
            modifier = Modifier.size(28.dp)
        )

        Spacer(
            modifier = Modifier.height(5.dp)
        )

        Text(
            text = label,
            color = Color(0xFFFF7A00),
            fontSize = 13.sp
        )
    }
}