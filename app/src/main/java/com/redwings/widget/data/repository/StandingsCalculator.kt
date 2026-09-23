package com.redwings.widget.data.repository

import com.redwings.widget.data.api.NhlStandingRow

data class WingsStanding(
    val wins: Int,
    val losses: Int,
    val otl: Int,
    val points: Int,
    val divRank: Int,
    val divGbOrPointsBack: String,
    val wcBack: String,
    val playoffIn: Boolean
)

object StandingsCalculator {

    fun atlanticStandingsLine(rows: List<NhlStandingRow>): String {
        val atlantic = rows.filter { it.divisionAbbrev == "A" }
            .sortedWith(compareByDescending<NhlStandingRow> { it.points ?: 0 }
                .thenByDescending { it.regulationWins ?: 0 })
        if (atlantic.isEmpty()) return "Atlantic standings unavailable"
        val leader = atlantic.first()
        val lw = leader.wins ?: 0
        val ll = (leader.losses ?: 0) + (leader.otLosses ?: 0)
        return atlantic.joinToString(" • ") { row ->
            val abbr = row.resolvedAbbrev.ifBlank { "?" }
            val pts = row.points ?: 0
            val gb = gamesBack(leaderPts = leader.points ?: 0, teamPts = pts)
            "$abbr: $pts pts ($gb)"
        }.ifBlank { "Atlantic standings unavailable" }
    }

    fun wingsSummary(rows: List<NhlStandingRow>): WingsStanding {
        val atlantic = rows.filter { it.divisionAbbrev == "A" }
            .sortedWith(compareByDescending<NhlStandingRow> { it.points ?: 0 }
                .thenByDescending { it.regulationWins ?: 0 })
        val eastern = rows.filter { it.conferenceAbbrev == "E" }
            .sortedWith(compareByDescending<NhlStandingRow> { it.points ?: 0 }
                .thenByDescending { it.regulationWins ?: 0 })

        val wings = rows.firstOrNull { it.resolvedAbbrev == "DET" }
            ?: return WingsStanding(0, 0, 0, 0, 8, "-", "-", false)

        val w = wings.wins ?: 0
        val l = wings.losses ?: 0
        val otl = wings.otLosses ?: 0
        val pts = wings.points ?: (w * 2 + otl)

        val divRank = atlantic.indexOfFirst { it.resolvedAbbrev == "DET" }
            .let { if (it >= 0) it + 1 else 8 }
        val leaderPts = atlantic.firstOrNull()?.points ?: pts
        val divBack = gamesBack(leaderPts, pts)

        val wcBack = wildcardBack(eastern)
        val playoffIn = isPlayoffIn(eastern)

        return WingsStanding(w, l, otl, pts, divRank, divBack, wcBack, playoffIn)
    }

    private fun gamesBack(leaderPts: Int, teamPts: Int): String {
        if (teamPts >= leaderPts) return "-"
        return "${leaderPts - teamPts}"
    }

    private fun wildcardBack(eastern: List<NhlStandingRow>): String {
        val divLeaders = eastern.filter { it.divisionAbbrev == "M" || it.divisionAbbrev == "A" }
            .groupBy { it.divisionAbbrev }
            .mapNotNull { (_, teams) ->
                teams.maxWithOrNull(compareBy({ it.points ?: 0 }, { it.regulationWins ?: 0 }))
            }
            .map { it.resolvedAbbrev }.toSet()
        val pool = eastern.filter { it.resolvedAbbrev !in divLeaders }
            .sortedWith(compareByDescending<NhlStandingRow> { it.points ?: 0 }
                .thenByDescending { it.regulationWins ?: 0 })
        val cutoff = pool.getOrNull(1)?.points
        val wingsPts = eastern.firstOrNull { it.resolvedAbbrev == "DET" }?.points ?: 0
        if (cutoff == null) return "-"
        if (wingsPts >= cutoff) return "IN"
        return "${cutoff - wingsPts}"
    }

    private fun isPlayoffIn(eastern: List<NhlStandingRow>): Boolean {
        val divLeaders = eastern.filter { it.divisionAbbrev == "M" || it.divisionAbbrev == "A" }
            .groupBy { it.divisionAbbrev }
            .mapNotNull { (_, teams) ->
                teams.maxWithOrNull(compareBy({ it.points ?: 0 }, { it.regulationWins ?: 0 }))
            }
            .map { it.resolvedAbbrev }.toSet()
        if ("DET" in divLeaders) return true
        val pool = eastern.filter { it.resolvedAbbrev !in divLeaders }
            .sortedWith(compareByDescending<NhlStandingRow> { it.points ?: 0 }
                .thenByDescending { it.regulationWins ?: 0 })
        return pool.take(2).any { it.resolvedAbbrev == "DET" }
    }
}
