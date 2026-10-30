package com.dealshot.app.presentation.mobile.player

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.dealshot.app.R
import com.dealshot.app.data.preferences.GuestPreviewRepository
import com.dealshot.app.data.video.Video
import com.dealshot.app.presentation.mobile.details.ShareBottomSheetMenu
import com.dealshot.app.presentation.shared.player.VideoPlayerIntent
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun MobilePlayerScreen(
    video: Video,
    state: com.dealshot.app.presentation.shared.player.VideoPlayerState,
    player: ExoPlayer,
    onIntent: (VideoPlayerIntent) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val fullBleed = state.isFullscreen || isLandscape

    var isControlsVisible by remember { mutableStateOf(true) }
    var isInMyList by remember { mutableStateOf(false) }
    var isLiked by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }

    if (showShareDialog) {
        ShareBottomSheetMenu(
            videoTitle = video.title.ifEmpty { state.title },
            videoUrl = video.url.ifEmpty { state.videoUrl },
            shareText = "${video.title}\n\n${video.url}",
            onDismiss = { showShareDialog = false }
        )
    }

    LaunchedEffect(player) {
        while (true) {
            val currentPosition = player.currentPosition
            val duration = player.duration.coerceAtLeast(0L)
            val playing = player.isPlaying

            onIntent(
                VideoPlayerIntent.PlaybackProgressChanged(
                    currentPosition = currentPosition,
                    duration = duration
                )
            )

            onIntent(
                VideoPlayerIntent.PlaybackStateChanged(
                    isPlaying = playing
                )
            )

            delay(250)
        }
    }

    if (state.showSubscriptionPopup) {
        AlertDialog(
            onDismissRequest = { /* Prevent dismiss by tapping outside */ },
            title = {
                Text("Continue watching", fontWeight = FontWeight.Bold, color = Color.Black)
            },
            text = {
                Text(
                    "Your 15-second preview has ended. Login to watch the full video.",
                    color = Color.DarkGray
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onIntent(VideoPlayerIntent.SubscriptionPopupDismissed)
                    onIntent(VideoPlayerIntent.SubscriptionClicked)
                }) {
                    Text("OK", color = Color(0xFF007AFF), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    onIntent(VideoPlayerIntent.SubscriptionPopupDismissed)
                }) {
                    Text("Not now", color = Color(0xFF007AFF))
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    /*
     * ------------------------------------------------------------
     * AUTO HIDE CONTROLS
     * ------------------------------------------------------------
     */
    LaunchedEffect(isControlsVisible) {
        if (isControlsVisible) {
            delay(3000.milliseconds)
            isControlsVisible = false
        }
    }

    BackHandler {
        if (isLandscape || state.isFullscreen) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            if (state.isFullscreen) {
                onIntent(VideoPlayerIntent.FullscreenClicked)
            }
        } else {
            onIntent(VideoPlayerIntent.BackClicked)
        }
    }

    DisposableEffect(player) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    /*
     * ------------------------------------------------------------
     * FULLSCREEN SYSTEM BAR HANDLING
     * ------------------------------------------------------------
     */
    LaunchedEffect(isLandscape, fullBleed) {
        activity?.let { act ->
            val window = act.window
            val controller = WindowCompat.getInsetsController(window, window.decorView)

            if (isLandscape || fullBleed) {
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            activity?.let { act ->
                val window = act.window
                val controller = WindowCompat.getInsetsController(window, window.decorView)
                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    /*
     * ------------------------------------------------------------
     * PLAYER LISTENER
     * ------------------------------------------------------------
     */
    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                onIntent(VideoPlayerIntent.PlaybackStateChanged(isPlaying = playing))
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    onIntent(
                        VideoPlayerIntent.PlaybackProgressChanged(
                            currentPosition = player.currentPosition,
                            duration = player.duration.coerceAtLeast(0L)
                        )
                    )
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e("EXO_ERROR", error.message ?: "")
            }
        }

        player.addListener(listener)

        onDispose {
            player.removeListener(listener)
        }
    }

    if (fullBleed) {
        // FULLSCREEN MODE
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        isControlsVisible = !isControlsVisible
                    }
            ) {
                AndroidView(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF111111)),
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            this.player = player
                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                            useController = false
                        }
                    },
                    update = { playerView ->
                        playerView.player = player
                    }
                )

                AnimatedVisibility(
                    visible = isControlsVisible,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f))
                    ) {
                        // Top Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter)
                                .zIndex(100f)
                                .windowInsetsPadding(WindowInsets.displayCutout)
                                .windowInsetsPadding(
                                    WindowInsets.safeDrawing.only(
                                        WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                                    )
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { onIntent(VideoPlayerIntent.BackClicked) }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.chevron_left),
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }

                            IconButton(onClick = { onIntent(VideoPlayerIntent.FullscreenClicked) }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.arrow_up_left_and_arrow_down_right),
                                    contentDescription = "Fullscreen",
                                    tint = Color.White
                                )
                            }
                        }

                        // Center Controls
                        Row(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalArrangement = Arrangement.spacedBy(40.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    val targetMs = (player.currentPosition - 10000L).coerceAtLeast(0L)
                                    player.seekTo(targetMs)
                                },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.gobackward_10),
                                    contentDescription = "Rewind 10s",
                                    tint = Color.White,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            IconButton(
                                onClick = {
                                    if (!state.isLoggedIn && (state.isPreviewExhausted || player.currentPosition >= (GuestPreviewRepository.GUEST_PREVIEW_LIMIT_MS - 500L))) {
                                        player.pause()
                                        player.seekTo(GuestPreviewRepository.GUEST_PREVIEW_LIMIT_MS)
                                        onIntent(VideoPlayerIntent.PlayPauseClicked)
                                    } else {
                                        if (player.isPlaying) {
                                            player.pause()
                                        } else {
                                            player.play()
                                        }
                                        onIntent(VideoPlayerIntent.PlayPauseClicked)
                                    }
                                },
                                modifier = Modifier.size(64.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = if (state.isPlaying) R.drawable.pause_fill else R.drawable.play_fill),
                                    contentDescription = if (state.isPlaying) "Pause" else "Play",
                                    tint = Color.White,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            IconButton(
                                onClick = {
                            val targetMs = (player.currentPosition + 10000L).coerceAtMost(state.totalDuration)
                            player.seekTo(targetMs)
                            if (!state.isLoggedIn && state.isPreviewExhausted) {
                                player.pause()
                                onIntent(VideoPlayerIntent.PlayPauseClicked)
                            }
                        },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.goforward_10),
                                    contentDescription = "Forward 10s",
                                    tint = Color.White,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        // Bottom Controls
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .windowInsetsPadding(
                                    WindowInsets.safeDrawing.only(
                                        WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal
                                    )
                                )
                                .padding(horizontal = 24.dp, vertical = 20.dp)
                        ) {
                            Text(
                                text = formatDigitalClock(state.totalDuration - state.currentPlaybackPosition),
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal,
                                modifier = Modifier.align(Alignment.End)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            val interactionSource = remember { MutableInteractionSource() }
                            var sliderPosition by remember { mutableFloatStateOf(0f) }
                            var isSeeking by remember { mutableStateOf(false) }

                            Slider(
                                value = if (isSeeking)
                                    sliderPosition
                                else
                                    if (state.totalDuration > 0) state.currentPlaybackPosition.toFloat() / state.totalDuration else 0f,
                                onValueChange = {
                            isSeeking = true
                            sliderPosition = it
                        },
                        onValueChangeFinished = {
                            val targetMs = (sliderPosition * state.totalDuration).toLong().coerceAtMost(state.totalDuration)
                            player.seekTo(targetMs)
                            if (!state.isLoggedIn && state.isPreviewExhausted) {
                                player.pause()
                                onIntent(VideoPlayerIntent.PlayPauseClicked)
                            }
                            isSeeking = false
                        },
                                interactionSource = interactionSource,
                                modifier = Modifier.fillMaxWidth(),
                                colors = SliderDefaults.colors(
                                    activeTrackColor = Color.White,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.3f),
                                    thumbColor = Color.White
                                ),
                                track = { sliderState ->
                                    SliderDefaults.Track(
                                        sliderState = sliderState,
                                        modifier = Modifier.height(3.dp),
                                        colors = SliderDefaults.colors(
                                            activeTrackColor = Color.White,
                                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                                        )
                                    )
                                },
                                thumb = { sliderState ->
                                    SliderDefaults.Thumb(
                                        interactionSource = interactionSource,
                                        colors = SliderDefaults.colors(thumbColor = Color.White),
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    } else {
        // NON-FULLSCREEN MODE (Framed player at top + Content below)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F0F11))
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            // 1. TOP FRAMED VIDEO PLAYER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        isControlsVisible = !isControlsVisible
                    }
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            this.player = player
                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                            useController = false
                        }
                    },
                    update = { playerView ->
                        playerView.player = player
                    }
                )

                androidx.compose.animation.AnimatedVisibility(
                    visible = isControlsVisible,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f))
                    ) {
                        // Top Bar Overlay (Back & Fullscreen buttons)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { onIntent(VideoPlayerIntent.BackClicked) }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.chevron_left),
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }

                            IconButton(onClick = { onIntent(VideoPlayerIntent.FullscreenClicked) }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.arrow_up_left_and_arrow_down_right),
                                    contentDescription = "Fullscreen",
                                    tint = Color.White
                                )
                            }
                        }

                        // Center Controls (10s rewind, Play/Pause, 10s forward)
                        Row(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalArrangement = Arrangement.spacedBy(32.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    val targetMs = (player.currentPosition - 10000L).coerceAtLeast(0L)
                                    player.seekTo(targetMs)
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.gobackward_10),
                                    contentDescription = "Rewind 10s",
                                    tint = Color.White,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.3f))
                                    .clickable {
                                        if (!state.isLoggedIn && (state.isPreviewExhausted || player.currentPosition >= (GuestPreviewRepository.GUEST_PREVIEW_LIMIT_MS - 500L))) {
                                            player.pause()
                                            player.seekTo(GuestPreviewRepository.GUEST_PREVIEW_LIMIT_MS)
                                            onIntent(VideoPlayerIntent.PlayPauseClicked)
                                        } else {
                                            if (player.isPlaying) {
                                                player.pause()
                                            } else {
                                                player.play()
                                            }
                                            onIntent(VideoPlayerIntent.PlayPauseClicked)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = if (state.isPlaying) R.drawable.pause_fill else R.drawable.play_fill),
                                    contentDescription = if (state.isPlaying) "Pause" else "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                            val targetMs = (player.currentPosition + 10000L).coerceAtMost(state.totalDuration)
                            player.seekTo(targetMs)
                            if (!state.isLoggedIn && state.isPreviewExhausted) {
                                player.pause()
                                onIntent(VideoPlayerIntent.PlayPauseClicked)
                            }
                        },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.goforward_10),
                                    contentDescription = "Forward 10s",
                                    tint = Color.White,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        // Bottom Control Container on Framed Player
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Playing ${video.title.ifEmpty { state.title.ifEmpty { "Video" } }}",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                            }

                            val interactionSource = remember { MutableInteractionSource() }
                            var sliderPosition by remember { mutableFloatStateOf(0f) }
                            var isSeeking by remember { mutableStateOf(false) }

                            Slider(
                                value = if (isSeeking)
                                    sliderPosition
                                else
                                    if (state.totalDuration > 0) state.currentPlaybackPosition.toFloat() / state.totalDuration else 0f,
                                onValueChange = {
                            isSeeking = true
                            sliderPosition = it
                        },
                        onValueChangeFinished = {
                            val targetMs = (sliderPosition * state.totalDuration).toLong().coerceAtMost(state.totalDuration)
                            player.seekTo(targetMs)
                            if (!state.isLoggedIn && state.isPreviewExhausted) {
                                player.pause()
                                onIntent(VideoPlayerIntent.PlayPauseClicked)
                            }
                            isSeeking = false
                        },
                                interactionSource = interactionSource,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(20.dp),
                                colors = SliderDefaults.colors(
                                    activeTrackColor = Color(0xFFFF5722),
                                    inactiveTrackColor = Color.White.copy(alpha = 0.3f),
                                    thumbColor = Color(0xFFFF5722)
                                ),
                                track = { sliderState ->
                                    SliderDefaults.Track(
                                        sliderState = sliderState,
                                        modifier = Modifier.height(3.dp),
                                        colors = SliderDefaults.colors(
                                            activeTrackColor = Color(0xFFFF5722),
                                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                                        )
                                    )
                                },
                                thumb = { sliderState ->
                                    SliderDefaults.Thumb(
                                        interactionSource = interactionSource,
                                        colors = SliderDefaults.colors(thumbColor = Color(0xFFFF5722)),
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = formatDigitalClock(state.currentPlaybackPosition),
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 11.sp
                                )
                                val remainingMs = (state.totalDuration - state.currentPlaybackPosition).coerceAtLeast(0L)
                                Text(
                                    text = "-${formatDigitalClock(remainingMs)}",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // 2. CONTENT BELOW PLAYER
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {

                // ROW 2: Metadata row (Duration • Category • Premium tag)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val durationText = video.duration.ifEmpty {
                        if (state.totalDuration > 0) formatDigitalClock(state.totalDuration) else "9:32"
                    }
                    Text(
                        text = durationText,
                        fontSize = 13.sp,
                        color = Color(0xFF999999)
                    )

                    Text(
                        text = "•",
                        fontSize = 13.sp,
                        color = Color(0xFF666666)
                    )

                    val categoryText = video.category.ifEmpty { "Academic" }
                    Text(
                        text = categoryText,
                        fontSize = 13.sp,
                        color = Color(0xFF999999)
                    )

                    Text(
                        text = "•",
                        fontSize = 13.sp,
                        color = Color(0xFF666666)
                    )

                    // Premium pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF4A2B14))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "Premium",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFF9500)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFF262628))
                )

                Spacer(modifier = Modifier.height(16.dp))
                

                Spacer(modifier = Modifier.height(28.dp))

                // Action Row 2: Dark card with My List, Like, Share
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1C1C1E))
                        .padding(vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // MY LIST
                        Column(
                            modifier = Modifier.clickable {
                                if (!isInMyList) {
                                    val title = video.title.ifEmpty { state.title.ifEmpty { "Video" } }
                                    Toast.makeText(context, "$title is added to the list", Toast.LENGTH_SHORT).show()
                                }
                                isInMyList = !isInMyList
                            },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                painter = painterResource(
                                    id = if (isInMyList) R.drawable.checkmark else R.drawable.plus
                                ),
                                contentDescription = if (isInMyList) "Added" else "My List",
                                tint = Color(0xFFFF7A00),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isInMyList) "Added" else "My List",
                                color = Color(0xFF8E8E93),
                                fontSize = 12.sp
                            )
                        }

                        // LIKE
                        Column(
                            modifier = Modifier.clickable {
                                if (!isLiked) {
                                    val title = video.title.ifEmpty { state.title.ifEmpty { "Video" } }
                                    Toast.makeText(context, "$title is added to the list", Toast.LENGTH_SHORT).show()
                                }
                                isLiked = !isLiked
                            },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                painter = painterResource(
                                    id = if (isLiked) R.drawable.heart_fill else R.drawable.heart
                                ),
                                contentDescription = if (isLiked) "Unlike" else "Like",
                                tint = Color(0xFFFF7A00),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Like",
                                color = Color(0xFF8E8E93),
                                fontSize = 12.sp
                            )
                        }

                        // SHARE
                        Column(
                            modifier = Modifier.clickable {
                                showShareDialog = true
                            },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.square_and_arrow_up),
                                contentDescription = "Share",
                                tint = Color(0xFFFF7A00),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Share",
                                color = Color(0xFF8E8E93),
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Sparkle icon at bottom right
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.sparkles),
                        contentDescription = null,
                        tint = Color(0xFF444446),
                        modifier = Modifier
                            .size(28.dp)
                            .align(Alignment.CenterEnd)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

/*
 * ================================================================
 * TIME FORMATTER
 * ================================================================
 */
fun formatDigitalClock(milliseconds: Long): String {
    val safeMilliseconds = milliseconds.coerceAtLeast(0L)
    val totalSeconds = (safeMilliseconds / 1000).toInt()
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (hours > 0) {
        String.format(
            Locale.getDefault(),
            "%d:%02d:%02d",
            hours,
            minutes,
            seconds
        )
    } else {
        String.format(
            Locale.getDefault(),
            "%02d:%02d",
            minutes,
            seconds
        )
    }
}
