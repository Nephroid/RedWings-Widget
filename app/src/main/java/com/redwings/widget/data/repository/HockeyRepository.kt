package com.redwings.widget.data.repository

import android.content.Context
import android.util.Log
import com.redwings.widget.data.api.NhlApiClient
import com.redwings.widget.data.api.NhlApiService
import com.redwings.widget.data.api.NhlGame
import com.redwings.widget.data.local.GameDao
import com.redwings.widget.data.model.LastGame
import com.redwings.widget.data.model.RedWingsGame
import com.redwings.widget.data.model.UpcomingGame
import com.redwings.widget.data.model.teamDisplayName
import com.redwings.widget.data.model.toUpcoming
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class HockeyRepository(
    private val gameDao: GameDao,
    private val appContext: Context? = null
) {

    val games: Flow<List<RedWingsGame>> = gameDao.getGames()

    /** ViewModel contract: upcoming games as lean UI models. */
    val upcomingGames: Flow<List<UpcomingGame>> = games.map { list ->
        list.filter { !it.isFinal }.sortedBy { it.gameTimeMillis }.map { it.toUpcoming() }
            .ifEmpty { list.map { it.toUpcoming() } }
    }

    private val apiService: NhlApiService = NhlApiClient.apiService

    companion object {
        const val PREFS = "RedWingsPrefs"
    }

    private fun prefs(ctx: Context) = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /** No-arg refresh for ViewModel (uses appContext). */
    suspend fun refreshGames() {
        val ctx = appContext ?: return
        refreshGames(ctx)
    }

    /** Prefs-backed last game for ViewModel offline fallback. */
    fun lastSavedGame(): LastGame? {
        val ctx = appContext ?: return null
        val p = prefs(ctx)
        if (!p.contains("last_game_opponent")) return null
        val wings = p.getInt("last_game_wings_score", 0)
        val opp = p.getInt("last_game_opponent_score", 0)
        return LastGame(
            opponent = p.getString("last_game_opponent", "Opponent") ?: "Opponent",
            wingsScore = wings,
            oppScore = opp,
            isWinner = p.getBoolean("last_game_is_winner", wings > opp),
            isHome = p.getBoolean("last_game_is_home", true),
            dateLabel = p.getString("last_game_date", "") ?: ""
        )
    }

    suspend fun refreshGames(context: Context) {
        withContext(Dispatchers.IO) {
            try {
                val schedule = apiService.getClubScheduleNow("DET")
                val standings = try {
                    apiService.getStandingsNow()
                } catch (e: Exception) {
                    Log.w("HockeyRepository", "Standings fetch failed: ${e.message}")
                    null
                }

                val rows = standings?.standings.orEmpty()
                val atlanticLine = if (rows.isNotEmpty()) {
                    StandingsCalculator.atlanticStandingsLine(rows)
                } else ""
                val wings = if (rows.isNotEmpty()) {
                    StandingsCalculator.wingsSummary(rows)
                } else null

                val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                if (atlanticLine.isNotBlank()) {
                    prefs.edit()
                        .putString("atlantic_standings", atlanticLine)
                        .putString("atlantic_line", atlanticLine)
                        .apply()
                }
                if (wings != null) {
                    val summary =
                        "${wings.wins}-${wings.losses}-${wings.otl} • ${wings.points} pts • ${wings.divRank}th ATL"
                    prefs.edit()
                        .putString(
                            "wings_summary",
                            summary
                        )
                        .putString("standings_summary", summary)
                        .putString("games_back_wild_card", wings.wcBack)
                        .putString("playoff_status", if (wings.playoffIn) "IN" else "OUT")
                        .apply()
                }

                val allGames = schedule.games.orEmpty()
                if (allGames.isEmpty()) {
                    saveSimulatedGames(context)
                    return@withContext
                }

                val now = System.currentTimeMillis()
                val sorted = allGames.sortedBy { parseUtcToMillis(it.startTimeUTC ?: it.gameDate) }

                val lastFinal = sorted.filter { isFinalState(it.gameState) }
                    .filter { parseUtcToMillis(it.startTimeUTC ?: it.gameDate) <= now }
                    .maxByOrNull { parseUtcToMillis(it.startTimeUTC ?: it.gameDate) }

                if (lastFinal != null) {
                    saveLastGamePrefs(prefs, lastFinal)
                } else if (!prefs.contains("last_game_opponent")) {
                    saveFallbackLastGame(prefs)
                }

                val upcoming = sorted.filter {
                    val t = parseUtcToMillis(it.startTimeUTC ?: it.gameDate)
                    t > now - TimeUnit.HOURS.toMillis(4) && !isFinalState(it.gameState)
                }.take(7)

                val mapped = upcoming.map { mapGame(it, atlanticLine) }
                val finalMapped = if (lastFinal != null) {
                    listOf(mapGame(lastFinal, atlanticLine))
                } else emptyList()

                val combined = (finalMapped + mapped).take(8)
                if (combined.isEmpty()) {
                    saveSimulatedGames(context)
                } else {
                    gameDao.replaceGames(combined)
                    Log.d("HockeyRepository", "Cached ${combined.size} Red Wings games")
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                Log.e("HockeyRepository", "Refresh failed: ${e.message}", e)
                saveSimulatedGames(context)
            }
        }
    }

    private fun mapGame(game: NhlGame, standingsSummary: String): RedWingsGame {
        val isHome = game.homeTeam?.abbrev == "DET"
        val opp = if (isHome) game.awayTeam else game.homeTeam
        val oppAbbrev = opp?.abbrev ?: "OPP"
        val final = isFinalState(game.gameState)
        val wingsScore = (if (isHome) game.homeTeam?.score else game.awayTeam?.score) ?: 0
        val oppScore = (if (isHome) game.awayTeam?.score else game.homeTeam?.score) ?: 0
        return RedWingsGame(
            gameId = game.id,
            gameTimeMillis = parseUtcToMillis(game.startTimeUTC ?: game.gameDate),
            opponentName = teamDisplayName(oppAbbrev),
            opponentAbbrev = oppAbbrev,
            isHomeGame = isHome,
            venueName = game.venue?.default ?: if (isHome) "Little Caesars Arena" else "Away",
            gameState = game.gameState ?: "FUT",
            wingsScore = wingsScore,
            opponentScore = oppScore,
            isFinal = final,
            isSimulated = false,
            standingsSummary = standingsSummary,
            headToHead = ""
        )
    }

    private fun saveLastGamePrefs(prefs: android.content.SharedPreferences, game: NhlGame) {
        val isHome = game.homeTeam?.abbrev == "DET"
        val opp = if (isHome) game.awayTeam else game.homeTeam
        val oppAbbrev = opp?.abbrev ?: "OPP"
        val wingsScore = (if (isHome) game.homeTeam?.score else game.awayTeam?.score) ?: 0
        val oppScore = (if (isHome) game.awayTeam?.score else game.homeTeam?.score) ?: 0
        val dateFmt = SimpleDateFormat("EEE, MMM d", Locale.US)
        prefs.edit()
            .putString("last_game_opponent", teamDisplayName(oppAbbrev))
            .putString("last_game_abbrev", oppAbbrev)
            .putInt("last_game_wings_score", wingsScore)
            .putInt("last_game_opponent_score", oppScore)
            .putBoolean("last_game_is_home", isHome)
            .putBoolean("last_game_is_winner", wingsScore > oppScore)
            .putString("last_game_date", dateFmt.format(java.util.Date(parseUtcToMillis(game.startTimeUTC ?: game.gameDate))))
            .putString("last_game_status", game.gameState ?: "OFF")
            .apply()
    }

    private fun saveFallbackLastGame(prefs: android.content.SharedPreferences) {
        prefs.edit()
            .putString("last_game_opponent", "Toronto Maple Leafs")
            .putString("last_game_abbrev", "TOR")
            .putInt("last_game_wings_score", 4)
            .putInt("last_game_opponent_score", 2)
            .putBoolean("last_game_is_home", true)
            .putBoolean("last_game_is_winner", true)
            .putString("last_game_date", "Sat, Oct 11")
            .putString("last_game_status", "OFF")
            .apply()
    }

    private suspend fun saveSimulatedGames(context: Context) {
        val now = System.currentTimeMillis()
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val atlantic = prefs.getString("atlantic_standings", "") ?: ""
        val list = listOf(
            RedWingsGame(990001, now + TimeUnit.HOURS.toMillis(26), "Toronto Maple Leafs", "TOR", true, "Little Caesars Arena", "FUT", 0, 0, false, true, atlantic, ""),
            RedWingsGame(990002, now + TimeUnit.DAYS.toMillis(2) + TimeUnit.HOURS.toMillis(3), "Montreal Canadiens", "MTL", false, "Bell Centre", "FUT", 0, 0, false, true, atlantic, ""),
            RedWingsGame(990003, now + TimeUnit.DAYS.toMillis(4) + TimeUnit.HOURS.toMillis(1), "Boston Bruins", "BOS", true, "Little Caesars Arena", "FUT", 0, 0, false, true, atlantic, "")
        )
        gameDao.replaceGames(list)
        if (!prefs.contains("last_game_opponent")) saveFallbackLastGame(prefs)
        Log.d("HockeyRepository", "Cached 3 simulated games (offline fallback)")
    }

    private fun isFinalState(state: String?): Boolean =
        state == "OFF" || state == "FINAL"

    fun parseUtcToMillis(iso: String?): Long {
        if (iso.isNullOrBlank()) return System.currentTimeMillis()
        val formats = listOf("yyyy-MM-dd'T'HH:mm:ss'Z'", "yyyy-MM-dd")
        for (f in formats) {
            try {
                val sdf = SimpleDateFormat(f, Locale.US)
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                val parsed = sdf.parse(iso)
                if (parsed != null) return parsed.time
            } catch (_: Exception) { }
        }
        return System.currentTimeMillis()
    }

    fun currentSeasonId(): String {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) // 0-based; Jul(6)+ starts new season
        return if (month >= Calendar.JULY) "$year${year + 1}" else "${year - 1}$year"
    }
}
