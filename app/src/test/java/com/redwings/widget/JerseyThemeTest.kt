package com.redwings.widget

import android.content.Context
import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.redwings.widget.ui.dashboard.AppHeaderBanner
import com.redwings.widget.ui.theme.AppJersey
import com.redwings.widget.ui.theme.AwayPalette
import com.redwings.widget.ui.theme.HeritagePalette
import com.redwings.widget.ui.theme.HomePalette
import com.redwings.widget.ui.theme.LocalJerseyPalette
import com.redwings.widget.ui.theme.RedWingsTheme
import com.redwings.widget.ui.theme.ReverseRetroPalette
import com.redwings.widget.ui.theme.StadiumSeriesPalette
import com.redwings.widget.ui.theme.toPalette
import com.redwings.widget.widget.WidgetBinder
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class JerseyThemeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testAppJerseyIndexCycling() {
        assertEquals(AppJersey.HERITAGE, AppJersey.fromIndex(0))
        assertEquals(AppJersey.HOME, AppJersey.fromIndex(1))
        assertEquals(AppJersey.AWAY, AppJersey.fromIndex(2))
        assertEquals(AppJersey.REVERSE_RETRO, AppJersey.fromIndex(3))
        assertEquals(AppJersey.STADIUM_SERIES, AppJersey.fromIndex(4))
        // Wraparound
        assertEquals(AppJersey.HERITAGE, AppJersey.fromIndex(5))
        assertEquals(AppJersey.HOME, AppJersey.fromIndex(6))
    }

    @Test
    fun testJerseyPalettes() {
        assertEquals(HeritagePalette, AppJersey.HERITAGE.toPalette())
        assertEquals(HomePalette, AppJersey.HOME.toPalette())
        assertEquals(AwayPalette, AppJersey.AWAY.toPalette())
        assertEquals(ReverseRetroPalette, AppJersey.REVERSE_RETRO.toPalette())
        assertEquals(StadiumSeriesPalette, AppJersey.STADIUM_SERIES.toPalette())
    }

    @Test
    fun testRedWingsThemeProvidesLocalJerseyPalette() {
        var observedBackground = androidx.compose.ui.graphics.Color.Unspecified

        composeTestRule.setContent {
            RedWingsTheme(jersey = AppJersey.REVERSE_RETRO) {
                val palette = LocalJerseyPalette.current
                observedBackground = palette.background
                Text(text = "Retro Test")
            }
        }

        assertEquals(ReverseRetroPalette.background, observedBackground)
    }

    @Test
    fun testAppHeaderBannerDisplaysChipAndTriggersToggle() {
        var toggleCount = 0

        composeTestRule.setContent {
            RedWingsTheme(jersey = AppJersey.HOME) {
                AppHeaderBanner(
                    isRefreshing = false,
                    onRefresh = {},
                    activeJersey = AppJersey.HOME,
                    onJerseyThemeToggle = { toggleCount++ }
                )
            }
        }

        composeTestRule.onNodeWithText("🔴 HOME").assertExists()
        composeTestRule.onNodeWithTag("jersey_theme_chip").performClick()
        assertEquals(1, toggleCount)
    }

    @Test
    fun testSharedPreferencesSynchronization() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appPrefs = context.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
        val widgetPrefs = context.getSharedPreferences(WidgetBinder.PREFS, Context.MODE_PRIVATE)

        val targetTheme = AppJersey.AWAY
        appPrefs.edit().putInt(MainActivity.KEY_THEME, targetTheme.id).apply()
        widgetPrefs.edit().putInt(MainActivity.KEY_THEME, targetTheme.id).apply()

        assertEquals(targetTheme.id, appPrefs.getInt(MainActivity.KEY_THEME, -1))
        assertEquals(targetTheme.id, widgetPrefs.getInt(MainActivity.KEY_THEME, -1))
    }
}
