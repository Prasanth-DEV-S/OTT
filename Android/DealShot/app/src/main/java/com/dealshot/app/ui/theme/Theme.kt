package com.dealshot.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Define Gold Palette Colors
val TrueOrange = GoldPrimary
val VividOrange = GoldSecondary
val DeepRustBg = DeepBlackBg
val SoftOrangeBg = Color(0xFFFFFDF5)
val SurfaceDarkf = SurfaceDarkGold
val SurfaceLight = Color(0xFFFFFFFF)

private val DarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    secondary = GoldSecondary,
    background = DeepBlackBg,
    surface = SurfaceDarkGold,
    onPrimary = Color(0xFF140E02),          // Dark text on gold button
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = GoldPrimary,
    secondary = GoldSecondary,
    background = SoftOrangeBg,
    surface = SurfaceLight,
    onPrimary = Color(0xFF140E02),
    onBackground = Color(0xFF161412),
    onSurface = Color(0xFF161412)
)

/*private val DarkColorScheme = darkColorScheme(
    primary = PrimaryPurple,
    secondary = LightPurple,
    background = DarkBackground,
    surface = SurfaceDark,
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryPurple,
    secondary = LightPurple,
    background = DarkBackground,
    surface = SurfaceDark,
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White

    *//* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    *//*
)*/

@Composable
fun StreamTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}