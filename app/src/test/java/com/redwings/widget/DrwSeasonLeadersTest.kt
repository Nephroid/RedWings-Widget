package com.redwings.widget

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.redwings.widget.data.api.CommonName
import com.redwings.widget.data.api.NhlApiClient
import com.redwings.widget.data.api.NhlClubSkater
import com.redwings.widget.data.api.NhlClubStatsResponse
import com.redwings.widget.data.model.TeamLeaderPlayerUi
import com.redwings.widget.data.model.TeamLeadersUi
import com.redwings.widget.data.model.defaultTopAssists
import com.redwings.widget.data.model.defaultTopGoals
import com.redwings.widget.data.model.defaultTopPoints
import com.redwings.widget.ui.dashboard.DrwSeasonLeadersCard
import com.redwings.widget.ui.theme.RedWingsTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class DrwSeasonLeadersTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun moshi_deserializesClubStatsResponse() {
        val json = """
            {
              "season": 20252026,
              "gameType": 2,
              "skaters": [
                {
                  "playerId": 8482078,
                  "headshot": "https://assets.nhle.com/mugs/nhl/20252026/DET/8482078.png",
                  "firstName": { "default": "Lucas" },
                  "lastName": { "default": "Raymond" },
                  "positionCode": "R",
                  "gamesPlayed": 82,
                  "goals": 31,
                  "assists": 41,
                  "points": 72,
                  "plusMinus": -12,
                  "penaltyMinutes": 30,
                  "shots": 160,
                  "shootingPctg": 0.19375
                },
                {
                  "playerId": 8477940,
                  "headshot": "https://assets.nhle.com/mugs/nhl/20252026/DET/8477940.png",
                  "firstName": { "default": "Dylan" },
                  "lastName": { "default": "Larkin" },
                  "positionCode": "C",
                  "gamesPlayed": 68,
                  "goals": 33,
                  "assists": 36,
                  "points": 69,
                  "plusMinus": 10,
                  "penaltyMinutes": 39,
                  "shots": 194,
                  "shootingPctg": 0.1701
                }
              ]
            }
        """.trimIndent()

        val adapter = NhlApiClient.moshi.adapter(NhlClubStatsResponse::class.java)
        val response = adapter.fromJson(json)

        assertNotNull(response)
        assertEquals(20252026, response?.season)
        assertEquals(2, response?.skaters?.size)

        val raymond = response!!.skaters!![0]
        assertEquals(8482078, raymond.playerId)
        assertEquals("Lucas", raymond.firstName?.default)
        assertEquals("Raymond", raymond.lastName?.default)
        assertEquals("R", raymond.positionCode)
        assertEquals(82, raymond.gamesPlayed)
        assertEquals(31, raymond.goals)
        assertEquals(41, raymond.assists)
        assertEquals(72, raymond.points)
    }

    @Test
    fun moshi_serializesAndDeserializesTeamLeadersUi() {
        val leaders = TeamLeadersUi(
            topPoints = defaultTopPoints(),
            topGoals = defaultTopGoals(),
            topAssists = defaultTopAssists()
        )

        val adapter = NhlApiClient.moshi.adapter(TeamLeadersUi::class.java)
        val json = adapter.toJson(leaders)
        assertNotNull(json)
        assertTrue(json.contains("Lucas Raymond"))
        assertTrue(json.contains("Dylan Larkin"))

        val parsed = adapter.fromJson(json)
        assertNotNull(parsed)
        assertEquals(5, parsed?.topPoints?.size)
        assertEquals(5, parsed?.topGoals?.size)
        assertEquals(5, parsed?.topAssists?.size)
        assertEquals("Lucas Raymond", parsed?.topPoints?.first()?.name)
        assertEquals("72 PTS", parsed?.topPoints?.first()?.primaryStat)
    }

    @Test
    fun sortingLogic_correctlyOrdersTopCategories() {
        val skaters = listOf(
            NhlClubSkater(
                playerId = 1,
                firstName = CommonName("Skater"),
                lastName = CommonName("One"),
                positionCode = "C",
                gamesPlayed = 80,
                goals = 25,
                assists = 50,
                points = 75,
                shots = 150
            ),
            NhlClubSkater(
                playerId = 2,
                firstName = CommonName("Skater"),
                lastName = CommonName("Two"),
                positionCode = "RW",
                gamesPlayed = 75,
                goals = 40,
                assists = 30,
                points = 70,
                shots = 220
            ),
            NhlClubSkater(
                playerId = 3,
                firstName = CommonName("Skater"),
                lastName = CommonName("Three"),
                positionCode = "D",
                gamesPlayed = 82,
                goals = 10,
                assists = 55,
                points = 65,
                shots = 120
            )
        )

        // Points sort: points DESC
        val topPoints = skaters.sortedWith(
            compareByDescending<NhlClubSkater> { it.points }
                .thenByDescending { it.goals }
                .thenByDescending { it.assists }
        )
        assertEquals(1, topPoints[0].playerId) // 75 pts
        assertEquals(2, topPoints[1].playerId) // 70 pts
        assertEquals(3, topPoints[2].playerId) // 65 pts

        // Goals sort: goals DESC
        val topGoals = skaters.sortedWith(
            compareByDescending<NhlClubSkater> { it.goals }
                .thenByDescending { it.points }
                .thenByDescending { it.shots }
        )
        assertEquals(2, topGoals[0].playerId) // 40 goals
        assertEquals(1, topGoals[1].playerId) // 25 goals
        assertEquals(3, topGoals[2].playerId) // 10 goals

        // Assists sort: assists DESC
        val topAssists = skaters.sortedWith(
            compareByDescending<NhlClubSkater> { it.assists }
                .thenByDescending { it.points }
                .thenBy { it.gamesPlayed }
        )
        assertEquals(3, topAssists[0].playerId) // 55 assists
        assertEquals(1, topAssists[1].playerId) // 50 assists
        assertEquals(2, topAssists[2].playerId) // 30 assists
    }

    @Test
    fun compose_rendersDrwSeasonLeadersCardAndTogglesCategories() {
        val leaders = TeamLeadersUi(
            topPoints = listOf(
                TeamLeaderPlayerUi(1, 8482078, "Lucas Raymond", "RW", 23, "", "72 PTS", "31G, 41A • 82 GP"),
                TeamLeaderPlayerUi(2, 8477940, "Dylan Larkin", "C", 71, "", "69 PTS", "33G, 36A • 68 GP")
            ),
            topGoals = listOf(
                TeamLeaderPlayerUi(1, 8477940, "Dylan Larkin", "C", 71, "", "33 G", "68 GP • 69 PTS"),
                TeamLeaderPlayerUi(2, 8482078, "Lucas Raymond", "RW", 23, "", "31 G", "82 GP • 72 PTS")
            ),
            topAssists = listOf(
                TeamLeaderPlayerUi(1, 8482078, "Lucas Raymond", "RW", 23, "", "41 A", "82 GP • 72 PTS"),
                TeamLeaderPlayerUi(2, 8479337, "Alex DeBrincat", "RW", 93, "", "40 A", "82 GP • 67 PTS")
            )
        )

        composeTestRule.setContent {
            RedWingsTheme {
                DrwSeasonLeadersCard(leaders = leaders)
            }
        }

        // Header and container displayed
        composeTestRule.onNodeWithTag("drw_season_leaders_card").assertIsDisplayed()
        composeTestRule.onNodeWithText("DRW SEASON LEADERS").assertIsDisplayed()
        composeTestRule.onNodeWithText("TOP 5").assertIsDisplayed()

        // Default tab is POINTS: Lucas Raymond 72 PTS displayed
        composeTestRule.onNodeWithText("72 PTS").assertIsDisplayed()
        composeTestRule.onNodeWithText("31G, 41A • 82 GP").assertIsDisplayed()

        // Toggle to GOALS tab
        composeTestRule.onNodeWithTag("drw_leaders_tab_goals").performClick()
        composeTestRule.onNodeWithText("33 G").assertIsDisplayed()
        composeTestRule.onNodeWithText("68 GP • 69 PTS").assertIsDisplayed()

        // Toggle to ASSISTS tab
        composeTestRule.onNodeWithTag("drw_leaders_tab_assists").performClick()
        composeTestRule.onNodeWithText("41 A").assertIsDisplayed()
        composeTestRule.onNodeWithText("Alex DeBrincat").assertIsDisplayed()
    }
}
