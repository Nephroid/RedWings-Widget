package com.redwings.widget.data.polling

/**
 * State representing game progression for polling interval calculations.
 */
enum class GameState {
    PRE_GAME,
    IN_PROGRESS,
    HALFTIME, // Intermission
    FINAL
}

/**
 * GamePollingRepository: Dynamic ingestion and polling calculations based on game leverage
 * and state as specified in instructions.md:
 * - 60s for Pre/Post/Halftime
 * - 15s for standard play
 * - 3–5s for high leverage (Excitement Index > 75 or < 2 min remaining in 3rd period)
 */
class GamePollingRepository {

    /**
     * Calculates optimal polling interval in milliseconds.
     * @param state Current GameState
     * @param excitementIndex 0..100 excitement index metric
     * @param remainingSecondsRemaining In-period seconds remaining (e.g. 120 = 2 min)
     * @param period Current period (1, 2, 3, or OT)
     */
    fun calculatePollingIntervalMs(
        state: GameState,
        excitementIndex: Int = 0,
        remainingSecondsRemaining: Int = 1200,
        period: Int = 1
    ): Long {
        return when (state) {
            GameState.PRE_GAME, GameState.FINAL, GameState.HALFTIME -> 60_000L
            GameState.IN_PROGRESS -> {
                val isHighLeverage = excitementIndex > 75 || (period >= 3 && remainingSecondsRemaining <= 120)
                if (isHighLeverage) {
                    3_000L
                } else {
                    15_000L
                }
            }
        }
    }
}
