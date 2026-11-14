package com.dealshot.app.presentation.mobile.details

import android.app.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dealshot.app.data.preferences.UserPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.PointerEventPass
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
import androidx.media3.common.Format
// ---- imports for volume / brightness gestures ----
import android.content.Context
import android.content.ContextWrapper
import android.media.AudioManager
import android.provider.Settings
import android.util.Log
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import kotlin.math.roundToInt

// ---- NEW imports for video quality settings ----
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.TransformOrigin
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.compose.material.icons.filled.Flag
import com.dealshot.app.presentation.shared.details.DetailsIntent

// How much of the player height a full 0%..100% swipe needs (1.5 = about 2/3 of the height)
private const val GESTURE_SENSITIVITY = 1.5f

// How long the indicator stays after the finger is lifted
private const val GESTURE_HIDE_DELAY_MS = 1_500L

private const val TAG = "DealShotGesture"

private val QualityBlue = Color(0xFF2196F3)

private enum class GestureType { BRIGHTNESS, VOLUME }

// ----------------------------------------------------------------
// NEW: one selectable video rendition (read from the stream itself)
// ----------------------------------------------------------------
private data class QualityOption(
    val height: Int,          // real height, or a negative key when the stream doesn't report one
    val label: String,
    val group: Tracks.Group,
    val trackIndex: Int,
    val bitrate: Int
)

@Composable
fun MobileDetailsScreen(
    video: Video,
    nextVideos: List<com.dealshot.app.data.remote.dto.VideoDto> = emptyList(),
    onSelectNextVideo: (com.dealshot.app.data.remote.dto.VideoDto) -> Unit = {},
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
    onSubscribeClick: () -> Unit = {},
    onReportClick: () -> Unit = {}
) {
    val context = LocalContext.current

    val preferenceManager = PreferenceManager(context)
    // Report is offered only to logged-in users (null hides it in the settings popup)
    val reportAction: (() -> Unit)? =
        if (!preferenceManager.getString("Mobile_No").isNullOrBlank()) onReportClick else null
    val heartScale by animateFloatAsState(
        targetValue = if (isLiked) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "heartScale"
    )
    val activity = context as? Activity

    // remember the window brightness we started with so we can restore it on exit
    val originalBrightness = remember {
        activity?.window?.attributes?.screenBrightness
            ?: WindowManagerBrightnessNone
    }

    val player = remember { com.dealshot.app.core.media.ExoPlayerFactory.createExoPlayer(context) }
    var isPlayerVisible by remember { mutableStateOf(false) }
    var isPromoPlaying by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }
    var isPlayerTouched by remember { mutableStateOf(false) }
    var hasShownSubscriptionPopup by remember { mutableStateOf(false) }

    var position by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var playing by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }

    var showNextOverlay by remember { mutableStateOf(false) }
    var countdownSeconds by remember { mutableIntStateOf(5) }

    // Detect when main video ends (Player.STATE_ENDED) to trigger 5-second autoplay countdown
    // Note: NEVER trigger countdown when promo video ends (isPromoPlaying = true or !isPlayerVisible)
    DisposableEffect(player, nextVideos, isPlayerVisible, isPromoPlaying) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    if (isPlayerVisible && !isPromoPlaying && nextVideos.isNotEmpty()) {
                        showNextOverlay = true
                    }
                }
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
        }
    }

    // 5-second countdown timer ticker
    LaunchedEffect(showNextOverlay) {
        if (showNextOverlay) {
            countdownSeconds = 5
            for (sec in 5 downTo 1) {
                countdownSeconds = sec
                delay(1000L)
            }
            val nextVideo = nextVideos.firstOrNull()
            if (nextVideo != null) {
                showNextOverlay = false
                onSelectNextVideo(nextVideo)
            }
        }
    }

    if (showShareDialog) {
        val shareUrl = com.dealshot.app.util.VideoShareHelper.generateShareUrl(video.id.ifEmpty { video.apiId })
        ShareBottomSheetMenu(
            videoTitle = video.title,
            videoUrl = shareUrl,
            shareText = com.dealshot.app.util.VideoShareHelper.buildShareMessage(video.title, shareUrl),
            videoId = video.id.ifEmpty { video.apiId },
            onDismiss = { showShareDialog = false }
        )
    }

    // Release player + restore screen when leaving
    DisposableEffect(Unit) {
        onDispose {
            player.release()
            activity?.let { act ->
                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                WindowCompat.getInsetsController(act.window, act.window.decorView)
                    .show(WindowInsetsCompat.Type.systemBars())

                // give brightness back to the system/user setting
                val attrs = act.window.attributes
                attrs.screenBrightness = originalBrightness
                act.window.attributes = attrs
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

    val userPreferences = remember { UserPreferences(context.applicationContext) }
    val isSubscriptionActive by userPreferences.isSubscriptionActive
        .collectAsStateWithLifecycle(initialValue = false)

    // Progress + 30 second subscription popup (same logic as the player screen)
    LaunchedEffect(isPlayerVisible, isSubscriptionActive) {
        if (!isPlayerVisible) return@LaunchedEffect
        while (true) {
            position = player.currentPosition
            duration = player.duration.coerceAtLeast(0L)
            playing = player.isPlaying

            if (!isPremiumUser && !isSubscriptionActive && !hasShownSubscriptionPopup && position >= 30_000L) {
                hasShownSubscriptionPopup = true
                player.pause()

                val isLoggedIn = !preferenceManager.getString("Mobile_No").isNullOrBlank()

                activity?.let { act ->
                    showSubscriptionPreviewDialog(
                        activity = act as ComponentActivity,
                        isLoggedIn = isLoggedIn,
                        onLogin = { onLoginClick() },
                        onSubscribe = { onSubscribeClick() },
                        onNotNow = {
                            onBackClick()
                        }
                    )
                }
            }
            delay(250)
        }
    }

    val startPlayback = {
        val actualVideoUrl = video.url.trim()

        if (actualVideoUrl.isNotBlank()) {
            isPromoPlaying = false

            // NEW: a new media item has new renditions, so drop any old quality override (back to Auto)
            player.trackSelectionParameters = player.trackSelectionParameters
                .buildUpon()
                .clearOverridesOfType(C.TRACK_TYPE_VIDEO)
                .build()

            player.setMediaItem(
                MediaItem.fromUri(actualVideoUrl)
            )
            player.prepare()

            isPlayerVisible = true
            player.play()
        }
    }

    LaunchedEffect(video.id, video.apiId, video.url) {
        showNextOverlay = false
        val actualVideoUrl = video.url.trim()
        if (actualVideoUrl.isNotBlank() && isPlayerVisible) {
            startPlayback()
        }
    }

    // Fullscreen: show only the player
    if (isFullscreen) {
        Box(modifier = Modifier.fillMaxSize()) {
            InlinePlayer(
                player = player,
                isFullscreen = true,
                isPlaying = playing,
                position = position,
                duration = duration,
                onFullscreenClick = { isFullscreen = false },
                modifier = Modifier.fillMaxSize(),
                showControls = true,
                onBackClick = onBackClick,
                onReportClick = reportAction
            )

            if (showNextOverlay && nextVideos.isNotEmpty()) {
                val nextVideo = nextVideos.first()
                DetailNextVideoOverlay(
                    nextVideo = nextVideo,
                    countdownSeconds = countdownSeconds,
                    onPlayNow = {
                        showNextOverlay = false
                        onSelectNextVideo(nextVideo)
                    },
                    onCancel = {
                        showNextOverlay = false
                    }
                )
            }
        }
        return
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState(), enabled = !isPlayerTouched)
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
                    showControls = isPlayerVisible && !isPromoPlaying,
                    isPromoPlaying = isPromoPlaying,
                    modifier = Modifier.fillMaxSize(),
                    onBackClick = onBackClick,
                    onTouchActiveChange = { isPlayerTouched = it },
                    onReportClick = reportAction
                )

                if (showNextOverlay && nextVideos.isNotEmpty()) {
                    val nextVideo = nextVideos.first()
                    DetailNextVideoOverlay(
                        nextVideo = nextVideo,
                        countdownSeconds = countdownSeconds,
                        onPlayNow = {
                            showNextOverlay = false
                            onSelectNextVideo(nextVideo)
                        },
                        onCancel = {
                            showNextOverlay = false
                        }
                    )
                }

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
                                    onSubscribe = { onLoginClick() },
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
                                        onSubscribe = { onLoginClick() },
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
                        val isLoggedIn = !preferenceManager.getString("Mobile_No").isNullOrBlank()

                        if (!isLoggedIn) {
                            android.widget.Toast.makeText(context, "Please log in to like this video .", android.widget.Toast.LENGTH_SHORT).show()
                            onLoginClick()
                        } else {
                            showShareDialog = true
                        }

                    }
                )

                // Report: logged-in users only (same check the Like / My List buttons use)
                DetailsActionItem(
                    icon = Icons.Default.Flag,
                    label = "Report",
                    onClick = onReportClick
                )
            }

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            // NEXT VIDEOS SECTION
            if (nextVideos.isNotEmpty()) {
                val nextVideosAsVideo = remember(nextVideos) {
                    nextVideos.map { dto ->
                        Video(
                            id = dto.effectiveId,
                            title = dto.title.orEmpty(),
                            description = dto.description.orEmpty(),
                            duration = dto.duration.orEmpty(),
                            thumbnail = dto.effectiveThumbnail,
                            url = dto.videoUrl.orEmpty(),
                            category = dto.category.orEmpty(),
                            apiId = dto.effectiveId,
                            thumbnailUrl = dto.effectiveThumbnail,
                            isPremium = dto.isPremium,
                            promoVideoUrl = dto.promoVideoUrl.orEmpty(),
                            featured = dto.featured
                        )
                    }
                }
                NextVideosSection(
                    video = video,
                    nextVideos = nextVideosAsVideo,
                    onSelectNextVideo = { selected ->
                        nextVideos.firstOrNull { it.effectiveId == selected.id }?.let { dto ->
                            onSelectNextVideo(dto)
                        }
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
           /* if (nextVideos.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFF1F2530), thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                val headerText = if (!video.category.isNullOrBlank()) {
                    "More in ${video.category}"
                } else {
                    "Next Videos"
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = headerText,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "${nextVideos.size} videos",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    nextVideos.forEach { nextVideo ->
                        NextVideoItemRow(
                            video = nextVideo,
                            onClick = {
                                onSelectNextVideo(nextVideo)
                            }
                        )
                    }
                }
            }*/
        }
    }
}

@Composable
fun NextVideosSection(
    video: Video,
    nextVideos: List<Video>,
    onSelectNextVideo: (Video) -> Unit,
    modifier: Modifier = Modifier
) {
    if (nextVideos.isEmpty()) return

    val headerText = if (!video.category.isNullOrBlank()) {
        "More in ${video.category}"
    } else {
        "Up Next"
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(20.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(20.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF6366F1), Color(0xFF22D3EE))
                        )
                    )
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = headerText,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF1A2030))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${nextVideos.size} videos",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Horizontal rail
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(end = 16.dp)
        ) {
            items(
                items = nextVideos,
                key = { it.id }
            ) { nextVideo ->
                NextVideoItemRow(
                    video = nextVideo,
                    onClick = { onSelectNextVideo(nextVideo) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun NextVideoItemRowd(
    video: Video,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF111622))
            .border(1.dp, Color(0xFF1F2530), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail
        Box(
            modifier = Modifier
                .width(128.dp)
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF1A2030))
        ) {
            AsyncImage(
                model = video.thumbnailUrl,
                contentDescription = video.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Bottom gradient for readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.5f to Color.Transparent,
                            1f to Color(0xAA000000)
                        )
                    )
            )

            // Center play icon
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0x99000000)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Duration badge
            if (!video.duration.isNullOrBlank()) {
                Text(
                    text = video.duration,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xCC000000))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Info
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = video.title.orEmpty(),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 19.sp
            )

            if (!video.category.isNullOrBlank()) {
                Text(
                    text = video.category,
                    color = Color(0xFF818CF8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFF6366F1).copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}
@Composable
fun NextVideoItemRow(
    video: Video,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(210.dp) // fixed width so the next card peeks in
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
    ) {
        // Thumbnail
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF1A2030))
        ) {
            AsyncImage(
                model = video.thumbnailUrl,
                contentDescription = video.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Bottom gradient for readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.6f to Color.Transparent,
                            1f to Color(0xAA000000)
                        )
                    )
            )

            // Play icon (bottom-left)
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .size(18.dp)
            )

            // Duration (bottom-right)
            video.duration?.takeIf { it.isNotBlank() }?.let { duration ->
                Text(
                    text = duration,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Title
        Text(
            text = video.title.orEmpty(),
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
    }
}

@Composable
private fun NextVideoItemRow(
    video: com.dealshot.app.data.remote.dto.VideoDto,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF131722))
            .border(1.dp, Color(0xFF242C3D), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: 16:9 Thumbnail with Duration Badge
        Box(
            modifier = Modifier
                .width(130.dp)
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1E232F))
        ) {
            AsyncImage(
                model = video.thumbnailUrl ?: video.thumbnail,
                contentDescription = video.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            video.duration?.takeIf { it.isNotBlank() }?.let { duration ->
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.8f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = duration,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Right side: Title & Category / Status
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = video.title.orEmpty().ifBlank { "Untitled Video" },
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                video.category?.takeIf { it.isNotBlank() }?.let { cat ->
                    Text(
                        text = cat,
                        color = Color(0xFF3B82F6),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = " • ",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
                Text(
                    text = if (video.isPremium) "Premium" else "Free",
                    color = if (video.isPremium) Color(0xFFF5C85A) else Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// Value that tells Android "use the system brightness" (WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE)
private const val WindowManagerBrightnessNone = -1f

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
    isPromoPlaying: Boolean = false,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    onTouchActiveChange: (Boolean) -> Unit = {},
    onReportClick: (() -> Unit)? = null
) {
    var controlsVisible by remember(showControls) {
        mutableStateOf(showControls)
    }

    // NEW: quality settings state
    var settingsOpen by remember { mutableStateOf(false) }
    var qualityOptions by remember { mutableStateOf<List<QualityOption>>(emptyList()) }
    var selectedHeight by remember { mutableStateOf<Int?>(null) }   // null = Auto
    val menuReportAction: (() -> Unit)? =
        if (onReportClick == null) {
            null
        } else {
            {
                settingsOpen = false
                onReportClick.invoke()
            }
        }

    // Controls auto-hide after 3s, but never while the settings menu is open
    LaunchedEffect(showControls, controlsVisible, settingsOpen) {
        if (showControls && controlsVisible && !settingsOpen) {
            delay(3_000L)
            controlsVisible = false
        }
    }

    // NEW: read the renditions the stream offers and keep them in sync with the player
    DisposableEffect(player) {
        fun refresh() {
            // DEBUG: shows exactly what the stream exposes (filter Logcat by "DealShotQuality")
            Log.d(
                "DealShotQuality",
                "uri=${player.currentMediaItem?.localConfiguration?.uri} | " +
                        player.currentTracks.groups
                            .filter { it.type == C.TRACK_TYPE_VIDEO }
                            .joinToString { g ->
                                (0 until g.length).joinToString { i ->
                                    val f = g.getTrackFormat(i)
                                    "${f.width}x${f.height}@${f.bitrate} " +
                                            "(${f.sampleMimeType}, supported=${g.isTrackSupported(i)})"
                                }
                            }
            )

            val options = buildQualityOptions(player.currentTracks)
            qualityOptions = options
            val overrideHeight = currentOverrideHeight(player)
            selectedHeight = if (options.any { it.height == overrideHeight }) overrideHeight else null
        }

        val listener = object : Player.Listener {
            override fun onTracksChanged(tracks: Tracks) {
                refresh()
            }
        }
        player.addListener(listener)
        refresh()

        onDispose { player.removeListener(listener) }
    }

    // Phone back button closes the menu first
    BackHandler(enabled = settingsOpen) { settingsOpen = false }

    // ------------------------------------------------------------
    // volume / brightness gesture state
    // ------------------------------------------------------------
    val context = LocalContext.current
    val view = LocalView.current
    val activity = remember(view) { view.context.findActivity() }
    val audioManager = remember(context) {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    var gestureType by remember { mutableStateOf(GestureType.VOLUME) }
    var gestureValue by remember { mutableFloatStateOf(0f) }      // 0f..1f
    var gestureVisible by remember { mutableStateOf(false) }
    var isDragging by remember { mutableStateOf(false) }
    var gestureTick by remember { mutableIntStateOf(0) }

    // Hide the indicator ~1.5s after the finger is lifted (restarts on every new gesture)
    LaunchedEffect(gestureTick, isDragging) {
        if (gestureVisible && !isDragging) {
            delay(GESTURE_HIDE_DELAY_MS)
            gestureVisible = false
        }
    }

    // Never leave the parent page scroll-locked if we leave mid-touch
    DisposableEffect(Unit) {
        onDispose { onTouchActiveChange(false) }
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
            // Tracks whether a finger is on the player (lets the parent page stop scrolling)
            .then(
                if (showControls) {
                    Modifier.pointerInput(Unit) {
                        awaitEachGesture {
                            awaitFirstDown(
                                requireUnconsumed = false,
                                pass = PointerEventPass.Initial
                            )
                            onTouchActiveChange(true)
                            do {
                                val event = awaitPointerEvent(PointerEventPass.Final)
                            } while (event.changes.any { it.pressed })
                            onTouchActiveChange(false)
                        }
                    }
                } else Modifier
            )
            // Vertical swipe: left half = brightness, right half = volume (off while the menu is open)
            .then(
                if (showControls && !settingsOpen) {
                    Modifier.pointerInput(Unit) {
                        var activeType = GestureType.VOLUME
                        var level = 0f

                        detectVerticalDragGestures(
                            onDragStart = { offset ->
                                activeType = if (offset.x < size.width / 2f) {
                                    GestureType.BRIGHTNESS
                                } else {
                                    GestureType.VOLUME
                                }
                                level = when (activeType) {
                                    GestureType.BRIGHTNESS -> readBrightness(activity)
                                    GestureType.VOLUME -> readVolumeFraction(audioManager)
                                }
                                gestureType = activeType
                                gestureValue = level
                                gestureVisible = true
                                isDragging = true
                            },
                            onDragEnd = {
                                isDragging = false
                                gestureTick++
                            },
                            onDragCancel = {
                                isDragging = false
                                gestureTick++
                            },
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()

                                // Swipe up (negative dragAmount) increases the value
                                level = (level - dragAmount / size.height * GESTURE_SENSITIVITY)
                                    .coerceIn(0f, 1f)

                                when (activeType) {
                                    GestureType.BRIGHTNESS -> applyBrightness(activity, level)
                                    GestureType.VOLUME -> applyVolumeFraction(audioManager, level)
                                }
                                gestureType = activeType
                                gestureValue = level
                                gestureVisible = true
                            }
                        )
                    }
                } else Modifier
            )
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
            visible = showControls && controlsVisible && !isPromoPlaying,
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

                    // RIGHT side: Settings (gear) + Fullscreen
                    Row(verticalAlignment = Alignment.CenterVertically) {

                        // NEW: Settings (gear) icon
                        IconButton(onClick = { settingsOpen = !settingsOpen }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Video quality",
                                tint = if (settingsOpen) QualityBlue else Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }

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
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            } else Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )

                ) {
                    Text(
                        text = formatDigitalClock(duration - position),
                        color = Color.White,
                        fontSize = 13.sp,
                        modifier = Modifier.align(Alignment.End)
                    )

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

        // ------------------------------------------------------------
        // volume / brightness indicator (brightness = left, volume = right)
        // ------------------------------------------------------------
        AnimatedVisibility(
            visible = gestureVisible,
            enter = fadeIn() + scaleIn(initialScale = 0.85f),
            exit = fadeOut() + scaleOut(targetScale = 0.85f),
            modifier = Modifier
                .align(
                    if (gestureType == GestureType.BRIGHTNESS) Alignment.CenterStart
                    else Alignment.CenterEnd
                )
                .then(
                    if (isFullscreen) {
                        Modifier.windowInsetsPadding(
                            WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)
                        )
                    } else Modifier
                )
                .padding(horizontal = 24.dp)
        ) {
            GestureIndicator(
                type = gestureType,
                value = gestureValue
            )
        }

        // ------------------------------------------------------------
        // NEW: tap-outside scrim + Video Quality popup
        // ------------------------------------------------------------
        if (showControls && settingsOpen) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { settingsOpen = false }
            )
        }

        AnimatedVisibility(
            visible = showControls && settingsOpen,
            enter = fadeIn() + scaleIn(
                initialScale = 0.9f,
                transformOrigin = TransformOrigin(1f, 0.5f)
            ),
            exit = fadeOut() + scaleOut(
                targetScale = 0.9f,
                transformOrigin = TransformOrigin(1f, 0.5f)
            ),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .then(
                    if (isFullscreen) {
                        Modifier.windowInsetsPadding(
                            WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)
                        )
                    } else Modifier
                )
                .padding(end = 12.dp, top = 8.dp, bottom = 8.dp)
        ) {
            QualityMenu(
                options = qualityOptions,
                selectedHeight = selectedHeight,
                onSelect = { option ->
                    applyQuality(player, option)
                    selectedHeight = option?.height
                    settingsOpen = false
                },
                onClose = { settingsOpen = false },
                onReportClick = menuReportAction
            )
        }
    }
}

// ----------------------------------------------------------------
// NEW: Video Quality popup UI
// ----------------------------------------------------------------
@Composable
private fun QualityMenu(
    options: List<QualityOption>,
    selectedHeight: Int?,
    onSelect: (QualityOption?) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    onReportClick: (() -> Unit)? = null
) {
    // Manual choices only make sense when the stream really has 2+ renditions
    val manualOptions = if (options.size >= 2) options else emptyList()

    Column(
        modifier = modifier
            .width(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xF2000000))
            // swallow taps inside the popup so they never reach the "tap outside" scrim
            .pointerInput(Unit) { detectTapGestures { } }
            // scrolls if the player is too short to show every row (small portrait player)
            .verticalScroll(rememberScrollState())
    ) {
        // Header: back arrow + title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clickable(onClick = onClose)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Video Quality",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.2f))
        )
        // Report (logged-in users only; null when logged out)
/*
        if (onReportClick != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clickable(onClick = onReportClick)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.width(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Report",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.2f))
            )
        }
*/
        manualOptions.forEach { option ->
            QualityRow(
                label = option.label,
                selected = selectedHeight == option.height,
                onClick = { onSelect(option) }
            )
        }

        QualityRow(
            label = "Auto",
            selected = selectedHeight == null,
            onClick = { onSelect(null) }
        )

        if (manualOptions.isEmpty()) {
            Text(
                text = "Only one quality available for this video",
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun QualityRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(if (selected) Color.White.copy(alpha = 0.08f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.width(24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = QualityBlue,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = label,
            color = if (selected) QualityBlue else Color.White,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// ----------------------------------------------------------------
// NEW: quality helpers (use Media3 track selection - no player restart)
// ----------------------------------------------------------------

private fun qualityLabel(height: Int): String = when {
    height >= 2160 -> "4K (${height}p)"
    height >= 1440 -> "2K (${height}p)"
    height >= 1080 -> "Full HD (${height}p)"
    height >= 720 -> "HD (${height}p)"
    height >= 480 -> "SD (${height}p)"
    else -> "${height}p"
}

/**
 * Reads the video renditions the stream actually offers (HLS variants / DASH representations).
 * Unsupported-by-device tracks are skipped; one entry per height (highest bitrate wins).
 * Returned highest quality first.
 */
@OptIn(UnstableApi::class)
private fun trackKey(format: Format): Int? = when {
    format.height > 0 -> format.height
    format.bitrate > 0 -> -(format.bitrate / 1000)   // no resolution in playlist -> identify by bitrate
    else -> null
}

@OptIn(UnstableApi::class)
private fun buildQualityOptions(tracks: Tracks): List<QualityOption> {
    val best = HashMap<Int, QualityOption>()

    for (group in tracks.groups) {
        if (group.type != C.TRACK_TYPE_VIDEO) continue

        for (i in 0 until group.length) {
            if (!group.isTrackSupported(i)) continue

            val format = group.getTrackFormat(i)
            val key = trackKey(format) ?: continue

            val existing = best[key]
            if (existing == null || format.bitrate > existing.bitrate) {
                best[key] = QualityOption(
                    height = key,
                    label = if (format.height > 0) qualityLabel(format.height)
                    else "${format.bitrate / 1000} kbps",
                    group = group,
                    trackIndex = i,
                    bitrate = format.bitrate
                )
            }
        }
    }

    return best.values.sortedByDescending { it.bitrate }
}

@OptIn(UnstableApi::class)
private fun currentOverrideHeight(player: ExoPlayer): Int? {
    val override = player.trackSelectionParameters.overrides.values
        .firstOrNull { it.type == C.TRACK_TYPE_VIDEO }
        ?: return null
    val index = override.trackIndices.firstOrNull() ?: return null
    return trackKey(override.mediaTrackGroup.getFormat(index))
}
/** Height of the currently forced rendition, or null when the player is in Auto (adaptive) mode. */


/**
 * option == null -> Auto: remove the override so adaptive bitrate (network + device) decides.
 * option != null -> force that rendition. The player keeps its position; only the video track switches.
 */
private fun applyQuality(player: ExoPlayer, option: QualityOption?) {
    val builder = player.trackSelectionParameters
        .buildUpon()
        .clearOverridesOfType(C.TRACK_TYPE_VIDEO)

    if (option != null) {
        builder.setOverrideForType(
            TrackSelectionOverride(option.group.mediaTrackGroup, option.trackIndex)
        )
    }

    player.trackSelectionParameters = builder.build()
}

// ----------------------------------------------------------------
// volume / brightness indicator UI
// ----------------------------------------------------------------
@Composable
private fun GestureIndicator(
    type: GestureType,
    value: Float
) {
    val percent = (value * 100).roundToInt()

    val icon: ImageVector = when (type) {
        GestureType.BRIGHTNESS ->
            if (value < 0.5f) Icons.Default.BrightnessLow else Icons.Default.BrightnessHigh
        GestureType.VOLUME -> when {
            value <= 0f -> Icons.Default.VolumeOff
            value < 0.5f -> Icons.Default.VolumeDown
            else -> Icons.Default.VolumeUp
        }
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(alpha = 0.65f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = if (type == GestureType.BRIGHTNESS) "Brightness" else "Volume",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Vertical progress bar
        Box(
            modifier = Modifier
                .width(6.dp)
                .height(64.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.White.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(value.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(50))
                    .background(orange)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "$percent%",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// ----------------------------------------------------------------
// NEW: brightness / volume helpers (no runtime permission needed)
// ----------------------------------------------------------------

/** Walks up ContextWrappers (Compose can wrap the Activity) to find the Activity. */
private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/** Current window brightness 0f..1f. Falls back to the system setting when the window uses "auto". */
private fun readBrightness(activity: Activity?): Float {
    if (activity == null) return 0.5f
    val windowValue = activity.window.attributes.screenBrightness
    if (windowValue >= 0f) return windowValue
    return try {
        Settings.System.getInt(
            activity.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS
        ) / 255f
    } catch (e: Exception) {
        0.5f
    }
}

/** Sets brightness for this window only (does not touch the system setting). */
private fun applyBrightness(activity: Activity?, value: Float) {
    val window = activity?.window ?: return
    val attrs = window.attributes
    // Never go fully to 0 so the screen cannot turn completely black
    attrs.screenBrightness = value.coerceIn(0.01f, 1f)
    window.attributes = attrs
}

private fun readVolumeFraction(audioManager: AudioManager): Float {
    val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    if (max <= 0) return 0f
    return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / max
}

private fun applyVolumeFraction(audioManager: AudioManager, value: Float) {
    val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    val index = (value * max).roundToInt().coerceIn(0, max)
    // flags = 0 -> don't show the system volume panel, we show our own indicator
    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0)
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
private fun showSubscriptionPreviewDialog(
    activity: ComponentActivity,
    isLoggedIn: Boolean,
    onLogin: () -> Unit,
    onSubscribe: () -> Unit,
    onNotNow: () -> Unit
) {
    val dialog = Dialog(activity)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(R.layout.dialog_subscription)
    dialog.setCancelable(false)
    dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

    val txtMessage = dialog.findViewById<TextView>(R.id.dialog_message)
    if (!isLoggedIn) {
        txtMessage?.text = "Your 30-second preview has ended. Login and subscribe to watch the full video."
    } else {
        txtMessage?.text = "Your 30-second preview has ended. subscribe to watch the full video."
    }

    val btnNotNow = dialog.findViewById<TextView>(R.id.btn_not_now)
    val btnOk = dialog.findViewById<TextView>(R.id.btn_ok)

    btnNotNow.setOnClickListener {
        dialog.dismiss()
        onNotNow()
    }

    btnOk.setOnClickListener {
        dialog.dismiss()
        if (!isLoggedIn) {
            onLogin()
        } else {
            onSubscribe()
        }
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

@Composable
private fun DetailNextVideoOverlay(
    nextVideo: com.dealshot.app.data.remote.dto.VideoDto,
    countdownSeconds: Int,
    onPlayNow: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xEC090C10))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "UP NEXT IN CATEGORY",
                color = Color(0xFFF5C85A),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF131722))
                    .border(1.dp, Color(0xFF242C3D), RoundedCornerShape(12.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(110.dp)
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E232F))
                ) {
                    AsyncImage(
                        model = nextVideo.thumbnailUrl ?: nextVideo.thumbnail,
                        contentDescription = nextVideo.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = nextVideo.title.orEmpty().ifBlank { "Untitled Video" },
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${nextVideo.duration.orEmpty().ifBlank { "Video" }} • ${nextVideo.category.orEmpty()}",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(35.dp)
            ) {
                CircularProgressIndicator(
                    progress = { (6 - countdownSeconds) / 5f },
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF5C85A),
                    strokeWidth = 2.dp,
                    trackColor = Color.White.copy(alpha = 0.2f)
                )
                Text(
                    text = "$countdownSeconds",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Playing next video in $countdownSeconds seconds...",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }

                Button(
                    onClick = onPlayNow,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5C85A), contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Play Now", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}