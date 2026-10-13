
package com.dealshot.app.presentation.mobile.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
    // Same enable rule as before
    val isButtonEnabled = !state.isLoading && state.phoneNumber.length == 10

    val buttonBrush = if (isButtonEnabled) {
        Brush.horizontalGradient(listOf(orange, VividOrange))
    } else {
        Brush.horizontalGradient(
            listOf(
                orange.copy(alpha = 0.35f),
                VividOrange.copy(alpha = 0.35f)
            )
        )
    }

    val keyboardController = LocalSoftwareKeyboardController.current
    val fieldColor = Color(0xFF171717)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        // Decorative background glow (separate composable)
        LoginBackground()

        // imePadding is applied outside so the available height shrinks with the keyboard
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = maxHeight)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                // -------------------------------------------------
                // LOGO
                // -------------------------------------------------

                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(orange.copy(alpha = 0.12f))
                        .border(1.dp, orange.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = "DealShot",
                        tint = orange,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = buildAnnotatedString {
                        append("Deal")
                        withStyle(SpanStyle(color = VividOrangeBg)) {
                            append("Shot")
                        }
                    },
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Sign in to continue",
                    fontSize = 16.sp,
                    color = Color(0xFFB0B0B0)
                )

                Spacer(modifier = Modifier.height(40.dp))

                // -------------------------------------------------
                // MOBILE NUMBER
                // -------------------------------------------------

                OutlinedTextField(
                    value = state.phoneNumber,
                    onValueChange = { value ->
                        onIntent(LoginIntent.PhoneChanged(value))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading,
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                    ),
                    label = { Text(text = "Mobile Number") },
                    placeholder = { Text(text = "Enter mobile number") },
                    leadingIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 16.dp, end = 8.dp)
                        ) {
                            Text(
                                text = "+91",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(20.dp)
                                    .background(Color(0xFF444444))
                            )
                        }
                    },
                    supportingText = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "10-digit mobile number",
                                color = Color(0xFF8A8A8A),
                                fontSize = 12.sp
                            )
                            Text(
                                text = "${state.phoneNumber.length}/10",
                                color = if (state.phoneNumber.length == 10) {
                                    orange
                                } else {
                                    Color(0xFF8A8A8A)
                                },
                                fontSize = 12.sp
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            if (isButtonEnabled) {
                                onIntent(LoginIntent.ContinueClicked)
                            }
                        }
                    ),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        disabledTextColor = Color(0xFF9E9E9E),

                        focusedBorderColor = orange,
                        unfocusedBorderColor = Color(0xFF3A3A3A),
                        disabledBorderColor = Color(0xFF2A2A2A),

                        focusedLabelColor = VividOrange,
                        unfocusedLabelColor = Color.Gray,
                        disabledLabelColor = Color(0xFF6E6E6E),

                        focusedPlaceholderColor = Color(0xFF6E6E6E),
                        unfocusedPlaceholderColor = Color(0xFF6E6E6E),

                        cursorColor = orange,

                        focusedContainerColor = fieldColor,
                        unfocusedContainerColor = fieldColor,
                        disabledContainerColor = fieldColor
                    )
                )

                // -------------------------------------------------
                // ERROR
                // -------------------------------------------------

                state.error?.let { error ->
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = error,
                        color = Color(0xFFFF6B6B),
                        fontSize = 14.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFF6B6B).copy(alpha = 0.12f))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // -------------------------------------------------
                // CONTINUE BUTTON
                // -------------------------------------------------

                Button(
                    onClick = { onIntent(LoginIntent.ContinueClicked) },
                    enabled = isButtonEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = buttonBrush,
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (state.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                            }

                            Text(
                                text = if (state.isLoading) "Loading..." else "Continue",
                                color = if (isButtonEnabled || state.isLoading) {
                                    Color.White
                                } else {
                                    Color.White.copy(alpha = 0.5f)
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/*
 * Decorative background only. No state, no logic.
 * Change size / alpha / alignment here without touching the login layout.
 */
@Composable
private fun BoxScope.LoginBackground() {
    Box(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .size(420.dp)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        orange.copy(alpha = 0.22f),
                        Color.Transparent
                    )
                )
            )
    )
}

