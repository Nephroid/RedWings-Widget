package com.redwings.widget.data.repository

import android.content.Context
import android.util.Log
import com.redwings.widget.data.api.NhlApiClient
import com.redwings.widget.data.api.NhlApiService
import com.redwings.widget.data.api.NhlGame
import com.redwings.widget.data.local.GameDao
import com.redwings.widget.data.model.GameStarUi
import com.redwings.widget.data.model.LastGame
import com.redwings.widget.data.model.RedWingsGame
import com.redwings.widget.data.model.UpcomingGame
import com.redwings.widget.data.model.defaultThreeStars
import com.redwings.widget.data.model.teamDisplayName
import com.redwings.widget.data.model.toUi
import com.redwings.widget.data.model.toUpcoming
import com.squareup.moshi.Types
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

    private val starsAdapter by lazy {
        NhlApiClient.moshi.adapter<List<GameStarUi>>(
            Types.newParameterizedType(List::class.java, GameStarUi::class.java)
        )
    }

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
        if (!p.contains("last_game_opponent")) {
            saveFallbackLastGame(p)
        }
        val wings = p.getInt("last_game_wings_score", 4)
        val opp = p.getInt("last_game_opponent_score", 2)
        val starsJson = p.getString("last_game_three_stars", null)
        val stars = if (!starsJson.isNullOrBlank()) {
            try {
                starsAdapter.fromJson(starsJson)?.takeIf { it.isNotEmpty() } ?: defaultThreeStars()
            } catch (_: Exception) {
                defaultThreeStars()
            }
        } else {
            defaultThreeStars()
        }
        return LastGame(
            opponent = p.getString("last_game_opponent", "Boston Bruins") ?: "Boston Bruins",
            opponentAbbrev = p.getString("last_game_abbrev", "BOS") ?: "BOS",
            wingsScore = wings,
            oppScore = opp,
            isWinner = p.getBoolean("last_game_is_winner", wings > opp),
            isHome = p.getBoolean("last_game_is_home", true),
            dateLabel = p.getString("last_game_date", "Tue, Sep 30") ?: "Tue, Sep 30",
            threeStars = stars
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
                val atlanticRecordLine = if (rows.isNotEmpty()) {
                    StandingsCalculator.atlanticRecordLine(rows)
                } else ""
                val wings = if (rows.isNotEmpty()) {
                    StandingsCalculator.wingsSummary(rows)
                } else null

                val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                val editor = prefs.edit()

                if (atlanticRecordLine.isNotBlank()) {
                    editor.putString("atlantic_standings", atlanticRecordLine)
                }
                if (atlanticLine.isNotBlank()) {
                    editor.putString("atlantic_line", atlanticLine)
                }
                if (wings != null) {
                    val divOrdinal = when (wings.divRank) {
                        1 -> "1st"
                        2 -> "2nd"
                        3 -> "3rd"
                        else -> "${wings.divRank}th"
                    }
                    val summary =
                        "${wings.wins}-${wings.losses}-${wings.otl} • ${wings.points} pts • $divOrdinal ATL"
                    editor.putString("wings_summary", summary)
                        .putString("standings_summary", summary)
                        .putString("games_back_wild_card", wings.wcBack)
                        .putString("playoff_status", if (wings.playoffIn) "IN" else "OUT")
                        .putString("team_record_DET", "${wings.wins}-${wings.losses}-${wings.otl}")
                }
                // Cache all 32 team records for fast widget & UI lookup
                rows.forEach { row ->
                    val abbr = row.resolvedAbbrev
                    if (abbr.isNotBlank()) {
                        val w = row.wins ?: 0
                        val l = row.losses ?: 0
                        val otl = row.otLosses ?: 0
                        editor.putString("team_record_$abbr", "$w-$l-$otl")
                    }
                }
                editor.putBoolean("is_simulated_fallback", false)
                editor.apply()

                val allGames = schedule.games.orEmpty()
                if (allGames.isEmpty()) {
                    val existing = gameDao.getNextGame()
                    if (existing == null) {
                        saveSimulatedGames(context)
                    }
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

                val nextGame = upcoming.firstOrNull()
                if (nextGame != null) {
                    val isHome = nextGame.homeTeam?.abbrev == "DET"
                    val nextVenue = nextGame.venue?.default
                        ?: if (isHome) "Little Caesars Arena" else "Away"
                    val oppAbbrev = if (isHome) nextGame.awayTeam?.abbrev else nextGame.homeTeam?.abbrev
                    prefs.edit()
                        .putString("next_game_venue", nextVenue)
                        .putString("next_game_opponent_abbrev", oppAbbrev ?: "OPP")
                        .apply()
                }

                val mapped = upcoming.map { mapGame(it, if (atlanticRecordLine.isNotBlank()) atlanticRecordLine else atlanticLine) }
                val finalMapped = if (lastFinal != null) {
                    listOf(mapGame(lastFinal, if (atlanticRecordLine.isNotBlank()) atlanticRecordLine else atlanticLine))
                } else emptyList()

                val combined = (finalMapped + mapped).take(8)
                if (combined.isEmpty()) {
                    val existing = gameDao.getNextGame()
                    if (existing == null) {
                        saveSimulatedGames(context)
                    }
                } else {
                    gameDao.replaceGames(combined)
                    Log.d("HockeyRepository", "Cached ${combined.size} Red Wings games")
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                Log.e("HockeyRepository", "Refresh failed: ${e.message}", e)
                val existing = gameDao.getNextGame()
                if (existing == null) {
                    saveSimulatedGames(context)
                }
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

    private suspend fun saveLastGamePrefs(prefs: android.content.SharedPreferences, game: NhlGame) {
        val isHome = game.homeTeam?.abbrev == "DET"
        val opp = if (isHome) game.awayTeam else game.homeTeam
        val oppAbbrev = opp?.abbrev ?: "OPP"
        val wingsScore = (if (isHome) game.homeTeam?.score else game.awayTeam?.score) ?: 0
        val oppScore = (if (isHome) game.awayTeam?.score else game.homeTeam?.score) ?: 0
        val dateFmt = SimpleDateFormat("EEE, MMM d", Locale.US)

        val previousGameId = prefs.getInt("last_game_id", -1)
        val existingStarsJson = prefs.getString("last_game_three_stars", null)
        val isStarsAlreadyCached = (previousGameId == game.id) &&
            !existingStarsJson.isNullOrBlank() &&
            prefs.getBoolean("last_game_stars_from_api", false)

        val fetchedStars = if (isStarsAlreadyCached) {
            null
        } else {
            try {
                val landing = apiService.getGameLanding(game.id)
                landing.summary?.threeStars?.map { it.toUi() }?.takeIf { it.isNotEmpty() }
            } catch (e: Exception) {
                Log.w("HockeyRepository", "Failed to fetch 3 stars for game ${game.id}: ${e.message}")
                null
            }
        }

        val starsToSave = when {
            fetchedStars != null -> fetchedStars
            isStarsAlreadyCached -> null
            previousGameId == game.id && !existingStarsJson.isNullOrBlank() -> null
            else -> defaultThreeStars()
        }

        val editor = prefs.edit()
            .putInt("last_game_id", game.id)
            .putString("last_game_opponent", teamDisplayName(oppAbbrev))
            .putString("last_game_abbrev", oppAbbrev)
            .putInt("last_game_wings_score", wingsScore)
            .putInt("last_game_opponent_score", oppScore)
            .putBoolean("last_game_is_home", isHome)
            .putBoolean("last_game_is_winner", wingsScore > oppScore)
            .putString("last_game_date", dateFmt.format(java.util.Date(parseUtcToMillis(game.startTimeUTC ?: game.gameDate))))
            .putString("last_game_status", game.gameState ?: "OFF")

        if (fetchedStars != null) {
            editor.putBoolean("last_game_stars_from_api", true)
        } else if (previousGameId != game.id) {
            editor.putBoolean("last_game_stars_from_api", false)
        }

        if (starsToSave != null) {
            try {
                editor.putString("last_game_three_stars", starsAdapter.toJson(starsToSave))
            } catch (e: Exception) {
                Log.w("HockeyRepository", "Failed to serialize three stars: ${e.message}")
            }
        }
        editor.apply()
    }

    private fun saveFallbackLastGame(prefs: android.content.SharedPreferences) {
        val fallbackStars = defaultThreeStars()
        val editor = prefs.edit()
            .putInt("last_game_id", -1)
            .putBoolean("last_game_stars_from_api", false)
            .putString("last_game_opponent", "Boston Bruins")
            .putString("last_game_abbrev", "BOS")
            .putInt("last_game_wings_score", 4)
            .putInt("last_game_opponent_score", 2)
            .putBoolean("last_game_is_home", true)
            .putBoolean("last_game_is_winner", true)
            .putString("last_game_date", "Tue, Sep 30")
            .putString("last_game_status", "FINAL")

        try {
            editor.putString("last_game_three_stars", starsAdapter.toJson(fallbackStars))
        } catch (e: Exception) {
            Log.w("HockeyRepository", "Failed to serialize fallback three stars: ${e.message}")
        }
        editor.apply()
    }

    private suspend fun saveSimulatedGames(context: Context) {
        val now = System.currentTimeMillis()
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val fallbackAtlantic = "BOS: 51-20-11 (113pts) • TOR: 46-26-10 (102pts) • FLA: 45-27-10 (100pts) • " +
            "DET: 42-30-10 (94pts) • TBL: 40-32-10 (90pts) • MTL: 37-36-9 (83pts) • " +
            "OTT: 34-39-9 (77pts) • BUF: 30-43-9 (69pts)"
        prefs.edit()
            .putString("atlantic_standings", fallbackAtlantic)
            .putString("atlantic_line", fallbackAtlantic)
            .putString("standings_summary", "4th in Atlantic • 94 pts")
            .putString("wings_summary", "42-30-10 • 94 pts • 4th ATL")
            .putString("games_back_wild_card", "IN")
            .putString("playoff_status", "CLINCHED")
            .putBoolean("is_simulated_fallback", true)
            .apply()

        val list = listOf(
            RedWingsGame(990001, now + TimeUnit.HOURS.toMillis(26), "Toronto Maple Leafs", "TOR", true, "Little Caesars Arena", "FUT", 0, 0, false, true, fallbackAtlantic, ""),
            RedWingsGame(990002, now + TimeUnit.DAYS.toMillis(2) + TimeUnit.HOURS.toMillis(3), "Montreal Canadiens", "MTL", false, "Bell Centre", "FUT", 0, 0, false, true, fallbackAtlantic, ""),
            RedWingsGame(990003, now + TimeUnit.DAYS.toMillis(4) + TimeUnit.HOURS.toMillis(1), "Boston Bruins", "BOS", true, "Little Caesars Arena", "FUT", 0, 0, false, true, fallbackAtlantic, ""),
            RedWingsGame(990004, now + TimeUnit.DAYS.toMillis(6) + TimeUnit.HOURS.toMillis(2), "Florida Panthers", "FLA", true, "Little Caesars Arena", "FUT", 0, 0, false, true, fallbackAtlantic, ""),
            RedWingsGame(990005, now + TimeUnit.DAYS.toMillis(8) + TimeUnit.HOURS.toMillis(4), "Tampa Bay Lightning", "TBL", false, "Amalie Arena", "FUT", 0, 0, false, true, fallbackAtlantic, ""),
            RedWingsGame(990006, now + TimeUnit.DAYS.toMillis(10) + TimeUnit.HOURS.toMillis(1), "Ottawa Senators", "OTT", true, "Little Caesars Arena", "FUT", 0, 0, false, true, fallbackAtlantic, ""),
            RedWingsGame(990007, now + TimeUnit.DAYS.toMillis(12) + TimeUnit.HOURS.toMillis(3), "Buffalo Sabres", "BUF", false, "KeyBank Center", "FUT", 0, 0, false, true, fallbackAtlantic, "")
        )
        gameDao.replaceGames(list)
        saveFallbackLastGame(prefs)
        Log.d("HockeyRepository", "Cached 7 simulated games (offline fallback)")
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
