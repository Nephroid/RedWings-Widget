package com.redwings.widget.ui

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.redwings.widget.RedWingsApp
import com.redwings.widget.data.model.LastGame
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

    val scheduleState: StateFlow<ScheduleUiState> = combine(
        upcomingGames, lastGame, standingsSummary, atlanticLine, isRefreshing
    ) { games, last, summary, line, refreshing ->
        when {
            games.isNotEmpty() -> ScheduleUiState.Data(
                nextGame = games.first().toUi(),
                upcoming = games.take(7).map { it.toUi() },
                lastGame = last,
                standingsSummary = summary,
                atlanticLine = line
            )
            refreshing -> ScheduleUiState.Loading
            else -> ScheduleUiState.Empty
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ScheduleUiState.Loading)

    private var countdownJob: Job? = null

    init {
        refreshData()
        viewModelScope.launch {
            upcomingGames.collect { startCountdownTicker(it.firstOrNull()) }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _errorMessage.value = null
            try {
                withContext(Dispatchers.IO) { repository.refreshGames() }
                _lastGame.value = repository.lastSavedGame()?.toUi()
                _standingsSummary.value =
                    prefs.getString(KEY_SUMMARY, _standingsSummary.value) ?: _standingsSummary.value
                _atlanticLine.value =
                    prefs.getString(KEY_LINE, _atlanticLine.value) ?: _atlanticLine.value
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e("GameViewModel", "Refresh failed", e)
                _errorMessage.value = "Couldn't reach the network. Showing offline data."
                _lastGame.value = _lastGame.value
                    ?: runCatching { repository.lastSavedGame()?.toUi() }.getOrNull()
            } finally {
                _isRefreshing.value = false
            }
        }
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
        super.onCleared()
    }

    private fun UpcomingGame.toUi() = NextGameUi(
        opponent = opponentName,
        venue = stadiumName,
        startTimeMillis = gameTimeMillis,
        isHome = isHomeGame
    )

    private fun LastGame.toUi() = LastGameUi(
        opponent = opponent,
        wingsScore = wingsScore,
        oppScore = oppScore,
        isWinner = isWinner,
        isHome = isHome,
        dateLabel = dateLabel
    )

    companion object {
        private const val KEY_SUMMARY = "standings_summary"
        private const val KEY_LINE = "atlantic_line"
    }
}
