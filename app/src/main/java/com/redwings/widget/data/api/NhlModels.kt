package com.redwings.widget.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NhlScheduleResponse(
    val games: List<NhlGame>? = null,
    val previousSeason: String? = null,
    val currentSeason: String? = null,
    val clubTimezone: String? = null
)

@JsonClass(generateAdapter = true)
data class NhlGame(
    val id: Int = 0,
    val gameDate: String? = null,
    val startTimeUTC: String? = null,
    val gameState: String? = null,
    val gameType: Int? = null,
    val homeTeam: NhlTeam? = null,
    val awayTeam: NhlTeam? = null,
    val venue: NhlVenue? = null
)

@JsonClass(generateAdapter = true)
data class NhlTeam(
    val abbrev: String? = null,
    val commonName: CommonName? = null,
    val placeName: PlaceName? = null,
    val score: Int? = null,
    val record: String? = null,
    val logo: String? = null
)

@JsonClass(generateAdapter = true)
data class CommonName(
    @Json(name = "default") val default: String? = null
)

@JsonClass(generateAdapter = true)
data class PlaceName(
    @Json(name = "default") val default: String? = null
)

@JsonClass(generateAdapter = true)
data class NhlVenue(
    @Json(name = "default") val default: String? = null
)

@JsonClass(generateAdapter = true)
data class NhlScoreboardResponse(
    val gamesByDate: List<NhlGamesByDate>? = null,
    val currentDate: String? = null
)

@JsonClass(generateAdapter = true)
data class NhlGamesByDate(
    val date: String? = null,
    val games: List<NhlGame>? = null
)

@JsonClass(generateAdapter = true)
data class NhlStandingsResponse(
    val standings: List<NhlStandingRow>? = null
)

@JsonClass(generateAdapter = true)
data class NhlStandingRow(
    val teamAbbrev: TeamAbbrev? = null,
    val teamName: TeamName? = null,
    val wins: Int? = null,
    val losses: Int? = null,
    val otLosses: Int? = null,
    val points: Int? = null,
    val goalDiff: Int? = null,
    val goalDifferential: Int? = null,
    val streakCode: String? = null,
    val streakCount: Int? = null,
    val wildcardSequence: Int? = null,
    val divisionAbbrev: String? = null,
    val conferenceAbbrev: String? = null,
    val gamesPlayed: Int? = null,
    val regulationWins: Int? = null,
    val pointsPctg: Double? = null
) {
    val resolvedGoalDiff: Int get() = goalDiff ?: goalDifferential ?: 0
    val resolvedAbbrev: String get() = teamAbbrev?.default ?: ""
}

@JsonClass(generateAdapter = true)
data class TeamAbbrev(
    @Json(name = "default") val default: String? = null
)

@JsonClass(generateAdapter = true)
data class TeamName(
    @Json(name = "default") val default: String? = null
)
