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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
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
import com.dealshot.app.ui.theme.SecondaryText
import com.dealshot.app.ui.theme.VividOrange
import com.dealshot.app.ui.theme.VividOrangeBg
import com.dealshot.app.ui.theme.orange

private const val OTP_LENGTH = 6

private val KeyBg = Color(0xFF16151D)
private val KeyFocusedBg = Color(0xFF2A2433)
private val KeyBorder = Color(0xFF2A2833)
private val OtpBoxBg = Color(0xFF0F0E15)
private val OtpBoxBorder = Color(0xFF1E1C29)
private val SuccessGreen = Color(0xFF4CD964)
private val ErrorRed = Color(0xFFFF6B6B)

/**
 * TV version of OtpScreen.
 *
 * Same state and intents as the phone screen (OtpState / OtpIntent), so the
 * shared OtpViewModel is reused without any change.
 *
 * TV differences:
 *  - Two-column landscape layout with TV safe-area margins (48dp x 27dp).
 *  - On-screen number keypad that works with the D-pad. The system
 *    keyboard is not used, because it is slow with a remote.
 *  - Remote number keys and backspace also work (hardware keys).
 *  - Every button shows a clear focus state (orange border + slight zoom).
 *  - Remote Back goes through OtpIntent.BackClicked.
 */
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

    // Focus the keypad when the screen opens
    LaunchedEffect(Unit) {
        runCatching { firstKeyFocus.requestFocus() }
    }

    // After the 6th digit, jump to "Verify & Continue"
    LaunchedEffect(state.otp.length) {
        if (state.otp.length == OTP_LENGTH && verifyEnabled) {
            runCatching { verifyFocus.requestFocus() }
        }
    }

    // Remote Back button
    BackHandler {
        onIntent(OtpIntent.BackClicked)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            // Remote number keys and backspace
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
                        append("Deal")
                        withStyle(SpanStyle(color = VividOrangeBg)) {
                            append("Shot")
                        }
                    },
                    color = Color.White,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "MOVIES  TV SHOWS  BEYOND",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = "Verify your number",
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Enter the 6-digit code sent to",
                    color = SecondaryText,
                    fontSize = 20.sp
                )

                Text(
                    text = "+91 ${state.mobileHint}",
                    color = SecondaryText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(28.dp))

                OtpBoxes(
                    otp = state.otp,
                    modifier = Modifier.fillMaxWidth()
                )

                // Success message / error (shown once)
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

                Spacer(modifier = Modifier.height(24.dp))

                TvKey(
                    modifier = Modifier.height(52.dp),
                    enabled = !state.isLoading,
                    onClick = { onIntent(OtpIntent.ResendClicked) }
                ) {
                    Text(
                        text = "Resend code",
                        color = orange,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
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
                            imageVector = Icons.AutoMirrored.Filled.Backspace,
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
                        .height(60.dp),
                    focusRequester = verifyFocus,
                    enabled = verifyEnabled,
                    accent = true,
                    onClick = { onIntent(OtpIntent.VerifyClicked) }
                ) {
                    Text(
                        text = if (state.isLoading) "Verifying..." else "Verify & Continue",
                        color = if (verifyEnabled) Color.White else Color.Gray,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
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
        modifier = modifier.widthIn(max = 440.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        repeat(OTP_LENGTH) { index ->

            val char = otp.getOrNull(index)?.toString().orEmpty()

            val isActive =
                index == otp.length ||
                        (index == OTP_LENGTH - 1 && otp.length == OTP_LENGTH)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .background(OtpBoxBg, RoundedCornerShape(14.dp))
                    .border(
                        width = 2.dp,
                        color = if (isActive) VividOrange else OtpBoxBorder,
                        shape = RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = char,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Focusable button for D-pad use.
 * Focused: orange border (white for the accent button) and a slight zoom.
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

    val shape = RoundedCornerShape(16.dp)

    val background = when {
        accent && enabled -> orange
        accent -> Color(0xFF261C38)
        isFocused -> KeyFocusedBg
        else -> KeyBg
    }

    val borderColor = when {
        isFocused && accent -> Color.White
        isFocused -> orange
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
                width = if (isFocused) 3.dp else 1.dp,
                color = borderColor,
                shape = shape
            )
            // onFocusChanged must come before clickable (the focusable node)
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
