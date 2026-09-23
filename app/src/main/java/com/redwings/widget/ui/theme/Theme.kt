package com.redwings.widget.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = WingsRed,
    onPrimary = WingsWhite,
    secondary = WingsSilver,
    onSecondary = WingsBlack,
    tertiary = WingsSilver,
    background = WingsBlack,
    onBackground = WingsWhite,
    surface = WingsDarkSurface,
    onSurface = WingsWhite,
    surfaceVariant = WingsDarkCard,
    onSurfaceVariant = WingsSilver,
    error = WingsLossRed,
    onError = WingsWhite
)

private val LightColorScheme = lightColorScheme(
    primary = WingsRed,
    onPrimary = WingsWhite,
    secondary = WingsSilver,
    onSecondary = WingsBlack,
    tertiary = WingsSilver,
    background = WingsLightBackground,
    onBackground = WingsBlack,
    surface = WingsLightCard,
    onSurface = WingsBlack,
    surfaceVariant = WingsLightBackground,
    onSurfaceVariant = WingsBlack,
    error = WingsLossRed,
    onError = WingsWhite
)

@Composable
fun RedWingsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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
