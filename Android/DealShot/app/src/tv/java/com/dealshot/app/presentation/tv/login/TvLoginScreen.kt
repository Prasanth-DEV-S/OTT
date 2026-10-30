package com.dealshot.app.presentation.tv.login

import android.view.KeyEvent as AndroidKeyEvent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import com.dealshot.app.R
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.dealshot.app.presentation.shared.login.LoginIntent
import com.dealshot.app.presentation.shared.login.LoginState
import com.dealshot.app.ui.theme.DeepBlackBg
import com.dealshot.app.ui.theme.GoldPrimary
import com.dealshot.app.ui.theme.GoldSecondary
import com.dealshot.app.ui.theme.GoldTextLight

private val FieldColor = Color(0xFF1F1C18)
private val KeyColor = Color(0xFF221F1B)
private val MutedText = Color(0xFF9E988D)

@Composable
fun TvLoginScreen(
    state: LoginState,
    onIntent: (LoginIntent) -> Unit
) {
    val isComplete = state.phoneNumber.length == 10 && state.phoneNumber.firstOrNull() in '6'..'9'
    val firstKeyFocus = remember { FocusRequester() }

    // Land focus on the keypad as soon as the screen opens (TV has no touch to "tap into" a field).
    LaunchedEffect(Unit) { firstKeyFocus.requestFocus() }

    fun appendDigit(d: Char) {
        if (!state.isLoading) onIntent(LoginIntent.PhoneChanged(state.phoneNumber + d))
    }

    fun backspace() {
        if (!state.isLoading) onIntent(LoginIntent.PhoneChanged(state.phoneNumber.dropLast(1)))
    }

    fun clearAll() {
        if (!state.isLoading) onIntent(LoginIntent.PhoneChanged(""))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlackBg)
            // Physical remote number keys / keyboard. D-pad + Enter are NOT consumed here.
            .onPreviewKeyEvent { ev ->
                if (ev.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                val code = ev.nativeKeyEvent.keyCode
                when (code) {
                    in AndroidKeyEvent.KEYCODE_0..AndroidKeyEvent.KEYCODE_9 -> {
                        appendDigit('0' + (code - AndroidKeyEvent.KEYCODE_0)); true
                    }
                    in AndroidKeyEvent.KEYCODE_NUMPAD_0..AndroidKeyEvent.KEYCODE_NUMPAD_9 -> {
                        appendDigit('0' + (code - AndroidKeyEvent.KEYCODE_NUMPAD_0)); true
                    }
                    AndroidKeyEvent.KEYCODE_DEL -> {
                        backspace(); true
                    }
                    else -> false
                }
            }
    ) {
        TvLoginBackground()

        // TV safe area (overscan): 48dp horizontal / 27dp vertical
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp, vertical = 27.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // ---------------- LEFT: branding ----------------
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Box(
                    modifier = Modifier.size(96.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .background(
                                Brush.radialGradient(
                                    listOf(GoldPrimary.copy(alpha = 0.35f), Color.Transparent)
                                )
                            )
                    )
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF1E1A15))
                            .border(1.dp, Color(0xFF453826), RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.5.dp, GoldPrimary, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.play_fill),
                                contentDescription = "DealShot",
                                tint = GoldPrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = GoldTextLight)) { append("Deal") }
                        withStyle(SpanStyle(color = GoldPrimary)) { append("Shot") }
                    },
                    fontSize = 52.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )

                Spacer(Modifier.height(10.dp))

                Text(
                    text = "Sign in to watch premium stories.",
                    fontSize = 20.sp,
                    color = Color(0xFF9E988D)
                )

                Spacer(Modifier.height(28.dp))

                Text(
                    text = "Use the number keys on your remote\nor the on-screen keypad.",
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = MutedText
                )
            }

            Spacer(Modifier.width(48.dp))

            // ---------------- RIGHT: input panel ----------------
            Column(
                modifier = Modifier
                    .width(400.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {

                PhoneDisplay(phone = state.phoneNumber, isComplete = isComplete)

                // Fixed-height slot so the keypad never jumps when an error appears
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    state.error?.let { error ->
                        Text(
                            text = error,
                            color = Color(0xFFFF6B6B),
                            fontSize = 14.sp,
                            maxLines = 1,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFF6B6B).copy(alpha = 0.12f))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }

                // ---- Keypad ----
                val rows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9")
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    rows.forEachIndexed { r, row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEachIndexed { c, label ->
                                TvKey(
                                    text = label,
                                    enabled = !state.isLoading,
                                    modifier = Modifier
                                        .weight(1f)
                                        .then(
                                            if (r == 0 && c == 0)
                                                Modifier.focusRequester(firstKeyFocus)
                                            else Modifier
                                        ),
                                    onClick = { appendDigit(label[0]) }
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TvKey(
                            text = "Clear",
                            fontSize = 16.sp,
                            enabled = !state.isLoading,
                            modifier = Modifier.weight(1f),
                            onClick = ::clearAll
                        )
                        TvKey(
                            text = "0",
                            enabled = !state.isLoading,
                            modifier = Modifier.weight(1f),
                            onClick = { appendDigit('0') }
                        )
                        TvKey(
                            text = "Delete",
                            fontSize = 16.sp,
                            enabled = !state.isLoading,
                            modifier = Modifier.weight(1f),
                            onClick = ::backspace
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ---- Continue ----
                TvContinueButton(
                    isLoading = state.isLoading,
                    isComplete = isComplete,
                    onClick = { onIntent(LoginIntent.ContinueClicked) }
                )
            }
        }
    }
}

// =====================================================================
// mobile number display
// =====================================================================
@Composable
private fun PhoneDisplay(phone: String, isComplete: Boolean) {
    val shape = RoundedCornerShape(12.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(shape)
            .background(FieldColor)
            .border(1.dp, if (isComplete) GoldPrimary else Color(0xFF363027), shape)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "+91",
            color = GoldPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Spacer(Modifier.width(12.dp))
        Box(
            Modifier
                .width(1.dp)
                .height(22.dp)
                .background(Color(0xFF383228))
        )
        Spacer(Modifier.width(16.dp))

        if (phone.isEmpty()) {
            Text(
                text = "10-digit phone number",
                color = Color(0xFF756E63),
                fontSize = 17.sp,
                modifier = Modifier.weight(1f)
            )
        } else {
            Text(
                text = phone.chunked(5).joinToString(" "),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
                modifier = Modifier.weight(1f)
            )
        }

        Text(
            text = "${phone.length}/10",
            color = if (isComplete) GoldPrimary else MutedText,
            fontSize = 14.sp
        )
    }
}

// =====================================================================
// Focusable keypad key
// =====================================================================
@Composable
private fun TvKey(
    text: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    fontSize: androidx.compose.ui.unit.TextUnit = 22.sp,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val scale by animateFloatAsState(if (focused) 1.08f else 1f, label = "keyScale")
    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .height(52.dp)
            .zIndex(if (focused) 1f else 0f)
            .scale(scale)
            .clip(shape)
            .background(if (focused) GoldPrimary.copy(alpha = 0.25f) else KeyColor)
            .border(
                width = if (focused) 2.dp else 1.dp,
                color = if (focused) GoldPrimary else Color(0xFF383025),
                shape = shape
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) Color.White else Color.White.copy(alpha = 0.4f),
            fontSize = fontSize,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// =====================================================================
// Continue button
// =====================================================================
@Composable
private fun TvContinueButton(
    isLoading: Boolean,
    isComplete: Boolean,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val scale by animateFloatAsState(if (focused) 1.04f else 1f, label = "ctaScale")
    val shape = RoundedCornerShape(12.dp)

    val brush = Brush.horizontalGradient(
        listOf(
            Color(0xFFE8C575),
            Color(0xFFC9942C),
            Color(0xFF9E6B15)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .zIndex(if (focused) 1f else 0f)
            .scale(scale)
            .alpha(if (isComplete || isLoading) 1f else 0.4f)
            .clip(shape)
            .background(brush, shape)
            .border(
                width = if (focused) 2.5.dp else 0.dp,
                color = if (focused) Color.White else Color.Transparent,
                shape = shape
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = !isLoading,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color(0xFF140E02),
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(10.dp))
            }
            Text(
                text = if (isLoading) "Loading..." else "Continue",
                color = Color(0xFF140E02),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            if (!isLoading) {
                Spacer(Modifier.width(6.dp))
                Icon(
                    painter = painterResource(id = R.drawable.arrow_right),
                    contentDescription = null,
                    tint = Color(0xFF140E02),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// =====================================================================
// Decorative background (landscape-friendly for TV)
// =====================================================================
@Composable
private fun TvLoginBackground() {
    Box(Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(640.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(GoldPrimary.copy(alpha = 0.18f), Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(520.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(GoldSecondary.copy(alpha = 0.12f), Color.Transparent)
                    )
                )
        )
    }
}
