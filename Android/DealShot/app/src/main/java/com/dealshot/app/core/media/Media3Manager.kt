package com.dealshot.app.core.media

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer

@UnstableApi
@OptIn(UnstableApi::class)
class Media3Manager(context: Context) {

    private val appContext = context.applicationContext

    private val renderersFactory =
        DefaultRenderersFactory(appContext).apply {
            setExtensionRendererMode(
                DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON
            )
        }

    val player: ExoPlayer =
        ExoPlayer.Builder(
            appContext,
            renderersFactory
        ).build()

    fun initialize(
        videoUrl: String,
        position: Long = 0L
    ) {
        val targetUri = resolveVideoUri(videoUrl)

        player.setMediaItem(
            MediaItem.fromUri(targetUri)
        )

        player.prepare()

        if (position > 0L) {
            player.seekTo(position)
        }

        player.playWhenReady = true
    }

    private fun resolveVideoUri(
        videoUrl: String
    ): Uri {

        val isStream =
            videoUrl.startsWith("http://") ||
                    videoUrl.startsWith("https://")

        if (isStream) {
            return Uri.parse(videoUrl)
        }

        val resourceId =
            appContext.resources.getIdentifier(
                videoUrl,
                "raw",
                appContext.packageName
            )

        return if (resourceId != 0) {
            Uri.parse(
                "android.resource://${appContext.packageName}/$resourceId"
            )
        } else {
            Uri.parse(videoUrl)
        }
    }

    fun play() {
        player.play()
    }

    fun pause() {
        player.pause()
    }

    fun togglePlayPause() {
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    fun seekTo(position: Long) {
        player.seekTo(position)
    }

    fun rewind10Seconds() {
        val newPosition =
            (player.currentPosition - 10_000L)
                .coerceAtLeast(0L)

        player.seekTo(newPosition)
    }

    fun forward10Seconds() {
        val duration = player.duration
        val newPosition =
            player.currentPosition + 10_000L

        if (duration > 0L) {
            player.seekTo(
                newPosition.coerceAtMost(duration)
            )
        } else {
            player.seekTo(newPosition)
        }
    }

    fun setPlaybackSpeed(
        speed: Float
    ) {
        player.setPlaybackParameters(
            PlaybackParameters(speed)
        )
    }

    fun addListener(
        listener: Player.Listener
    ) {
        player.addListener(listener)
    }

    fun removeListener(
        listener: Player.Listener
    ) {
        player.removeListener(listener)
    }

    fun release() {
        player.release()
    }
}