package com.dealshot.app.presentation.tv.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dealshot.app.data.remote.dto.VideoDto
import com.dealshot.app.presentation.shared.home.HomeIntent
import com.dealshot.app.presentation.shared.home.HomeState
import com.dealshot.app.ui.theme.orange

private val FocusColor = Color(0xFFFF9800)

@Composable
fun TvHomeScreen(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        TvTopBar(state = state, onIntent = onIntent)

        when {
            state.isLoading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            state.error != null -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { Text(text = state.error, color = Color.White, fontSize = 22.sp) }

            else -> TvHomeContent(state = state, onIntent = onIntent)
        }
    }
}

@Composable
private fun TvTopBar(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp, vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Deal", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Text("Shot", color = orange, fontSize = 32.sp, fontWeight = FontWeight.Bold)

        Spacer(Modifier.width(40.dp))

        LazyRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.sections) { section ->
                val selected = state.selectedSection.equals(section, ignoreCase = true)
                TvChip(
                    label = section,
                    selected = selected,
                    onClick = { onIntent(HomeIntent.SectionClicked(section)) }
                )
            }
        }

        Spacer(Modifier.width(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TvChip("Liked", false) { onIntent(HomeIntent.LikedVideosClicked) }
            TvChip("My List", false) { onIntent(HomeIntent.MyListClicked) }
            TvChip("Log out", false) { onIntent(HomeIntent.LogoutClicked) }
        }
    }
}

@Composable
private fun TvChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(50.dp)

    Box(
        modifier = Modifier
            .clip(shape)
            .background(
                when {
                    focused -> Color.White
                    selected -> FocusColor
                    else -> Color(0xFF151515)
                }
            )
            .border(2.dp, if (focused) Color.White else Color.Transparent, shape)
            .onFocusChanged { focused = it.isFocused }
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (focused || selected) Color.Black else Color(0xFFE5E5E5),
            fontSize = 18.sp,
            fontWeight = if (selected || focused) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun TvHomeContent(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit
) {
    val allVideos = state.allVideos
    val playable = allVideos.filter { !it.category.isNullOrBlank() && !it.videoUrl.isNullOrBlank() }

    val rows: List<Pair<String, List<VideoDto>>>
    val heroVideos: List<VideoDto>

    when {
        state.selectedSection.equals("Home", ignoreCase = true) -> {
            heroVideos = state.bannerVideos
            rows = playable
                .map { it.category!!.trim() }
                .distinct()
                .sorted()
                .map { cat ->
                    cat to allVideos.filter {
                        it.category?.trim()?.equals(cat, ignoreCase = true) == true
                    }
                }
        }

        state.selectedSection.equals("Premium", ignoreCase = true) -> {
            val premium = allVideos.filter { it.isPremium && !it.videoUrl.isNullOrBlank() }
            heroVideos = premium
            rows = listOf("Premium" to premium)
        }

        else -> {
            val cat = state.selectedSection
            val vids = allVideos.filter {
                it.category?.trim()?.equals(cat, ignoreCase = true) == true
            }
            heroVideos = vids
            rows = listOf(cat to vids)
        }
    }

    if (rows.all { it.second.isEmpty() } && heroVideos.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No videos available", color = Color.White, fontSize = 22.sp)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(28.dp),
        contentPadding = PaddingValues(bottom = 48.dp)
    ) {
        if (heroVideos.isNotEmpty()) {
            item(key = "hero") {
                TvHero(video = heroVideos.first(), onIntent = onIntent)
            }
        }

        items(rows.filter { it.second.isNotEmpty() }, key = { it.first }) { (title, videos) ->
            TvVideoRow(title = title, videos = videos, onIntent = onIntent)
        }
    }
}

@Composable
private fun TvHero(
    video: VideoDto,
    onIntent: (HomeIntent) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp)
            .height(340.dp)
            .clip(RoundedCornerShape(24.dp))
    ) {
        AsyncImage(
            model = video.thumbnailUrl ?: video.thumbnail,
            contentDescription = video.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color(0x99000000), Color.Black)
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(32.dp)
        ) {
            Text(
                text = "DEALSHOT ORIGINAL",
                color = Color(0xFFFF8A00),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp
            )
            Spacer(Modifier.height(6.dp))
            video.title?.let {
                Text(
                    text = it,
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                TvHeroButton("Watch Now", primary = true) {
                    onIntent(HomeIntent.VideoClicked(video))
                }
                TvHeroButton("My List", primary = false) {
                    onIntent(HomeIntent.MyListClicked)
                }
            }
        }
    }
}

@Composable
private fun TvHeroButton(
    label: String,
    primary: Boolean,
    onClick: () -> Unit
) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(
                when {
                    focused -> FocusColor
                    primary -> Color.White
                    else -> Color(0xCC252525)
                }
            )
            .onFocusChanged { focused = it.isFocused }
            .clickable(onClick = onClick)
            .padding(horizontal = 28.dp, vertical = 14.dp)
    ) {
        Text(
            text = label,
            color = if (primary || focused) Color.Black else Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun TvVideoRow(
    title: String,
    videos: List<VideoDto>,
    onIntent: (HomeIntent) -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            TvChip("See all", false) { onIntent(HomeIntent.SeeAllClicked(title)) }
        }

        Spacer(Modifier.height(12.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 48.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(videos, key = { it.id }) { video ->
                TvVideoCard(video = video) {
                    onIntent(HomeIntent.VideoClicked(video))
                }
            }
        }
    }
}

@Composable
private fun TvVideoCard(
    video: VideoDto,
    onClick: () -> Unit
) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1.08f else 1f, label = "cardScale")
    val shape = RoundedCornerShape(14.dp)

    Column(
        modifier = Modifier
            .width(260.dp)
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
                .height(146.dp)
                .clip(shape)
                .border(3.dp, if (focused) FocusColor else Color.Transparent, shape)
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
