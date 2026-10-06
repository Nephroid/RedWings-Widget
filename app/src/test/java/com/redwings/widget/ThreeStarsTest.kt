package com.redwings.widget

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.redwings.widget.data.api.CommonName
import com.redwings.widget.data.api.NhlApiClient
import com.redwings.widget.data.api.NhlGameLandingResponse
import com.redwings.widget.data.api.NhlThreeStar
import com.redwings.widget.data.model.GameStarUi
import com.redwings.widget.data.model.defaultThreeStars
import com.redwings.widget.data.model.toUi
import com.redwings.widget.ui.LastGameUi
import com.redwings.widget.ui.dashboard.LastResultCard
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
class ThreeStarsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun moshi_deserializesGameLandingThreeStars() {
        val json = """
            {
              "id": 2026020035,
              "gameState": "FINAL",
              "summary": {
                "threeStars": [
                  {
                    "star": 1,
                    "playerId": 8478398,
                    "teamAbbrev": "WPG",
                    "headshot": "https://assets.nhle.com/mugs/nhl/20262027/WPG/8478398.png",
                    "name": { "default": "K. Connor" },
                    "sweaterNo": 81,
                    "position": "L",
                    "goals": 2,
                    "assists": 1,
                    "points": 3
                  },
                  {
                    "star": 2,
                    "playerId": 8482149,
                    "teamAbbrev": "WPG",
                    "headshot": "https://assets.nhle.com/mugs/nhl/20262027/WPG/8482149.png",
                    "name": { "default": "C. Perfetti" },
                    "sweaterNo": 91,
                    "position": "C",
                    "goals": 1,
                    "assists": 2,
                    "points": 3
                  },
                  {
                    "star": 3,
                    "playerId": 8478042,
                    "teamAbbrev": "DET",
                    "headshot": "https://assets.nhle.com/mugs/nhl/20262027/DET/8478042.png",
                    "name": { "default": "V. Arvidsson" },
                    "sweaterNo": 33,
                    "position": "L",
                    "goals": 1,
                    "assists": 0,
                    "points": 1
                  }
                ]
              }
            }
        """.trimIndent()

        val adapter = NhlApiClient.moshi.adapter(NhlGameLandingResponse::class.java)
        val response = adapter.fromJson(json)

        assertNotNull(response)
        assertEquals(2026020035, response?.id)
        val stars = response?.summary?.threeStars
        assertNotNull(stars)
        assertEquals(3, stars?.size)

        val star1 = stars!![0]
        assertEquals(1, star1.star)
        assertEquals(8478398, star1.playerId)
        assertEquals("WPG", star1.teamAbbrev)
        assertEquals("K. Connor", star1.name?.default)
        assertEquals(2, star1.goals)
        assertEquals(1, star1.assists)
        assertEquals(3, star1.points)
    }

    @Test
    fun toUi_formatsSkaterAndGoalieStatsCorrectly() {
        val skaterStar = NhlThreeStar(
            star = 1,
            playerId = 8477940,
            teamAbbrev = "DET",
            headshot = null, // test fallback headshot
            name = CommonName(default = "D. Larkin"),
            sweaterNo = 71,
            position = "C",
            goals = 2,
            assists = 1,
            points = 3
        )
        val skaterUi = skaterStar.toUi()
        assertEquals(1, skaterUi.star)
        assertEquals("D. Larkin", skaterUi.name)
        assertEquals("https://assets.nhle.com/mugs/nhl/latest/8477940.png", skaterUi.headshotUrl)
        assertEquals("2G • 1A • 3 PTS", skaterUi.statLine)
        assertEquals("DET", skaterUi.teamAbbrev)
        assertEquals(71, skaterUi.sweaterNo)
        assertEquals("C", skaterUi.position)

        val goalieStar = NhlThreeStar(
            star = 2,
            playerId = 8479406,
            teamAbbrev = "MIN",
            headshot = "https://assets.nhle.com/mugs/nhl/latest/8479406.png",
            name = CommonName(default = "F. Gustavsson"),
            sweaterNo = 32,
            position = "G",
            goalsAgainstAverage = 0.00,
            savePctg = 1.000
        )
        val goalieUi = goalieStar.toUi()
        assertEquals(2, goalieUi.star)
        assertEquals("F. Gustavsson", goalieUi.name)
        assertEquals("1.000 SV% • 0.00 GAA", goalieUi.statLine)
    }

    @Test
    fun defaultThreeStars_containsThreeValidRedWingsStars() {
        val stars = defaultThreeStars()
        assertEquals(3, stars.size)
        assertEquals(1, stars[0].star)
        assertEquals(2, stars[1].star)
        assertEquals(3, stars[2].star)
        stars.forEach { star ->
            assertEquals("DET", star.teamAbbrev)
            assertTrue(star.name.isNotBlank())
            assertTrue(star.headshotUrl.startsWith("https://assets.nhle.com/mugs/nhl/latest/"))
            assertTrue(star.headshotUrl.endsWith(".png"))
            assertTrue(star.statLine.isNotBlank())
        }
    }

    @Test
    fun lastResultCard_rendersThreeStarsOfGameWhenPresent() {
        val lastGame = LastGameUi(
            opponent = "Boston Bruins",
            opponentAbbrev = "BOS",
            wingsScore = 4,
            oppScore = 2,
            isWinner = true,
            isHome = true,
            dateLabel = "Tue, Sep 30",
            threeStars = defaultThreeStars()
        )

        composeTestRule.setContent {
            RedWingsTheme {
                LastResultCard(lastGame = lastGame)
            }
        }

        // Header section for 3 stars
        composeTestRule.onNodeWithText("3 PLAYERS OF THE GAME").assertIsDisplayed()

        // 1st Star
        composeTestRule.onNodeWithTag("star_player_card_1").assertIsDisplayed()
        composeTestRule.onNodeWithText("D. Larkin").assertIsDisplayed()
        composeTestRule.onNodeWithText("1ST").assertIsDisplayed()

        // 2nd Star
        composeTestRule.onNodeWithTag("star_player_card_2").assertIsDisplayed()
        composeTestRule.onNodeWithText("L. Raymond").assertIsDisplayed()
        composeTestRule.onNodeWithText("2ND").assertIsDisplayed()

        // 3rd Star
        composeTestRule.onNodeWithTag("star_player_card_3").assertIsDisplayed()
        composeTestRule.onNodeWithText("M. Seider").assertIsDisplayed()
        composeTestRule.onNodeWithText("3RD").assertIsDisplayed()
    }

    @Test
    fun lastResultCard_doesNotRenderThreeStarsSectionWhenEmpty() {
        val lastGame = LastGameUi(
            opponent = "Boston Bruins",
            opponentAbbrev = "BOS",
            wingsScore = 4,
            oppScore = 2,
            isWinner = true,
            isHome = true,
            dateLabel = "Tue, Sep 30",
            threeStars = emptyList()
        )

        composeTestRule.setContent {
            RedWingsTheme {
                LastResultCard(lastGame = lastGame)
            }
        }

        composeTestRule.onNodeWithText("3 PLAYERS OF THE GAME").assertDoesNotExist()
        composeTestRule.onNodeWithTag("star_player_card_1").assertDoesNotExist()
        composeTestRule.onNodeWithTag("three_stars_section").assertDoesNotExist()
        // Core score should still be displayed
        composeTestRule.onNodeWithText("DET").assertIsDisplayed()
        composeTestRule.onNodeWithText("BOS").assertIsDisplayed()
    }

    @Test
    fun lastResultCard_honorsExplicitEmptyThreeStarsParamEvenIfLastGameHasStars() {
        val lastGame = LastGameUi(
            opponent = "Boston Bruins",
            opponentAbbrev = "BOS",
            wingsScore = 4,
            oppScore = 2,
            isWinner = true,
            isHome = true,
            dateLabel = "Tue, Sep 30",
            threeStars = defaultThreeStars()
        )

        composeTestRule.setContent {
            RedWingsTheme {
                LastResultCard(lastGame = lastGame, threeStars = emptyList())
            }
        }

        composeTestRule.onNodeWithTag("three_stars_section").assertDoesNotExist()
        composeTestRule.onNodeWithTag("star_player_card_1").assertDoesNotExist()
    }

    @Test
    fun toUi_handlesRealisticGoalieDecimalsAndZeroPlayerId() {
        // Goalie with .951 SV% and 2.0 GAA
        val goalieStar = NhlThreeStar(
            star = 2,
            playerId = 8475852,
            teamAbbrev = "CHI",
            headshot = null,
            name = CommonName(default = "P. Mrazek"),
            sweaterNo = 34,
            position = "G",
            goalsAgainstAverage = 2.0,
            savePctg = 0.951
        )
        val goalieUi = goalieStar.toUi()
        assertEquals(".951 SV% • 2.00 GAA", goalieUi.statLine)
        assertEquals("https://assets.nhle.com/mugs/nhl/latest/8475852.png", goalieUi.headshotUrl)

        // Goalie with only SV% (GAA null)
        val svOnlyStar = NhlThreeStar(
            star = 3,
            playerId = 8479406,
            teamAbbrev = "MIN",
            name = CommonName(default = "F. Gustavsson"),
            position = "G",
            savePctg = 0.933
        )
        val svOnlyUi = svOnlyStar.toUi()
        assertEquals(".933 SV%", svOnlyUi.statLine)

        // Invalid playerId with null headshot produces empty headshotUrl to avoid network error
        val invalidPlayerStar = NhlThreeStar(
            star = 1,
            playerId = 0,
            headshot = null,
            name = CommonName(default = "Unknown")
        )
        val invalidUi = invalidPlayerStar.toUi()
        assertEquals("", invalidUi.headshotUrl)
    }

    @Test
    fun moshi_roundtripsGameStarUiList() {
        val adapter = NhlApiClient.moshi.adapter<List<GameStarUi>>(
            com.squareup.moshi.Types.newParameterizedType(List::class.java, GameStarUi::class.java)
        )
        val original = defaultThreeStars()
        val json = adapter.toJson(original)
        assertNotNull(json)
        val restored = adapter.fromJson(json)
        assertEquals(3, restored?.size)
        assertEquals(original[0].name, restored?.get(0)?.name)
        assertEquals(original[1].headshotUrl, restored?.get(1)?.headshotUrl)
        assertEquals(original[2].statLine, restored?.get(2)?.statLine)
    }

    @Test
    fun repository_lastSavedGame_recoversFromCorruptOrEmptyThreeStarsJson() {
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
        val db = com.redwings.widget.data.local.AppDatabase.getDatabase(context)
        val repo = com.redwings.widget.data.repository.HockeyRepository(
            gameDao = db.gameDao(),
            appContext = context
        )
        val prefs = context.getSharedPreferences("RedWingsPrefs", android.content.Context.MODE_PRIVATE)

        // 1. Empty JSON array should fallback to defaultThreeStars
        prefs.edit().putString("last_game_three_stars", "[]").apply()
        val gameWithEmptyJson = repo.lastSavedGame()
        assertNotNull(gameWithEmptyJson)
        assertEquals(3, gameWithEmptyJson?.threeStars?.size)

        // 2. Corrupt JSON should fallback to defaultThreeStars
        prefs.edit().putString("last_game_three_stars", "{invalid-json").apply()
        val gameWithCorruptJson = repo.lastSavedGame()
        assertNotNull(gameWithCorruptJson)
        assertEquals(3, gameWithCorruptJson?.threeStars?.size)

        // 3. Valid JSON should be preserved
        val customStars = listOf(
            GameStarUi(star = 1, playerId = 9999, name = "Test Star", teamAbbrev = "DET", statLine = "1G • 1 PT")
        )
        val adapter = NhlApiClient.moshi.adapter<List<GameStarUi>>(
            com.squareup.moshi.Types.newParameterizedType(List::class.java, GameStarUi::class.java)
        )
        prefs.edit().putString("last_game_three_stars", adapter.toJson(customStars)).apply()
        val gameWithCustomStars = repo.lastSavedGame()
        assertNotNull(gameWithCustomStars)
        assertEquals(1, gameWithCustomStars?.threeStars?.size)
        assertEquals("Test Star", gameWithCustomStars?.threeStars?.first()?.name)
    }
}
