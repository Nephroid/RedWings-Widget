package com.redwings.widget.ui.theme

import android.content.Context
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext

private const val PREFS_NAME = "com.redwings.widget.PREFS"
private const val LEGACY_PREFS_NAME = "RedWingsPrefs"
const val KEY_THEME = "widget_theme_index"

@Composable
fun rememberDefaultAppJersey(): AppJersey {
    val currentPalette = LocalJerseyPalette.current
    val matching = AppJersey.values().firstOrNull { it.toPalette() == currentPalette }
    if (matching != null && matching != AppJersey.HOME) {
        return matching
    }
    val context = LocalContext.current
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val index = if (prefs.contains(KEY_THEME)) {
        prefs.getInt(KEY_THEME, AppJersey.HOME.id)
    } else {
        context.getSharedPreferences(LEGACY_PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_THEME, AppJersey.HOME.id)
    }
    return AppJersey.fromIndex(index)
}

private val DarkColorScheme = darkColorScheme(
    primary = WingsRed,
    onPrimary = WingsWhite,
    secondary = WingsSilver,
    onSecondary = WingsBlack,
    tertiary = WingsGold,
    background = WingsBlack,
    onBackground = WingsWhite,
    surface = WingsDarkSurface,
    onSurface = WingsWhite,
    surfaceVariant = WingsDarkCard,
    onSurfaceVariant = WingsSilver,
    surfaceContainer = WingsDarkCard,
    surfaceContainerHigh = WingsDarkElevated,
    surfaceContainerLow = WingsDarkSurface,
    error = WingsLossRed,
    onError = WingsWhite
)

private val LightColorScheme = lightColorScheme(
    primary = WingsRed,
    onPrimary = WingsWhite,
    secondary = WingsSilver,
    onSecondary = WingsBlack,
    tertiary = WingsGold,
    background = IceBlue,
    onBackground = WingsBlack,
    surface = WingsLightCard,
    onSurface = WingsBlack,
    surfaceVariant = WingsLightBackground,
    onSurfaceVariant = WingsBlack,
    surfaceContainer = WingsLightCard,
    surfaceContainerHigh = WingsLightCard,
    surfaceContainerLow = IceBlue,
    error = WingsLossRed,
    onError = WingsWhite
)

@Composable
fun RedWingsTheme(
    jersey: AppJersey = rememberDefaultAppJersey(),
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Brand first: dynamic wallpaper tinting washes out the Wings red,
    // so it stays OFF unless a caller explicitly opts in.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val palette = jersey.toPalette()

    val baseScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val colorScheme = baseScheme.copy(
        background = palette.background,
        surface = palette.cardSurface,
        surfaceVariant = palette.cardSurface,
        surfaceContainer = palette.cardSurface,
        surfaceContainerHigh = palette.cardSurface,
        surfaceContainerLow = palette.background,
        onBackground = palette.primaryText,
        onSurface = palette.primaryText,
        onSurfaceVariant = palette.secondaryText,
        primary = palette.accentRed
    )

    CompositionLocalProvider(LocalJerseyPalette provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
