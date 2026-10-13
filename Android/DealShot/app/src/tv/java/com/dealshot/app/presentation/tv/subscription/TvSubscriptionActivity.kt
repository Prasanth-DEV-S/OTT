package com.dealshot.app.presentation.tv.subscription

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.dealshot.app.ui.theme.StreamTheme

/**
 * TV counterpart of SubscriptionActivity. Accepts the same intent extras
 * ("title", "description", "url") and uses the same SubscriptionViewModel.
 * Declare it in the TV manifest (see notes) and launch it instead of the
 * mobile SubscriptionActivity from the TV nav graph.
 */
class TvSubscriptionActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val title = intent.getStringExtra("title") ?: ""
        val description = intent.getStringExtra("description") ?: ""
        val url = intent.getStringExtra("url") ?: ""

        setContent {
            StreamTheme {
                TvSubscriptionRoute(
                    title = title,
                    description = description,
                    url = url,
                    onFinished = { finish() }
                )
            }
        }
    }
}
