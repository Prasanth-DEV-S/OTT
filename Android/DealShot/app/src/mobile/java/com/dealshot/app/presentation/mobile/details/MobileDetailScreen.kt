package com.dealshot.app.presentation.mobile.details

import android.app.Dialog
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
import android.view.Window
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import com.dealshot.app.R
import com.dealshot.app.data.preferences.PreferenceManager
import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.dealshot.app.presentation.mobile.player.formatDigitalClock
import kotlinx.coroutines.delay
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
    onShareClick: () -> Unit = {},
    onLoginClick: () -> Unit = {},
    isPremiumUser: Boolean = false,
    onSubscribeClick: () -> Unit = {}
) {
    val context = LocalContext.current

    val preferenceManager = PreferenceManager(context)
    val heartScale by animateFloatAsState(
        targetValue = if (isLiked) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "heartScale"
    )
    val activity = context as? Activity

    val player = remember { ExoPlayer.Builder(context).build() }
    var isPlayerVisible by remember { mutableStateOf(false) }
    var isPromoPlaying by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }
    var hasShownSubscriptionPopup by remember { mutableStateOf(false) }

    var position by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var playing by remember { mutableStateOf(false) }

    // Release player + restore screen when leaving
    DisposableEffect(Unit) {
        onDispose {
            player.release()
            activity?.let { act ->
                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                WindowCompat.getInsetsController(act.window, act.window.decorView)
                    .show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    LaunchedEffect(video.promoVideoUrl, video.url) {
        delay(5_000L)

        val promoUrl = video.promoVideoUrl.trim()

        if (
            promoUrl.isNotBlank() &&
            !isPlayerVisible &&
            !isPromoPlaying
        ) {
            player.setMediaItem(MediaItem.fromUri(promoUrl))
            player.prepare()
            player.play()

            isPromoPlaying = true
        }
    }

    // Phone back button exits fullscreen first
    BackHandler(enabled = isFullscreen) { isFullscreen = false }

    // Fullscreen: rotate + hide system bars
    LaunchedEffect(isFullscreen) {
        activity?.let { act ->
            val controller = WindowCompat.getInsetsController(act.window, act.window.decorView)
            if (isFullscreen) {
                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    // Progress + 30 second subscription popup (same logic as the player screen)
    LaunchedEffect(isPlayerVisible) {
        if (!isPlayerVisible) return@LaunchedEffect
        while (true) {
            position = player.currentPosition
            duration = player.duration.coerceAtLeast(0L)
            playing = player.isPlaying

            if (!isPremiumUser && !hasShownSubscriptionPopup && position >= 30_000L) {
                hasShownSubscriptionPopup = true
                player.pause()

                if (preferenceManager.getString("Mobile_No").isNullOrBlank()) {
                    activity?.let { act ->
                        showSubscriptionDialog(
                            activity = act as ComponentActivity,
                            onSubscribe = { onSubscribeClick() },
                            onNotNow = {
//                                player.play()
                                onBackClick()
                            }
                        )
                    }
                }
            }
            delay(250)
        }
    }

    val startPlayback = {
        val actualVideoUrl = video.url.trim()

        if (actualVideoUrl.isNotBlank()) {
            isPromoPlaying = false

            player.setMediaItem(
                MediaItem.fromUri(actualVideoUrl)
            )
            player.prepare()

            isPlayerVisible = true
            player.play()
        }
    }

    // Fullscreen: show only the player
    if (isFullscreen) {
        InlinePlayer(
            player = player,
            isFullscreen = true,
            isPlaying = playing,
            position = position,
            duration = duration,
            onFullscreenClick = { isFullscreen = false },
            modifier = Modifier.fillMaxSize(),
            showControls = true,
            onBackClick = onBackClick
        )
        return
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // ------------------------------------------------------------
        // HERO: thumbnail, or inline player once Play is pressed
        // ------------------------------------------------------------

        if (isPlayerVisible || isPromoPlaying) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(18.dp))
            ) {
                InlinePlayer(
                    player = player,
                    isFullscreen = false,
                    isPlaying = playing,
                    position = position,
                    duration = duration,
                    onFullscreenClick = { isFullscreen = true },
                    showControls = isPlayerVisible,
                    modifier = Modifier.fillMaxSize(),
                    onBackClick = onBackClick
                )

// Show back arrow on promo video always.
// On the actual video, show it only when player controls are visible.
                if (isPromoPlaying || !isPlayerVisible) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .zIndex(10f)
                            .background(
                                Color.Black.copy(alpha = 0.5f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        } else {
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
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

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

                // Back arrow over the thumbnail
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .zIndex(10f)
                        .background(
                            Color.Black.copy(alpha = 0.5f),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
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

            if (!isPlayerVisible) {
                Button(
                    onClick = { startPlayback() },
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

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "Watch Now",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
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
                    /*onClick = if (preferenceManager.getString("Mobile_No").isNullOrBlank()) {
                        onLoginClick()
                    } else {
                        onMyListClick()
                    }*/
                    /* this for Log out if the no number 14102026*/
                            onClick = {
                        if (preferenceManager.getString("Mobile_No").isNullOrBlank()) {
//                            onLoginClick()
                            activity?.let { act ->
                                showLoginDialog(
                                    activity = act as ComponentActivity,
                                    onSubscribe = { onSubscribeClick() },
                                    onNotNow = { onBackClick() },
                                    "Continue watching",
                                    "Please log in to Add this video to your list."
                                )
                            }
                        } else {
                            onMyListClick()
                        }
                    }
                )
                Column(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .clickable {
                            /* this for Log out if the no number14102026*/
                            if (preferenceManager.getString("Mobile_No").isNullOrBlank()) {
//                                onLoginClick()
                                activity?.let { act ->
                                    showLoginDialog(
                                        activity = act as ComponentActivity,
                                        onSubscribe = { onSubscribeClick() },
                                        onNotNow = { onBackClick() },
                                        "Continue watching",
                                        "Please log in to like this video."
                                    )
                                }
                            } else {
                                onLikeClick()
                            }
                        },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (isLiked) {
                            Icons.Filled.ThumbUp
                        } else {
                            Icons.Outlined.ThumbUp
                        },
                        contentDescription = if (isLiked) "Unlike" else "Like",
                        tint = if (isLiked) {
                            orange
                        } else {
                            orange
                        },
                        modifier = Modifier
                            .size(28.dp)
                            .scale(heartScale)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Like",
                        color = orange,
                        fontSize = 13.sp
                    )
                }

                DetailsActionItem(
                    icon = Icons.Default.Share,
                    label = "Share",
                    onClick = {
                        if (preferenceManager.getString("Mobile_No").isNullOrBlank()) {
//                                onLoginClick()
                            activity?.let { act ->
                                showLoginDialog(
                                    activity = act as ComponentActivity,
                                    onSubscribe = { onSubscribeClick() },
                                    onNotNow = { onBackClick() },
                                    "Continue watching",
                                    "Please log in to share this video."
                                )
                            }
                        } else {
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

                    }
                )
            }

            Spacer(
                modifier = Modifier.height(10.dp)
            )

        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun InlinePlayer(
    player: ExoPlayer,
    isFullscreen: Boolean,
    isPlaying: Boolean,
    position: Long,
    duration: Long,
    onFullscreenClick: () -> Unit,
    showControls: Boolean,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null
) {
    var controlsVisible by remember(showControls) {
        mutableStateOf(showControls)
    }

    LaunchedEffect(showControls, controlsVisible) {
        if (showControls && controlsVisible) {
            delay(3_000L)
            controlsVisible = false
        }
    }

    Box(
        modifier = modifier
            .background(Color.Black)
            .clickable(
                enabled = showControls,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                controlsVisible = !controlsVisible
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
            update = { it.player = player }
        )

        AnimatedVisibility(
            visible = showControls && controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {

                // TOP BAR
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .then(
                            if (isFullscreen) {
                                Modifier
                                    .windowInsetsPadding(
                                        WindowInsets.safeDrawing.only(
                                            WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                                        )
                                    )
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            } else {
                                Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            }
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back arrow on the LEFT
                    if (onBackClick != null) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    } else {
                        // Keep fullscreen button at the right when back button is absent
                        Spacer(modifier = Modifier.size(48.dp))
                    }

                    // Fullscreen icon on the RIGHT
                    IconButton(onClick = onFullscreenClick) {
                        Icon(
                            painter = painterResource(
                                id = if (isFullscreen) {
                                    R.drawable.exitfullscreen
                                } else {
                                    R.drawable.arrow_up_left_and_arrow_down_right
                                }
                            ),
                            contentDescription = if (isFullscreen) {
                                "Exit fullscreen"
                            } else {
                                "Enter fullscreen"
                            },
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // CENTER CONTROLS
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(40.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            player.seekTo((player.currentPosition - 10_000L).coerceAtLeast(0L))
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
                        onClick = { if (player.isPlaying) player.pause() else player.play() },
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.PauseCircle
                            else Icons.Default.PlayCircle,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    IconButton(
                        onClick = {
                            player.seekTo(
                                (player.currentPosition + 10_000L).coerceAtMost(duration)
                            )
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

                // BOTTOM: time + seek bar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .then(
                            if (isFullscreen) {
                                Modifier
                                    .windowInsetsPadding(
                                        WindowInsets.safeDrawing.only(
                                            WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal
                                        )
                                    )
                                    .padding(bottom = 20.dp)
                            } else Modifier
                        )
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = formatDigitalClock(duration - position),
                        color = Color.White,
                        fontSize = 13.sp,
                        modifier = Modifier.align(Alignment.End)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    val interactionSource = remember { MutableInteractionSource() }
                    var sliderPosition by remember { mutableFloatStateOf(0f) }
                    var isSeeking by remember { mutableStateOf(false) }

                    Slider(
                        value = if (isSeeking) sliderPosition
                        else if (duration > 0) position.toFloat() / duration else 0f,
                        onValueChange = {
                            isSeeking = true
                            sliderPosition = it
                        },
                        onValueChangeFinished = {
                            player.seekTo((sliderPosition * duration).toLong())
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
                        thumb = {
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
@Composable
private fun DetailsActionItem(
    icon: ImageVector,
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
            tint = orange,
            modifier = Modifier.size(28.dp)
        )

        Spacer(
            modifier = Modifier.height(5.dp)
        )

        Text(
            text = label,
            color = orange,
            fontSize = 13.sp
        )
    }


}
private fun showSubscriptionDialog(
    activity: ComponentActivity,
    onSubscribe: () -> Unit,
    onNotNow: () -> Unit
)
{

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

private fun showLoginDialog(
    activity: ComponentActivity,
    onSubscribe: () -> Unit,
    onNotNow: () -> Unit,
    title: String,
    content: String
)
{

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
    val txtTitle =
        dialog.findViewById<TextView>(
            R.id.dialog_title
        )
    txtTitle.text = title
    val txtContent =
        dialog.findViewById<TextView>(
            R.id.dialog_message
        )
    txtContent.text = content





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