package com.dealshot.app.presentation.tv.otp

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import com.dealshot.app.R
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dealshot.app.presentation.shared.otp.OtpIntent
import com.dealshot.app.presentation.shared.otp.OtpState
import com.dealshot.app.ui.theme.DeepBlackBg
import com.dealshot.app.ui.theme.GoldPrimary
import com.dealshot.app.ui.theme.GoldSecondary
import com.dealshot.app.ui.theme.GoldTextLight

private const val OTP_LENGTH = 6

private val KeyBg = Color(0xFF221F1B)
private val KeyFocusedBg = Color(0xFF382F23)
private val KeyBorder = Color(0xFF383025)
private val OtpBoxBg = Color(0xFF1F1C18)
private val OtpBoxBorder = Color(0xFF363027)
private val SuccessGreen = Color(0xFF81C784)
private val ErrorRed = Color(0xFFFF6B6B)

@Composable
fun TvOtpScreen(
    state: OtpState,
    onIntent: (OtpIntent) -> Unit
) {
    val firstKeyFocus = remember { FocusRequester() }
    val verifyFocus = remember { FocusRequester() }

    val inputEnabled = !state.isLoading
    val verifyEnabled = state.canVerify && !state.isLoading

    fun appendDigit(digit: Int) {
        if (inputEnabled && state.otp.length < OTP_LENGTH) {
            onIntent(OtpIntent.OtpChanged(state.otp + digit))
        }
    }

    fun backspace() {
        if (inputEnabled && state.otp.isNotEmpty()) {
            onIntent(OtpIntent.OtpChanged(state.otp.dropLast(1)))
        }
    }

    fun clearAll() {
        if (inputEnabled && state.otp.isNotEmpty()) {
            onIntent(OtpIntent.OtpChanged(""))
        }
    }

    LaunchedEffect(Unit) {
        runCatching { firstKeyFocus.requestFocus() }
    }

    var resendTimer by remember { mutableIntStateOf(30) }

    LaunchedEffect(resendTimer) {
        if (resendTimer > 0) {
            delay(1000L)
            resendTimer -= 1
        }
    }

    LaunchedEffect(state.otp.length) {
        if (state.otp.length == OTP_LENGTH && verifyEnabled) {
            runCatching { verifyFocus.requestFocus() }
        }
    }

    BackHandler {
        onIntent(OtpIntent.BackClicked)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlackBg)
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) {
                    return@onPreviewKeyEvent false
                }

                val digit = event.key.toDigitOrNull()

                when {
                    digit != null -> {
                        appendDigit(digit)
                        true
                    }

                    event.key == Key.Backspace || event.key == Key.Delete -> {
                        backspace()
                        true
                    }

                    else -> false
                }
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp, vertical = 27.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // -------------------------------------------------
            // LEFT: branding, message, OTP boxes, resend
            // -------------------------------------------------

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {

                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = GoldTextLight)) { append("Deal") }
                        withStyle(SpanStyle(color = GoldPrimary)) { append("Shot") }
                    },
                    color = Color.White,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Verify your number",
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Enter the 6-digit code sent to",
                    color = Color(0xFF9E988D),
                    fontSize = 20.sp
                )

                val displayPhone = if (state.phoneNumber.isNotBlank()) state.phoneNumber else (state.mobileHint ?: "")
                if (displayPhone.isNotBlank()) {
                    Text(
                        text = if (displayPhone.startsWith("+")) displayPhone else "+91 $displayPhone",
                        color = GoldPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                OtpBoxes(
                    otp = state.otp,
                    modifier = Modifier.fillMaxWidth()
                )

                state.message?.let { message ->
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = message,
                        color = SuccessGreen,
                        fontSize = 18.sp
                    )
                }

                state.error?.let { error ->
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = error,
                        color = ErrorRed,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TvKey(
                        modifier = Modifier.height(48.dp),
                        enabled = !state.isLoading,
                        onClick = { onIntent(OtpIntent.BackClicked) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.chevron_left),
                                contentDescription = null,
                                tint = Color(0xFF9E988D),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Change phone number",
                                color = Color(0xFF9E988D),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    val formattedTime = String.format("00:%02d", resendTimer)

                    TvKey(
                        modifier = Modifier.height(48.dp),
                        enabled = !state.isLoading && resendTimer == 0,
                        onClick = {
                            resendTimer = 30
                            onIntent(OtpIntent.ResendClicked)
                        }
                    ) {
                        Text(
                            text = if (resendTimer > 0) "Resend code in $formattedTime" else "Resend code",
                            color = if (resendTimer > 0) Color(0xFF9E988D) else GoldPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(48.dp))

            // -------------------------------------------------
            // RIGHT: keypad + verify
            // -------------------------------------------------

            Column(
                modifier = Modifier.width(KEYPAD_WIDTH),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                val rows = listOf(
                    listOf(1, 2, 3),
                    listOf(4, 5, 6),
                    listOf(7, 8, 9)
                )

                rows.forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(KEY_GAP)) {
                        row.forEach { digit ->
                            TvKey(
                                modifier = Modifier.size(KEY_WIDTH, KEY_HEIGHT),
                                focusRequester = if (digit == 1) firstKeyFocus else null,
                                enabled = inputEnabled,
                                onClick = { appendDigit(digit) }
                            ) {
                                Text(
                                    text = digit.toString(),
                                    color = Color.White,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(KEY_GAP))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(KEY_GAP)) {

                    TvKey(
                        modifier = Modifier.size(KEY_WIDTH, KEY_HEIGHT),
                        enabled = inputEnabled && state.otp.isNotEmpty(),
                        onClick = { backspace() }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_backspace),
                            contentDescription = "Delete",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    TvKey(
                        modifier = Modifier.size(KEY_WIDTH, KEY_HEIGHT),
                        enabled = inputEnabled,
                        onClick = { appendDigit(0) }
                    ) {
                        Text(
                            text = "0",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    TvKey(
                        modifier = Modifier.size(KEY_WIDTH, KEY_HEIGHT),
                        enabled = inputEnabled && state.otp.isNotEmpty(),
                        onClick = { clearAll() }
                    ) {
                        Text(
                            text = "Clear",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                TvKey(
                    modifier = Modifier
                        .width(KEYPAD_WIDTH)
                        .height(56.dp),
                    focusRequester = verifyFocus,
                    enabled = verifyEnabled,
                    accent = true,
                    onClick = { onIntent(OtpIntent.VerifyClicked) }
                ) {
                    Text(
                        text = if (state.isLoading) "Verifying..." else "Verify OTP",
                        color = Color(0xFF140E02),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (state.isSuccess) {
            TvLoginSuccessDialog()
        }
    }
}

@Composable
private fun TvLoginSuccessDialog() {
    Dialog(
        onDismissRequest = {},
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
                .background(Color(0xFF161412))
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

                Text(
                    text = "Login Successful!",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.3.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "You have logged in successfully.",
                    color = Color(0xFF9E988D),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

// ───────────────────────────── Components ─────────────────────────────

private val KEY_WIDTH = 88.dp
private val KEY_HEIGHT = 64.dp
private val KEY_GAP = 12.dp
private val KEYPAD_WIDTH = KEY_WIDTH * 3 + KEY_GAP * 2

@Composable
private fun OtpBoxes(
    otp: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(OTP_LENGTH) { index ->

            val char = otp.getOrNull(index)?.toString().orEmpty()

            val isActive =
                index == otp.length ||
                        (index == OTP_LENGTH - 1 && otp.length == OTP_LENGTH)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(OtpBoxBg)
                    .border(
                        width = if (isActive) 2.dp else 1.dp,
                        color = if (isActive) GoldPrimary else OtpBoxBorder,
                        shape = RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = char,
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Focusable button for D-pad use.
 */
@Composable
private fun TvKey(
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester? = null,
    enabled: Boolean = true,
    accent: Boolean = false,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.08f else 1f,
        label = "tvKeyScale"
    )

    val shape = RoundedCornerShape(12.dp)

    val background = when {
        accent && enabled -> GoldPrimary
        accent -> Color(0xFF382F23)
        isFocused -> KeyFocusedBg
        else -> KeyBg
    }

    val borderColor = when {
        isFocused && accent -> Color.White
        isFocused -> GoldPrimary
        else -> KeyBorder
    }

    Box(
        modifier = modifier
            .then(
                if (focusRequester != null) {
                    Modifier.focusRequester(focusRequester)
                } else {
                    Modifier
                }
            )
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = if (enabled) 1f else 0.45f
            }
            .clip(shape)
            .background(background)
            .border(
                width = if (isFocused) 2.5.dp else 1.dp,
                color = borderColor,
                shape = shape
            )
            .onFocusChanged { isFocused = it.isFocused }
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
        content = content
    )
}

private fun Key.toDigitOrNull(): Int? = when (this) {
    Key.Zero, Key.NumPad0 -> 0
    Key.One, Key.NumPad1 -> 1
    Key.Two, Key.NumPad2 -> 2
    Key.Three, Key.NumPad3 -> 3
    Key.Four, Key.NumPad4 -> 4
    Key.Five, Key.NumPad5 -> 5
    Key.Six, Key.NumPad6 -> 6
    Key.Seven, Key.NumPad7 -> 7
    Key.Eight, Key.NumPad8 -> 8
    Key.Nine, Key.NumPad9 -> 9
    else -> null
}
