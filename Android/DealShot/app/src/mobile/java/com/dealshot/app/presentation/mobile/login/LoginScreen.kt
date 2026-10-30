
package com.dealshot.app.presentation.mobile.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.res.painterResource
import com.dealshot.app.R
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
import androidx.compose.ui.draw.shadow
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
import com.dealshot.app.ui.theme.CardBgDark
import com.dealshot.app.ui.theme.CardBorderBottom
import com.dealshot.app.ui.theme.CardBorderTop
import com.dealshot.app.ui.theme.DeepBlackBg
import com.dealshot.app.ui.theme.ErrorRed
import com.dealshot.app.ui.theme.FieldBg
import com.dealshot.app.ui.theme.FieldBorderUnfocused
import com.dealshot.app.ui.theme.FieldDivider
import com.dealshot.app.ui.theme.FieldPlaceholder
import com.dealshot.app.ui.theme.GoldButtonText
import com.dealshot.app.ui.theme.GoldGradientCenter
import com.dealshot.app.ui.theme.GoldGradientEnd
import com.dealshot.app.ui.theme.GoldGradientStart
import com.dealshot.app.ui.theme.GoldPrimary
import com.dealshot.app.ui.theme.GoldTextLight
import com.dealshot.app.ui.theme.IconBoxBg
import com.dealshot.app.ui.theme.IconBoxBorder
import com.dealshot.app.ui.theme.TextMuted

@Composable
fun LoginScreen(
    state: LoginState,
    onIntent: (LoginIntent) -> Unit
) {
    val isButtonEnabled = !state.isLoading && state.phoneNumber.length == 10 && state.phoneNumber.firstOrNull() in '6'..'9'

    // Gold metallic gradient for continue button
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

    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlackBg)
    ) {

        // Premium layered background with golden waves and particles
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
                            // Subtle radial gold halo glow
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

                            // Rounded icon container
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
                                // Play icon inside rounded circle
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
                                    append("Deal")
                                }
                                withStyle(SpanStyle(color = GoldPrimary)) {
                                    append("Shot")
                                }
                            },
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Sign in to watch premium stories.",
                            fontSize = 14.sp,
                            color = TextMuted,
                            fontWeight = FontWeight.Normal
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        // ---------------------------------------------
                        // PHONE NUMBER INPUT
                        // ---------------------------------------------
                        OutlinedTextField(
                            value = state.phoneNumber,
                            onValueChange = { value ->
                                onIntent(LoginIntent.PhoneChanged(value))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.isLoading,
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                letterSpacing = 0.5.sp
                            ),
                            placeholder = {
                                Text(
                                    text = "10-digit phone number",
                                    color = FieldPlaceholder,
                                    fontSize = 15.sp
                                )
                            },
                            leadingIcon = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(start = 14.dp, end = 6.dp)
                                ) {
                                    Text(
                                        text = "+91",
                                        color = GoldPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(1.dp)
                                            .height(20.dp)
                                            .background(FieldDivider)
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
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                disabledTextColor = TextMuted,

                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = FieldBorderUnfocused,
                                disabledBorderColor = FieldBorderUnfocused.copy(alpha = 0.6f),

                                focusedPlaceholderColor = FieldPlaceholder,
                                unfocusedPlaceholderColor = FieldPlaceholder,

                                cursorColor = GoldPrimary,

                                focusedContainerColor = FieldBg,
                                unfocusedContainerColor = FieldBg,
                                disabledContainerColor = FieldBg
                            )
                        )

                        // ---------------------------------------------
                        // ERROR DISPLAY
                        // ---------------------------------------------
                        state.error?.let { error ->
                            Spacer(modifier = Modifier.height(10.dp))

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

                        Spacer(modifier = Modifier.height(20.dp))

                        // ---------------------------------------------
                        // CONTINUE BUTTON
                        // ---------------------------------------------
                        Button(
                            onClick = { onIntent(LoginIntent.ContinueClicked) },
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
                                        text = if (state.isLoading) "Loading..." else "Continue",
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
                    }
                }
            }
        }
    }
}