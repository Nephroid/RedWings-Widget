package com.redwings.widget.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.redwings.widget.ui.CountdownState
import com.redwings.widget.ui.LastGameUi
import com.redwings.widget.ui.NextGameUi
import com.redwings.widget.ui.ScheduleUiState
import com.redwings.widget.ui.theme.AppJersey
import com.redwings.widget.ui.theme.LocalJerseyPalette
import com.redwings.widget.ui.theme.WingsRed

/**
 * High-density single-column layout for Google Pixel 9 Pro XL (Portrait)
 * and Pixel 10 Fold (Cover Screen).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SinglePaneDashboard(
    scheduleState: ScheduleUiState,
    countdown: CountdownState,
    isRefreshing: Boolean,
    errorMessage: String?,
    onRefresh: () -> Unit,
    activeJersey: AppJersey = AppJersey.HOME,
    onJerseyThemeToggle: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val pullRefreshState = rememberPullToRefreshState()
    val data = scheduleState as? ScheduleUiState.Data
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

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Banner
            item {
                AppHeaderBanner(
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh,
                    subtitle = data?.standingsSummary?.ifBlank { "4th in Atlantic • 94 pts" },
                    activeJersey = activeJersey,
                    onJerseyThemeToggle = onJerseyThemeToggle
                )
            }

            // Error banner if any
            errorMessage?.let { msg ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = msg,
                            modifier = Modifier.padding(14.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // 1. Next Game Hero Card
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    NextGameHero(
                        countdown = countdown,
                        game = data?.nextGame,
                        tileWidth = 68.dp,
                        showFlankingBadges = false
                    )
                }
            }

            // 2. Last Result Bento Card
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    LastResultCard(
                        lastGame = data?.lastGame ?: LastGameUi(
                            opponent = "Boston Bruins",
                            opponentAbbrev = "BOS",
                            wingsScore = 4,
                            oppScore = 2,
                            isWinner = true,
                            isHome = true,
                            dateLabel = "Tue, Sep 30"
                        ),
                        showFormGuide = true
                    )
                }
            }

            // 3. Upcoming Schedule Section
            val upcomingList = data?.upcoming?.takeIf { it.isNotEmpty() } ?: listOf(
                NextGameUi("Toronto Maple Leafs", "TOR", "Little Caesars Arena", System.currentTimeMillis() + 86400000L * 2, true),
                NextGameUi("Montreal Canadiens", "MTL", "Bell Centre", System.currentTimeMillis() + 86400000L * 4, false),
                NextGameUi("Boston Bruins", "BOS", "Little Caesars Arena", System.currentTimeMillis() + 86400000L * 6, true)
            )

            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    UpcomingSection(games = upcomingList, isExpanded = false)
                }
            }

            // 4. Atlantic Division Standings Card
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    StandingsCard(
                        standings = data?.standings.orEmpty(),
                        playoffChaseText = data?.playoffChaseText ?: "CLINCHED PLAYOFF SPOT",
                        summary = data?.standingsSummary ?: "4th in Atlantic • 94 pts",
                        atlanticLine = data?.atlanticLine.orEmpty(),
                        showExtendedStats = false
                    )
                }
            }

            // 5. App Footer
            item {
                AppFooter()
            }
        }
    }
}
