package com.redwings.widget

import com.redwings.widget.data.model.NHL_TEAMS
import com.redwings.widget.data.model.formatOpponentPrefix
import com.redwings.widget.data.model.getTeamLogoUrl
import com.redwings.widget.data.model.getTeamLogoUrlFallback
import com.redwings.widget.data.model.teamDisplayName
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure-JVM tests for TeamUtils: logo URL mapping, full 32-team
 * coverage, and home/away opponent prefix.
 */
class TeamUtilsTest {

    @Test
    fun logoUrl_containsUppercaseAbbrev() {
        assertTrue(getTeamLogoUrl("DET").contains("det.png"))
        assertTrue(getTeamLogoUrl("tor").contains("tor.png"))
        assertTrue(getTeamLogoUrlFallback("bos").contains("BOS_light.svg"))
    }

    @Test
    fun all32Abbrevs_mapToFullNamesAndLogos() {
        val expected = setOf(
            "ANA", "BOS", "BUF", "CAR", "CBJ", "CGY", "CHI", "COL",
            "DAL", "DET", "EDM", "FLA", "LAK", "MIN", "MTL", "NSH",
            "NJD", "NYI", "NYR", "OTT", "PHI", "PIT", "SEA", "SJS",
            "STL", "TBL", "TOR", "UTA", "VAN", "VGK", "WPG", "WSH"
        )
        assertEquals("NHL has 32 teams", expected, NHL_TEAMS.keys)
        NHL_TEAMS.forEach { (abbrev, fullName) ->
            assertEquals(fullName, teamDisplayName(abbrev))
            val expectedSlug = com.redwings.widget.data.model.ESPN_SLUG_MAP[abbrev] ?: abbrev.lowercase()
            assertTrue(
                "$abbrev logo should embed expected slug $expectedSlug.png, got: ${getTeamLogoUrl(abbrev)}",
                getTeamLogoUrl(abbrev).contains("$expectedSlug.png")
            )
        }
    }

    @Test
    fun espnSlugMappings_specialCasesResolveCorrectly() {
        assertEquals("https://a.espncdn.com/i/teamlogos/nhl/500/la.png", getTeamLogoUrl("LAK"))
        assertEquals("https://a.espncdn.com/i/teamlogos/nhl/500/sj.png", getTeamLogoUrl("SJS"))
        assertEquals("https://a.espncdn.com/i/teamlogos/nhl/500/tb.png", getTeamLogoUrl("TBL"))
        assertEquals("https://a.espncdn.com/i/teamlogos/nhl/500/ott.png", getTeamLogoUrl("OTT"))
    }

    @Test
    fun teamDisplayName_spotChecks() {
        assertEquals("Detroit Red Wings", teamDisplayName("DET"))
        assertEquals("Toronto Maple Leafs", teamDisplayName("TOR"))
        assertEquals("Utah Mammoth", teamDisplayName("UTA"))
        assertEquals("Opponent", teamDisplayName(null))
        assertEquals("Opponent", teamDisplayName(""))
    }

    @Test
    fun unknownAbbrev_fallsBackToDetroit() {
        assertTrue(getTeamLogoUrl("ZZZ").contains("det.png"))
        assertTrue(getTeamLogoUrl(null).contains("det.png"))
    }

    @Test
    fun opponentPrefix_homeVsAway() {
        assertEquals("vs", formatOpponentPrefix(true))
        assertEquals("at", formatOpponentPrefix(false))
    }
}
