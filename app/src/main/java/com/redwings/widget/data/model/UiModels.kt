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

@Immutable
@JsonClass(generateAdapter = true)
data class TeamLeaderPlayerUi(
    val rank: Int = 1,
    val playerId: Int = 0,
    val name: String = "",
    val position: String = "",
    val sweaterNo: Int? = null,
    val headshotUrl: String = "",
    val primaryStat: String = "",
    val secondaryStat: String = ""
)

@Immutable
@JsonClass(generateAdapter = true)
data class TeamLeadersUi(
    val topPoints: List<TeamLeaderPlayerUi> = defaultTopPoints(),
    val topGoals: List<TeamLeaderPlayerUi> = defaultTopGoals(),
    val topAssists: List<TeamLeaderPlayerUi> = defaultTopAssists()
)

fun defaultTopPoints(): List<TeamLeaderPlayerUi> = listOf(
    TeamLeaderPlayerUi(1, 8482078, "Lucas Raymond", "RW", 23, "https://assets.nhle.com/mugs/nhl/latest/8482078.png", "72 PTS", "31G, 41A • 82 GP"),
    TeamLeaderPlayerUi(2, 8477940, "Dylan Larkin", "C", 71, "https://assets.nhle.com/mugs/nhl/latest/8477940.png", "69 PTS", "33G, 36A • 68 GP"),
    TeamLeaderPlayerUi(3, 8479337, "Alex DeBrincat", "RW", 93, "https://assets.nhle.com/mugs/nhl/latest/8479337.png", "67 PTS", "27G, 40A • 82 GP"),
    TeamLeaderPlayerUi(4, 8477456, "J.T. Compher", "C", 37, "https://assets.nhle.com/mugs/nhl/latest/8477456.png", "48 PTS", "19G, 29A • 77 GP"),
    TeamLeaderPlayerUi(5, 8474141, "Patrick Kane", "RW", 88, "https://assets.nhle.com/mugs/nhl/latest/8474141.png", "47 PTS", "20G, 27A • 50 GP")
)

fun defaultTopGoals(): List<TeamLeaderPlayerUi> = listOf(
    TeamLeaderPlayerUi(1, 8477940, "Dylan Larkin", "C", 71, "https://assets.nhle.com/mugs/nhl/latest/8477940.png", "33 G", "68 GP • 69 PTS"),
    TeamLeaderPlayerUi(2, 8482078, "Lucas Raymond", "RW", 23, "https://assets.nhle.com/mugs/nhl/latest/8482078.png", "31 G", "82 GP • 72 PTS"),
    TeamLeaderPlayerUi(3, 8479337, "Alex DeBrincat", "RW", 93, "https://assets.nhle.com/mugs/nhl/latest/8479337.png", "27 G", "82 GP • 67 PTS"),
    TeamLeaderPlayerUi(4, 8474141, "Patrick Kane", "RW", 88, "https://assets.nhle.com/mugs/nhl/latest/8474141.png", "20 G", "50 GP • 47 PTS"),
    TeamLeaderPlayerUi(5, 8477456, "J.T. Compher", "C", 37, "https://assets.nhle.com/mugs/nhl/latest/8477456.png", "19 G", "77 GP • 48 PTS")
)

fun defaultTopAssists(): List<TeamLeaderPlayerUi> = listOf(
    TeamLeaderPlayerUi(1, 8482078, "Lucas Raymond", "RW", 23, "https://assets.nhle.com/mugs/nhl/latest/8482078.png", "41 A", "82 GP • 72 PTS"),
    TeamLeaderPlayerUi(2, 8479337, "Alex DeBrincat", "RW", 93, "https://assets.nhle.com/mugs/nhl/latest/8479337.png", "40 A", "82 GP • 67 PTS"),
    TeamLeaderPlayerUi(3, 8477940, "Dylan Larkin", "C", 71, "https://assets.nhle.com/mugs/nhl/latest/8477940.png", "36 A", "68 GP • 69 PTS"),
    TeamLeaderPlayerUi(4, 8481542, "Moritz Seider", "D", 53, "https://assets.nhle.com/mugs/nhl/latest/8481542.png", "33 A", "82 GP • 42 PTS"),
    TeamLeaderPlayerUi(5, 8477456, "J.T. Compher", "C", 37, "https://assets.nhle.com/mugs/nhl/latest/8477456.png", "29 A", "77 GP • 48 PTS")
)


