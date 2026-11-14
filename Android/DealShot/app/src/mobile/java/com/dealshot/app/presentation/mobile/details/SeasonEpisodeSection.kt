package com.dealshot.app.presentation.mobile.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
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

// STYLE SPECIFICATIONS:
// - Background: #0E1117
// - Primary text: white (#FFFFFF)
// - Secondary metadata: bright blue (#3B82F6)
// - Dividers: #1F2530
// - Font: 16px side padding
private val ScreenBg = Color(0xFF0E1117)
private val PrimaryTextColor = Color.White
private val MetadataTextColor = Color(0xFF3B82F6)
private val DividerColor = Color(0xFF1F2530)

data class EpisodeItemData(
    val title: String,
    val seasonEpisodeMeta: String,
    val imageUrl: String,
    val gradientColors: List<Color>
)

val demoEpisodeList = listOf(
    EpisodeItemData(
        title = "The House of SS",
        seasonEpisodeMeta = "S1 E1 • 27 May 2026 • 46m",
        imageUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=500",
        gradientColors = listOf(Color(0xFF3D2A1D), Color(0xFF1A120B))
    ),
    EpisodeItemData(
        title = "Jayshree's Second Chance",
        seasonEpisodeMeta = "S1 E2 • 27 May 2026 • 22m",
        imageUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500",
        gradientColors = listOf(Color(0xFF1C2D42), Color(0xFF0D1724))
    ),
    EpisodeItemData(
        title = "The Silent Dinner",
        seasonEpisodeMeta = "S1 E3 • 27 May 2026 • 31m",
        imageUrl = "https://images.unsplash.com/photo-1511795409834-ef04bbd61622?w=500",
        gradientColors = listOf(Color(0xFF3E3218), Color(0xFF1B160A))
    ),
    EpisodeItemData(
        title = "Cracks in the Walls",
        seasonEpisodeMeta = "S1 E4 • 27 May 2026 • 28m",
        imageUrl = "https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?w=500",
        gradientColors = listOf(Color(0xFF2B3038), Color(0xFF12151A))
    )
)

/**
 * Single static frame showing the Season 1 tab and 4 episode rows.
 */
@Composable
fun SeasonEpisodeSection(
    seasonName: String = "Season 1",
    episodes: List<EpisodeItemData> = demoEpisodeList,
    onEpisodeClick: (EpisodeItemData) -> Unit = {},
    onDownloadClick: (EpisodeItemData) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
    ) {
        // SEASON TAB
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                    .width(IntrinsicSize.Max)
            ) {
                // Text "Season 1" in bold white (22px), left-aligned
                Text(
                    text = seasonName,
                    color = PrimaryTextColor,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                // A 3px white underline indicator directly beneath the text
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(PrimaryTextColor)
                )
            }
            // A full-width 1px divider line below it
            HorizontalDivider(
                color = DividerColor,
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // EPISODE LIST (4 rows, 16px vertical spacing between rows)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            episodes.forEach { episode ->
                EpisodeRowItem(
                    episode = episode,
                    onEpisodeClick = { onEpisodeClick(episode) },
                    onDownloadClick = { onDownloadClick(episode) }
                )
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
        // LEFT: 16:9 thumbnail (about 310x174px), 10px rounded corners
        Box(
            modifier = Modifier
                .width(140.dp)
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(10.dp))
                .background(Brush.linearGradient(episode.gradientColors))
        ) {
            AsyncImage(
                model = episode.imageUrl,
                contentDescription = episode.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Soft dark gradient overlay
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

            // Small white play icon in the bottom-left corner
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

        Spacer(modifier = Modifier.width(12.dp))

        // MIDDLE: Episode title (white, bold, 20px, max 2 lines), and below it "S1 E# • date • duration" (blue, 15px)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = episode.title,
                color = PrimaryTextColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = episode.seasonEpisodeMeta,
                color = MetadataTextColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // RIGHT: A thin white download icon (arrow down into a tray), vertically centered
        Icon(
            painter = painterResource(id = R.drawable.ic_download),
            contentDescription = "Download Episode",
            tint = Color.White,
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .clickable(onClick = onDownloadClick)
                .padding(8.dp)
                .size(24.dp)
        )
    }
}

@Preview(name = "Season 1 & Episode List - 1080px Wide", widthDp = 360, heightDp = 640)
@Composable
private fun SeasonEpisodeSectionPreview() {
    SeasonEpisodeSection()
}
