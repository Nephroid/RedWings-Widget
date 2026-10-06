package com.redwings.widget

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.redwings.widget.ui.CountdownState
import com.redwings.widget.ui.LastGameUi
import com.redwings.widget.ui.LastResultCard
import com.redwings.widget.ui.NextGameHero
import com.redwings.widget.ui.NextGameUi
import com.redwings.widget.ui.theme.RedWingsTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.util.concurrent.TimeUnit

/**
 * Minimal Roborazzi dashboard screenshot, following the Tigers
 * GreetingScreenshotTest pattern (compose rule + captureRoboImage).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class GreetingScreenshotTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun redwings_dashboard_screenshot() {
        val nextGame = NextGameUi(
            opponent = "Toronto Maple Leafs",
            opponentAbbrev = "TOR",
            venue = "Little Caesars Arena",
            startTimeMillis = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(26),
            isHome = true
        )
        val countdown = CountdownState(
            days = 1, hours = 2, minutes = 15, seconds = 30,
            isLive = false, text = "1d 02h 15m 30s"
        )
        val lastGame = LastGameUi(
            opponent = "Montreal Canadiens",
            opponentAbbrev = "MTL",
            wingsScore = 4,
            oppScore = 2,
            isWinner = true,
            isHome = false,
            dateLabel = "Sat, Oct 11"
        )
        val standings = listOf(
            com.redwings.widget.ui.StandingsRowUi(1, "BOS", 82, 51, 20, 11, 113),
            com.redwings.widget.ui.StandingsRowUi(2, "TOR", 82, 46, 26, 10, 102),
            com.redwings.widget.ui.StandingsRowUi(3, "FLA", 82, 45, 27, 10, 100),
            com.redwings.widget.ui.StandingsRowUi(4, "DET", 82, 42, 30, 10, 94, isRedWings = true),
            com.redwings.widget.ui.StandingsRowUi(5, "TBL", 82, 40, 32, 10, 90),
            com.redwings.widget.ui.StandingsRowUi(6, "MTL", 82, 37, 36, 9, 83),
            com.redwings.widget.ui.StandingsRowUi(7, "OTT", 82, 34, 39, 9, 77),
            com.redwings.widget.ui.StandingsRowUi(8, "BUF", 82, 30, 43, 9, 69)
        )

        composeTestRule.setContent {
            RedWingsTheme {
                Column(modifier = Modifier.padding(16.dp)) {
                    NextGameHero(countdownProvider = { countdown }, game = nextGame)
                    Spacer(Modifier.height(12.dp))
                    LastResultCard(lastGame = lastGame)
                    Spacer(Modifier.height(12.dp))
                    com.redwings.widget.ui.StandingsCard(
                        standings = standings,
                        playoffChaseText = "CLINCHED PLAYOFF SPOT",
                        summary = "4th in Atlantic • 94 pts"
                    )
                }
            }
        }

        composeTestRule.onRoot()
            .captureRoboImage(filePath = "src/test/screenshots/redwings_dashboard_preview.png")
    }

    @Test
    fun redwings_single_pane_dashboard_screenshot() {
        val scheduleData = com.redwings.widget.ui.ScheduleUiState.Data(
            nextGame = NextGameUi("Toronto Maple Leafs", "TOR", "Little Caesars Arena", System.currentTimeMillis() + 86400000L * 2, true),
            lastGame = LastGameUi("Boston Bruins", "BOS", 4, 2, true, true, "Tue, Sep 30"),
            standingsSummary = "4th in Atlantic • 94 pts"
        )
        val countdown = CountdownState(days = 1, hours = 2, minutes = 15, seconds = 30, text = "1d 02h 15m 30s")

        composeTestRule.setContent {
            RedWingsTheme {
                com.redwings.widget.ui.dashboard.SinglePaneDashboard(
                    scheduleState = scheduleData,
                    countdownProvider = { countdown },
                    isRefreshing = false,
                    errorMessage = null,
                    onRefresh = {}
                )
            }
        }

        composeTestRule.onRoot()
            .captureRoboImage(filePath = "src/test/screenshots/redwings_single_pane_preview.png")
    }

    @Test
    @Config(qualifiers = RobolectricDeviceQualifiers.Pixel9ProFold)
    fun redwings_dual_pane_dashboard_screenshot() {
        val scheduleData = com.redwings.widget.ui.ScheduleUiState.Data(
            nextGame = NextGameUi("Toronto Maple Leafs", "TOR", "Little Caesars Arena", System.currentTimeMillis() + 86400000L * 2, true),
            lastGame = LastGameUi("Boston Bruins", "BOS", 4, 2, true, true, "Tue, Sep 30"),
            standingsSummary = "4th in Atlantic • 94 pts"
        )
        val countdown = CountdownState(days = 1, hours = 2, minutes = 15, seconds = 30, text = "1d 02h 15m 30s")

        composeTestRule.setContent {
            RedWingsTheme {
                com.redwings.widget.ui.dashboard.DualPaneDashboard(
                    scheduleState = scheduleData,
                    countdownProvider = { countdown },
                    isRefreshing = false,
                    errorMessage = null,
                    onRefresh = {}
                )
            }
        }

        composeTestRule.onRoot()
            .captureRoboImage(filePath = "src/test/screenshots/redwings_dual_pane_preview.png")
    }

    @Test
    @Config(qualifiers = RobolectricDeviceQualifiers.Pixel9ProFold)
    fun redwings_tabletop_dashboard_screenshot() {
        val scheduleData = com.redwings.widget.ui.ScheduleUiState.Data(
            nextGame = NextGameUi("Toronto Maple Leafs", "TOR", "Little Caesars Arena", System.currentTimeMillis() + 86400000L * 2, true),
            lastGame = LastGameUi("Boston Bruins", "BOS", 4, 2, true, true, "Tue, Sep 30"),
            standingsSummary = "4th in Atlantic • 94 pts"
        )
        val countdown = CountdownState(days = 1, hours = 2, minutes = 15, seconds = 30, text = "1d 02h 15m 30s")

        composeTestRule.setContent {
            RedWingsTheme {
                com.redwings.widget.ui.dashboard.TabletopDashboard(
                    scheduleState = scheduleData,
                    countdownProvider = { countdown },
                    isRefreshing = false,
                    onRefresh = {}
                )
            }
        }

        composeTestRule.onRoot()
            .captureRoboImage(filePath = "src/test/screenshots/redwings_tabletop_preview.png")
    }
}
