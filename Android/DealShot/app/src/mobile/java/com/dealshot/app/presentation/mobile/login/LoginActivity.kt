package com.dealshot.app.presentation.mobile.login

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import com.dealshot.app.presentation.mobile.otp.OtpActivity

class LoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        setContent {
            MaterialTheme {
                LoginRoute(
                    onNavigateToOtp = { phoneNumber, mobileHint ->
                        val intent = Intent(
                            this@LoginActivity,
                            OtpActivity::class.java
                        ).apply {
                            putExtra(
                                OtpActivity.EXTRA_PHONE_NUMBER,
                                phoneNumber
                            )
                            putExtra(
                                OtpActivity.EXTRA_MOBILE_HINT,
                                mobileHint
                            )
                        }
                        startActivity(intent)
                    },
                )
            }
        }
    }
}