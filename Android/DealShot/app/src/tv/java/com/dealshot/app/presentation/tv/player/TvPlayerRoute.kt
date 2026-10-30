package com.dealshot.app.presentation.tv.player

import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import com.dealshot.app.data.video.Video
import com.dealshot.app.presentation.shared.player.VideoPlayerEffect
import com.dealshot.app.presentation.shared.player.VideoPlayerIntent
import com.dealshot.app.presentation.shared.player.VideoPlayerViewModel

/**
 * TV entry point for playback. Mirrors MobilePlayerRoute: same
 * VideoPlayerViewModel (via the same factory), same Initialize / progress
 * restore / save-on-pause / save-on-dispose flow, same ExoPlayer setup.
 */
@OptIn(UnstableApi::class)
@Composable
fun TvPlayerRoute(
    video: Video,
    onBack: () -> Unit,
    onOpenSubscription: (Video) -> Unit,
    viewModelFactory: ViewModelProvider.Factory
) {
    val context = LocalContext.current

    val viewModel: VideoPlayerViewModel = viewModel(factory = viewModelFactory)
    val state by viewModel.uiState.collectAsStateWithLifecycle()



    // Save progress when the app goes to background.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.onIntent(VideoPlayerIntent.SaveProgressRequested)
            } else if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onIntent(VideoPlayerIntent.CheckAuthStatus)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Initialize state and load saved progress (same arguments as mobile).
    LaunchedEffect(video.id) {
        viewModel.onIntent(
            VideoPlayerIntent.Initialize(
                videoId = video.id.toString(),
                title = video.title,
                description = video.description,
                videoUrl = video.url,
                isPremiumUser = false,
                position = 0L
            )
        )
    }

    var isVideoUnavailable by remember(video.url) { mutableStateOf(false) }

    val exoPlayer = remember(video.url) {
        val renderersFactory = DefaultRenderersFactory(context).apply {
            setExtensionRendererMode(
                DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON
            )
        }

        ExoPlayer.Builder(context, renderersFactory)
            .build()
            .apply {
                val isHttpUrl =
                    video.url.startsWith("http://") ||
                            video.url.startsWith("https://")

                val targetUri = if (isHttpUrl) {
                    Uri.parse(video.url)
                } else {
                    val resId = context.resources.getIdentifier(
                        video.url,
                        "raw",
                        context.packageName
                    )
                    if (resId != 0) {
                        Uri.parse("android.resource://${context.packageName}/$resId")
                    } else {
                        Uri.parse(video.url)
                    }
                }

                try {
                    setMediaItem(MediaItem.fromUri(targetUri))
                    playWhenReady = false
                    prepare()
                } catch (e: Exception) {
                    Log.e("TvPlayer", "Video unavailable: ${video.url}", e)
                    isVideoUnavailable = true
                }
            }
    }

    LaunchedEffect(viewModel, exoPlayer) {
        viewModel.effect.collect { effect ->
            when (effect) {
                VideoPlayerEffect.NavigateBack -> onBack()
                VideoPlayerEffect.OpenSubscription -> onOpenSubscription(video)
                is VideoPlayerEffect.PauseAndShowLogin -> {
                    exoPlayer.pause()
                    exoPlayer.seekTo(effect.limitMs)
                }
                is VideoPlayerEffect.ResumePlayback -> {
                    if (effect.positionMs > 0L) {
                        exoPlayer.seekTo(effect.positionMs)
                    } else if (exoPlayer.currentPosition > 0L) {
                        exoPlayer.seekTo(exoPlayer.currentPosition)
                    }
                    exoPlayer.play()
                }
            }
        }
    }

    // Save the latest position and release the player when leaving.
    DisposableEffect(exoPlayer, video.id) {
        onDispose {
            viewModel.onIntent(VideoPlayerIntent.SaveProgressRequested)
            exoPlayer.release()
        }
    }

    // Restore the saved position once, after progress has loaded.
    var restoredVideoId by remember(video.id) { mutableStateOf<String?>(null) }

    LaunchedEffect(video.id, state.videoId, state.isProgressLoaded) {
        val id = video.id.toString()
        if (
            state.videoId == id &&
            state.isProgressLoaded &&
            restoredVideoId != id
        ) {
            if (state.position > 0L) {
                exoPlayer.seekTo(state.position)
            }
            restoredVideoId = id
        }
    }

    TvPlayerScreen(
        state = state,
        player = exoPlayer,
        isVideoUnavailable = isVideoUnavailable,
        onIntent = viewModel::onIntent
    )
}