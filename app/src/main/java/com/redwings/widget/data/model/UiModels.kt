package com.redwings.widget.data.model

import androidx.compose.runtime.Immutable
import com.redwings.widget.data.api.NhlThreeStar
import com.squareup.moshi.JsonClass
import java.util.Locale

/**
 * Lean UI-facing models (decoupled from Room entity).
 * ViewModel + Dashboard depend on these; HockeyRepository maps to/from RedWingsGame.
 */
data class UpcomingGame(
    val gameId: Int = 0,
    val gameTimeMillis: Long = 0L,
    val opponentName: String = "Opponent",
    val opponentAbbrev: String = "OPP",
    val stadiumName: String = "Little Caesars Arena",
    val isHomeGame: Boolean = true
)

@Immutable
@JsonClass(generateAdapter = true)
data class GameStarUi(
    val star: Int = 1,
    val playerId: Int = 0,
    val name: String = "",
    val teamAbbrev: String = "",
    val headshotUrl: String = "",
    val position: String = "",
    val sweaterNo: Int? = null,
    val statLine: String = ""
)

data class LastGame(
    val opponent: String = "Opponent",
    val opponentAbbrev: String = "OPP",
    val wingsScore: Int = 0,
    val oppScore: Int = 0,
    val isWinner: Boolean = false,
    val isHome: Boolean = true,
    val dateLabel: String = "",
    val threeStars: List<GameStarUi> = defaultThreeStars()
)

fun RedWingsGame.toUpcoming() = UpcomingGame(
    gameId = gameId,
    gameTimeMillis = gameTimeMillis,
    opponentName = opponentName,
    opponentAbbrev = opponentAbbrev,
    stadiumName = venueName,
    isHomeGame = isHomeGame
)

fun NhlThreeStar.toUi(): GameStarUi {
    val resolvedName = name?.default?.takeIf { it.isNotBlank() } ?: "Player $playerId"
    val resolvedHeadshot = headshot?.takeIf { it.isNotBlank() }
        ?: if (playerId > 0) "https://assets.nhle.com/mugs/nhl/latest/$playerId.png" else ""
    val isGoalie = position.equals("G", ignoreCase = true) || goalsAgainstAverage != null || savePctg != null

    val stats = when {
        isGoalie -> {
            val sv = savePctg
            val gaa = goalsAgainstAverage
            when {
                sv != null && gaa != null -> {
                    val pct = String.format(Locale.US, "%.3f", sv).removePrefix("0")
                    "$pct SV% • ${String.format(Locale.US, "%.2f", gaa)} GAA"
                }
                sv != null -> {
                    val pct = String.format(Locale.US, "%.3f", sv).removePrefix("0")
                    "$pct SV%"
                }
                gaa != null -> "${String.format(Locale.US, "%.2f", gaa)} GAA"
                else -> "Goalie"
            }
        }
        goals != null || assists != null || points != null -> {
            val g = goals ?: 0
            val a = assists ?: 0
            val pts = points ?: (g + a)
            val ptsLabel = if (pts == 1) "1 PT" else "$pts PTS"
            "${g}G • ${a}A • $ptsLabel"
        }
        else -> {
            val pos = position.orEmpty()
            val num = sweaterNo?.let { "#$it" }.orEmpty()
            listOf(teamAbbrev.orEmpty(), pos, num).filter { it.isNotBlank() }.joinToString(" • ")
        }
    }

    return GameStarUi(
        star = star,
        playerId = playerId,
        name = resolvedName,
        teamAbbrev = teamAbbrev ?: "",
        headshotUrl = resolvedHeadshot,
        position = position ?: "",
        sweaterNo = sweaterNo,
        statLine = stats
    )
}

fun defaultThreeStars(): List<GameStarUi> = listOf(
    GameStarUi(
        star = 1,
        playerId = 8477940,
        name = "D. Larkin",
        teamAbbrev = "DET",
        headshotUrl = "https://assets.nhle.com/mugs/nhl/latest/8477940.png",
        position = "C",
        sweaterNo = 71,
        statLine = "2G • 1A • 3 PTS"
    ),
    GameStarUi(
        star = 2,
        playerId = 8482078,
        name = "L. Raymond",
        teamAbbrev = "DET",
        headshotUrl = "https://assets.nhle.com/mugs/nhl/latest/8482078.png",
        position = "L",
        sweaterNo = 23,
        statLine = "1G • 1A • 2 PTS"
    ),
    GameStarUi(
        star = 3,
        playerId = 8481542,
        name = "M. Seider",
        teamAbbrev = "DET",
        headshotUrl = "https://assets.nhle.com/mugs/nhl/latest/8481542.png",
        position = "D",
        sweaterNo = 53,
        statLine = "0G • 2A • 2 PTS"
    )
)

