package com.dealshot.app.ui.theme

import androidx.compose.ui.graphics.Color

val PrimaryPurple = Color(0xFFFFA726)
val LightPurple = Color(0xFFFFCC80)

val DarkBackground = Color(0xFF000000)
val SurfaceDark = Color(0xFF1C1C1E)

val CardBackground = Color(0xFF1C1C1E)
val CardBorder = Color(0xFF2A2A2C)

val SecondaryText = Color(0xFFA0A0A0)
val HintText = Color(0xFF6C6C70)

// Premium Gold Palette
val GoldPrimary = Color(0xFFFFA726)          // Vivid orange/gold from the image
val GoldSecondary = Color(0xFFFFB74D)        // Slightly lighter variant
val GoldLight = Color(0xFFFFCC80)            // Bright highlight gold // Deep dark orange
val GoldTextLight = Color(0xFFFFFFFF)        // Pure white for crisp text
val DeepBlackBg = Color(0xFF000000)          // Pitch black background
val SurfaceDarkGold = Color(0xFF1C1C1E)      // Dark grey for icons/cards
val SurfaceLightGold = Color(0xFFFFFFFF)

// Button Gradient Tokens
val GoldGradientStart = Color(0xFFE8C575)
val GoldGradientCenter = Color(0xFFC9942C)
val GoldGradientEnd = Color(0xFF9E6B15)
val GoldButtonText = Color(0xFF140E02)

// Card & Container Tokens
val CardBgDark = Color(0xFF1C1C1E)
val CardBorderTop = Color(0xFF2A2A2C)
val CardBorderBottom = Color(0xFF151515)
val IconBoxBg = Color(0xFF1C1C1E)
val IconBoxBorder = Color(0xFF2A2A2C)

// Input Field Tokens
val FieldBg = Color(0xFF1F1C18)
val FieldBorderUnfocused = Color(0xFF363027)
val FieldPlaceholder = Color(0xFF756E63)
val FieldDivider = Color(0xFF383228)

// Status & Muted Text Tokens
val TextMuted = Color(0xFFA0A0A0)
val ErrorRed = Color(0xFFFF6B6B)
val SuccessGreen = Color(0xFF81C784)

// Background Wave Tokens
val WaveDarkStart = Color(0xFF261A0A)
val WaveDarkEnd = Color(0xFF100B04)
val WaveMidStart = Color(0xFF422E12)
val WaveMidEnd = Color(0xFF1A1206)
val WaveTopStart = Color(0xFF5E4319)
val WaveTopEnd = Color(0xFF2B1D0B)
val WaveHighlight = Color(0xFFC4983D)

// Legacy variable mappings (redirected to Gold palette for seamless app-wide theme change)
val orange = GoldPrimary
val TrueOrangeBg = GoldPrimary
val VividOrangeBg = GoldSecondary
val DeepRustBgBg = DeepBlackBg
val SoftOrangeBgBg = Color(0xFFFFFDF5)
val SurfaceDarkfBg = SurfaceDarkGold
val SurfaceLightBg = SurfaceLightGold