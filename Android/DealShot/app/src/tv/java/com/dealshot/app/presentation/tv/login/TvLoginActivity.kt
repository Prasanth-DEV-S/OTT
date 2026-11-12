package com.dealshot.app.presentation.tv.login

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme

class TvLoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                TvLoginRoute(
                    onNavigateToOtp = { phoneNumber, mobileHint ->
                        // TODO: navigate to TV OTP screen once implemented
                    }
                )
            }
        }

        com.dealshot.app.ui.GoldenBackgroundHelper.apply(this)
    }
}
