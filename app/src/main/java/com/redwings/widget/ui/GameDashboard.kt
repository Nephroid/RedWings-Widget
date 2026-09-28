package com.redwings.widget.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.redwings.widget.R
import com.redwings.widget.data.model.getTeamLogoUrlFallback
import com.redwings.widget.ui.theme.LivePulseGreen
import com.redwings.widget.ui.theme.RedWingsTheme
import com.redwings.widget.ui.theme.WingsDeepRed
import com.redwings.widget.ui.theme.WingsGold
import com.redwings.widget.ui.theme.WingsLossRed
import com.redwings.widget.ui.theme.WingsRed
import com.redwings.widget.ui.theme.WingsWhite
import com.redwings.widget.ui.theme.WingsWinGreen
import com.redwings.widget.widget.StandingsFormatter
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
    val pullRefreshState = rememberPullToRefreshState()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "RED WINGS",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = WingsRed,
                    titleContentColor = WingsWhite,
                    actionIconContentColor = WingsWhite
                ),
                actions = {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .size(22.dp),
                            strokeWidth = 2.dp,
                            color = WingsWhite
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Subtle ice-rink background geometry
            IceBackground()

            when (scheduleState) {
                ScheduleUiState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = WingsRed) }

                ScheduleUiState.Empty -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No games scheduled", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(12.dp))
                        if (isRefreshing) CircularProgressIndicator(color = WingsRed)
                    }
                }

                is ScheduleUiState.Data -> {
                    PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = onRefresh,
                        state = pullRefreshState,
                        indicator = {
                            PullToRefreshDefaults.Indicator(
                                state = pullRefreshState,
                                isRefreshing = isRefreshing,
                                containerColor = WingsRed,
                                color = WingsWhite,
                                modifier = Modifier.align(Alignment.TopCenter)
                            )
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            errorMessage?.let {
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.errorContainer
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = it,
                                            modifier = Modifier.padding(14.dp),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                            item { NextGameHero(countdown = countdown, game = scheduleState.nextGame) }
                            scheduleState.lastGame?.let { item { LastResultCard(lastGame = it) } }
                            item {
                                Text(
                                    text = "Upcoming (${scheduleState.upcoming.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                                )
                            }
                            itemsIndexed(
                                scheduleState.upcoming,
                                key = { _, it -> it.startTimeMillis to it.opponent }
                            ) { index, game ->
                                UpcomingRow(game = game, index = index)
                            }
                            item {
                                StandingsCard(
                                    standings = scheduleState.standings,
                                    playoffChaseText = scheduleState.playoffChaseText,
                                    summary = scheduleState.standingsSummary,
                                    atlanticLine = scheduleState.atlanticLine
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Subtle ice-rink background geometry (center circle & center line). */
@Composable
fun IceBackground(modifier: Modifier = Modifier) {
    val strokeColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)
    Canvas(modifier = modifier.fillMaxSize()) {
        val centerX = size.width / 2f
        val centerY = size.height * 0.45f
        val radius = size.width * 0.40f

        // Center line
        drawLine(
            color = strokeColor,
            start = Offset(0f, centerY),
            end = Offset(size.width, centerY),
            strokeWidth = 2.dp.toPx()
        )
        // Center ice circle
        drawCircle(
            color = strokeColor,
            radius = radius,
            center = Offset(centerX, centerY),
            style = Stroke(width = 2.dp.toPx())
        )
        // Center ice dot
        drawCircle(
            color = strokeColor,
            radius = 6.dp.toPx(),
            center = Offset(centerX, centerY)
        )
    }
}

/** Countdown hero for the next game with gradient backdrop, logos, and flip-clock digit boxes. */
@Composable
fun NextGameHero(countdown: CountdownState, game: NextGameUi?, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(WingsRed, WingsDeepRed)
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Pill banner
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.22f)
                ) {
                    Text(
                        text = if (countdown.isLive) "● LIVE NOW" else "NEXT GAME",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (countdown.isLive) LivePulseGreen else WingsWhite.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                // Matchup with opponent logo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    val oppAbbr = game?.opponentAbbrev ?: "OPP"
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(getTeamLogoUrlFallback(oppAbbr))
                            .crossfade(true)
                            .build(),
                        placeholder = painterResource(R.drawable.ic_puck_vector),
                        error = painterResource(R.drawable.ic_puck_vector),
                        contentDescription = "${game?.opponent ?: "Opponent"} logo",
                        modifier = Modifier
                            .size(36.dp)
                            .padding(end = 8.dp)
                    )

                    Text(
                        text = game?.let {
                            if (it.isHome) "vs ${it.opponent}" else "at ${it.opponent}"
                        } ?: "TBD",
                        style = MaterialTheme.typography.headlineSmall,
                        color = WingsWhite,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                }

                // Venue & game time
                game?.let {
                    Text(
                        text = "${it.venue} • ${formatGameTime(it.startTimeMillis)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = WingsWhite.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(4.dp))

                // Flip-clock countdown digit boxes or Live badge
                if (countdown.isLive) {
                    LivePuckDropBanner()
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CountdownDigitBox(value = countdown.days, unit = "D")
                        CountdownDigitBox(value = countdown.hours, unit = "H")
                        CountdownDigitBox(value = countdown.minutes, unit = "M")
                        CountdownDigitBox(value = countdown.seconds, unit = "S")
                    }
                }
            }
        }
    }
}

/** Single digit container inside the flip-clock style hero countdown. */
@Composable
fun CountdownDigitBox(value: Long, unit: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.Black.copy(alpha = 0.28f),
        modifier = Modifier.width(62.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = String.format("%02d", value.coerceAtLeast(0L)),
                style = MaterialTheme.typography.displayMedium.copy(fontFamily = FontFamily.Monospace),
                color = WingsWhite,
                fontWeight = FontWeight.Black,
                fontSize = 30.sp,
                lineHeight = 34.sp
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.labelSmall,
                color = WingsWhite.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/** Animated pulsing banner when game is in progress. */
@Composable
private fun LivePuckDropBanner() {
    val transition = rememberInfiniteTransition(label = "pulse")
    val alpha by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.Black.copy(alpha = 0.35f),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(LivePulseGreen.copy(alpha = alpha))
            )
            Text(
                text = "PUCK DROP • GAME LIVE",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = WingsWhite
            )
        }
    }
}

/** Final-result card for the last game with prominent W/L badge and logos. */
@Composable
fun LastResultCard(lastGame: LastGameUi, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Opponent team logo
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(getTeamLogoUrlFallback(lastGame.opponentAbbrev))
                        .crossfade(true)
                        .build(),
                    placeholder = painterResource(R.drawable.ic_puck_vector),
                    error = painterResource(R.drawable.ic_puck_vector),
                    contentDescription = "${lastGame.opponent} logo",
                    modifier = Modifier
                        .size(42.dp)
                        .padding(end = 12.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "LAST FINAL • ${lastGame.dateLabel}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "DET ${lastGame.wingsScore} – ${lastGame.oppScore} ${lastGame.opponent}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (lastGame.isHome) "Home • Little Caesars Arena" else "Away",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // Prominent W / L Square Badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (lastGame.isWinner) WingsWinGreen else WingsLossRed,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (lastGame.isWinner) "W" else "L",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = WingsWhite
                    )
                }
            }
        }
    }
}

/** Reusable lazy-column upcoming list (up to 7 rows). */
@Composable
fun UpcomingList(games: List<NextGameUi>, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        itemsIndexed(games.take(7), key = { _, it -> it.startTimeMillis to it.opponent }) { index, game ->
            UpcomingRow(game = game, index = index)
        }
    }
}

/** Single upcoming row: opponent logo + date + vs/at + home/away pill. */
@Composable
fun UpcomingRow(game: NextGameUi, modifier: Modifier = Modifier, index: Int = 0) {
    val bgTint = if (index % 2 == 0) {
        MaterialTheme.colorScheme.surfaceContainerHigh
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bgTint)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(getTeamLogoUrlFallback(game.opponentAbbrev))
                        .crossfade(true)
                        .build(),
                    placeholder = painterResource(R.drawable.ic_puck_vector),
                    error = painterResource(R.drawable.ic_puck_vector),
                    contentDescription = "${game.opponent} logo",
                    modifier = Modifier
                        .size(32.dp)
                        .padding(end = 12.dp)
                )

                Column {
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
            }

            Spacer(Modifier.width(8.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (game.isHome) WingsRed.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Text(
                    text = if (game.isHome) "HOME" else "AWAY",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (game.isHome) WingsRed else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** Full Atlantic Division Standings Table with Detroit row highlighted and playoff chase status. */
@Composable
fun StandingsCard(
    standings: List<StandingsRowUi>,
    playoffChaseText: String = "",
    summary: String = "",
    atlanticLine: String = "",
    modifier: Modifier = Modifier
) {
    val rows = if (standings.isNotEmpty()) {
        standings
    } else {
        StandingsFormatter.parseAtlanticRowsForUi(atlanticLine)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ATLANTIC DIVISION",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                if (summary.isNotBlank()) {
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Table Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("#", modifier = Modifier.width(22.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Team", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("GP", modifier = Modifier.width(32.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("W", modifier = Modifier.width(32.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("L", modifier = Modifier.width(32.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("OT", modifier = Modifier.width(32.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("PTS", modifier = Modifier.width(36.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 1.dp
            )

            // Table Data Rows
            rows.forEach { row ->
                val isDet = row.isRedWings || row.teamAbbrev.equals("DET", ignoreCase = true)
                val bg = if (isDet) WingsRed.copy(alpha = 0.12f) else Color.Transparent

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(bg)
                        .padding(vertical = 5.dp, horizontal = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${row.rank}.",
                        modifier = Modifier.width(22.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isDet) FontWeight.Bold else FontWeight.Normal,
                        color = if (isDet) WingsRed else MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(getTeamLogoUrlFallback(row.teamAbbrev))
                                .crossfade(true)
                                .build(),
                            placeholder = painterResource(R.drawable.ic_puck_vector),
                            error = painterResource(R.drawable.ic_puck_vector),
                            contentDescription = "${row.teamAbbrev} logo",
                            modifier = Modifier
                                .size(20.dp)
                                .padding(end = 6.dp)
                        )
                        Text(
                            text = row.teamAbbrev,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isDet) FontWeight.Black else FontWeight.SemiBold,
                            color = if (isDet) WingsRed else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text("${row.gamesPlayed}", modifier = Modifier.width(32.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.bodyMedium)
                    Text("${row.wins}", modifier = Modifier.width(32.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.bodyMedium)
                    Text("${row.losses}", modifier = Modifier.width(32.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.bodyMedium)
                    Text("${row.otLosses}", modifier = Modifier.width(32.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = "${row.points}",
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isDet) WingsRed else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Bottom Playoff Chase status
            val chase = playoffChaseText.ifBlank { "In playoff hunt" }
            val isClinched = chase.contains("CLINCHED", ignoreCase = true)
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                thickness = 1.dp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isClinched) "★ CLINCHED PLAYOFF BERTH" else "EASTERN WILDCARD: $chase".uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isClinched) WingsWinGreen else WingsGold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

/** Backward-compatible overload for existing previews and tests. */
@Composable
fun StandingsCard(summary: String, atlanticLine: String, modifier: Modifier = Modifier) {
    StandingsCard(
        standings = emptyList(),
        playoffChaseText = "",
        summary = summary,
        atlanticLine = atlanticLine,
        modifier = modifier
    )
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
            game = NextGameUi(opponent = "Toronto Maple Leafs", opponentAbbrev = "TOR", venue = "Little Caesars Arena", isHome = true)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewLastResultCard() {
    RedWingsTheme {
        LastResultCard(
            LastGameUi(opponent = "Boston Bruins", opponentAbbrev = "BOS", wingsScore = 4, oppScore = 2, isWinner = true, isHome = true, dateLabel = "Tue, Sep 22")
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewUpcomingList() {
    RedWingsTheme {
        UpcomingList(
            games = listOf(
                NextGameUi("Toronto Maple Leafs", "TOR", "Little Caesars Arena", 1L, true),
                NextGameUi("Boston Bruins", "BOS", "TD Garden", 2L, false),
                NextGameUi("Tampa Bay Lightning", "TBL", "Little Caesars Arena", 3L, true)
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewStandingsCard() {
    RedWingsTheme {
        StandingsCard(
            standings = listOf(
                StandingsRowUi(1, "BOS", 82, 51, 20, 11, 113),
                StandingsRowUi(2, "TOR", 82, 46, 26, 10, 102),
                StandingsRowUi(3, "FLA", 82, 45, 27, 10, 100),
                StandingsRowUi(4, "DET", 82, 42, 30, 10, 94, isRedWings = true),
                StandingsRowUi(5, "TBL", 82, 40, 32, 10, 90),
                StandingsRowUi(6, "MTL", 82, 37, 36, 9, 83),
                StandingsRowUi(7, "OTT", 82, 34, 39, 9, 77),
                StandingsRowUi(8, "BUF", 82, 30, 43, 9, 69)
            ),
            playoffChaseText = "CLINCHED PLAYOFF SPOT",
            summary = "4th in Atlantic • 94 pts"
        )
    }
}
