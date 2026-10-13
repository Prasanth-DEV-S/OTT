package com.dealshot.app.presentation.mobile.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.dealshot.app.ui.theme.*

@Composable
fun LoginBackground() {
    val themeBg = MaterialTheme.colorScheme
    val themeAccent = MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = Color.Black
            )
    ) {

        // Top-right purple glow
        Box(
            modifier = Modifier
                .size(500.dp)
                .offset(
                    x = 200.dp,
                    y = (-180).dp
                )
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            VividOrange,
                            Color.Transparent
                        )
                    )
                )
        )

        // Bottom purple glow
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(250.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            TrueOrangeBg
                        )
                    )
                )
        )
    }
}