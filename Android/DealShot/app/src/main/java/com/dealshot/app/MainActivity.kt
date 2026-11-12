package com.dealshot.app

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.dealshot.app.data.remote.ApiClient
import com.dealshot.app.data.remote.repository.VideoRepositoryImpl
import com.dealshot.app.ui.GoldenBackgroundHelper
import com.dealshot.app.ui.theme.StreamTheme
import com.dealshot.app.util.DeepLinkHelper
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var initialDeepLinkId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        val videoId = DeepLinkHelper.extractVideoId(intent?.data)
        if (!videoId.isNullOrBlank()) {
            handleDeepLink(videoId)
        }

        setContent {
            StreamTheme {
                StreamApp(initialDeepLinkId = initialDeepLinkId)
            }
        }

        GoldenBackgroundHelper.apply(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val videoId = DeepLinkHelper.extractVideoId(intent.data)
        if (!videoId.isNullOrBlank()) {
            handleDeepLink(videoId)
        }
    }

    private fun handleDeepLink(videoId: String) {
        lifecycleScope.launch {
            val repository = VideoRepositoryImpl(ApiClient.videoApi)
            val result = repository.resolveSharedVideo(videoId)
            result.fold(
                onSuccess = { resolveDto ->
                    val effectiveId = resolveDto.getEffectiveId().ifBlank { videoId }
                    initialDeepLinkId = effectiveId
                },
                onFailure = { error ->
                    val msg = error.localizedMessage ?: "This video is not available"
                    Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}
