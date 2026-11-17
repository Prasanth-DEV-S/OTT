package com.dealshot.app.presentation.mobile.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dealshot.app.R
import com.dealshot.app.data.remote.dto.EpisodeDto
import com.dealshot.app.data.remote.dto.SeasonDto

private val ScreenBg = Color(0xFF0E1117)
private val PrimaryTextColor = Color.White
private val MetadataTextColor = Color(0xFF3B82F6)
private val DividerColor = Color(0xFF1F2530)
private val InactiveTextColor = Color(0xFF8E9BAE)

data class EpisodeItemData(
    val id: String = "",
    val title: String,
    val seasonEpisodeMeta: String,
    val imageUrl: String,
    val isLocked: Boolean = false,
    val gradientColors: List<Color> = listOf(Color(0xFF2B3038), Color(0xFF12151A))
)

val demoEpisodeList = listOf(
    EpisodeItemData(
        title = "The House of SS",
        seasonEpisodeMeta = "S1 E1 • 46m",
        imageUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=500",
        gradientColors = listOf(Color(0xFF3D2A1D), Color(0xFF1A120B))
    ),
    EpisodeItemData(
        title = "Jayshree's Second Chance",
        seasonEpisodeMeta = "S1 E2 • 22m",
        imageUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500",
        gradientColors = listOf(Color(0xFF1C2D42), Color(0xFF0D1724))
    ),
    EpisodeItemData(
        title = "The Silent Dinner",
        seasonEpisodeMeta = "S1 E3 • 31m",
        imageUrl = "https://images.unsplash.com/photo-1511795409834-ef04bbd61622?w=500",
        gradientColors = listOf(Color(0xFF3E3218), Color(0xFF1B160A))
    ),
    EpisodeItemData(
        title = "Cracks in the Walls",
        seasonEpisodeMeta = "S1 E4 • 28m",
        imageUrl = "https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?w=500",
        gradientColors = listOf(Color(0xFF2B3038), Color(0xFF12151A))
    )
)

@Composable
fun SeasonEpisodeSection(
    seasons: List<SeasonDto> = emptyList(),
    selectedSeasonNumber: Int = 1,
    episodes: List<EpisodeDto> = emptyList(),
    isLoadingEpisodes: Boolean = false,
    onSeasonSelect: (Int) -> Unit = {},
    onEpisodeSelect: (EpisodeDto) -> Unit = {},
    onDownloadClick: (EpisodeDto) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ScreenBg)
    ) {
        // SEASON TABS
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            if (seasons.isEmpty()) {
                // Fallback single tab if seasons list not provided yet
                Column(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                        .width(IntrinsicSize.Max)
                ) {
                    Text(
                        text = "Season $selectedSeasonNumber",
                        color = PrimaryTextColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(PrimaryTextColor)
                    )
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(seasons) { season ->
                        val isSelected = season.seasonNumber == selectedSeasonNumber
                        val titleText = season.title.ifBlank { "Season ${season.seasonNumber}" }

                        Column(
                            modifier = Modifier
                                .clickable { onSeasonSelect(season.seasonNumber) }
                                .width(IntrinsicSize.Max)
                        ) {
                            Text(
                                text = titleText,
                                color = if (isSelected) PrimaryTextColor else InactiveTextColor,
                                fontSize = 18.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .background(if (isSelected) PrimaryTextColor else Color.Transparent)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                color = DividerColor,
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // EPISODE LIST
        if (isLoadingEpisodes) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = com.dealshot.app.ui.theme.GoldPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
        } else if (episodes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No episodes available for this season.",
                    color = InactiveTextColor,
                    fontSize = 14.sp
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                episodes.forEach { episode ->
                    val durationText = formatSeconds(episode.durationSeconds)
                    val meta = "S${selectedSeasonNumber} E${episode.episodeNumber}" +
                            (if (durationText.isNotBlank()) " • $durationText" else "")

                    val itemData = EpisodeItemData(
                        id = episode.id,
                        title = episode.title,
                        seasonEpisodeMeta = meta,
                        imageUrl = episode.thumbnailUrl.orEmpty(),
                        isLocked = episode.isLocked
                    )

                    EpisodeRowItem(
                        episode = itemData,
                        onEpisodeClick = { onEpisodeSelect(episode) },
                        onDownloadClick = { onDownloadClick(episode) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EpisodeRowItem(
    episode: EpisodeItemData,
    onEpisodeClick: () -> Unit,
    onDownloadClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEpisodeClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(140.dp)
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(10.dp))
                .background(Brush.linearGradient(episode.gradientColors))
        ) {
            if (episode.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = episode.imageUrl,
                    contentDescription = episode.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.75f)
                            ),
                            startY = 0.4f
                        )
                    )
            )

            if (episode.isLocked) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = com.dealshot.app.ui.theme.orange,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(24.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                        .size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = episode.title,
                color = PrimaryTextColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = episode.seasonEpisodeMeta,
                color = MetadataTextColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

//        Icon(
//            painter = painterResource(id = R.drawable.ic_download),
//            contentDescription = "Download Episode",
//            tint = Color.White,
//            modifier = Modifier
//                .clip(RoundedCornerShape(4.dp))
//                .clickable(onClick = onDownloadClick)
//                .padding(8.dp)
//                .size(24.dp)
//        )
    }
}

private fun formatSeconds(seconds: Int): String {
    if (seconds <= 0) return ""
    val minutes = seconds / 60
    val secs = seconds % 60
    return if (minutes > 0) {
        if (secs > 0) "${minutes}m ${secs}s" else "${minutes}m"
    } else {
        "${secs}s"
    }
}

@Preview(name = "Season 1 & Episode List - 1080px Wide", widthDp = 360, heightDp = 640)
@Composable
private fun SeasonEpisodeSectionPreview() {
    SeasonEpisodeSection()
}
