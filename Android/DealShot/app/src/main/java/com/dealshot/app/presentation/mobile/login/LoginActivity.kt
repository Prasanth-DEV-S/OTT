package com.dealshot.app.presentation.mobile.login

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.dealshot.app.presentation.mobile.otp.OtpActivity

class LoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )

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