package com.redwings.widget

import com.redwings.widget.data.polling.GamePollingRepository
import com.redwings.widget.data.polling.GameState
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GamePollingRepositoryTest {

    private lateinit var repository: GamePollingRepository

    @Before
    fun setUp() {
        repository = GamePollingRepository()
    }

    @Test
    fun calculatePollingInterval_preGame_returns60s() {
        val interval = repository.calculatePollingIntervalMs(GameState.PRE_GAME)
        assertEquals(60_000L, interval)
    }

    @Test
    fun calculatePollingInterval_final_returns60s() {
        val interval = repository.calculatePollingIntervalMs(GameState.FINAL)
        assertEquals(60_000L, interval)
    }

    @Test
    fun calculatePollingInterval_halftime_returns60s() {
        val interval = repository.calculatePollingIntervalMs(GameState.HALFTIME)
        assertEquals(60_000L, interval)
    }

    @Test
    fun calculatePollingInterval_inProgress_normal_returns15s() {
        val interval = repository.calculatePollingIntervalMs(
            state = GameState.IN_PROGRESS,
            excitementIndex = 40,
            remainingSecondsRemaining = 600,
            period = 2
        )
        assertEquals(15_000L, interval)
    }

    @Test
    fun calculatePollingInterval_inProgress_highExcitement_returns3s() {
        val interval = repository.calculatePollingIntervalMs(
            state = GameState.IN_PROGRESS,
            excitementIndex = 80,
            remainingSecondsRemaining = 500,
            period = 2
        )
        assertEquals(3_000L, interval)
    }

    @Test
    fun calculatePollingInterval_inProgress_clutchRemainingTime_returns3s() {
        val interval = repository.calculatePollingIntervalMs(
            state = GameState.IN_PROGRESS,
            excitementIndex = 50,
            remainingSecondsRemaining = 90, // < 2 min
            period = 3 // 3rd period
        )
        assertEquals(3_000L, interval)
    }
}
