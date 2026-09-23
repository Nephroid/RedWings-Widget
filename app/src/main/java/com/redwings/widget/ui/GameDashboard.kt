package com.redwings.widget.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.redwings.widget.ui.theme.RedWingsTheme
import com.redwings.widget.ui.theme.WingsLossRed
import com.redwings.widget.ui.theme.WingsRed
import com.redwings.widget.ui.theme.WingsWinGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Entry point: collects ViewModel state and renders stateless sections. */
@Composable
fun GameDashboard(viewModel: GameViewModel, modifier: Modifier = Modifier) {
    val scheduleState by viewModel.scheduleState.collectAsState()
    val countdown by viewModel.countdown.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    GameDashboardContent(
        scheduleState = scheduleState,
        countdown = countdown,
        isRefreshing = isRefreshing,
        errorMessage = errorMessage,
        onRefresh = { viewModel.triggerManualRefresh() },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDashboardContent(
    scheduleState: ScheduleUiState,
    countdown: CountdownState,
    isRefreshing: Boolean,
    errorMessage: String?,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        topBar = {
            TopAppBar(
                title = { Text("Red Wings Schedule", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = WingsRed,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .size(22.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        IconButton(
                            onClick = onRefresh,
                            modifier = Modifier.testTag("refresh_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh schedule")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        when (scheduleState) {
            ScheduleUiState.Loading -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            ScheduleUiState.Empty -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No games scheduled", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(12.dp))
                    if (isRefreshing) CircularProgressIndicator()
                }
            }

            is ScheduleUiState.Data -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                errorMessage?.let {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = it,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                item { NextGameHero(countdown = countdown, game = scheduleState.nextGame) }
                scheduleState.lastGame?.let { item { LastResultCard(lastGame = it) } }
                item {
                    Text(
                        "Upcoming (7)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(scheduleState.upcoming, key = { it.startTimeMillis to it.opponent }) { game ->
                    UpcomingRow(game = game)
                }
                item {
                    StandingsCard(
                        summary = scheduleState.standingsSummary,
                        atlanticLine = scheduleState.atlanticLine
                    )
                }
            }
        }
    }
}

/** Countdown hero for the next game. */
@Composable
fun NextGameHero(countdown: CountdownState, game: NextGameUi?, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = WingsRed)
    ) {
        Column(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = if (countdown.isLive) "LIVE NOW" else "NEXT GAME",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = game?.let {
                    if (it.isHome) "vs ${it.opponent}" else "at ${it.opponent}"
                } ?: "TBD",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            game?.let {
                Text(
                    text = "${it.venue} • ${formatGameTime(it.startTimeMillis)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = countdown.text,
                style = MaterialTheme.typography.displaySmall.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Black
            )
        }
    }
}

/** Final-result card for the last game. */
@Composable
fun LastResultCard(lastGame: LastGameUi, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "LAST FINAL • ${lastGame.dateLabel}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "DET ${lastGame.wingsScore} – ${lastGame.oppScore} ${lastGame.opponent}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (lastGame.isHome) "Home" else "Away",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AssistChip(
                onClick = {},
                label = { Text(if (lastGame.isWinner) "W" else "L") },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (lastGame.isWinner) WingsWinGreen else WingsLossRed,
                    labelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

/** Reusable lazy-column upcoming list (up to 7 rows). */
@Composable
fun UpcomingList(games: List<NextGameUi>, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(games.take(7), key = { it.startTimeMillis to it.opponent }) { game ->
            UpcomingRow(game = game)
        }
    }
}

/** Single upcoming row: date + vs/at + home/away pill. */
@Composable
fun UpcomingRow(game: NextGameUi, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (game.isHome) "vs ${game.opponent}" else "at ${game.opponent}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = formatGameTime(game.startTimeMillis),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(8.dp))
            AssistChip(
                onClick = {},
                label = { Text(if (game.isHome) "HOME" else "AWAY") }
            )
        }
    }
}

/** Atlantic standings summary card. */
@Composable
fun StandingsCard(summary: String, atlanticLine: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                "Atlantic Standings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(summary, style = MaterialTheme.typography.bodyMedium)
            Text(
                atlanticLine,
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
            )
        }
    }
}

@Composable
private fun formatGameTime(millis: Long): String {
    if (millis <= 0L) return "TBD"
    return remember(millis) {
        SimpleDateFormat("EEE, MMM d • h:mm a", Locale.US).format(Date(millis))
    }
}

// ---- Previews ----

@Preview(showBackground = true)
@Composable
private fun PreviewNextGameHero() {
    RedWingsTheme {
        NextGameHero(
            countdown = CountdownState(days = 2, hours = 4, minutes = 12, seconds = 30, text = "2d 04h 12m 30s"),
            game = NextGameUi(opponent = "Toronto Maple Leafs", venue = "Little Caesars Arena", isHome = true)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewLastResultCard() {
    RedWingsTheme {
        LastResultCard(
            LastGameUi(opponent = "BOS", wingsScore = 4, oppScore = 2, isWinner = true, isHome = true, dateLabel = "Tue, Sep 22")
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewUpcomingList() {
    RedWingsTheme {
        UpcomingList(
            games = listOf(
                NextGameUi("Toronto Maple Leafs", "Little Caesars Arena", 1L, true),
                NextGameUi("Boston Bruins", "TD Garden", 2L, false),
                NextGameUi("Tampa Bay Lightning", "Little Caesars Arena", 3L, true)
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewStandingsCard() {
    RedWingsTheme {
        StandingsCard(summary = "3rd in Atlantic • 6th in East", atlanticLine = "DET 42-30-10 • 94 pts")
    }
}
