package com.dealshot.app.presentation.mobile.login

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dealshot.app.presentation.shared.login.LoginIntent
import com.dealshot.app.presentation.shared.login.LoginState
import com.dealshot.app.ui.theme.*
@Composable
fun LoginScreen(
    state: LoginState,
    onIntent: (LoginIntent) -> Unit
) {
    val isButtonEnabled = !state.isLoading && state.phoneNumber.length == 10

    val buttonBrush = if (isButtonEnabled) {
        Brush.horizontalGradient(
            colors = listOf(
                orange,        // TrueOrange from your theme variables
                VividOrange
            )
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                orange.copy(alpha = 0.35f), // Faded, dull orange representation
                VividOrange.copy(alpha = 0.35f)
            )
        )
    }
//    LoginBackground()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        // ---------------------------------------------------------
        // TITLE
        // ---------------------------------------------------------

        Icon(
            Icons.Default.PlayCircle,
            contentDescription = "PiP",
            tint = orange,
            modifier = Modifier.size(40.dp)
        )
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
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = "Sign in to continue",
            fontSize = 16.sp,
            color = Color.LightGray
        )

        Spacer(
            modifier = Modifier.height(32.dp)
        )

        // ---------------------------------------------------------
        // MOBILE NUMBER
        // ---------------------------------------------------------

        OutlinedTextField(
            value = state.phoneNumber,

            onValueChange = { value ->

                Log.d(
                    "LOGIN_SCREEN",
                    "TEXT CHANGED = $value"
                )

                onIntent(
                    LoginIntent.PhoneChanged(value)
                )
            },

            modifier = Modifier
                .fillMaxWidth(),

            enabled = !state.isLoading,

            singleLine = true,

            label = {
                Text(
                    text = "Mobile Number"
                )
            },

            placeholder = {
                Text(
                    text = "Enter mobile number"
                )
            },

            leadingIcon = {
                Text(
                    text = "+91",
                    fontWeight = FontWeight.Medium
                )
            },

            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone
            ),

            shape = RoundedCornerShape(16.dp),

            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,

                focusedBorderColor = orange,
                unfocusedBorderColor = Color(0xFF555555),

                focusedLabelColor = VividOrange,
                unfocusedLabelColor = Color.Gray,

                cursorColor = Color.Black,

                focusedContainerColor = Color(0xFF171717),
                unfocusedContainerColor = Color(0xFF171717),

                disabledContainerColor = Color(0xFF171717)
            )
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        // ---------------------------------------------------------
        // ERROR
        // ---------------------------------------------------------

        state.error?.let { error ->

            Text(
                text = error,
                color = Color(0xFFFF6B6B),
                fontSize = 14.sp
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )
        }

        // ---------------------------------------------------------
        // CONTINUE BUTTON
        // ---------------------------------------------------------

// 1. Define the dynamic brush colors based on your evaluation state
        val isButtonEnabled = !state.isLoading && state.phoneNumber.length == 10

        val buttonBrush = if (isButtonEnabled) {
            Brush.horizontalGradient(
                colors = listOf(
                    orange,        // TrueOrange from your theme variables
                    VividOrange
                )
            )
        } else {
            Brush.horizontalGradient(
                colors = listOf(
                    orange.copy(alpha = 0.35f), // Faded, dull orange representation
                    VividOrange.copy(alpha = 0.35f)
                )
            )
        }

        Button(
            onClick = {
                onIntent(LoginIntent.ContinueClicked)
            },

            // Use the evaluated state variable here
            enabled = isButtonEnabled,

            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent
            ),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = buttonBrush, // Apply the dynamic brush context here
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (state.isLoading) "Loading..." else "Continue",
                    // Also fade the text slightly when disabled for a better UI look
                    color = if (isButtonEnabled) Color.White else Color.White.copy(alpha = 0.5f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

    }


}
