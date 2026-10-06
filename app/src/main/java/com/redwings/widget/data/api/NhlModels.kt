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

@JsonClass(generateAdapter = true)
data class NhlGameLandingResponse(
    val id: Int? = null,
    val gameState: String? = null,
    val summary: NhlGameSummary? = null
)

@JsonClass(generateAdapter = true)
data class NhlGameSummary(
    val threeStars: List<NhlThreeStar>? = null
)

@JsonClass(generateAdapter = true)
data class NhlThreeStar(
    val star: Int = 1,
    val playerId: Int = 0,
    val teamAbbrev: String? = null,
    val headshot: String? = null,
    val name: CommonName? = null,
    val sweaterNo: Int? = null,
    val position: String? = null,
    val goals: Int? = null,
    val assists: Int? = null,
    val points: Int? = null,
    val goalsAgainstAverage: Double? = null,
    val savePctg: Double? = null
)

@JsonClass(generateAdapter = true)
data class NhlClubStatsResponse(
    val season: Int? = null,
    val gameType: Int? = null,
    val skaters: List<NhlClubSkater>? = null
)

@JsonClass(generateAdapter = true)
data class NhlClubSkater(
    val playerId: Int = 0,
    val headshot: String? = null,
    val firstName: CommonName? = null,
    val lastName: CommonName? = null,
    val positionCode: String? = null,
    val gamesPlayed: Int = 0,
    val goals: Int = 0,
    val assists: Int = 0,
    val points: Int = 0,
    val plusMinus: Int = 0,
    val penaltyMinutes: Int = 0,
    val shots: Int = 0,
    val shootingPctg: Double = 0.0
)


