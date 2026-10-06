package com.dealshot.app.presentation.mobile.otp

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dealshot.app.presentation.shared.otp.OtpIntent
import com.dealshot.app.presentation.shared.otp.OtpState
import com.dealshot.app.ui.theme.SecondaryText
import com.dealshot.app.ui.theme.VividOrangeBg
import com.dealshot.app.ui.theme.orange

@Composable
fun OtpScreen(
    state: OtpState,
    onIntent: (OtpIntent) -> Unit
) {

    Log.d(
        "OTP_SCREEN",
        "OtpScreen COMPOSED"
    )

    Box(
        modifier = Modifier.fillMaxSize()
            .background(Color.Black)
    ) {

        // ---------------------------------------------------------
        // BACKGROUND
        // ---------------------------------------------------------

//        LoginBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // -----------------------------------------------------
            // TOP BAR
            // -----------------------------------------------------

            Spacer(
                modifier = Modifier.height(40.dp)
            )

            Box(
                modifier = Modifier.fillMaxWidth()
            ) {

                IconButton(
                    onClick = {

                        Log.d(
                            "OTP_SCREEN",
                            "BACK BUTTON CLICKED"
                        )

                        onIntent(
                            OtpIntent.BackClicked
                        )
                    },

                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .background(
                            color = Color(0xFF1E1E24)
                                .copy(alpha = 0.6f),
                            shape = CircleShape
                        )
                        .size(44.dp)
                ) {

                    Icon(
                        imageVector =
                            Icons.AutoMirrored.Filled.ArrowBack,

                        contentDescription = "Back",

                        tint = Color.White
                    )
                }

                Column(
                    modifier = Modifier.align(
                        Alignment.Center
                    ),
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    Text(
                        text = buildAnnotatedString {

                            append("Deal")

                            withStyle(
                                SpanStyle(
                                    color = VividOrangeBg
                                )
                            ) {
                                append("Shot")
                            }
                        },

                        color = Color.White,

                        fontSize = 28.sp,

                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "MOVIES  TV SHOWS  BEYOND",

                        color = Color.Gray,

                        fontSize = 9.sp,

                        fontWeight = FontWeight.SemiBold,

                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(50.dp)
            )

            // -----------------------------------------------------
            // OTP ICON
            // -----------------------------------------------------

            Box(
                modifier = Modifier
                    .size(140.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                VividOrangeBg
                                    .copy(alpha = 0.25f),

                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
                    .border(
                        width = 1.dp,
                        color = Color(0xFF32264D),
                        shape = CircleShape
                    ),

                contentAlignment = Alignment.Center
            ) {

                Icon(
                    imageVector =
                        Icons.Default.ChatBubble,

                    contentDescription = null,

                    tint = orange,

                    modifier = Modifier.size(52.dp)
                )
            }

            Spacer(
                modifier = Modifier.height(40.dp)
            )

            // -----------------------------------------------------
            // TITLE
            // -----------------------------------------------------

            Text(
                text = "Verify your number",

                color = Color.White,

                fontSize = 32.sp,

                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text = "Enter the 6-digit code sent to",

                color = SecondaryText,

                fontSize = 16.sp
            )

            Text(
                text = "+91 ${state.mobileHint}",

                color = SecondaryText,

                fontSize = 16.sp,

                fontWeight = FontWeight.Medium
            )
            Spacer(
                modifier = Modifier.height(32.dp)
            )
            state.message?.let { message ->

                Text(
                    text = message,
                    color = Color.Green,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            state.error?.let { error ->

                Text(
                    text = error,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }


            Spacer(
                modifier = Modifier.height(32.dp)
            )

            // -----------------------------------------------------
            // OTP INPUT
            // -----------------------------------------------------

            CustomOtpView(
                otpText = state.otp,

                onOtpTextChange = { value ->

                    Log.d(
                        "OTP_SCREEN",
                        "OTP CHANGED = $value"
                    )

                    onIntent(
                        OtpIntent.OtpChanged(value)
                    )
                },

                modifier = Modifier.fillMaxWidth()
            )

            // -----------------------------------------------------
            // ERROR
            // -----------------------------------------------------

            state.error?.let { error ->

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp
                )
            }

            Spacer(
                modifier = Modifier.height(40.dp)
            )

            // -----------------------------------------------------
            // VERIFY BUTTON
            // -----------------------------------------------------

            Button(
                onClick = {

                    Log.d(
                        "OTP_SCREEN",
                        "VERIFY BUTTON CLICKED"
                    )

                    onIntent(
                        OtpIntent.VerifyClicked
                    )
                },

                enabled = state.canVerify &&
                        !state.isLoading,

                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),

                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,

                    disabledContainerColor =
                        Color.Transparent
                ),

                contentPadding = PaddingValues(0.dp)
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush =
                                if (state.canVerify) {

                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            orange,
                                            VividOrangeBg
                                        )
                                    )

                                } else {

                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF261C38),
                                            Color(0xFF191224)
                                        )
                                    )
                                },

                            shape = RoundedCornerShape(16.dp)
                        ),

                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text =
                            if (state.isLoading) {
                                "Verifying..."
                            } else {
                                "Verify & Continue"
                            },

                        color =
                            if (state.canVerify) {
                                Color.White
                            } else {
                                Color.Gray
                            },

                        fontSize = 18.sp,

                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            // -----------------------------------------------------
            // RESEND
            // -----------------------------------------------------

            TextButton(
                onClick = {

                    Log.d(
                        "OTP_SCREEN",
                        "RESEND CLICKED"
                    )

                    onIntent(
                        OtpIntent.ResendClicked
                    )
                },

                enabled = !state.isLoading
            ) {

                Text(
                    text = "Resend code",

                    color = orange,

                    fontWeight = FontWeight.SemiBold,

                    fontSize = 16.sp
                )
            }
            Spacer(
                modifier = Modifier.height(24.dp)
            )
        }
    }
}