package com.dealshot.app.presentation.mobile.home

import android.util.Log
import android.view.LayoutInflater
import androidx.annotation.OptIn
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dealshot.app.presentation.shared.home.HomeIntent
import com.dealshot.app.presentation.shared.home.HomeState
import com.dealshot.app.ui.theme.orange
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.dealshot.app.data.preferences.UserPreferences
import com.dealshot.app.data.remote.dto.VideoDto
import com.dealshot.app.ui.theme.HintText
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.pager.PageSize
import androidx.compose.ui.draw.alpha
import androidx.media3.common.PlaybackException
import kotlinx.coroutines.delay
import com.dealshot.app.R
@kotlin.OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun HomeScreen(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit
) {


    val context = LocalContext.current
    val sections = state.sections
    // 1. Single shared ExoPlayer instance initialized once for the whole screen
    val sharedExoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            volume = 0f
            repeatMode = Player.REPEAT_MODE_ONE
        }
    }

    val featuredExoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            volume = 0f
            repeatMode = Player.REPEAT_MODE_ONE
        }
    }
    var showProfileDialog by remember {
        mutableStateOf(false)
    }

    // Tracks the globally playing video across all rows and sections
    var activeVideoUrl by remember { mutableStateOf<String?>(null) }

    // 2. Lifecycle cleanup: Releases hardware codecs when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            sharedExoPlayer.release()
            featuredExoPlayer.release()
        }
    }
    if (showProfileDialog) {
        ProfileDialog(
            onDismiss = {
                showProfileDialog = false
            },
            onLogout = {
                showProfileDialog = false

                onIntent(
                    HomeIntent.LogoutClicked
                )
            }
        )
    }
    val allVideos = state.allVideos

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 60.dp,
                    bottom = 16.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            )
            {
                Text(
                    text = "Deal",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Shot",
                    color = orange,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(
                onClick = {
                    onIntent(HomeIntent.LikedVideosClicked)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = "Liked Videos",
                    tint = orange
                )
            }

            TextButton(
                onClick = {
                    onIntent(HomeIntent.MyListClicked)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.BookmarkBorder,
                    contentDescription = "My List",
                    tint = orange
                )
            }

            IconButton(
                onClick = {
                    onIntent(HomeIntent.ProfileClicked)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile",
                    tint = orange,
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        // Category Header

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(
                top = 4.dp,
                bottom = 8.dp
            )
        ) {

            items(sections) { section ->

                val isSelected =
                    state.selectedSection.equals(
                        section,
                        ignoreCase = true
                    )

                Box(
                    modifier = Modifier
                        .clip(
                            RoundedCornerShape(50.dp)
                        )
                        .background(
                            color = if (isSelected) {
                                Color(0xFFFF9800)
                            } else {
                                Color(0xFF151515)
                            }
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) {
                                Color(0xFFFF9800)
                            } else {
                                Color(0xFF151515)
                            },
                            shape = RoundedCornerShape(50.dp)
                        )
                        .clickable(
                            indication = null,
                            interactionSource = remember {
                                MutableInteractionSource()
                            }
                        ) {

                            onIntent(
                                HomeIntent.SectionClicked(section)
                            )
                        }
                        .padding(
                            horizontal = 18.dp,
                            vertical = 10.dp
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = section,
                        color = if (isSelected) {
                            Color.Black
                        } else {
                            Color(0xFFE5E5E5)
                        },
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) {
                            FontWeight.Bold
                        } else {
                            FontWeight.Medium
                        }
                    )
                }
            }
        }


        // Video Categories
        if (state.isLoading) {

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

        } else if (state.error != null) {

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.error,
                    color = Color.White
                )
            }

        } else {

            val availableCategories = allVideos
                .filter { video ->
                    !video.category.isNullOrBlank() &&
                            !video.videoUrl.isNullOrBlank()
                }
                .map { video ->
                    video.category!!.trim()
                }
                .distinct()
                .sorted()

            when {

                state.selectedSection.equals("Home", ignoreCase = true) -> {

                    HomeContent(
                        bannerVideos = state.bannerVideos,
                        allVideos = allVideos,
                        availableCategories = availableCategories,
                        sharedExoPlayer = sharedExoPlayer,
                        featuredExoPlayer = featuredExoPlayer,
                        activeVideoUrl = activeVideoUrl,
                        myWatchLaterVideoIds = state.myWatchLaterVideoIds,
                        onActiveVideoUrlChanged = {
                            activeVideoUrl = it
                        },
                        onIntent = onIntent
                    )
                }

                state.selectedSection.equals("Premium", ignoreCase = true) -> {

                    val premiumVideos = allVideos.filter {
                        it.isPremium && !it.videoUrl.isNullOrBlank()
                    }

                    PremiumContent(
                        premiumVideos = premiumVideos,
                        sharedExoPlayer = sharedExoPlayer,
                        featuredExoPlayer = featuredExoPlayer,
                        activeVideoUrl = activeVideoUrl,
                        myWatchLaterVideoIds = state.myWatchLaterVideoIds,
                        onActiveVideoUrlChanged = {
                            activeVideoUrl = it
                        },
                        onIntent = onIntent
                    )
                }

                else -> {

                    val selectedCategory = state.selectedSection

                    val categoryVideos = allVideos.filter { video ->
                        video.category
                            ?.trim()
                            ?.equals(
                                selectedCategory,
                                ignoreCase = true
                            ) == true
                    }

                    CategoryContent(
                        selectedCategory = selectedCategory,
                        categoryVideos = categoryVideos,
                        sharedExoPlayer = sharedExoPlayer,
                        featuredExoPlayer = featuredExoPlayer,
                        activeVideoUrl = activeVideoUrl,
                        myWatchLaterVideoIds = state.myWatchLaterVideoIds,
                        onActiveVideoUrlChanged = {
                            activeVideoUrl = it
                        },
                        onIntent = onIntent
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumContent(
    premiumVideos: List<VideoDto>,
    sharedExoPlayer: ExoPlayer,
    featuredExoPlayer: ExoPlayer,
    activeVideoUrl: String?,
    myWatchLaterVideoIds: Set<String> = emptySet(),
    onActiveVideoUrlChanged: (String?) -> Unit,
    onIntent: (HomeIntent) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(28.dp),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            bottom = 24.dp
        )
    ) {
        item {
            if (premiumVideos.isNotEmpty()) {
                FeaturedVideoSection(
                    videos = premiumVideos,
                    exoPlayer = featuredExoPlayer,
                    myWatchLaterVideoIds = myWatchLaterVideoIds,
                    onWatchNow = { video ->
                        onIntent(HomeIntent.VideoClicked(video))
                    },
                    onMyList = { video ->
                        onIntent(HomeIntent.ToggleMyList(video))
                    }
                )
            }
        }

        item {
            if (premiumVideos.isNotEmpty()) {
                VideoCategorySection(
                    title = "Premium",
                    videos = premiumVideos,
                    sharedExoPlayer = sharedExoPlayer,
                    activeVideoUrl = activeVideoUrl,
                    onPlayVideo = { video ->
                        playVideo(
                            video = video,
                            activeVideoUrl = activeVideoUrl,
                            onActiveUrlChanged = onActiveVideoUrlChanged,
                            player = sharedExoPlayer
                        )
                    },
                    onSeeAll = { categoryName ->
                        onIntent(HomeIntent.SeeAllClicked(categoryName))
                    },
                    onVideoClick = { video ->
                        onIntent(HomeIntent.VideoClicked(video))
                    }
                )
            }
        }
    }
}


private fun playVideo(
    video: VideoDto,
    activeVideoUrl: String?,
    onActiveUrlChanged: (String?) -> Unit,
    player: ExoPlayer
) {
    val url = video.videoUrl?.trim()

    if (url.isNullOrBlank()) {
        return
    }

    if (activeVideoUrl == url) {
        return
    }

    onActiveUrlChanged(url)

    player.stop()
    player.clearMediaItems()

    player.setMediaItem(
        MediaItem.fromUri(url)
    )

    player.prepare()
    player.playWhenReady = true
}
@Composable
private fun CategoryContent(
    selectedCategory: String,
    categoryVideos: List<VideoDto>,
    sharedExoPlayer: ExoPlayer,
    featuredExoPlayer: ExoPlayer,
    activeVideoUrl: String?,
    myWatchLaterVideoIds: Set<String> = emptySet(),
    onActiveVideoUrlChanged: (String?) -> Unit,
    onIntent: (HomeIntent) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(28.dp),
        contentPadding = PaddingValues(
            bottom = 24.dp
        )
    ) {

        item {
            if (categoryVideos.isNotEmpty()) {

                FeaturedVideoSection(
                    videos = categoryVideos,
                    exoPlayer = featuredExoPlayer,
                    myWatchLaterVideoIds = myWatchLaterVideoIds,

                    onWatchNow = { video ->
                        onIntent(
                            HomeIntent.VideoClicked(video)
                        )
                    },

                    onMyList = { video ->
                        onIntent(
                            HomeIntent.ToggleMyList(video)
                        )
                    }
                )
            }
        }

        item {
            if (categoryVideos.isNotEmpty()) {

                VideoCategorySection(
                    title = selectedCategory,
                    videos = categoryVideos,
                    sharedExoPlayer = sharedExoPlayer,
                    activeVideoUrl = activeVideoUrl,

                    onPlayVideo = { video ->
                        playVideo(
                            video = video,
                            activeVideoUrl = activeVideoUrl,
                            onActiveUrlChanged = onActiveVideoUrlChanged,
                            player = sharedExoPlayer
                        )
                    },

                    onSeeAll = { categoryName ->
                        onIntent(
                            HomeIntent.SeeAllClicked(categoryName)
                        )
                    },

                    onVideoClick = { video ->
                        onIntent(
                            HomeIntent.VideoClicked(video)
                        )
                    }
                )
            }
        }
    }
}
@Composable
fun VideoCategorySection(
    title: String,
    videos: List<VideoDto>,
    sharedExoPlayer: ExoPlayer,
    activeVideoUrl: String?,
    onPlayVideo: (VideoDto) -> Unit,
    onVideoClick: (VideoDto) -> Unit,
    onSeeAll: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    )
    {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = title,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    onSeeAll(title)
                }
            ) {
                Text(
                    text = "See all",
                    color = HintText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "See all",
                    tint = orange,
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        LazyRow(
            contentPadding = PaddingValues(
                start = 12.dp,
                end = 12.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = videos,
                key = { video -> video.id }
            ) { video ->
                val isPlaying =
                    video.videoUrl != null && activeVideoUrl == video.videoUrl

                VideoCard(
                    video = video,
                    isPlaying = isPlaying,
                    exoPlayer = sharedExoPlayer,
                    onActive = { onPlayVideo(video) },
                    onClick = { onVideoClick(video) }
                )
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun VideoCard(
    video: VideoDto,
    isPlaying: Boolean,
    exoPlayer: ExoPlayer,
    onActive: () -> Unit,
    onClick: () -> Unit
) {
    // Tell the parent system to switch to this video file when selected

    Column(
        modifier = Modifier
            .width(190.dp)
            .clickable(onClick = onClick)
    ) {
        if (isPlaying) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        useController = false
                        resizeMode =
                            AspectRatioFrameLayout.RESIZE_MODE_FIT
                        player = exoPlayer
                    }
                },
                update = { playerView ->
                    if (playerView.player != exoPlayer) {
                        playerView.player = exoPlayer
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(14.dp)),
                onRelease = {
                    it.player = null
                }
            )
        } else {
            // Lightweight placeholder surface prevents OOM exceptions
            AsyncImage(
                model = video.thumbnailUrl ?: video.thumbnail,
                contentDescription = video.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(14.dp))
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        video.title?.let {
            Text(
                text = it,
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(5.dp))

        video.duration?.let {
            Text(
                text = it,
                color = Color(0xFF9E9E9E),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun ProfileDialog(
    onDismiss: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val userPreferences = UserPreferences(context)
    val mobileNumber: String? by userPreferences.mobileNumber
        .collectAsStateWithLifecycle(initialValue = null)
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable {
                    onDismiss()
                }
                .padding(
                    top = 70.dp,
                    end = 16.dp
                ),
            contentAlignment = Alignment.TopEnd
        ) {

            Column(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(32.dp)
                    )
                    .background(
                        Color(0xFF1E1E1E)
                    )
                    .clickable {
                        // Consume click so it does not dismiss
                    }
                    .width(
                        250.dp
                    )
                    .padding(
                        horizontal = 24.dp,
                        vertical = 22.dp
                    )
            ) {
                Text(
                    text = mobileNumber ?: "guest",
                    color = Color(0xFFBDBDBD),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal
                )

                Spacer(
                    modifier = Modifier.height(22.dp)
                )
//                val isGuest = mobileNumber.equals("guest",false)
                val isGuest = mobileNumber.isNullOrBlank()
                Text(
                    text = if (isGuest) "Log In" else "Log out",
                    color = if (isGuest) {
                        Color(0xFF1E6705)
                    } else {
                        Color(0xFFFF5252)
                    },
                    fontSize = 20.sp,
                    modifier = Modifier
                        .clickable {
                            onLogout()
                        }
                )
            }
        }
    }
}

@Composable
private fun HomeContent(
    bannerVideos: List<VideoDto>,
    allVideos: List<VideoDto>,
    availableCategories: List<String>,
    sharedExoPlayer: ExoPlayer,
    featuredExoPlayer: ExoPlayer,
    activeVideoUrl: String?,
    myWatchLaterVideoIds: Set<String> = emptySet(),
    onActiveVideoUrlChanged: (String?) -> Unit,
    onIntent: (HomeIntent) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(28.dp),
        contentPadding = PaddingValues(
            bottom = 24.dp
        )
    ) {

        // Featured Banner
        item {
            if (bannerVideos.isNotEmpty()) {
                FeaturedVideoSection(
                    videos = bannerVideos,
                    exoPlayer = featuredExoPlayer,
                    myWatchLaterVideoIds = myWatchLaterVideoIds,

                    onWatchNow = { video ->
                        onIntent(
                            HomeIntent.VideoClicked(video)
                        )
                    },

                    onMyList = { video ->
                        onIntent(
                            HomeIntent.ToggleMyList(video)
                        )
                    }
                )
            }
        }

        // Dynamic Categories
        items(
            items = availableCategories
        ) { category ->

            val categoryVideos = allVideos.filter { video ->
                video.category
                    ?.trim()
                    ?.equals(
                        category,
                        ignoreCase = true
                    ) == true
            }

            if (categoryVideos.isNotEmpty()) {

                VideoCategorySection(
                    title = category,
                    videos = categoryVideos,
                    sharedExoPlayer = sharedExoPlayer,
                    activeVideoUrl = activeVideoUrl,

                    onPlayVideo = { video ->
                        playVideo(
                            video = video,
                            activeVideoUrl = activeVideoUrl,
                            onActiveUrlChanged = onActiveVideoUrlChanged,
                            player = sharedExoPlayer
                        )
                    },
                    onSeeAll = { categoryName ->
                        onIntent(
                            HomeIntent.SeeAllClicked(categoryName)
                        )
                    },
                    onVideoClick = { video ->
                        onIntent(
                            HomeIntent.VideoClicked(video)
                        )
                    }
                )
            }
        }
    }
}

private const val BANNER_VIDEO_DELAY_MS = 5_000L

@OptIn(UnstableApi::class)
@Composable
private fun FeaturedVideoSection(
    videos: List<VideoDto>,
    exoPlayer: ExoPlayer,
    myWatchLaterVideoIds: Set<String> = emptySet(),
    onWatchNow: (VideoDto) -> Unit,
    onMyList: (VideoDto) -> Unit
) {
    if (videos.isEmpty()) return

    var isBuffering by remember {
        mutableStateOf(false)
    }

    var hasPlayerError by remember {
        mutableStateOf(false)
    }

    var isVideoReady by remember {
        mutableStateOf(false)
    }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { Int.MAX_VALUE }
    )

    /*
     * IMPORTANT:
     * Use settledPage for playback.
     */
    val currentIndex =
        pagerState.settledPage % videos.size

    val currentVideo =
        videos[currentIndex]

    /*
     * --------------------------------
     * PLAYER LISTENER
     * --------------------------------
     */

    DisposableEffect(exoPlayer) {

        val listener = object : Player.Listener {

            override fun onPlaybackStateChanged(
                playbackState: Int
            ) {

                isBuffering =
                    playbackState == Player.STATE_BUFFERING

            }

            override fun onRenderedFirstFrame() {

                isVideoReady = true
                isBuffering = false
            }

            override fun onIsPlayingChanged(
                isPlaying: Boolean
            ) {
            }

            override fun onPlayerError(
                error: PlaybackException
            ) {

                Log.e(
                    "FeaturedVideo",
                    "Banner player error",
                    error
                )

                hasPlayerError = true
                isBuffering = false
                isVideoReady = false
            }
        }

        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
        }
    }

    /*
     * --------------------------------
     * CHANGE BANNER
     * --------------------------------
     *
     * IMPORTANT:
     *
     */

    LaunchedEffect(pagerState.settledPage) {
        val currentIndex = pagerState.settledPage % videos.size
        val currentVideo = videos[currentIndex]

        // Immediately hide previous video frame
        isVideoReady = false
        hasPlayerError = false
        isBuffering = false

        exoPlayer.stop()
        exoPlayer.clearMediaItems()

        delay(BANNER_VIDEO_DELAY_MS)

        val videoUrl = currentVideo.promoVideoUrl

        if (!videoUrl.isNullOrBlank()) {
            try {
                exoPlayer.setMediaItem(
                    MediaItem.fromUri(videoUrl)
                )

                exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
                exoPlayer.volume = 0f
                exoPlayer.prepare()
                exoPlayer.playWhenReady = true
            } catch (e: Exception) {
                hasPlayerError = true
            }
        }
    }

    /*
     * --------------------------------
     * UI
     * --------------------------------
     */

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 10.dp,
                end = 10.dp,
                top = 12.dp,
                bottom = 24.dp
            )
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .clip(RoundedCornerShape(24.dp))
        ) {

            // 1. THUMBNAIL / PAGER
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp),
                pageSpacing = 12.dp,
                pageSize = PageSize.Fill
            ) { page ->

                val actualPage = page % videos.size
                val video = videos[actualPage]

                AsyncImage(
                    model = video.thumbnailUrl ?: video.thumbnail,
                    contentDescription = video.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(20.dp))
                )
            }

            // 2. VIDEO
            if (!hasPlayerError) {
                AndroidView(
                    factory = { context ->
                        LayoutInflater.from(context)
                            .inflate(
                                R.layout.view_banner_player,
                                null
                            ) as PlayerView
                    },
                    update = { playerView ->
                        playerView.player = exoPlayer
                    },
                    onRelease = { playerView ->
                        playerView.player = null
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(20.dp))
                        .alpha(
                            if (isVideoReady) 1f else 0f
                        )
                )
            }

            // 3. GRADIENT
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0x99000000),
                                Color(0xFF000000)
                            )
                        )
                    )
            )

            // 4. TEXT + BUTTONS
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(24.dp)
            ) {

                Text(
                    text = "DEALSHOT ORIGINAL",
                    color = Color(0xFFFF8A00),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp
                )

                Spacer(modifier = Modifier.height(5.dp))

                currentVideo.title?.let { title ->
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(7.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Button(
                        onClick = {
                            onWatchNow(currentVideo)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Watch Now"
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = "Watch Now",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    val isInMyWatchLater = currentVideo.id in myWatchLaterVideoIds

                    Button(
                        onClick = {
                            onMyList(currentVideo)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xCC252525),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = if (isInMyWatchLater) Icons.Default.Check else Icons.Default.Add,
                            contentDescription = if (isInMyWatchLater) "In My List" else "My List"
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(text = if (isInMyWatchLater) "Added" else "My List")
                    }
                }
            }

            // 5. INDICATORS
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                videos.forEachIndexed { index, _ ->

                    Box(
                        modifier = Modifier
                            .size(
                                width = if (index == currentIndex) {
                                    18.dp
                                } else {
                                    6.dp
                                },
                                height = 6.dp
                            )
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (index == currentIndex) {
                                    Color(0xFFFF8A00)
                                } else {
                                    Color.White.copy(alpha = 0.5f)
                                }
                            )
                    )
                }
            }
        }
    }
}







