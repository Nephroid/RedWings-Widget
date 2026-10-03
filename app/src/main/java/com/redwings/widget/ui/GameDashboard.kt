package com.redwings.widget.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import com.redwings.widget.ui.dashboard.AppDarkBg
import com.redwings.widget.ui.dashboard.DualPaneDashboard
import com.redwings.widget.ui.dashboard.IceRinkBackground
import com.redwings.widget.ui.dashboard.SinglePaneDashboard
import com.redwings.widget.ui.dashboard.TabletopDashboard
import com.redwings.widget.ui.theme.AppJersey
import com.redwings.widget.ui.theme.LocalJerseyPalette
import com.redwings.widget.ui.theme.WingsRed

// Re-exports for backward-compatibility with tests & external callers

@Composable
fun NextGameHero(
    countdownProvider: () -> CountdownState,
    game: NextGameUi?,
    modifier: Modifier = Modifier
) = com.redwings.widget.ui.dashboard.NextGameHero(countdownProvider = countdownProvider, game = game, modifier = modifier)

@Composable
fun LastResultCard(
    lastGame: LastGameUi,
    modifier: Modifier = Modifier
) = com.redwings.widget.ui.dashboard.LastResultCard(lastGame = lastGame, modifier = modifier)

@Composable
fun StandingsCard(
    standings: List<StandingsRowUi>,
    playoffChaseText: String = "",
    summary: String = "",
    atlanticLine: String = "",
    modifier: Modifier = Modifier
) = com.redwings.widget.ui.dashboard.StandingsCard(
    standings = standings,
    playoffChaseText = playoffChaseText,
    summary = summary,
    atlanticLine = atlanticLine,
    modifier = modifier
)

@Composable
fun StandingsCard(summary: String, atlanticLine: String, modifier: Modifier = Modifier) =
    com.redwings.widget.ui.dashboard.StandingsCard(summary = summary, atlanticLine = atlanticLine, modifier = modifier)

@Composable
fun UpcomingList(games: List<NextGameUi>, modifier: Modifier = Modifier) =
    com.redwings.widget.ui.dashboard.UpcomingList(games = games, modifier = modifier)

@Composable
fun IceRinkBackground(modifier: Modifier = Modifier) =
    com.redwings.widget.ui.dashboard.IceRinkBackground(modifier = modifier)

@Composable
fun SinglePaneDashboard(
    scheduleState: ScheduleUiState,
    countdownProvider: () -> CountdownState,
    isRefreshing: Boolean,
    errorMessage: String?,
    onRefresh: () -> Unit,
    activeJersey: AppJersey = AppJersey.HOME,
    onJerseyThemeToggle: () -> Unit = {},
    modifier: Modifier = Modifier
) = com.redwings.widget.ui.dashboard.SinglePaneDashboard(
    scheduleState = scheduleState,
    countdownProvider = countdownProvider,
    isRefreshing = isRefreshing,
    errorMessage = errorMessage,
    onRefresh = onRefresh,
    activeJersey = activeJersey,
    onJerseyThemeToggle = onJerseyThemeToggle,
    modifier = modifier
)

@Composable
fun DualPaneDashboard(
    scheduleState: ScheduleUiState,
    countdownProvider: () -> CountdownState,
    isRefreshing: Boolean,
    errorMessage: String?,
    onRefresh: () -> Unit,
    activeJersey: AppJersey = AppJersey.HOME,
    onJerseyThemeToggle: () -> Unit = {},
    modifier: Modifier = Modifier
) = com.redwings.widget.ui.dashboard.DualPaneDashboard(
    scheduleState = scheduleState,
    countdownProvider = countdownProvider,
    isRefreshing = isRefreshing,
    errorMessage = errorMessage,
    onRefresh = onRefresh,
    activeJersey = activeJersey,
    onJerseyThemeToggle = onJerseyThemeToggle,
    modifier = modifier
)

@Composable
fun TabletopDashboard(
    scheduleState: ScheduleUiState,
    countdownProvider: () -> CountdownState,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    activeJersey: AppJersey = AppJersey.HOME,
    onJerseyThemeToggle: () -> Unit = {},
    modifier: Modifier = Modifier
) = com.redwings.widget.ui.dashboard.TabletopDashboard(
    scheduleState = scheduleState,
    countdownProvider = countdownProvider,
    isRefreshing = isRefreshing,
    onRefresh = onRefresh,
    activeJersey = activeJersey,
    onJerseyThemeToggle = onJerseyThemeToggle,
    modifier = modifier
)

/**
 * Entry point: collects ViewModel state and dispatches dynamically to either:
 * - SinglePaneDashboard (Pixel 9 Pro XL Portrait & Pixel 10 Fold Cover)
 * - DualPaneDashboard (Pixel 10 Fold Unfolded Inner Screen, Pixel 9 Pro XL Landscape, & ASUS Chromebook CM34 Flip)
 * - TabletopDashboard (Pixel 10 Fold & Chromebook CM34 in Tabletop / Stand posture)
 */
@Composable
fun GameDashboard(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
    activeJersey: AppJersey = AppJersey.HOME,
    onJerseyThemeToggle: () -> Unit = {},
    isTabletop: Boolean = false
) {
    val scheduleState by viewModel.scheduleState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    GameDashboardContent(
        scheduleState = scheduleState,
        countdownProvider = { viewModel.countdown.value },
        isRefreshing = isRefreshing,
        errorMessage = errorMessage,
        onRefresh = { viewModel.triggerManualRefresh() },
        activeJersey = activeJersey,
        onJerseyThemeToggle = onJerseyThemeToggle,
        isTabletop = isTabletop,
        modifier = modifier
    )
}

@Composable
fun GameDashboardContent(
    scheduleState: ScheduleUiState,
    countdownProvider: () -> CountdownState,
    isRefreshing: Boolean,
    errorMessage: String?,
    onRefresh: () -> Unit,
    activeJersey: AppJersey = AppJersey.HOME,
    onJerseyThemeToggle: () -> Unit = {},
    isTabletop: Boolean = false,
    modifier: Modifier = Modifier
) {
    val config = LocalConfiguration.current
    val screenWidth = config.screenWidthDp
    val isExpanded = screenWidth >= 600
    val palette = LocalJerseyPalette.current

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = palette.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(palette.background)
        ) {
            IceRinkBackground()

            when (scheduleState) {
                ScheduleUiState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = palette.accentRed)
                }

                ScheduleUiState.Empty, is ScheduleUiState.Data -> {
                    when {
                        isTabletop -> {
                            TabletopDashboard(
                                scheduleState = scheduleState,
                                countdownProvider = countdownProvider,
                                isRefreshing = isRefreshing,
                                onRefresh = onRefresh,
                                activeJersey = activeJersey,
                                onJerseyThemeToggle = onJerseyThemeToggle
                            )
                        }
                        isExpanded -> {
                            DualPaneDashboard(
                                scheduleState = scheduleState,
                                countdownProvider = countdownProvider,
                                isRefreshing = isRefreshing,
                                errorMessage = errorMessage,
                                onRefresh = onRefresh,
                                activeJersey = activeJersey,
                                onJerseyThemeToggle = onJerseyThemeToggle
                            )
                        }
                        else -> {
                            SinglePaneDashboard(
                                scheduleState = scheduleState,
                                countdownProvider = countdownProvider,
                                isRefreshing = isRefreshing,
                                errorMessage = errorMessage,
                                onRefresh = onRefresh,
                                activeJersey = activeJersey,
                                onJerseyThemeToggle = onJerseyThemeToggle
                            )
                        }
                    }
                }
            }
        }
    }
}
