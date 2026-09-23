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
            wingsScore = 4,
            oppScore = 2,
            isWinner = true,
            isHome = false,
            dateLabel = "Sat, Oct 11"
        )

        composeTestRule.setContent {
            RedWingsTheme {
                Column(modifier = Modifier.padding(16.dp)) {
                    NextGameHero(countdown = countdown, game = nextGame)
                    Spacer(Modifier.height(12.dp))
                    LastResultCard(lastGame = lastGame)
                }
            }
        }

        composeTestRule.onRoot()
            .captureRoboImage(filePath = "src/test/screenshots/redwings_dashboard_preview.png")
    }
}
