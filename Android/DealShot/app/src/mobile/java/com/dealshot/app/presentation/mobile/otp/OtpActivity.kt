package com.dealshot.app.presentation.mobile.otp

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import com.dealshot.app.presentation.shared.otp.OtpViewModel

class OtpActivity : ComponentActivity() {

    private val viewModel: OtpViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        val phoneNumber =
            intent.getStringExtra(EXTRA_PHONE_NUMBER).orEmpty()

        setContent {

            MaterialTheme {

                OtpRoute(
                    phoneNumber = phoneNumber,

                    onNavigateToHome = {

                        Log.d(
                            "OTP_ACTIVITY",
                            "onNavigateToHome CALLED"
                        )
                    },

                    onNavigateBack = {
                        finish()
                    },
                    mobileHint = "test"
                )
            }
        }

        com.dealshot.app.ui.GoldenBackgroundHelper.apply(this)
    }
    companion object {
        const val EXTRA_PHONE_NUMBER = "extra_phone_number"
        const val EXTRA_MOBILE_HINT = "extra_mobile_hint"
    }
}