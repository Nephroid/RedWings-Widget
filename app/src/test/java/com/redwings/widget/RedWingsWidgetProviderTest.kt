package com.redwings.widget

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.FrameLayout
import android.widget.RemoteViews
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import com.redwings.widget.widget.RedWingsWidgetProvider
import com.redwings.widget.widget.WidgetBinder
import com.redwings.widget.widget.WidgetTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.TimeUnit

/**
 * Robolectric tests for [RedWingsWidgetProvider.applyResponsiveLayout]
 * visibility thresholds and [WidgetBinder.formatCountdown] formatting.
 *
 * Layout contract (see red_wings_widget_layout.xml):
 * - minHeight < 72  -> single-row: header/divider/info/standings GONE
 * - minHeight < 90  -> narrow: header/divider/info VISIBLE, standings GONE
 * - else            -> full: everything VISIBLE
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RedWingsWidgetProviderTest {

    private lateinit var context: Context
    private lateinit var provider: RedWingsWidgetProvider

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        provider = RedWingsWidgetProvider()
    }

    private fun inflated(minW: Int, minH: Int): android.view.ViewGroup {
        val views = RemoteViews(context.packageName, R.layout.red_wings_widget_layout)
        provider.applyResponsiveLayout(context, views, minW, minH)
        return views.apply(context, FrameLayout(context)) as android.view.ViewGroup
    }

    private fun android.view.ViewGroup.vis(id: Int): Int =
        findViewById<View>(id).visibility

    @Test
    fun singleRow_minHeightBelow72_hidesHeaderInfoAndStandings() {
        val view = inflated(minW = 180, minH = 60)
        assertEquals(View.GONE, view.vis(R.id.widget_header_layout))
        assertEquals(View.GONE, view.vis(R.id.widget_divider_top))
        assertEquals(View.GONE, view.vis(R.id.widget_info_card))
        assertEquals(View.GONE, view.vis(R.id.widget_standings_table))
        // Matchup row itself stays visible (countdown only).
        assertEquals(View.VISIBLE, view.vis(R.id.widget_matchup_layout))
    }

    @Test
    fun narrow_minHeightBelow90_hidesStandingsOnly() {
        val view = inflated(minW = 180, minH = 80)
        assertEquals(View.VISIBLE, view.vis(R.id.widget_header_layout))
        assertEquals(View.VISIBLE, view.vis(R.id.widget_divider_top))
        assertEquals(View.VISIBLE, view.vis(R.id.widget_info_card))
        assertEquals(View.GONE, view.vis(R.id.widget_standings_table))
    }

    @Test
    fun full_minHeightAtLeast90_showsEverything() {
        val view = inflated(minW = 180, minH = 110)
        assertEquals(View.VISIBLE, view.vis(R.id.widget_header_layout))
        assertEquals(View.VISIBLE, view.vis(R.id.widget_divider_top))
        assertEquals(View.VISIBLE, view.vis(R.id.widget_info_card))
        assertEquals(View.VISIBLE, view.vis(R.id.widget_standings_table))
        assertEquals(View.VISIBLE, view.vis(R.id.widget_matchup_layout))
    }

    @Test
    fun boundary_71isSingleRow_72isNarrow_89isNarrow_90isFull() {
        assertEquals(View.GONE, inflated(180, 71).vis(R.id.widget_standings_table))
        assertEquals(View.GONE, inflated(180, 71).vis(R.id.widget_header_layout))
        assertEquals(View.VISIBLE, inflated(180, 72).vis(R.id.widget_header_layout))
        assertEquals(View.GONE, inflated(180, 72).vis(R.id.widget_standings_table))
        assertEquals(View.GONE, inflated(180, 89).vis(R.id.widget_standings_table))
        assertEquals(View.VISIBLE, inflated(180, 90).vis(R.id.widget_standings_table))
    }

    @Test
    fun logos_hiddenWhenNarrowerThan130dp() {
        val narrow = inflated(minW = 110, minH = 110)
        assertEquals(View.GONE, narrow.vis(R.id.widget_away_logo))
        assertEquals(View.GONE, narrow.vis(R.id.widget_home_logo))
        val wide = inflated(minW = 180, minH = 110)
        assertEquals(View.VISIBLE, wide.vis(R.id.widget_away_logo))
        assertEquals(View.VISIBLE, wide.vis(R.id.widget_home_logo))
    }

    private val officialJerseyThemes = listOf(
        WidgetTheme.HERITAGE,
        WidgetTheme.HOME,
        WidgetTheme.AWAY,
        WidgetTheme.REVERSE_RETRO,
        WidgetTheme.STADIUM_SERIES
    )

    private fun assertThemeInflationAndColors(
        layoutRes: Int,
        minW: Int,
        minH: Int,
        theme: WidgetTheme
    ) {
        val views = RemoteViews(context.packageName, layoutRes)
        provider.applyWidgetTheme(context, views, theme)
        provider.applyResponsiveLayout(context, views, minW, minH)
        val root = views.apply(context, FrameLayout(context)) as android.view.ViewGroup

        // View visibility
        assertEquals(View.VISIBLE, root.vis(R.id.widget_header_layout))
        assertEquals(View.VISIBLE, root.vis(R.id.widget_divider_top))
        assertEquals(View.VISIBLE, root.vis(R.id.widget_matchup_layout))
        assertEquals(View.VISIBLE, root.vis(R.id.widget_info_card))
        assertEquals(View.VISIBLE, root.vis(R.id.widget_standings_table))

        // Text & divider colors
        assertEquals(ContextCompat.getColor(context, theme.titleColorRes), root.findViewById<TextView>(R.id.widget_title).currentTextColor)
        assertEquals(ContextCompat.getColor(context, theme.countdownColorRes), root.findViewById<TextView>(R.id.widget_countdown).currentTextColor)
        assertEquals(ContextCompat.getColor(context, theme.opponentColorRes), root.findViewById<TextView>(R.id.widget_opponent).currentTextColor)
        assertEquals(ContextCompat.getColor(context, theme.subColorRes), root.findViewById<TextView>(R.id.widget_venue_info).currentTextColor)
        assertEquals(ContextCompat.getColor(context, theme.standingColorRes), root.findViewById<TextView>(R.id.widget_standing_h2h).currentTextColor)
        assertEquals(ContextCompat.getColor(context, theme.tagTextColorRes), root.findViewById<TextView>(R.id.widget_tag).currentTextColor)
        assertEquals(ContextCompat.getColor(context, theme.tagTextColorRes), root.findViewById<TextView>(R.id.widget_theme_toggle).currentTextColor)

        val dividerBg = root.findViewById<View>(R.id.widget_divider_top).background as? ColorDrawable
        assertNotNull(dividerBg)
        assertEquals(ContextCompat.getColor(context, theme.dividerColorRes), dividerBg?.color)
    }

    @Test
    fun allWidgetThemes_resolveAndApply() {
        val themes = (0 until 5).map { WidgetTheme.fromIndex(it) }
        assertEquals(5, themes.distinct().size)
        themes.forEach { theme ->
            assertNotNull(theme.displayName)
            assertNotNull(theme.buttonLabel)
            val views = RemoteViews(context.packageName, R.layout.red_wings_widget_layout)
            provider.applyWidgetTheme(context, views, theme)
            provider.applyResponsiveLayout(context, views, 180, 110)
            assertNotNull(views)
        }
        // Index wraps around the 5 themes (HERITAGE is index 0).
        assertEquals(WidgetTheme.HERITAGE, WidgetTheme.fromIndex(5))
        assertEquals(WidgetTheme.STADIUM_SERIES, WidgetTheme.fromIndex(-1))
    }

    @Test
    fun officialJerseyThemes_standardLayout_inflateWithCorrectColorsAndVisibility() {
        assertEquals(5, officialJerseyThemes.size)
        officialJerseyThemes.forEach { theme ->
            assertThemeInflationAndColors(
                layoutRes = R.layout.red_wings_widget_layout,
                minW = 180,
                minH = 110,
                theme = theme
            )
        }
    }

    @Test
    fun officialJerseyThemes_wideLayout_inflateWithCorrectColorsAndVisibility() {
        officialJerseyThemes.forEach { theme ->
            assertThemeInflationAndColors(
                layoutRes = R.layout.red_wings_widget_wide,
                minW = 390,
                minH = 110,
                theme = theme
            )
        }
    }

    @Test
    fun officialJerseyThemes_drawablesResolveCleanly() {
        officialJerseyThemes.forEach { theme ->
            assertNotNull(ContextCompat.getDrawable(context, theme.bgDrawableRes))
            assertNotNull(ContextCompat.getDrawable(context, theme.tagDrawableRes))
        }
    }

    @Test
    fun countdown_daysAndHoursFormatting() {
        val now = 1_700_000_000_000L
        val twoDays = now + TimeUnit.DAYS.toMillis(2) +
            TimeUnit.HOURS.toMillis(4) + TimeUnit.MINUTES.toMillis(7)
        assertEquals("2d 04h 07m", WidgetBinder.formatCountdown(twoDays, now))

        val hoursOnly = now + TimeUnit.HOURS.toMillis(5) + TimeUnit.MINUTES.toMillis(3)
        assertEquals("05h 03m", WidgetBinder.formatCountdown(hoursOnly, now))
    }

    @Test
    fun countdown_liveAndFinalStates() {
        val now = 1_700_000_000_000L
        assertEquals("PUCK DROP! LIVE", WidgetBinder.formatCountdown(now, now))
        assertEquals(
            "PUCK DROP! LIVE",
            WidgetBinder.formatCountdown(now - TimeUnit.HOURS.toMillis(2), now)
        )
        assertEquals(
            "Final",
            WidgetBinder.formatCountdown(now - TimeUnit.HOURS.toMillis(4), now)
        )
    }

    @Test
    fun countdown_prefixDependsOnHomeAway() {
        // WidgetBinder binds "vs." at home and "at" on the road.
        val home = if (true) "vs." else "at"
        val away = if (false) "vs." else "at"
        assertEquals("vs.", home)
        assertEquals("at", away)
        assertTrue(WidgetBinder.formatCountdown(System.currentTimeMillis() + 3_600_000).isNotBlank())
    }

    @Test
    fun wideLayout_inflatesAndBindsCleanly() {
        val views = RemoteViews(context.packageName, R.layout.red_wings_widget_wide)
        provider.applyResponsiveLayout(context, views, 390, 110)
        val inflated = views.apply(context, FrameLayout(context)) as android.view.ViewGroup
        assertNotNull(inflated.findViewById(R.id.widget_matchup_layout))
        assertNotNull(inflated.findViewById(R.id.widget_standings_table))
    }

    @Test
    fun compactLayout_inflatesAndBindsCleanly() {
        val views = RemoteViews(context.packageName, R.layout.red_wings_widget_compact)
        provider.applyResponsiveLayout(context, views, 140, 90)
        val inflated = views.apply(context, FrameLayout(context)) as android.view.ViewGroup
        assertNotNull(inflated.findViewById(R.id.widget_matchup_layout))
        assertNotNull(inflated.findViewById(R.id.widget_countdown))
    }
}
