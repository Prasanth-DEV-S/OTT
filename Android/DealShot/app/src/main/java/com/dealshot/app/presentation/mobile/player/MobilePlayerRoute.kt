package com.dealshot.app.presentation.mobile.player

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import com.dealshot.app.data.video.Video
import com.dealshot.app.presentation.shared.player.VideoPlayerViewModel
import com.dealshot.app.presentation.shared.player.VideoPlayerEffect
import com.dealshot.app.presentation.shared.player.VideoPlayerIntent
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
@OptIn(UnstableApi::class)
@Composable
fun MobilePlayerRoute(
    video: Video,
    onBack: () -> Unit,
    onOpenSubscription: (Video) -> Unit
) {
    val context = LocalContext.current

    val viewModel: VideoPlayerViewModel  = viewModel()

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {

                VideoPlayerEffect.NavigateBack -> {
                    onBack()
                }

                VideoPlayerEffect.OpenSubscription -> {
                    onOpenSubscription(video)
                }
            }
        }
    }
    LaunchedEffect(video) {
        viewModel.onIntent(
            VideoPlayerIntent.Initialize(
                title = video.title,
                description = video.description,
                videoUrl = video.url,
                isPremiumUser = false,
                position = 0L
            )
        )
    }

    var isVideoUnavailable by remember(video.url) {
        mutableStateOf(false)
    }

    val exoPlayer = remember(video.url) {

        val renderersFactory =
            DefaultRenderersFactory(context).apply {
                setExtensionRendererMode(
                    DefaultRenderersFactory
                        .EXTENSION_RENDERER_MODE_ON
                )
            }

        ExoPlayer.Builder(
            context,
            renderersFactory
        ).build().apply {

            val isHttpUrl =
                video.url.startsWith("http://") ||
                        video.url.startsWith("https://")

            val targetUri =
                if (isHttpUrl) {
                    Uri.parse(video.url)
                } else {
                    val resId =
                        context.resources.getIdentifier(
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
                setMediaItem(
                    MediaItem.fromUri(targetUri)
                )

                playWhenReady = true
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

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
        }
    }

    MobilePlayerScreen(
        state = state,
        player = exoPlayer,
        onIntent = viewModel::onIntent
    )
}