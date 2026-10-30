package com.dealshot.app.presentation.tv.player

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.dealshot.app.R
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.dealshot.app.presentation.shared.player.VideoPlayerIntent
import com.dealshot.app.presentation.shared.player.VideoPlayerState
import kotlinx.coroutines.delay
import java.util.Locale

private const val SEEK_STEP_MS = 10_000L
private const val CONTROLS_HIDE_MS = 4_000L


private val AccentOrange = Color(0xFFFF9800)

/**
 * TV player. Same playback / progress / subscription-gate behaviour as
 * MobilePlayerScreen, with a remote-control UI:
 *  - OK / Play-Pause: play or pause
 *  - Left / Right (and Rewind / Fast-forward keys): seek -10s / +10s
 *  - Up / Down: show controls
 *  - Back: exit the player
 */
@OptIn(UnstableApi::class)
@Composable
fun TvPlayerScreen(
    state: VideoPlayerState,
    player: ExoPlayer,
    isVideoUnavailable: Boolean,
    onIntent: (VideoPlayerIntent) -> Unit
) {


    var isControlsVisible by remember { mutableStateOf(true) }
    var interactionTick by remember { mutableIntStateOf(0) }
    var isBuffering by remember { mutableStateOf(true) }
    var hasPlayerError by remember { mutableStateOf(false) }

    val isPremiumUser by rememberUpdatedState(state.isPremiumUser)
    val rootFocus = remember { FocusRequester() }

    fun showControls() {
        isControlsVisible = true
        interactionTick++
    }

    fun seekBy(deltaMs: Long) {
        val maxAllowedMs = state.totalDuration
        val targetMs = (player.currentPosition + deltaMs).coerceIn(0L, maxAllowedMs)
        player.seekTo(targetMs)
        if (!state.isLoggedIn && state.isPreviewExhausted) {
            player.pause()
            onIntent(VideoPlayerIntent.PlayPauseClicked)
        }
        showControls()
    }

    fun togglePlayPause() {
        if (!state.isLoggedIn && state.isPreviewExhausted) {
            player.pause()
            onIntent(VideoPlayerIntent.PlayPauseClicked)
        } else {
            if (player.isPlaying) player.pause() else player.play()
            onIntent(VideoPlayerIntent.PlayPauseClicked)
        }
        showControls()
    }

    // Remote Back leaves the player (same as the mobile back button).
    BackHandler(enabled = !state.showSubscriptionPopup) {
        onIntent(VideoPlayerIntent.BackClicked)
    }

    // Keep key focus on the player root.
    LaunchedEffect(state.showSubscriptionPopup) {
        if (!state.showSubscriptionPopup) runCatching { rootFocus.requestFocus() }
    }

    // Progress ticker + 15s subscription gate (same logic as mobile).
    LaunchedEffect(player) {
        while (true) {
            val currentPosition = player.currentPosition
            val duration = player.duration.coerceAtLeast(0L)

            onIntent(
                VideoPlayerIntent.PlaybackProgressChanged(
                    currentPosition = currentPosition,
                    duration = duration
                )
            )
            onIntent(
                VideoPlayerIntent.PlaybackStateChanged(isPlaying = player.isPlaying)
            )

            // ViewModel handles preview limit logic via checkGuestPreviewLimit
            
            if (player.isPlaying && state.showSubscriptionPopup) {
                player.pause()
            }
            
            delay(250)
        }
    }

    // Auto-hide controls while playing.
    LaunchedEffect(isControlsVisible, interactionTick) {
        if (isControlsVisible) {
            delay(CONTROLS_HIDE_MS)
            isControlsVisible = false
        }
    }

    // Player listener.
    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                onIntent(VideoPlayerIntent.PlaybackStateChanged(isPlaying = playing))
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                isBuffering = playbackState == Player.STATE_BUFFERING
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
                hasPlayerError = true
                isBuffering = false
            }
        }
        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .onPreviewKeyEvent { event ->
                if (state.showSubscriptionPopup) return@onPreviewKeyEvent false
                if (event.type != KeyEventType.KeyDown) {
                    // Consume the matching key-up for keys we handle.
                    return@onPreviewKeyEvent event.key in HandledKeys
                }
                when (event.key) {
                    Key.DirectionCenter, Key.Enter, Key.NumPadEnter, Key.MediaPlayPause -> {
                        togglePlayPause(); true
                    }
                    Key.MediaPlay -> {
                        player.play(); showControls(); true
                    }
                    Key.MediaPause -> {
                        player.pause(); showControls(); true
                    }
                    Key.DirectionLeft, Key.MediaRewind -> {
                        seekBy(-SEEK_STEP_MS); true
                    }
                    Key.DirectionRight, Key.MediaFastForward -> {
                        seekBy(SEEK_STEP_MS); true
                    }
                    Key.DirectionUp, Key.DirectionDown -> {
                        showControls(); true
                    }
                    else -> false
                }
            }
            .focusRequester(rootFocus)
            .focusable()
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.player = player
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    useController = false
                    keepScreenOn = true
                    isFocusable = false
                    isFocusableInTouchMode = false
                }
            },
            update = { it.player = player },
            onRelease = { it.player = null }
        )

        if (isBuffering && !hasPlayerError && !isVideoUnavailable) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(56.dp),
                color = AccentOrange
            )
        }

        if (hasPlayerError || isVideoUnavailable) {
            Text(
                text = "This video can't be played right now.",
                color = Color.White,
                fontSize = 24.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        AnimatedVisibility(
            visible = isControlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            TvPlayerControls(state = state)
        }
    }

    if (state.showSubscriptionPopup) {
        TvSubscriptionDialog(
            onSubscribe = {
                onIntent(VideoPlayerIntent.SubscriptionPopupDismissed)
                onIntent(VideoPlayerIntent.SubscriptionClicked)
            },
            onNotNow = {
                onIntent(VideoPlayerIntent.SubscriptionPopupDismissed)
            }
        )
    }
}

private val HandledKeys = setOf(
    Key.DirectionCenter, Key.Enter, Key.NumPadEnter, Key.MediaPlayPause,
    Key.MediaPlay, Key.MediaPause, Key.DirectionLeft, Key.MediaRewind,
    Key.DirectionRight, Key.MediaFastForward, Key.DirectionUp, Key.DirectionDown
)

@Composable
private fun TvPlayerControls(state: VideoPlayerState) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Gradient scrims for readability.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(listOf(Color(0xCC000000), Color.Transparent))
                )
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(listOf(Color.Transparent, Color(0xE6000000)))
                )
        )

        Icon(
            painter = painterResource(
                id = if (state.isPlaying) R.drawable.pause_fill else R.drawable.play_fill
            ),
            contentDescription = if (state.isPlaying) "Pause" else "Play",
            tint = Color.White,
            modifier = Modifier
                .align(Alignment.Center)
                .size(96.dp)
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 64.dp, vertical = 40.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatClock(state.currentPlaybackPosition),
                    color = Color.White,
                    fontSize = 18.sp
                )
                Text(
                    text = formatClock(state.totalDuration - state.currentPlaybackPosition),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            Spacer(Modifier.height(10.dp))

            val progress = if (state.totalDuration > 0) {
                (state.currentPlaybackPosition.toFloat() / state.totalDuration).coerceIn(0f, 1f)
            } else 0f

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.White.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(6.dp)
                        .background(AccentOrange)
                )
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = "◀ ▶  Seek 10s     OK  Play / Pause     Back  Exit",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp
            )
        }
    }
}

/**
 * Compose replacement for the mobile XML dialog (R.layout.dialog_subscription),
 * whose TextViews are not focusable with a D-pad. Same two actions:
 * Subscribe -> SubscriptionClicked, Not now -> resume playback.
 */
@Composable
private fun TvSubscriptionDialog(
    onSubscribe: () -> Unit,
    onNotNow: () -> Unit
) {
    val subscribeFocus = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        runCatching { subscribeFocus.requestFocus() }
    }

    Dialog(
        onDismissRequest = onNotNow,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF1E1E1E))
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Login to keep watching",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Your free preview has ended.",
                color = Color(0xFFBDBDBD),
                fontSize = 18.sp
            )
            Spacer(Modifier.height(28.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                DialogButton(
                    label = "Not Now",
                    onClick = onNotNow
                )
                DialogButton(
                    label = "Login",
                    onClick = onSubscribe,
                    modifier = Modifier.focusRequester(subscribeFocus)
                )
            }
        }
    }
}

@Composable
private fun DialogButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(if (focused) AccentOrange else Color(0xFF333333))
            .border(2.dp, if (focused) Color.White else Color.Transparent, shape)
            .onFocusChanged { focused = it.isFocused }
            .clickable(onClick = onClick)
            .padding(horizontal = 32.dp, vertical = 14.dp)
    ) {
        Text(
            text = label,
            color = if (focused) Color.Black else Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun formatClock(milliseconds: Long): String {
    val totalSeconds = (milliseconds.coerceAtLeast(0L) / 1000).toInt()
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
}