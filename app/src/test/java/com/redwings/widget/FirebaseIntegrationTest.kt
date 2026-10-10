package com.redwings.widget

import android.content.Context
import android.widget.RemoteViews
import androidx.test.core.app.ApplicationProvider
import com.redwings.widget.data.firebase.FirebaseAiContentEngine
import com.redwings.widget.data.firebase.RemoteConfigValues
import com.redwings.widget.data.model.GameStarUi
import com.redwings.widget.data.model.TeamLeaderPlayerUi
import com.redwings.widget.data.model.TeamLeadersUi
import com.redwings.widget.widget.WidgetCrashGuard
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FirebaseIntegrationTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testRemoteConfigValuesDefaults() {
        val config = RemoteConfigValues()
        assertEquals(14400000L, config.widgetOffDayPollMs) // 4 hours
        assertEquals(1800000L, config.widgetGameDayPrePollMs) // 30 mins
        assertEquals(60000L, config.widgetLivePollMs) // 60 secs
        assertTrue(config.isSeasonLeadersEnabled)
        assertTrue(config.isPlayoffChaseEnabled)
        assertEquals(false, config.emergencyWidgetCircuitBreaker)
        assertEquals(120, config.maxLogoDimPx)
    }

    @Test
    fun testWidgetCrashGuardValidation() {
        val views = RemoteViews(context.packageName, R.layout.red_wings_widget_layout)
        val isSafe = WidgetCrashGuard.validateRemoteViews(views, 101)
        assertTrue("Standard widget layout must be within safe Binder parcel ceiling", isSafe)

        val emergencyViews = WidgetCrashGuard.buildEmergencyViews(context)
        assertNotNull(emergencyViews)
        assertEquals(R.layout.red_wings_widget_layout, emergencyViews.layoutId)
    }

    @Test
    fun testFirebaseAiHeuristicsPreGame() {
        val engine = FirebaseAiContentEngine(context)
        engine.refreshPreGameIntelligence(
            opponentName = "Toronto Maple Leafs",
            opponentAbbrev = "TOR",
            isHome = true,
            detRecord = "42-30-10",
            oppRecord = "46-26-10"
        )

        val state = engine.intelligenceState.value
        assertTrue("Should have generated storyline", state.matchupStoryline.isNotBlank())
        assertTrue("Storyline should mention Atlantic or Detroit", state.matchupStoryline.contains("Detroit") || state.matchupStoryline.contains("Atlantic"))
        assertTrue("Key battle should target TOR star", state.keyBattle.contains("Matthews") || state.keyBattle.contains("Larkin"))
    }

    @Test
    fun testFirebaseAiHeuristicsPostGame() {
        val engine = FirebaseAiContentEngine(context)
        val stars = listOf(
            GameStarUi(star = 1, name = "Dylan Larkin", teamAbbrev = "DET", position = "C", statLine = "2G, 1A"),
            GameStarUi(star = 2, name = "Lucas Raymond", teamAbbrev = "DET", position = "RW", statLine = "0G, 3A"),
            GameStarUi(star = 3, name = "Cam Talbot", teamAbbrev = "DET", position = "G", statLine = "34 SV, .944%")
        )

        engine.refreshPostGameIntelligence(
            wingsScore = 5,
            oppScore = 2,
            oppAbbrev = "BOS",
            stars = stars
        )

        val state = engine.intelligenceState.value
        assertEquals("Offensive Explosion", state.dynamicRecapPill)
        assertEquals(3, state.starCommentaries.size)
        assertTrue("Goalie commentary should mention crease/saves", state.starCommentaries[3]?.contains("crease") == true || state.starCommentaries[3]?.contains("chances") == true)
        assertTrue("Goal scorer commentary should mention scoring/offensive", state.starCommentaries[1]?.contains("scoring") == true || state.starCommentaries[1]?.contains("chances") == true)
    }

    @Test
    fun test82GamePacingCalculation() {
        val engine = FirebaseAiContentEngine(context)
        val leaders = TeamLeadersUi(
            topGoals = listOf(
                TeamLeaderPlayerUi(
                    rank = 1,
                    playerId = 8477934,
                    name = "Dylan Larkin",
                    position = "C",
                    primaryStat = "33 G",
                    secondaryStat = "68 GP • 69 PTS"
                )
            ),
            topPoints = listOf(
                TeamLeaderPlayerUi(
                    rank = 1,
                    playerId = 8482078,
                    name = "Lucas Raymond",
                    position = "RW",
                    primaryStat = "72 PTS",
                    secondaryStat = "31G, 41A • 68 GP"
                )
            )
        )

        val pacing = engine.calculatePacingProjections(leaders)
        assertNotNull(pacing[8477934])
        assertNotNull(pacing[8482078])

        val raymondPace = pacing[8482078].orEmpty()
        assertTrue("Raymond pacing should project ~86 points", raymondPace.contains("PTS"))
        assertTrue("Should include 82-Game Pace marker", raymondPace.contains("82-Game Pace"))
    }
}
