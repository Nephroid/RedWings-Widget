package com.redwings.widget.ui

/**
 * UI-layer state for the tight Red Wings schedule app.
 * No Gemini / weather / roster / transactions types live here.
 */
data class CountdownState(
    val days: Long = 0,
    val hours: Long = 0,
    val minutes: Long = 0,
    val seconds: Long = 0,
    val isLive: Boolean = false,
    val text: String = "00d 00h 00m 00s"
)

data class NextGameUi(
    val opponent: String = "",
    val venue: String = "",
    val startTimeMillis: Long = 0L,
    val isHome: Boolean = true
)

data class LastGameUi(
    val opponent: String = "",
    val wingsScore: Int = 0,
    val oppScore: Int = 0,
    val isWinner: Boolean = false,
    val isHome: Boolean = true,
    val dateLabel: String = ""
)

sealed interface ScheduleUiState {
    data object Loading : ScheduleUiState
    data object Empty : ScheduleUiState
    data class Data(
        val nextGame: NextGameUi?,
        val upcoming: List<NextGameUi> = emptyList(),
        val lastGame: LastGameUi? = null,
        val standingsSummary: String = "",
        val atlanticLine: String = ""
    ) : ScheduleUiState
}
