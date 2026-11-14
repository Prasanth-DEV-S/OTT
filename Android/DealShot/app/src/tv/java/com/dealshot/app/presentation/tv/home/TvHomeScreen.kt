package com.dealshot.app.presentation.tv.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.ui.res.painterResource
import com.dealshot.app.R
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dealshot.app.data.remote.dto.VideoDto
import com.dealshot.app.presentation.shared.home.HomeIntent
import com.dealshot.app.presentation.shared.home.HomeState
import com.dealshot.app.ui.theme.DeepBlackBg
import com.dealshot.app.ui.theme.GoldPrimary
import com.dealshot.app.ui.theme.GoldTextLight

private val FocusGradient = Brush.horizontalGradient(
    listOf(
        Color(0xFF7A1515),
        Color(0xFF3B0B0B)
    )
)

private val UnfocusedTileBg = Color(0xFF15141A)

@Composable
fun TvHomeScreen(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit
) {
    var sidebarExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlackBg)
    ) {
        // Left Expandable Navigation Sidebar (Supports D-pad & Mouse Hover)
        TvNavigationSidebar(
            state = state,
            isExpanded = sidebarExpanded,
            onExpandChange = { sidebarExpanded = it },
            onIntent = onIntent
        )

        // Main Content Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            when {
                state.isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = GoldPrimary) }

                state.error != null -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { Text(text = state.error, color = Color.White, fontSize = 22.sp) }

                else -> TvHomeContent(state = state, onIntent = onIntent)
            }
        }
    }
}

private data class TvSidebarItem(
    val sectionName: String,
    val iconResId: Int? = null,
    val iconVector: ImageVector? = null,
    val label: String
)

@Composable
private fun TvNavigationSidebar(
    state: HomeState,
    isExpanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    onIntent: (HomeIntent) -> Unit
) {
    val items = listOf(
        TvSidebarItem("Home", iconVector = Icons.Default.Home, label = "Home"),
        TvSidebarItem("National", iconResId = R.drawable.globe, label = "National"),
        TvSidebarItem("International", iconResId = R.drawable.globe, label = "International"),
        TvSidebarItem("Motivation", iconVector = Icons.Default.StarOutline, label = "Motivation"),
        TvSidebarItem("Inspirational", iconVector = Icons.Default.Lightbulb, label = "Inspirational"),
        TvSidebarItem("Academic", iconVector = Icons.Default.School, label = "Academic"),
        TvSidebarItem("Settings", iconResId = R.drawable.ic_settings, label = "Settings")
    )

    val sidebarInteraction = remember { MutableInteractionSource() }
    val isSidebarHovered by sidebarInteraction.collectIsHoveredAsState()

    LaunchedEffect(isSidebarHovered) {
        if (isSidebarHovered) onExpandChange(true)
    }

    val sidebarWidth by animateDpAsState(
        targetValue = if (isExpanded || isSidebarHovered) 220.dp else 80.dp,
        label = "sidebarWidth"
    )

    Column(
        modifier = Modifier
            .width(sidebarWidth)
            .fillMaxHeight()
            .hoverable(sidebarInteraction)
            .background(Color(0xFF0D0C0E))
            .border(width = 1.dp, color = Color(0xFF221F28))
            .padding(vertical = 24.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Top Logo Badge: DS
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 28.dp, start = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .shadow(elevation = 12.dp, shape = RoundedCornerShape(14.dp), spotColor = GoldPrimary)
                    .clip(RoundedCornerShape(14.dp))
                    .background(GoldPrimary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "DS",
                    color = Color(0xFF140E02),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            AnimatedVisibility(
                visible = isExpanded || isSidebarHovered,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally()
            ) {
                Row {
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "DEALSHOT",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp,
                        maxLines = 1
                    )
                }
            }
        }

        // Navigation Items Column
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            items.forEach { item ->
                val isSelected = state.selectedSection.equals(item.sectionName, ignoreCase = true)
                TvSidebarRowItem(
                    item = item,
                    isSelected = isSelected,
                    isExpanded = isExpanded || isSidebarHovered,
                    onActive = { active ->
                        if (active) onExpandChange(true)
                    },
                    onClick = {
                        if (item.sectionName == "Settings") {
                            onIntent(HomeIntent.ProfileClicked)
                        } else {
                            onIntent(HomeIntent.SectionClicked(item.sectionName))
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun TvSidebarRowItem(
    item: TvSidebarItem,
    isSelected: Boolean,
    isExpanded: Boolean,
    onActive: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()

    val active = isFocused || isHovered || isSelected

    LaunchedEffect(isFocused, isHovered) {
        if (isFocused || isHovered) onActive(true)
    }

    val shape = RoundedCornerShape(14.dp)

    val backgroundBrush = if (active) {
        FocusGradient
    } else {
        Brush.horizontalGradient(listOf(UnfocusedTileBg, UnfocusedTileBg))
    }

    val borderColor = if (isFocused || isHovered) {
        GoldPrimary
    } else if (isSelected) {
        Color(0xFF9E6813)
    } else {
        Color.Transparent
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .hoverable(interactionSource)
            .clip(shape)
            .background(backgroundBrush)
            .border(
                width = if (isFocused || isHovered) 2.5.dp else if (isSelected) 1.5.dp else 0.dp,
                color = borderColor,
                shape = shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (item.iconResId != null) {
                Icon(
                    painter = painterResource(id = item.iconResId),
                    contentDescription = item.label,
                    tint = if (active) GoldPrimary else Color(0xFF888494),
                    modifier = Modifier.size(24.dp)
                )
            } else if (item.iconVector != null) {
                Icon(
                    imageVector = item.iconVector,
                    contentDescription = item.label,
                    tint = if (active) GoldPrimary else Color(0xFF888494),
                    modifier = Modifier.size(24.dp)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally()
            ) {
                Row {
                    Spacer(Modifier.width(14.dp))
                    Text(
                        text = item.label,
                        color = if (active) Color.White else Color(0xFFA8A4B4),
                        fontSize = 15.sp,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
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
            val categoryRows = playable
                .map { it.category!!.trim() }
                .filter { cat ->
                    !cat.equals("Banner", ignoreCase = true) && !cat.equals("images", ignoreCase = true)
                }
                .distinct()
                .sorted()
                .map { cat ->
                    cat to allVideos.filter {
                        it.category?.trim()?.equals(cat, ignoreCase = true) == true
                    }
                }
            val hasPremium = com.dealshot.app.data.subscription.hasPremiumAccess(state.subscriptionState)
            rows = if (!hasPremium && state.continueWatching.isNotEmpty()) {
                listOf("Continue Watching" to state.continueWatching) + categoryRows
            } else {
                categoryRows
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
        contentPadding = PaddingValues(top = 28.dp, bottom = 48.dp, start = 32.dp, end = 32.dp)
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
                color = GoldPrimary,
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
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()

    val active = isFocused || isHovered

    val shape = RoundedCornerShape(14.dp)

    val backgroundBrush = if (active) {
        FocusGradient
    } else if (primary) {
        Brush.horizontalGradient(listOf(Color.White, Color.White))
    } else {
        Brush.horizontalGradient(listOf(Color(0xCC252525), Color(0xCC252525)))
    }

    Box(
        modifier = Modifier
            .hoverable(interactionSource)
            .clip(shape)
            .background(backgroundBrush)
            .border(
                width = if (active) 2.5.dp else 0.dp,
                color = if (active) GoldPrimary else Color.Transparent,
                shape = shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 28.dp, vertical = 14.dp)
    ) {
        Text(
            text = label,
            color = if (active) Color.White else if (primary) Color.Black else Color.White,
            fontSize = 18.sp,
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
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            TvChip("See all", false) { onIntent(HomeIntent.SeeAllClicked(title)) }
        }

        Spacer(Modifier.height(12.dp))

        LazyRow(
            contentPadding = PaddingValues(vertical = 12.dp),
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
private fun TvChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()

    val active = isFocused || isHovered || selected

    val shape = RoundedCornerShape(50.dp)

    val backgroundBrush = if (active) {
        FocusGradient
    } else {
        Brush.horizontalGradient(listOf(UnfocusedTileBg, UnfocusedTileBg))
    }

    Box(
        modifier = Modifier
            .hoverable(interactionSource)
            .clip(shape)
            .background(backgroundBrush)
            .border(
                width = if (isFocused || isHovered) 2.5.dp else if (selected) 1.5.dp else 0.dp,
                color = if (isFocused || isHovered) GoldPrimary else if (selected) Color(0xFFC48D2A) else Color.Transparent,
                shape = shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 22.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (active) Color.White else Color(0xFFE5E5E5),
            fontSize = 16.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun TvVideoCard(
    video: VideoDto,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()

    val active = isFocused || isHovered
    val scale by animateFloatAsState(if (active) 1.08f else 1f, label = "cardScale")
    val shape = RoundedCornerShape(14.dp)

    Column(
        modifier = Modifier
            .width(260.dp)
            .hoverable(interactionSource)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(146.dp)
                .clip(shape)
                .background(if (active) Color(0xFF380B0B) else UnfocusedTileBg)
                .border(
                    width = if (active) 3.dp else 0.dp,
                    color = if (active) GoldPrimary else Color.Transparent,
                    shape = shape
                )
        ) {
            AsyncImage(
                model = video.thumbnailUrl ?: video.thumbnail,
                contentDescription = video.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(Modifier.height(8.dp))

        video.title?.let {
            Text(
                text = it,
                color = if (active) GoldTextLight else Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        video.duration?.let {
            Text(text = it, color = Color(0xFF9E9E9E), fontSize = 13.sp)
        }
    }
}
