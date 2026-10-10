package com.redwings.widget.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.redwings.widget.ui.CountdownState
import com.redwings.widget.ui.LastGameUi
import com.redwings.widget.ui.NextGameUi
import com.redwings.widget.ui.ScheduleUiState
import com.redwings.widget.ui.theme.AppJersey
import com.redwings.widget.ui.theme.LocalJerseyPalette

/**
 * 50/50 Dual-Pane Command Center for Google Pixel 10 Fold (Unfolded Inner Screen),
 * Google Pixel 9 Pro XL (Landscape Mode), and ASUS Chromebook CM34 Flip.
 *
 * Left Pane: Game Day Pulse (Hero Card + Last Result + AppFooter)
 * Right Pane: League Intelligence (Atlantic Division Standings + Upcoming Carousel)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DualPaneDashboard(
    scheduleState: ScheduleUiState,
    countdownProvider: () -> CountdownState,
    isRefreshing: Boolean,
    errorMessage: String?,
    onRefresh: () -> Unit,
    activeJersey: AppJersey = AppJersey.HOME,
    onJerseyThemeToggle: () -> Unit = {},
    onLightTheLamp: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val pullRefreshState = rememberPullToRefreshState()
    val data = scheduleState as? ScheduleUiState.Data
    val scrollStateLeft = rememberScrollState()
    val scrollStateRight = rememberScrollState()
    val palette = LocalJerseyPalette.current

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        state = pullRefreshState,
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = pullRefreshState,
                isRefreshing = isRefreshing,
                containerColor = palette.accentRed,
                color = palette.headerText,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        },
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
    ) {
        IceRinkBackground()

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            AppHeaderBanner(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                subtitle = "NHL COMMAND CENTER • ${data?.standingsSummary ?: "4th in Atlantic"}",
                activeJersey = activeJersey,
                onJerseyThemeToggle = onJerseyThemeToggle
            )

            errorMessage?.let { msg ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = msg,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.TopCenter
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 1400.dp)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                // LEFT PANE: GAME DAY PULSE (~50% width)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .verticalScroll(scrollStateLeft),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    NextGameHero(
                        countdownProvider = countdownProvider,
                        game = data?.nextGame,
                        tileWidth = 72.dp,
                        showFlankingBadges = true
                    )

                    com.redwings.widget.ui.components.LightTheLampBanner(
                        pulseCount = data?.fanPulseCount ?: 1926L,
                        onLightTheLamp = onLightTheLamp
                    )

                    LastResultCard(
                        lastGame = data?.lastGame ?: LastGameUi(
                            opponent = "Boston Bruins",
                            opponentAbbrev = "BOS",
                            wingsScore = 4,
                            oppScore = 2,
                            isWinner = true,
                            isHome = true,
                            dateLabel = "Tue, Sep 30",
                            threeStars = com.redwings.widget.data.model.defaultThreeStars()
                        ),
                        showFormGuide = true
                    )

                    DrwSeasonLeadersCard(
                        leaders = data?.teamLeaders ?: com.redwings.widget.data.model.TeamLeadersUi(),
                        pacing = data?.milestonePacing ?: emptyMap()
                    )

                    Spacer(Modifier.height(8.dp))
                    AppFooter()
                }

                // RIGHT PANE: LEAGUE & SCHEDULE (~50% width)
                Column(
                    modifier = Modifier
                        .weight(1.05f)
                        .fillMaxSize()
                        .verticalScroll(scrollStateRight),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    StandingsCard(
                        standings = data?.standings.orEmpty(),
                        playoffChaseText = data?.playoffChaseText ?: "CLINCHED PLAYOFF SPOT",
                        summary = data?.standingsSummary ?: "4th in Atlantic • 94 pts",
                        atlanticLine = data?.atlanticLine.orEmpty(),
                        showExtendedStats = true
                    )

                    val upcomingList = data?.upcoming?.takeIf { it.isNotEmpty() } ?: listOf(
                        NextGameUi("Toronto Maple Leafs", "TOR", "Little Caesars Arena", System.currentTimeMillis() + 86400000L * 2, true),
                        NextGameUi("Montreal Canadiens", "MTL", "Bell Centre", System.currentTimeMillis() + 86400000L * 4, false),
                        NextGameUi("Boston Bruins", "BOS", "Little Caesars Arena", System.currentTimeMillis() + 86400000L * 6, true)
                    )

                    UpcomingSection(games = upcomingList, isExpanded = true)
                }
            }
        }
    }
}
}
