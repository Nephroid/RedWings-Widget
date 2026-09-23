package com.redwings.widget.data.model

/**
 * Lean UI-facing models (decoupled from Room entity).
 * ViewModel + Dashboard depend on these; HockeyRepository maps to/from RedWingsGame.
 */
data class UpcomingGame(
    val gameId: Int = 0,
    val gameTimeMillis: Long = 0L,
    val opponentName: String = "Opponent",
    val stadiumName: String = "Little Caesars Arena",
    val isHomeGame: Boolean = true
)

data class LastGame(
    val opponent: String = "Opponent",
    val wingsScore: Int = 0,
    val oppScore: Int = 0,
    val isWinner: Boolean = false,
    val isHome: Boolean = true,
    val dateLabel: String = ""
)

fun RedWingsGame.toUpcoming() = UpcomingGame(
    gameId = gameId,
    gameTimeMillis = gameTimeMillis,
    opponentName = opponentName,
    stadiumName = venueName,
    isHomeGame = isHomeGame
)
