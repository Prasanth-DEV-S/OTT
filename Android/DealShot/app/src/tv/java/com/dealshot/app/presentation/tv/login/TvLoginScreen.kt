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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
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
//import androidx.compose.ui.input.key.nativeKeyEvent
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
import com.dealshot.app.ui.theme.*

private val FieldColor = Color(0xFF171717)
private val KeyColor = Color(0xFF222222)
private val MutedText = Color(0xFF8A8A8A)

@Composable
fun TvLoginScreen(
    state: LoginState,
    onIntent: (LoginIntent) -> Unit
) {
    val isComplete = state.phoneNumber.length == 10
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
            .background(Color.Black)
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
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(orange.copy(alpha = 0.12f))
                        .border(1.dp, orange.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = "DealShot",
                        tint = orange,
                        modifier = Modifier.size(52.dp)
                    )
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    text = buildAnnotatedString {
                        append("Deal")
                        withStyle(SpanStyle(color = VividOrangeBg)) { append("Shot") }
                    },
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Sign in to continue",
                    fontSize = 22.sp,
                    color = Color(0xFFB0B0B0)
                )

                Spacer(Modifier.height(32.dp))

                Text(
                    text = "Use the number keys on your remote\nor the on-screen keypad.",
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
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
                // Stays focusable even before 10 digits so D-pad can always reach it;
                // the ViewModel already validates and sets state.error.
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
// Phone number display (read-only; input comes from keypad / remote)
// =====================================================================
@Composable
private fun PhoneDisplay(phone: String, isComplete: Boolean) {
    val shape = RoundedCornerShape(16.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(shape)
            .background(FieldColor)
            .border(1.dp, if (isComplete) orange else Color(0xFF3A3A3A), shape)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "+91",
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp
        )
        Spacer(Modifier.width(12.dp))
        Box(
            Modifier
                .width(1.dp)
                .height(24.dp)
                .background(Color(0xFF444444))
        )
        Spacer(Modifier.width(16.dp))

        if (phone.isEmpty()) {
            Text(
                text = "Enter mobile number",
                color = Color(0xFF6E6E6E),
                fontSize = 18.sp,
                modifier = Modifier.weight(1f)
            )
        } else {
            Text(
                text = phone.chunked(5).joinToString(" "),
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp,
                modifier = Modifier.weight(1f)
            )
        }

        Text(
            text = "${phone.length}/10",
            color = if (isComplete) orange else MutedText,
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
            .zIndex(if (focused) 1f else 0f) // draw the scaled key above its neighbours
            .scale(scale)
            .clip(shape)
            .background(if (focused) orange.copy(alpha = 0.25f) else KeyColor)
            .border(
                width = if (focused) 2.dp else 1.dp,
                color = if (focused) orange else Color(0xFF333333),
                shape = shape
            )
            // clickable() makes it D-pad focusable and handles DPAD_CENTER / ENTER
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
    val shape = RoundedCornerShape(16.dp)

    val brush = Brush.horizontalGradient(listOf(orange, VividOrange))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .zIndex(if (focused) 1f else 0f)
            .scale(scale)
            .alpha(if (isComplete || isLoading) 1f else 0.4f)
            .clip(shape)
            .background(brush, shape)
            .border(
                width = if (focused) 3.dp else 0.dp,
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
                    modifier = Modifier.size(22.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(10.dp))
            }
            Text(
                text = if (isLoading) "Loading..." else "Continue",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// =====================================================================
// Decorative background (landscape-friendly, private to TV)
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
                        colors = listOf(orange.copy(alpha = 0.22f), Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(520.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(VividOrange.copy(alpha = 0.14f), Color.Transparent)
                    )
                )
        )
    }
}
