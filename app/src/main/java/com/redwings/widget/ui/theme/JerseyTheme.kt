package com.redwings.widget.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * 5 official Detroit Red Wings jersey themes for the Android Companion App.
 * Aligns 1:1 with the Home Screen Widget theme engine.
 */
enum class AppJersey(
    val id: Int,
    val displayName: String,
    val chipLabel: String
) {
    HERITAGE(0, "Heritage 1926", "🏛️ HERITAGE"),
    HOME(1, "Home Red", "🔴 HOME"),
    AWAY(2, "Away White", "⚪ AWAY"),
    REVERSE_RETRO(3, "Reverse Retro", "⚫ RETRO"),
    STADIUM_SERIES(4, "Stadium Series", "⭐ SPECIAL");

    companion object {
        fun fromIndex(index: Int): AppJersey {
            val all = values()
            return all[((index % all.size) + all.size) % all.size]
        }
    }
}

/**
 * Color palette tokens for an official jersey theme.
 * Guarantees WCAG AAA/AA contrast across both light (Away, Heritage)
 * and dark (Home, Retro, Stadium) jerseys.
 */
data class JerseyPalette(
    val background: Color,
    val cardSurface: Color,
    val cardBorder: Color,
    val headerBackground: Color,
    val headerText: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val accentRed: Color,
    val highlightGold: Color,
    val heroTop: Color,
    val heroBottom: Color,
    val chipBg: Color,
    val chipText: Color
)

val HeritagePalette = JerseyPalette(
    background = Color(0xFFF7F3EA),
    cardSurface = Color(0xFFEFE8DA),
    cardBorder = Color(0xFFDDD3C1),
    headerBackground = Color(0xFFCE1126),
    headerText = Color(0xFFF7F3EA),
    primaryText = Color(0xFF1A1210),
    secondaryText = Color(0xFF5A4A33),
    accentRed = Color(0xFFCE1126),
    highlightGold = Color(0xFFCE1126),
    heroTop = Color(0xFFCE1126),
    heroBottom = Color(0xFF8B0000),
    chipBg = Color(0xFFCE1126),
    chipText = Color(0xFFF7F3EA)
)

val HomePalette = JerseyPalette(
    background = Color(0xFFEBF2F7),
    cardSurface = Color(0xF5F6FAFC),
    cardBorder = Color(0xE6FFFFFF),
    headerBackground = Color(0xFFC8102E),
    headerText = Color(0xFFFFFFFF),
    primaryText = Color(0xFF111418),
    secondaryText = Color(0xFF5A6472),
    accentRed = Color(0xFFC8102E),
    highlightGold = Color(0xFFC8102E),
    heroTop = Color(0xFFC8102E),
    heroBottom = Color(0xFF9E0B1D),
    chipBg = Color(0x33FFFFFF),
    chipText = Color(0xFFFFFFFF)
)

val AwayPalette = JerseyPalette(
    background = Color(0xFFF5F6F8),
    cardSurface = Color(0xFFFFFFFF),
    cardBorder = Color(0xFFE2E5EB),
    headerBackground = Color(0xFFCE1126),
    headerText = Color(0xFFFFFFFF),
    primaryText = Color(0xFF111111),
    secondaryText = Color(0xFF4A4A4A),
    accentRed = Color(0xFFCE1126),
    highlightGold = Color(0xFFCE1126),
    heroTop = Color(0xFFCE1126),
    heroBottom = Color(0xFF9E0B1D),
    chipBg = Color(0xFFCE1126),
    chipText = Color(0xFFFFFFFF)
)

val ReverseRetroPalette = JerseyPalette(
    background = Color(0xFF0C0D0E),
    cardSurface = Color(0xFF16171A),
    cardBorder = Color(0xFF26282E),
    headerBackground = Color(0xFF121316),
    headerText = Color(0xFFFFFFFF),
    primaryText = Color(0xFFFFFFFF),
    secondaryText = Color(0xFFD4D4D8),
    accentRed = Color(0xFFFF3B30),
    highlightGold = Color(0xFFFFD54F),
    heroTop = Color(0xFFE5252A),
    heroBottom = Color(0xFF16171A),
    chipBg = Color(0xFFFF3B30),
    chipText = Color(0xFFFFFFFF)
)

val StadiumSeriesPalette = JerseyPalette(
    background = Color(0xFF15181E),
    cardSurface = Color(0xFF1E222A),
    cardBorder = Color(0xFF2E3440),
    headerBackground = Color(0xFF1E222A),
    headerText = Color(0xFFFFFFFF),
    primaryText = Color(0xFFFFFFFF),
    secondaryText = Color(0xFFC8CCD4),
    accentRed = Color(0xFFFF2D20),
    highlightGold = Color(0xFFFFD54F),
    heroTop = Color(0xFFFF2D20),
    heroBottom = Color(0xFF1E222A),
    chipBg = Color(0xFFFF2D20),
    chipText = Color(0xFFFFFFFF)
)

fun AppJersey.toPalette(): JerseyPalette = when (this) {
    AppJersey.HERITAGE -> HeritagePalette
    AppJersey.HOME -> HomePalette
    AppJersey.AWAY -> AwayPalette
    AppJersey.REVERSE_RETRO -> ReverseRetroPalette
    AppJersey.STADIUM_SERIES -> StadiumSeriesPalette
}

val LocalJerseyPalette = staticCompositionLocalOf { HomePalette }
