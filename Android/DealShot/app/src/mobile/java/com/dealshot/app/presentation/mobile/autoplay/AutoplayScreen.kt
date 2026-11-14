package com.dealshot.app.presentation.mobile.autoplay

import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

private val ScreenBg = Color(0xFF090C10)
private val DividerColor = Color(0xFF1F2530)
private val TextMuted = Color(0xFF94A3B8)

@OptIn(UnstableApi::class)
@Composable
fun AutoplayScreen(
    viewModel: AutoplayViewModel = viewModel(),
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    // Handle back button - cancel countdown first if running
    BackHandler {
        if (uiState.showNextOverlay) {
            viewModel.cancelCountdown()
        } else if (uiState.isFullScreen) {
            viewModel.setFullScreen(false)
        } else {
            onBackClick()
        }
    }

    // Media3 ExoPlayer Instance Setup
    val exoPlayer = remember(context) {
        com.dealshot.app.core.media.ExoPlayerFactory.createExoPlayer(context)
    }

    // ExoPlayer Listener for Player.STATE_ENDED and playback state changes
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    viewModel.onVideoEnded()
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                viewModel.updateIsPlaying(isPlaying)
            }
        }

        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // Load and play video when currentVideo changes
    LaunchedEffect(uiState.currentVideo?.videoUrl) {
        val current = uiState.currentVideo ?: return@LaunchedEffect
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        exoPlayer.setMediaItem(MediaItem.fromUri(current.videoUrl))
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    // Auto-scroll LazyColumn to bring current video into view when index changes
    LaunchedEffect(uiState.currentIndex) {
        if (uiState.categoryVideos.isNotEmpty()) {
            listState.animateScrollToItem(uiState.currentIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
            .statusBarsPadding()
    ) {
        // 1. PINNED 16:9 VIDEO PLAYER CONTAINER
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(Color.Black)
        ) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = true
                        setShowNextButton(false)
                        setShowPreviousButton(false)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // 5-second "Next Video" Overlay
            if (uiState.showNextOverlay && uiState.nextVideo != null) {
                NextVideoOverlay(
                    nextVideo = uiState.nextVideo!!,
                    countdownSeconds = uiState.countdownSeconds,
                    onPlayNow = { viewModel.playNextVideoNow() },
                    onCancel = { viewModel.cancelCountdown() }
                )
            }

            // "Category Completed" Overlay
            if (uiState.showCategoryCompletedOverlay) {
                CategoryCompletedOverlay(
                    categoryName = uiState.selectedCategoryName,
                    categories = uiState.availableCategories,
                    onReplay = { viewModel.replayCategory() },
                    onSelectCategory = { catId -> viewModel.loadCategory(catId) }
                )
            }
        }

        // 2. CURRENTLY PLAYING VIDEO HEADER INFO & AUTOPLAY SWITCH
        uiState.currentVideo?.let { currentVideo ->
            CurrentVideoHeader(
                title = currentVideo.title,
                categoryName = uiState.selectedCategoryName,
                publishedTime = currentVideo.publishedTime,
                isAutoplayEnabled = uiState.isAutoplayEnabled,
                onToggleAutoplay = { viewModel.toggleAutoplay(it) }
            )
        }

        HorizontalDivider(color = DividerColor, thickness = 1.dp)

        // 3. SECTION HEADER ("More in National")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "More in ${uiState.selectedCategoryName}",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${uiState.categoryVideos.size} videos",
                color = TextMuted,
                fontSize = 12.sp
            )
        }

        // 4. VERTICAL SCROLLABLE PLAYLIST FOR THE SAME CATEGORY
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            itemsIndexed(
                items = uiState.categoryVideos,
                key = { _, video -> video.id }
            ) { index, video ->
                val isCurrent = index == uiState.currentIndex
                val isNext = index == uiState.currentIndex + 1

                AutoplayVideoRow(
                    video = video,
                    isCurrent = isCurrent,
                    isNext = isNext,
                    onClick = {
                        viewModel.selectVideo(video)
                    }
                )
            }
        }
    }
}
