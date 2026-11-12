package com.dealshot.app.presentation.mobile.details

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.dealshot.app.ui.theme.StreamTheme

class MobileDetailsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val title =
            intent.getStringExtra("title") ?: ""

        val description =
            intent.getStringExtra("description") ?: ""

        val url =
            intent.getStringExtra("url") ?: ""

        val position =
            intent.getLongExtra(
                "position",
                0L
            )

        val isPremiumUser =
            intent.getBooleanExtra(
                "IS_PREMIUM",
                false
            )

        WindowCompat.setDecorFitsSystemWindows(
            window,
            false
        )

        setContent {

            StreamTheme {

//                MobileDetailsRoute(
//                    video = video,
//                    onBack = {
//                        navController.popBackStack()
//                    },
//                    onPlayVideo = {
//                        navController.navigate(
//                            StreamRoutes.player(video.id)
//                        )
//                    }
//                )
            }
        }

        com.dealshot.app.ui.GoldenBackgroundHelper.apply(this)
    }
}