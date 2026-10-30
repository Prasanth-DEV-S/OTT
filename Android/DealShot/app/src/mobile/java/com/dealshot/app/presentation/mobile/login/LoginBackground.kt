package com.dealshot.app.presentation.mobile.login

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.dealshot.app.ui.theme.DeepBlackBg
import com.dealshot.app.ui.theme.GoldLight
import com.dealshot.app.ui.theme.GoldPrimary

@Composable
fun LoginBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlackBg)
    ) {

        // Ambient top-right soft golden glow
        Box(
            modifier = Modifier
                .size(600.dp)
                .offset(x = 220.dp, y = (-200).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            GoldPrimary.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Bottom layered wavy curves and floating golden sparkles
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // -----------------------------------------------------------------
            // Floating Sparkle Particles
            // -----------------------------------------------------------------
            val pRadius1 = 2.8.dp.toPx()
            val pRadius2 = 2.2.dp.toPx()
            val pRadius3 = 2.5.dp.toPx()
            val pRadius4 = 2.0.dp.toPx()

            val particles = listOf(
                Pair(Offset(width * 0.38f, height * 0.16f), pRadius1),
                Pair(Offset(width * 0.70f, height * 0.22f), pRadius2),
                Pair(Offset(width * 0.82f, height * 0.44f), pRadius3),
                Pair(Offset(width * 0.25f, height * 0.68f), pRadius4),
                Pair(Offset(width * 0.60f, height * 0.77f), pRadius2),
                Pair(Offset(width * 0.90f, height * 0.71f), pRadius3)
            )

            particles.forEach { (center, radius) ->
                // Outer glow
                drawCircle(
                    color = GoldLight.copy(alpha = 0.3f),
                    radius = radius * 2.5f,
                    center = center
                )
                // Core
                drawCircle(
                    color = Color.White.copy(alpha = 0.85f),
                    radius = radius,
                    center = center
                )
            }

            // -----------------------------------------------------------------
            // Layered Bottom Wavy Curves
            // -----------------------------------------------------------------

            // Wave Layer 1 (Deepest background wave)
            val path1 = Path().apply {
                moveTo(0f, height * 0.82f)
                cubicTo(
                    width * 0.25f, height * 0.88f,
                    width * 0.65f, height * 0.78f,
                    width, height * 0.84f
                )
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }
            drawPath(
                path = path1,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        com.dealshot.app.ui.theme.WaveDarkStart,
                        com.dealshot.app.ui.theme.WaveDarkEnd
                    )
                )
            )

            // Wave Layer 2 (Middle wave)
            val path2 = Path().apply {
                moveTo(0f, height * 0.87f)
                cubicTo(
                    width * 0.30f, height * 0.82f,
                    width * 0.70f, height * 0.92f,
                    width, height * 0.86f
                )
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }
            drawPath(
                path = path2,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        com.dealshot.app.ui.theme.WaveMidStart,
                        com.dealshot.app.ui.theme.WaveMidEnd
                    )
                )
            )

            // Wave Layer 3 (Top foreground wave)
            val path3 = Path().apply {
                moveTo(0f, height * 0.92f)
                cubicTo(
                    width * 0.35f, height * 0.88f,
                    width * 0.65f, height * 0.95f,
                    width, height * 0.89f
                )
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }
            drawPath(
                path = path3,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        com.dealshot.app.ui.theme.WaveTopStart,
                        com.dealshot.app.ui.theme.WaveTopEnd
                    )
                )
            )

            // Subtle top highlight stroke on Wave 3
            val strokePath = Path().apply {
                moveTo(0f, height * 0.92f)
                cubicTo(
                    width * 0.35f, height * 0.88f,
                    width * 0.65f, height * 0.95f,
                    width, height * 0.89f
                )
            }
            drawPath(
                path = strokePath,
                color = com.dealshot.app.ui.theme.WaveHighlight.copy(alpha = 0.5f),
                style = Stroke(width = 1.5f.dp.toPx())
            )
        }
    }
}