package com.dealshot.app.presentation.mobile.player

import android.app.Activity
import android.app.Dialog
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.util.Log
import android.view.Window
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import com.dealshot.app.presentation.shared.player.VideoPlayerIntent
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds
import com.dealshot.app.R

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(UnstableApi::class)
@Composable
fun MobilePlayerScreen(
    state: com.dealshot.app.presentation.shared.player.VideoPlayerState,
    player: ExoPlayer,
    onIntent: (VideoPlayerIntent) -> Unit
) {
    Log.i("MobilePlayerScreen","MobilePlayerScreen")
    val context = LocalContext.current
    val activity = context as? Activity
    val isLandscape =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    /*
     * ------------------------------------------------------------
     * SAME LOCAL UI STATE AS YOUR ORIGINAL PLAYER
     * ------------------------------------------------------------
     */

    var showSubscriptionPopup by remember {
        mutableStateOf(false)
    }

    var hasShownSubscriptionPopup by remember {
        mutableStateOf(false)
    }

    val fullBleed =
        state.isFullscreen || isLandscape
    var isMuted by remember {
        mutableStateOf(false)
    }

    var isControlsVisible by remember {
        mutableStateOf(true)
    }

    /*
     * ------------------------------------------------------------
     * KEEP EXOPLAYER
     * ------------------------------------------------------------
     */

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

            if (
                !state.isPremiumUser &&
                !hasShownSubscriptionPopup &&
                currentPosition >= 15_000L && false
            ) {

                hasShownSubscriptionPopup = true

                player.pause()

                showSubscriptionPopup = true
            }

            delay(250)
        }
    }

    LaunchedEffect(showSubscriptionPopup) {
        if (showSubscriptionPopup ) {
            activity?.let { act ->

                showSubscriptionDialog(
                    activity = act as ComponentActivity,

                    onSubscribe = {
                        showSubscriptionPopup = false

                        // Use your existing subscription navigation here
                        onIntent(
                            VideoPlayerIntent.SubscriptionClicked
                        )
                    },

                    onNotNow = {
                        showSubscriptionPopup = false
                        player.play()
                    }
                )
            }
        }
    }
    /*
     * ------------------------------------------------------------
     * AUTO HIDE CONTROLS
     * SAME AS OLD PLAYER
     * ------------------------------------------------------------
     */

    LaunchedEffect(isControlsVisible) {

        if (isControlsVisible) {

            delay(3000.milliseconds)

            isControlsVisible = false
        }
    }

    /*
     * ------------------------------------------------------------
     * FULLSCREEN SYSTEM BAR HANDLING
     * SAME AS OLD PLAYER
     * ------------------------------------------------------------
     */

    LaunchedEffect(fullBleed) {

        activity?.let { act ->

            val window = act.window

            val controller =
                WindowCompat.getInsetsController(
                    window,
                    window.decorView
                )

            if (fullBleed) {

                act.requestedOrientation =
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

                controller.hide(
                    WindowInsetsCompat.Type.systemBars()
                )

                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat
                        .BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

            } else {

                act.requestedOrientation =
                    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

                controller.show(
                    WindowInsetsCompat.Type.systemBars()
                )
            }
        }
    }

    /*
     * ------------------------------------------------------------
     * PLAYER LISTENER
     * ------------------------------------------------------------
     */

    DisposableEffect(player) {

        val listener =
            object : Player.Listener {

                override fun onIsPlayingChanged(
                    playing: Boolean
                ) {
                    onIntent(
                        VideoPlayerIntent.PlaybackStateChanged(
                            isPlaying = playing
                        )
                    )
                }

                override fun onPlaybackStateChanged(
                    playbackState: Int
                ) {

                    if (
                        playbackState ==
                        Player.STATE_READY
                    ) {
                        onIntent(
                            VideoPlayerIntent.PlaybackProgressChanged(
                                currentPosition = player.currentPosition,
                                duration = player.duration.coerceAtLeast(0L)
                            )
                        )
                    }
                }

                override fun onPlayerError(
                    error: PlaybackException
                ) {
                    Log.e(
                        "EXO_ERROR",
                        error.message ?: ""
                    )
                }
            }

        player.addListener(listener)

        onDispose {
            player.removeListener(listener)
        }
    }

    /*
     * ------------------------------------------------------------
     * ORIGINAL PLAYER ROOT
     * ------------------------------------------------------------
     */

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)

            .then(
                if (!fullBleed) {

                    Modifier
                        .statusBarsPadding()
                        .padding(
                            start = 8.dp,
                            end = 8.dp,
                            bottom = 8.dp
                        )


                } else {

                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    )
    {


        if (!fullBleed) {

            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )
        }

    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .then(
                if (!fullBleed) {
                    Modifier
                        .statusBarsPadding()
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 8.dp
                        )
                } else {
                    Modifier
                }
            )
    )
    {
        if (!fullBleed) {
            Spacer(modifier = Modifier.height(16.dp))
        }

        // CLICKABLE VIDEO FRAME CONTAINER
        Box(
            modifier = (if (fullBleed)
                Modifier.fillMaxSize()
            else
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    ).clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    isControlsVisible = !isControlsVisible
                }
        )
        {
            // BASE LAYER: Video Player Stream
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(if (fullBleed) RoundedCornerShape(0.dp) else RoundedCornerShape(24.dp))
                    .background(Color(0xFF111111)),
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        this.player = player
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        useController = false
                    }
                },
                update = { playerView ->

                    playerView.player = player
                }
            )

            // OVERLAY CONTROLS
            // OVERLAY CONTROLS
            androidx.compose.animation.AnimatedVisibility(
                visible = isControlsVisible,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(
                            if (fullBleed) RoundedCornerShape(0.dp) else RoundedCornerShape(
                                24.dp
                            )
                        )
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    // A. TOP BAR OVERLAY
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .padding(
                                top = if (fullBleed) 20.dp else 0.dp
                            )
                            .windowInsetsPadding(WindowInsets.displayCutout)
                            .zIndex(100f)
                            .then(
                                if (fullBleed) {
                                    Modifier
                                        .windowInsetsPadding(
                                            WindowInsets.safeDrawing.only(
                                                WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                                            )
                                        )
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                } else {
                                    Modifier
                                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
                                        .padding(start = 16.dp)
                                }
                            ),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    )
                    {
                        IconButton(onClick = {
                            onIntent(VideoPlayerIntent.BackClicked)
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { /* Handle PiP */ }) {
                                Icon(
                                    imageVector = Icons.Default.PictureInPicture,
                                    contentDescription = "PiP",
                                    tint = Color.White
                                )
                            }
                            IconButton(onClick = { /* Handle Cast */ }) {
                                Icon(
                                    imageVector = Icons.Default.Cast,
                                    contentDescription = "Cast",
                                    tint = Color.White
                                )
                            }
                            IconButton(onClick = { onIntent(VideoPlayerIntent.SettingsClicked)}) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = Color.White
                                )
                            }
                            IconButton(onClick = {  onIntent(VideoPlayerIntent.FullscreenClicked)}) {
                                Icon(
                                    imageVector = if (fullBleed) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                    contentDescription = "Fullscreen",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    // B. CENTER PLAYBACK CONTROLS
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalArrangement = Arrangement.spacedBy(40.dp),
                        verticalAlignment = Alignment.CenterVertically
                    )
                    {
                        IconButton(
                            onClick = {
                                val targetMs = (player.currentPosition - 10000L).coerceAtLeast(0L)
                                player.seekTo(targetMs)
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Replay10,
                                contentDescription = "Rewind 10s",
                                tint = Color.White,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        IconButton(
                            onClick = {
                                if (player.isPlaying) {
                                    player.pause()
                                } else {
                                    player.play()
                                }

                                onIntent(
                                    VideoPlayerIntent.PlayPauseClicked
                                )
                            },
                            modifier = Modifier.size(64.dp)
                        ) {
                            Icon(
                                imageVector = if (state.isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                contentDescription = if (state.isPlaying) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        IconButton(
                            onClick = {
                                val targetMs = (player.currentPosition + 10000L).coerceAtMost(state.totalDuration)
                                player.seekTo(targetMs)
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Forward10,
                                contentDescription = "Forward 10s",
                                tint = Color.White,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    // C. BOTTOM CONTROL CONTAINER

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(
                                start = 8.dp,
                                end = 8.dp,
                                bottom = if (fullBleed) 20.dp else 0
                                    .dp
                            )
                            .then(
                                if (fullBleed) {
                                    Modifier.windowInsetsPadding(
                                        WindowInsets.safeDrawing.only(
                                            WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal
                                        )
                                    )
                                } else Modifier
                            )
                            .padding(horizontal = 16.dp, vertical = 16.dp)
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
                                player.seekTo((sliderPosition * state.totalDuration).toLong())
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

}


private fun showSubscriptionDialog(
    activity: ComponentActivity,
    onSubscribe: () -> Unit,
    onNotNow: () -> Unit
) {

    val dialog =
        Dialog(activity)

    dialog.requestWindowFeature(
        Window.FEATURE_NO_TITLE
    )

    dialog.setContentView(
        R.layout.dialog_subscription
    )

    dialog.setCancelable(false)

    dialog.window?.setBackgroundDrawableResource(
        android.R.color.transparent
    )

    val btnNotNow =
        dialog.findViewById<TextView>(
            R.id.btn_not_now
        )

    val btnOk =
        dialog.findViewById<TextView>(
            R.id.btn_ok
        )

    btnNotNow.setOnClickListener {

        dialog.dismiss()

        onNotNow()
    }

    btnOk.setOnClickListener {

        dialog.dismiss()

        onSubscribe()
    }

    dialog.show()
}

/*
 * ================================================================
 * TIME FORMATTER
 * ================================================================
 */

fun formatDigitalClock(milliseconds: Long): String {

    val safeMilliseconds =
        milliseconds.coerceAtLeast(0L)

    val totalSeconds =
        (safeMilliseconds / 1000).toInt()

    val hours =
        totalSeconds / 3600

    val minutes =
        (totalSeconds % 3600) / 60

    val seconds =
        totalSeconds % 60

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


