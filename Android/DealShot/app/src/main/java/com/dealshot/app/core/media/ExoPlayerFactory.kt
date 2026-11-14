package com.dealshot.app.core.media

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer

@OptIn(UnstableApi::class)
object ExoPlayerFactory {

    fun createRenderersFactory(context: Context): DefaultRenderersFactory {
        return DefaultRenderersFactory(context.applicationContext).apply {
            setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
            setEnableDecoderFallback(true)
        }
    }

    fun createExoPlayer(context: Context): ExoPlayer {
        val renderersFactory = createRenderersFactory(context)
        return ExoPlayer.Builder(context.applicationContext, renderersFactory).build()
    }
}
