package com.dealshot.app.presentation.mobile.otp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import com.dealshot.app.presentation.shared.otp.OtpViewModel

class OtpActivity : ComponentActivity() {

    private val viewModel: OtpViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(
            "OTP_ACTIVITY",
            "OtpActivity CREATED"
        )

        val phoneNumber =
            intent.getStringExtra(EXTRA_PHONE_NUMBER).orEmpty()

        Log.d(
            "OTP_ACTIVITY",
            "PHONE NUMBER = $phoneNumber"
        )

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
    }
    companion object {
        const val EXTRA_PHONE_NUMBER = "extra_phone_number"
        const val EXTRA_MOBILE_HINT = "extra_mobile_hint"
    }
}