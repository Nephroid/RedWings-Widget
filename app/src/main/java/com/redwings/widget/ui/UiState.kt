package com.redwings.widget.ui

import androidx.compose.runtime.Immutable
import com.redwings.widget.data.model.GameStarUi

/**
 * UI-layer state for the tight Red Wings schedule app.
 * No Gemini / weather / roster / transactions types live here.
 */
@Immutable
data class CountdownState(
    val days: Long = 0,
    val hours: Long = 0,
    val minutes: Long = 0,
    val seconds: Long = 0,
    val isLive: Boolean = false,
    val text: String = "00d 00h 00m 00s"
)

@Immutable
data class NextGameUi(
    val opponent: String = "",
    val opponentAbbrev: String = "OPP",
    val venue: String = "",
    val startTimeMillis: Long = 0L,
    val isHome: Boolean = true,
    val broadcast: String = "",
    val awayRecord: String = "",
    val homeRecord: String = ""
)

@Immutable
data class LastGameUi(
    val opponent: String = "",
    val opponentAbbrev: String = "OPP",
    val wingsScore: Int = 0,
    val oppScore: Int = 0,
    val isWinner: Boolean = false,
    val isHome: Boolean = true,
    val dateLabel: String = "",
    val recapPills: List<String> = emptyList(),
    val threeStars: List<GameStarUi> = com.redwings.widget.data.model.defaultThreeStars()
)

@Immutable
data class StandingsRowUi(
    val rank: Int,
    val teamAbbrev: String,
    val gamesPlayed: Int,
    val wins: Int,
    val losses: Int,
    val otLosses: Int,
    val points: Int,
    val isRedWings: Boolean = false,
    val goalDiff: Int = 0,
    val streak: String = ""
)

@Immutable
sealed interface ScheduleUiState {
    data object Loading : ScheduleUiState
    data object Empty : ScheduleUiState
    @Immutable
    data class Data(
        val nextGame: NextGameUi?,
        val upcoming: List<NextGameUi> = emptyList(),
        val lastGame: LastGameUi? = null,
        val standingsSummary: String = "",
        val atlanticLine: String = "",
        val standings: List<StandingsRowUi> = emptyList(),
        val playoffChaseText: String = ""
    ) : ScheduleUiState
}
