package com.redwings.widget

import com.redwings.widget.data.api.NhlStandingRow
import com.redwings.widget.data.api.TeamAbbrev
import com.redwings.widget.data.repository.StandingsCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure-JVM tests for [StandingsCalculator]: Atlantic sort order,
 * Wings summary points-back math, and playoff-IN detection.
 */
class StandingsCalculatorTest {

    private fun row(
        abbrev: String,
        pts: Int,
        div: String,
        conf: String = "E",
        regulationWins: Int = 20,
        wins: Int = 30,
        losses: Int = 20,
        otl: Int = 5
    ) = NhlStandingRow(
        teamAbbrev = TeamAbbrev(abbrev),
        wins = wins,
        losses = losses,
        otLosses = otl,
        points = pts,
        divisionAbbrev = div,
        conferenceAbbrev = conf,
        regulationWins = regulationWins
    )

    @Test
    fun atlanticLine_sortsByPointsDescending() {
        val rows = listOf(
            row("DET", 55, "A"),
            row("BOS", 70, "A"),
            row("FLA", 61, "A"),
            row("TOR", 62, "A")
        )
        val line = StandingsCalculator.atlanticStandingsLine(rows)
        val bos = line.indexOf("BOS:")
        val tor = line.indexOf("TOR:")
        val fla = line.indexOf("FLA:")
        val det = line.indexOf("DET:")
        assertTrue("line was: $line", bos >= 0 && tor >= 0 && fla >= 0 && det >= 0)
        assertTrue("expected BOS < TOR < FLA < DET, got: $line", bos < tor && tor < fla && fla < det)
    }

    @Test
    fun atlanticLine_tieBreaksOnRegulationWins() {
        val rows = listOf(
            row("TOR", 62, "A", regulationWins = 20),
            row("FLA", 62, "A", regulationWins = 25)
        )
        val line = StandingsCalculator.atlanticStandingsLine(rows)
        assertTrue(
            "higher regulationWins first, got: $line",
            line.indexOf("FLA:") < line.indexOf("TOR:")
        )
    }

    @Test
    fun atlanticLine_leaderShowsDashOthersShowPointsBack() {
        val rows = listOf(row("BOS", 70, "A"), row("TOR", 62, "A"))
        val line = StandingsCalculator.atlanticStandingsLine(rows)
        assertTrue("leader shows (-), got: $line", line.contains("BOS: 70 pts (-)"))
        assertTrue("70-62=8 back, got: $line", line.contains("TOR: 62 pts (8)"))
    }

    @Test
    fun atlanticLine_emptyRowsReturnsUnavailable() {
        assertEquals(
            "Atlantic standings unavailable",
            StandingsCalculator.atlanticStandingsLine(emptyList())
        )
    }

    @Test
    fun wingsSummary_pointsBackMathAndDivRank() {
        val rows = listOf(
            row("BOS", 70, "A", wins = 33, losses = 15, otl = 4),
            row("TOR", 62, "A"),
            row("FLA", 61, "A"),
            row("DET", 55, "A", wins = 25, losses = 20, otl = 5),
            row("CAR", 68, "M"),
            row("NYR", 60, "M")
        )
        val wings = StandingsCalculator.wingsSummary(rows)
        assertEquals(25, wings.wins)
        assertEquals(20, wings.losses)
        assertEquals(5, wings.otl)
        assertEquals(55, wings.points)
        assertEquals(4, wings.divRank)
        assertEquals("15", wings.divGbOrPointsBack)
    }

    @Test
    fun wingsSummary_noRowsReturnsDefault() {
        val wings = StandingsCalculator.wingsSummary(emptyList())
        assertEquals(0, wings.points)
        assertEquals(8, wings.divRank)
        assertFalse(wings.playoffIn)
    }

    @Test
    fun wingsSummary_playoffInAsDivisionLeader() {
        val rows = listOf(
            row("DET", 75, "A", regulationWins = 30),
            row("BOS", 70, "A"),
            row("TOR", 62, "A"),
            row("CAR", 68, "M"),
            row("NYR", 60, "M")
        )
        val wings = StandingsCalculator.wingsSummary(rows)
        assertTrue(wings.playoffIn)
        assertEquals("IN", wings.wcBack)
        assertEquals(1, wings.divRank)
        assertEquals("-", wings.divGbOrPointsBack)
    }

    @Test
    fun wingsSummary_playoffInAsWildCard() {
        val rows = listOf(
            row("BOS", 80, "A"),
            row("TOR", 65, "A"),
            row("DET", 64, "A", regulationWins = 26),
            row("CAR", 78, "M"),
            row("NYR", 60, "M"),
            row("PIT", 58, "M")
        )
        val wings = StandingsCalculator.wingsSummary(rows)
        assertTrue("DET 2nd in WC pool should be IN", wings.playoffIn)
        assertEquals("IN", wings.wcBack)
    }

    @Test
    fun wingsSummary_playoffOutShowsWildcardGap() {
        val rows = listOf(
            row("BOS", 80, "A"),
            row("TOR", 65, "A"),
            row("NYR", 64, "M"),
            row("DET", 60, "A"),
            row("CAR", 78, "M"),
            row("PIT", 58, "M")
        )
        val wings = StandingsCalculator.wingsSummary(rows)
        assertFalse("DET 3rd in WC pool should be OUT", wings.playoffIn)
        // Pool minus leaders {BOS, CAR} = TOR 65, NYR 64, DET 60...; cutoff 64 -> 64-60 = 4
        assertEquals("4", wings.wcBack)
    }
}
