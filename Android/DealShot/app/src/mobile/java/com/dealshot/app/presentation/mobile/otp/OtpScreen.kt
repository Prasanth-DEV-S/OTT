package com.dealshot.app.presentation.mobile.otp

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.res.painterResource
import com.dealshot.app.R
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import kotlinx.coroutines.delay
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.dealshot.app.presentation.mobile.login.LoginBackground
import com.dealshot.app.presentation.shared.otp.OtpIntent
import com.dealshot.app.presentation.shared.otp.OtpState
import com.dealshot.app.ui.theme.CardBgDark
import com.dealshot.app.ui.theme.CardBorderBottom
import com.dealshot.app.ui.theme.CardBorderTop
import com.dealshot.app.ui.theme.DeepBlackBg
import com.dealshot.app.ui.theme.ErrorRed
import com.dealshot.app.ui.theme.FieldBg
import com.dealshot.app.ui.theme.FieldBorderUnfocused
import com.dealshot.app.ui.theme.GoldButtonText
import com.dealshot.app.ui.theme.GoldGradientCenter
import com.dealshot.app.ui.theme.GoldGradientEnd
import com.dealshot.app.ui.theme.GoldGradientStart
import com.dealshot.app.ui.theme.GoldPrimary
import com.dealshot.app.ui.theme.GoldTextLight
import com.dealshot.app.ui.theme.IconBoxBg
import com.dealshot.app.ui.theme.IconBoxBorder
import com.dealshot.app.ui.theme.SuccessGreen
import com.dealshot.app.ui.theme.TextMuted

@Composable
fun OtpScreen(
    state: OtpState,
    onIntent: (OtpIntent) -> Unit
) {
    var resendTimer by remember { mutableIntStateOf(30) }

    LaunchedEffect(resendTimer) {
        if (resendTimer > 0) {
            delay(1000L)
            resendTimer -= 1
        }
    }

    val isButtonEnabled = state.canVerify && !state.isLoading

    val buttonBrush = if (isButtonEnabled) {
        Brush.horizontalGradient(
            listOf(
                GoldGradientStart,
                GoldGradientCenter,
                GoldGradientEnd
            )
        )
    } else {
        Brush.horizontalGradient(
            listOf(
                GoldGradientStart.copy(alpha = 0.35f),
                GoldGradientCenter.copy(alpha = 0.35f),
                GoldGradientEnd.copy(alpha = 0.35f)
            )
        )
    }

    val displayPhone = if (state.phoneNumber.isNotBlank()) {
        state.phoneNumber
    } else {
        state.mobileHint ?: ""
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlackBg)
    ) {

        // Luxury dark background with golden waves and floating particles
        LoginBackground()

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
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                // -------------------------------------------------
                // CENTRAL CARD CONTAINER
                // -------------------------------------------------
                Box(
                    modifier = Modifier
                        .widthIn(max = 420.dp)
                        .fillMaxWidth()
                        .shadow(
                            elevation = 24.dp,
                            shape = RoundedCornerShape(24.dp),
                            spotColor = GoldPrimary.copy(alpha = 0.25f),
                            ambientColor = GoldPrimary.copy(alpha = 0.15f)
                        )
                        .clip(RoundedCornerShape(24.dp))
                        .background(CardBgDark.copy(alpha = 0.94f))
                        .border(
                            width = 1.dp,
                            brush = Brush.verticalGradient(
                                listOf(
                                    CardBorderTop,
                                    CardBorderBottom
                                )
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(horizontal = 28.dp, vertical = 32.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        // ---------------------------------------------
                        // LOGO ICON BOX
                        // ---------------------------------------------
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(80.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(
                                                GoldPrimary.copy(alpha = 0.35f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )

                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(IconBoxBg)
                                    .border(
                                        1.dp,
                                        IconBoxBorder,
                                        RoundedCornerShape(16.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .border(1.5.dp, GoldPrimary, RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.play_fill),
                                        contentDescription = "DealShot",
                                        tint = GoldPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // ---------------------------------------------
                        // TITLE
                        // ---------------------------------------------
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(color = GoldTextLight)) {
                                    append("Verify ")
                                }
                                withStyle(SpanStyle(color = GoldPrimary)) {
                                    append("OTP")
                                }
                            },
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Enter the 6-digit OTP sent to",
                            fontSize = 14.sp,
                            color = TextMuted
                        )

                        if (displayPhone.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (displayPhone.startsWith("+")) displayPhone else "+91 $displayPhone",
                                fontSize = 15.sp,
                                color = GoldPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // ---------------------------------------------
                        // OTP 6-BOX FIELD
                        // ---------------------------------------------
                        CustomOtpView(
                            otpText = state.otp,
                            onOtpTextChange = { value ->
                                onIntent(OtpIntent.OtpChanged(value))
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // ---------------------------------------------
                        // MESSAGES / ERRORS
                        // ---------------------------------------------
                        state.message?.let { message ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = message,
                                color = SuccessGreen,
                                fontSize = 13.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SuccessGreen.copy(alpha = 0.12f))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }

                        state.error?.let { error ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = error,
                                color = ErrorRed,
                                fontSize = 13.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ErrorRed.copy(alpha = 0.12f))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // ---------------------------------------------
                        // VERIFY BUTTON
                        // ---------------------------------------------
                        Button(
                            onClick = { onIntent(OtpIntent.VerifyClicked) },
                            enabled = isButtonEnabled,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
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
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    if (state.isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = GoldButtonText,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }

                                    Text(
                                        text = if (state.isLoading) "Verifying..." else "Verify OTP",
                                        color = if (isButtonEnabled || state.isLoading) {
                                            GoldButtonText
                                        } else {
                                            GoldButtonText.copy(alpha = 0.5f)
                                        },
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    if (!state.isLoading) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            painter = painterResource(id = R.drawable.arrow_right),
                                            contentDescription = null,
                                            tint = if (isButtonEnabled) {
                                                GoldButtonText
                                            } else {
                                                GoldButtonText.copy(alpha = 0.5f)
                                            },
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // ---------------------------------------------
                        // CHANGE PHONE NUMBER ACTION
                        // ---------------------------------------------
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onIntent(OtpIntent.BackClicked) }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.chevron_left),
                                contentDescription = "Change phone number",
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Change phone number",
                                color = TextMuted,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // ---------------------------------------------
                        // RESEND CODE ACTION / COUNTDOWN TIMER
                        // ---------------------------------------------
                        val formattedTime = String.format("00:%02d", resendTimer)

                        if (resendTimer > 0) {
                            Text(
                                text = "Resend code in $formattedTime",
                                color = TextMuted,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            TextButton(
                                onClick = {
                                    resendTimer = 30
                                    onIntent(OtpIntent.ResendClicked)
                                },
                                enabled = !state.isLoading
                            ) {
                                Text(
                                    text = "Resend code",
                                    color = GoldPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // -------------------------------------------------
        // LOGIN SUCCESSFUL POPUP DIALOG
        // -------------------------------------------------
        if (state.isSuccess) {
            LoginSuccessDialog()
        }
    }
}

@Composable
fun LoginSuccessDialog(
    onDismiss: () -> Unit = {}
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 380.dp)
                .fillMaxWidth()
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = GoldPrimary.copy(alpha = 0.35f),
                    ambientColor = GoldPrimary.copy(alpha = 0.20f)
                )
                .clip(RoundedCornerShape(20.dp))
                .background(CardBgDark)
                .border(
                    width = 1.dp,
                    color = GoldPrimary,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 28.dp, vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Gold Circle with Checkmark Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .border(2.dp, GoldPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.checkmark),
                        contentDescription = "Success",
                        tint = GoldPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Title: Login Successful!
                Text(
                    text = "Login Successful!",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.3.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Subtitle: You have logged in successfully.
                Text(
                    text = "You have logged in successfully.",
                    color = TextMuted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun CustomOtpView(
    otpText: String,
    onOtpTextChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = otpText,
        onValueChange = {
            if (it.length <= 6 && it.all(Char::isDigit)) {
                onOtpTextChange(it)
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number
        ),
        modifier = modifier,
        decorationBox = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(6) { index ->
                    val char = otpText.getOrNull(index)?.toString() ?: ""
                    val isActive = index == otpText.length || (index == 5 && otpText.length == 6)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(FieldBg)
                            .border(
                                width = if (isActive) 1.5.dp else 1.dp,
                                color = if (isActive) GoldPrimary else FieldBorderUnfocused,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (char.isNotEmpty()) {
                            Text(
                                text = char,
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else if (index == otpText.length) {
                            // Active glowing cursor indicator
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(20.dp)
                                    .background(GoldPrimary)
                            )
                        }
                    }
                }
            }
        }
    )
}
