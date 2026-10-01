package com.kitapcepte.core.designsystem.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Immutable
data class AppCustomShapes(
    val pill: Shape = CircleShape,
    val card: Shape = RoundedCornerShape(20.dp),
    val bottomSheet: Shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    val button: Shape = RoundedCornerShape(16.dp),
    val buttonPill: Shape = RoundedCornerShape(50),
    val tag: Shape = RoundedCornerShape(8.dp),
    val input: Shape = RoundedCornerShape(14.dp)
)

val LocalAppShapes = staticCompositionLocalOf { AppCustomShapes() }

val Shapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)
