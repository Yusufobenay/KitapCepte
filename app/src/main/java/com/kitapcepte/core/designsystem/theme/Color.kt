package com.kitapcepte.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val Primary = Color(0xFF5B3FE8)
val PrimaryContainer = Color(0xFFE8E3FB)
val OnPrimary = Color(0xFFFFFFFF)
val OnPrimaryContainer = Color(0xFF5B3FE8)

val BackgroundGradientStart = Color(0xFF6E7BE6)
val BackgroundGradientEnd = Color(0xFFA08BEF)

val Surface = Color(0xFFEEEBF8)
val SurfaceWhite = Color(0xFFFFFFFF)
val OnSurface = Color(0xFF1F1B33)
val OnSurfaceVariant = Color(0xFF8D8AA3)

val BadgeTopItem = Color(0xFFFFD93B)
val OnBadgeTopItem = Color(0xFF1F1B33)
val BadgeCart = Color(0xFFF04A3C)
val OnBadgeCart = Color(0xFFFFFFFF)

val BorderLight = Color(0xFFE0DCF0)

val BackgroundGradient = Brush.linearGradient(
    colors = listOf(BackgroundGradientStart, BackgroundGradientEnd),
    start = Offset.Zero,
    end = Offset.Infinite
)

@Immutable
data class ExtendedColors(
    val badgeTopItem: Color = BadgeTopItem,
    val onBadgeTopItem: Color = OnBadgeTopItem,
    val badgeCart: Color = BadgeCart,
    val onBadgeCart: Color = OnBadgeCart,
    val surfaceWhite: Color = SurfaceWhite,
    val borderLight: Color = BorderLight,
    val gradientStart: Color = BackgroundGradientStart,
    val gradientEnd: Color = BackgroundGradientEnd
)

val LocalExtendedColors = staticCompositionLocalOf { ExtendedColors() }
