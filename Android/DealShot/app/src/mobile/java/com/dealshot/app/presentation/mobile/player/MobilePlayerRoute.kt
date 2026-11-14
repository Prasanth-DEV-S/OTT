
package com.dealshot.app.presentation.mobile.player

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.dealshot.app.data.video.Video
import com.dealshot.app.presentation.shared.player.VideoPlayerEffect
import com.dealshot.app.presentation.shared.player.VideoPlayerIntent
import com.dealshot.app.presentation.shared.player.VideoPlayerViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
@OptIn(UnstableApi::class)
@Composable
fun MobilePlayerRoute(
    video: Video,
    onBack: () -> Unit,
    onOpenSubscription: (Video) -> Unit,
    onOpenLogin: () -> Unit = {},
    viewModelFactory: androidx.lifecycle.ViewModelProvider.Factory
) {
    val context = LocalContext.current

    // The factory must provide VideoPlayerViewModel with its repository.
    val viewModel: VideoPlayerViewModel = viewModel(
        factory = viewModelFactory
    )

    val state by viewModel.uiState.collectAsStateWithLifecycle()




    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.onIntent(
                    VideoPlayerIntent.SaveProgressRequested
                )
            } else if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onIntent(VideoPlayerIntent.CheckAuthStatus)
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Initialize MVI state and load saved progress for this backend video.
    LaunchedEffect(video.id) {
        viewModel.onIntent(
            VideoPlayerIntent.Initialize(
                videoId = video.id.toString(),
                title = video.title,
                description = video.description,
                videoUrl = video.url,
                category = video.category,
                isPremiumUser = false,
                position = 0L
            )
        )
    }

    var isVideoUnavailable by remember(video.url) {
        mutableStateOf(false)
    }

    val exoPlayer = remember(video.url) {
        com.dealshot.app.core.media.ExoPlayerFactory.createExoPlayer(context).apply {
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
                        Uri.parse(
                            "android.resource://${context.packageName}/$resId"
                        )
                    } else {
                        Uri.parse(video.url)
                    }
                }

                try {
                    setMediaItem(MediaItem.fromUri(targetUri))
                    playWhenReady = false
                    prepare()
                } catch (e: Exception) {
                    Log.e(
                        "MobilePlayer",
                        "Video unavailable: ${video.url}",
                        e
                    )
                    isVideoUnavailable = true
                }
            }
    }

    LaunchedEffect(viewModel, exoPlayer) {
        viewModel.effect.collect { effect ->
            when (effect) {
                VideoPlayerEffect.NavigateBack -> onBack()
                VideoPlayerEffect.OpenSubscription -> onOpenSubscription(video)
                VideoPlayerEffect.OpenLogin -> onOpenLogin()
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
                is VideoPlayerEffect.UpdatePlaybackUrl -> {
                    try {
                        val currentPos = if (effect.positionMs > 0L) effect.positionMs else exoPlayer.currentPosition
                        exoPlayer.setMediaItem(MediaItem.fromUri(effect.playbackUrl))
                        exoPlayer.prepare()
                        if (currentPos > 0L) {
                            exoPlayer.seekTo(currentPos)
                        }
                        if (effect.shouldPlay) {
                            exoPlayer.play()
                        } else {
                            exoPlayer.pause()
                        }
                    } catch (e: Exception) {
                        Log.e("MobilePlayerRoute", "Failed to switch playback URL: ${effect.playbackUrl}", e)
                    }
                }
            }
        }
    }

    // Save the latest position when this route leaves composition.
    DisposableEffect(exoPlayer, video.id) {
        onDispose {
            viewModel.onIntent(VideoPlayerIntent.SaveProgressRequested)
            exoPlayer.release()
        }
    }

    var restoredVideoId by remember(video.id) {
        mutableStateOf<String?>(null)
    }

    // Resume playback automatically when user logs in
    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) {
            val targetPos = if (state.currentPlaybackPosition > 0L) {
                state.currentPlaybackPosition
            } else if (state.position > 0L) {
                state.position
            } else {
                0L
            }

            if (targetPos > 0L) {
                exoPlayer.seekTo(targetPos)
            }
            if (!state.showSubscriptionPopup) {
                exoPlayer.play()
            }
        }
    }

    // Apply the restored position ONCE after progress is loaded.
    LaunchedEffect(
        video.id,
        state.videoId,
        state.isProgressLoaded
    ) {
        val id = video.id.toString()

        if (
            state.videoId == id &&
            state.isProgressLoaded &&
            restoredVideoId != id
        ) {
            if (state.position > 0L) {
                exoPlayer.seekTo(state.position)
                if (!state.showSubscriptionPopup) {
                    exoPlayer.play()
                } else {
                    exoPlayer.pause()
                }
            }

            restoredVideoId = id
        }
    }

    MobilePlayerScreen(
        video = video,
        state = state,
        player = exoPlayer,
        onIntent = viewModel::onIntent
    )
}