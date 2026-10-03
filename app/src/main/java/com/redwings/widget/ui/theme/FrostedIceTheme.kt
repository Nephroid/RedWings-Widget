package com.redwings.widget.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object FrostedIceColors {
    val RedWingsRed = Color(0xFFC8102E)
    val IceBackground = Color(0xFFE2EBF1)
    val GlassSurface = Color.White.copy(alpha = 0.72f)
    val GlassSurfaceOpaque = Color.White.copy(alpha = 0.88f)
    val GlassBorder = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = 0.85f),
            Color.White.copy(alpha = 0.20f)
        )
    )
    val TextPrimary = Color(0xFF111827)
    val TextMuted = Color(0xFF4B5563)
    val WinGreen = Color(0xFF10B981)
    val ActiveGlowRed = Color(0xFFC8102E).copy(alpha = 0.40f)
}

private val FrostedIceColorScheme = lightColorScheme(
    primary = FrostedIceColors.RedWingsRed,
    background = FrostedIceColors.IceBackground,
    surface = FrostedIceColors.GlassSurface,
    onPrimary = Color.White,
    onBackground = FrostedIceColors.TextPrimary,
    onSurface = FrostedIceColors.TextPrimary
)

@Composable
fun FrostedIceTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FrostedIceColorScheme,
        typography = Typography,
        content = content
    )
}
