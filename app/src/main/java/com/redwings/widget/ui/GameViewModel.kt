package com.redwings.widget.ui

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.redwings.widget.RedWingsApp
import com.redwings.widget.data.model.LastGame
import com.redwings.widget.data.model.TeamLeadersUi
import com.redwings.widget.data.model.UpcomingGame
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Tight schedule ViewModel (no Gemini / weather / roster / transactions).
 *
 * Data-layer contract — single integration point, adjust here if names drift:
 * - RedWingsApp exposes `val container` with `hockeyRepository`
 * - HockeyRepository.upcomingGames: Flow<List<UpcomingGame>>
 * - suspend HockeyRepository.refreshGames()
 * - HockeyRepository.lastSavedGame(): LastGame? (prefs-backed offline fallback)
 * - UpcomingGame(gameTimeMillis, opponentName, stadiumName, isHomeGame)
 * - LastGame(opponent, wingsScore, oppScore, isWinner, isHome, dateLabel)
 */
class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as RedWingsApp).container.hockeyRepository
    private val prefs = application.getSharedPreferences("RedWingsPrefs", Context.MODE_PRIVATE)

    val upcomingGames: StateFlow<List<UpcomingGame>> = repository.upcomingGames
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _countdown = MutableStateFlow(CountdownState())
    val countdown: StateFlow<CountdownState> = _countdown.asStateFlow()

    private val _lastGame = MutableStateFlow<LastGameUi?>(null)
    val lastGame: StateFlow<LastGameUi?> = _lastGame.asStateFlow()

    private val _standingsSummary =
        MutableStateFlow(prefs.getString(KEY_SUMMARY, "Atlantic Division") ?: "Atlantic Division")
    val standingsSummary: StateFlow<String> = _standingsSummary.asStateFlow()

    private val _atlanticLine = MutableStateFlow(prefs.getString(KEY_LINE, "DET —") ?: "DET —")
    val atlanticLine: StateFlow<String> = _atlanticLine.asStateFlow()

    private val app = application as RedWingsApp
    private val aiEngine = app.container.aiContentEngine
    private val fanPulseRepo = app.container.fanPulseRepository

    private val _teamLeaders =
        MutableStateFlow(repository.getCachedSeasonLeaders())
    val teamLeaders: StateFlow<TeamLeadersUi> = _teamLeaders.asStateFlow()

    val fanPulseCount: StateFlow<Long> = fanPulseRepo.fanPulseCount

    val scheduleState: StateFlow<ScheduleUiState> = combine(
        upcomingGames, lastGame, standingsSummary, atlanticLine, teamLeaders,
        aiEngine.intelligenceState, fanPulseRepo.fanPulseCount
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        val games = args[0] as List<UpcomingGame>
        val last = args[1] as LastGameUi?
        val summary = args[2] as String
        val line = args[3] as String
        val leaders = args[4] as TeamLeadersUi
        val intelligence = args[5] as com.redwings.widget.data.firebase.GameIntelligenceState
        val pulses = args[6] as Long

        val wcBack = prefs.getString("games_back_wild_card", "IN") ?: "IN"
        val poStatus = prefs.getString("playoff_status", "CLINCHED") ?: "CLINCHED"
        val inPlayoffs = poStatus.equals("IN", ignoreCase = true) ||
            poStatus.contains("CLINCHED", ignoreCase = true)
        val chaseText = com.redwings.widget.widget.StandingsFormatter
            .formatPlayoffChase(wcBack, inPlayoffs).toString()
        val rawAtlantic = prefs.getString("atlantic_standings", null) ?: line
        val standingsList = com.redwings.widget.widget.StandingsFormatter
            .parseAtlanticRowsForUi(rawAtlantic)

        val fallbackLast = last ?: repository.lastSavedGame()?.toUi(intelligence) ?: LastGameUi(
            opponent = "Boston Bruins",
            opponentAbbrev = "BOS",
            wingsScore = 4,
            oppScore = 2,
            isWinner = true,
            isHome = true,
            dateLabel = "Tue, Sep 30",
            recapPills = if (intelligence.dynamicRecapPill.isNotBlank()) listOf(intelligence.dynamicRecapPill) else emptyList(),
            threeStars = com.redwings.widget.data.model.defaultThreeStars()
        )

        when {
            games.isNotEmpty() -> {
                ScheduleUiState.Data(
                    nextGame = games.first().toUi(intelligence),
                    upcoming = games.take(7).map { it.toUi(intelligence) },
                    lastGame = fallbackLast,
                    standingsSummary = summary,
                    atlanticLine = line,
                    standings = standingsList,
                    playoffChaseText = chaseText,
                    teamLeaders = leaders,
                    fanPulseCount = pulses,
                    milestonePacing = intelligence.milestonePacing
                )
            }
            else -> {
                val now = System.currentTimeMillis()
                val fallbackUpcoming = listOf(
                    NextGameUi("Toronto Maple Leafs", "TOR", "Little Caesars Arena", now + 86400000L * 2, true, storyline = intelligence.matchupStoryline, keyBattle = intelligence.keyBattle),
                    NextGameUi("Montreal Canadiens", "MTL", "Bell Centre", now + 86400000L * 4, false),
                    NextGameUi("Boston Bruins", "BOS", "Little Caesars Arena", now + 86400000L * 6, true),
                    NextGameUi("Florida Panthers", "FLA", "Little Caesars Arena", now + 86400000L * 8, true),
                    NextGameUi("Tampa Bay Lightning", "TBL", "Amalie Arena", now + 86400000L * 10, false),
                    NextGameUi("Ottawa Senators", "OTT", "Little Caesars Arena", now + 86400000L * 12, true),
                    NextGameUi("Buffalo Sabres", "BUF", "KeyBank Center", now + 86400000L * 14, false)
                )
                ScheduleUiState.Data(
                    nextGame = fallbackUpcoming.first(),
                    upcoming = fallbackUpcoming,
                    lastGame = fallbackLast,
                    standingsSummary = summary.ifBlank { "Atlantic Division" },
                    atlanticLine = line,
                    standings = standingsList,
                    playoffChaseText = chaseText,
                    teamLeaders = leaders,
                    fanPulseCount = pulses,
                    milestonePacing = intelligence.milestonePacing
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ScheduleUiState.Loading)

    private var countdownJob: Job? = null
    private var pollingJob: Job? = null
    private val pollingRepository = com.redwings.widget.data.polling.GamePollingRepository()

    init {
        refreshData()
        viewModelScope.launch {
            upcomingGames.collect { 
                startCountdownTicker(it.firstOrNull()) 
                startPollingJob(it.firstOrNull())
            }
        }
    }

    private fun startPollingJob(nextGame: UpcomingGame?) {
        pollingJob?.cancel()
        if (nextGame == null) return
        pollingJob = viewModelScope.launch {
            while (isActive) {
                val diff = nextGame.gameTimeMillis - System.currentTimeMillis()
                val state = when {
                    diff > 0 -> com.redwings.widget.data.polling.GameState.PRE_GAME
                    diff <= 0 && diff > -TimeUnit.HOURS.toMillis(3) -> com.redwings.widget.data.polling.GameState.IN_PROGRESS
                    else -> com.redwings.widget.data.polling.GameState.FINAL
                }
                
                // In a real app we'd get excitementIndex/period from the live game feed
                val interval = pollingRepository.calculatePollingIntervalMs(state)
                delay(interval)
                refreshData()
            }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _errorMessage.value = null
            try {
                withContext(Dispatchers.IO) { repository.refreshGames() }
                val intel = aiEngine.intelligenceState.value
                _lastGame.value = repository.lastSavedGame()?.toUi(intel)
                _standingsSummary.value =
                    prefs.getString(KEY_SUMMARY, _standingsSummary.value) ?: _standingsSummary.value
                _atlanticLine.value =
                    prefs.getString(KEY_LINE, _atlanticLine.value) ?: _atlanticLine.value
                val leaders = repository.getCachedSeasonLeaders()
                _teamLeaders.value = leaders
                aiEngine.calculatePacingProjections(leaders)

                val next = upcomingGames.value.firstOrNull()
                if (next != null) {
                    val detRec = prefs.getString("team_record_DET", "42-30-10") ?: "42-30-10"
                    val oppRec = prefs.getString("team_record_${next.opponentAbbrev}", "40-30-12") ?: "40-30-12"
                    aiEngine.refreshPreGameIntelligence(
                        opponentName = next.opponentName,
                        opponentAbbrev = next.opponentAbbrev,
                        isHome = next.isHomeGame,
                        detRecord = detRec,
                        oppRecord = oppRec
                    )
                    fanPulseRepo.attachGamePulseListener(next.gameId)
                }

                val last = repository.lastSavedGame()
                if (last != null) {
                    aiEngine.refreshPostGameIntelligence(
                        wingsScore = last.wingsScore,
                        oppScore = last.oppScore,
                        oppAbbrev = last.opponentAbbrev,
                        stars = last.threeStars
                    )
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e("GameViewModel", "Refresh failed", e)
                _errorMessage.value = "Couldn't reach the network. Showing offline data."
                val intel = aiEngine.intelligenceState.value
                _lastGame.value = _lastGame.value
                    ?: runCatching { repository.lastSavedGame()?.toUi(intel) }.getOrNull()
                _teamLeaders.value = repository.getCachedSeasonLeaders()
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun lightTheLamp(gameId: Int = upcomingGames.value.firstOrNull()?.gameId ?: 990001) {
        fanPulseRepo.lightTheLamp(gameId)
    }

    fun triggerManualRefresh() = refreshData()

    private fun startCountdownTicker(nextGame: UpcomingGame?) {
        countdownJob?.cancel()
        if (nextGame == null) {
            _countdown.value = CountdownState(text = "No Games Scheduled")
            return
        }
        countdownJob = viewModelScope.launch {
            while (isActive) {
                val diff = nextGame.gameTimeMillis - System.currentTimeMillis()
                _countdown.value = when {
                    diff <= 0 && diff > -TimeUnit.HOURS.toMillis(3) ->
                        CountdownState(isLive = true, text = "LIVE NOW")
                    diff <= 0 -> CountdownState(text = "Game Finished")
                    else -> {
                        val days = TimeUnit.MILLISECONDS.toDays(diff)
                        val hours = TimeUnit.MILLISECONDS.toHours(diff) % 24
                        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60
                        val seconds = TimeUnit.MILLISECONDS.toSeconds(diff) % 60
                        val text = buildString {
                            if (days > 0) append("${days}d ")
                            append("%02dh %02dm %02ds".format(hours, minutes, seconds))
                        }
                        CountdownState(days, hours, minutes, seconds, false, text)
                    }
                }
                delay(1000)
            }
        }
    }

    override fun onCleared() {
        countdownJob?.cancel()
        pollingJob?.cancel()
        fanPulseRepo.cleanup()
        super.onCleared()
    }

    private fun UpcomingGame.toUi(intelligence: com.redwings.widget.data.firebase.GameIntelligenceState? = null): NextGameUi {
        val detRecord = prefs.getString("team_record_DET", null)
            ?: prefs.getString("wings_summary", null)?.substringBefore(" •")
            ?: ""
        val oppRecord = prefs.getString("team_record_$opponentAbbrev", "") ?: ""
        val awayRec = if (isHomeGame) oppRecord else detRecord
        val homeRec = if (isHomeGame) detRecord else oppRecord
        return NextGameUi(
            opponent = opponentName,
            opponentAbbrev = opponentAbbrev,
            venue = stadiumName,
            startTimeMillis = gameTimeMillis,
            isHome = isHomeGame,
            awayRecord = awayRec,
            homeRecord = homeRec,
            storyline = intelligence?.matchupStoryline.orEmpty(),
            keyBattle = intelligence?.keyBattle.orEmpty()
        )
    }

    private fun LastGame.toUi(intelligence: com.redwings.widget.data.firebase.GameIntelligenceState? = null): LastGameUi {
        val pills = if (!intelligence?.dynamicRecapPill.isNullOrBlank()) {
            listOf(intelligence!!.dynamicRecapPill)
        } else emptyList()

        val starsWithComments = threeStars.map { star ->
            val comment = intelligence?.starCommentaries?.get(star.star)
            if (!comment.isNullOrBlank()) star.copy(commentary = comment) else star
        }

        return LastGameUi(
            opponent = opponent,
            opponentAbbrev = opponentAbbrev,
            wingsScore = wingsScore,
            oppScore = oppScore,
            isWinner = isWinner,
            isHome = isHome,
            dateLabel = dateLabel,
            recapPills = pills,
            threeStars = starsWithComments
        )
    }

    companion object {
        private const val KEY_SUMMARY = "standings_summary"
        private const val KEY_LINE = "atlantic_line"
    }
}
