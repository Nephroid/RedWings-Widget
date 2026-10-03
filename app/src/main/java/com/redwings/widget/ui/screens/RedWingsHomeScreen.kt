package com.redwings.widget.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.redwings.widget.data.model.getTeamLogoUrl
import com.redwings.widget.ui.GameViewModel
import com.redwings.widget.ui.ScheduleUiState
import com.redwings.widget.ui.components.AnchoredArenaCapsule
import com.redwings.widget.ui.components.CountdownClock
import com.redwings.widget.ui.components.FrostedGlassCard
import com.redwings.widget.ui.components.GlowingSeedBadge
import com.redwings.widget.ui.theme.FrostedIceColors
import com.redwings.widget.ui.theme.FrostedIceTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RedWingsHomeScreen(
    viewModel: GameViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.scheduleState.collectAsState()
    val countdown by viewModel.countdown.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val pullRefreshState = rememberPullToRefreshState()

    FrostedIceTheme {
        Scaffold(
            containerColor = FrostedIceColors.IceBackground,
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.triggerManualRefresh() },
                state = pullRefreshState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    // 1. Red Wings Header with Jersey Dual-Stripes & PILS Badge
                    item {
                        RedWingsHeaderBar(
                            isRefreshing = isRefreshing,
                            onRefresh = { viewModel.triggerManualRefresh() }
                        )
                    }

                    // Extract data state
                    val data = (uiState as? ScheduleUiState.Data)

                    // 2. Primary Matchup Card with Countdown & Overlapping Arena Capsule
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            PrimaryMatchupCard(
                                remainingSecondsProvider = {
                                    val days = countdown.days.toLong()
                                    val hours = countdown.hours.toLong()
                                    val mins = countdown.minutes.toLong()
                                    val secs = countdown.seconds.toLong()
                                    days * 86400L + hours * 3600L + mins * 60L + secs
                                },
                                awayTeam = "TOR",
                                awayRecord = "46-26-10",
                                homeTeam = "DET",
                                homeRecord = "42-30-10",
                                timeLabel = "Sat, Oct 4 • 7:00 PM"
                            )
                        }
                    }

                    // 3. Last Result Card (DET 4 - 2 BOS [W])
                    item {
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            LastResultCard(
                                wingsScore = data?.lastGame?.wingsScore ?: 4,
                                oppScore = data?.lastGame?.oppScore ?: 2,
                                oppAbbrev = data?.lastGame?.opponentAbbrev ?: "BOS",
                                dateLabel = data?.lastGame?.dateLabel ?: "Tue, Sep 30"
                            )
                        }
                    }

                    // 4. Upcoming Games Pager with Dot Indicators
                    item {
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            UpcomingGamesPagerCard()
                        }
                    }

                    // 5. Atlantic Division Standings Card
                    item {
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            AtlanticStandingsCard()
                        }
                    }

                    // 6. Version Info
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Detroit Red Wings v${com.redwings.widget.BuildConfig.VERSION_NAME}",
                                style = MaterialTheme.typography.labelSmall,
                                color = FrostedIceColors.TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Header Bar: Solid RedWingsRed card with white jersey dual-stripes, centered wordmark, and PILS badge.
 */
@Composable
fun RedWingsHeaderBar(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(FrostedIceColors.RedWingsRed)
    ) {
        // Top Jersey Stripe
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(Color.White)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(FrostedIceColors.RedWingsRed)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(Color.White)
        )

        // Center Content
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Rounded PILS Badge
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.20f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.40f))
            ) {
                Text(
                    text = "PILS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            // Red Wings Wordmark & Crest
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = getTeamLogoUrl("DET"),
                    contentDescription = "Red Wings Logo",
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "RED WINGS",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 3.sp,
                        color = Color.White
                    )
                )
            }

            // Refresh action
            if (isRefreshing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                IconButton(onClick = onRefresh, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = Color.White
                    )
                }
            }
        }

        // Bottom Jersey Stripe
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(Color.White)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(FrostedIceColors.RedWingsRed)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(Color.White)
        )
    }
}

/**
 * Main Match Card (TOR @ DET) with countdown timer and overlapping 50% "Little Caesars Arena" capsule.
 */
@Composable
fun PrimaryMatchupCard(
    remainingSecondsProvider: () -> Long,
    awayTeam: String,
    awayRecord: String,
    homeTeam: String,
    homeRecord: String,
    timeLabel: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp), // Extra room for capsule overlap
        contentAlignment = Alignment.BottomCenter
    ) {
        FrostedGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = timeLabel,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = FrostedIceColors.TextMuted
                )
                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Away Team
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        AsyncImage(
                            model = getTeamLogoUrl(awayTeam),
                            contentDescription = awayTeam,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = awayTeam,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = FrostedIceColors.TextPrimary
                        )
                        Text(
                            text = awayRecord,
                            style = MaterialTheme.typography.labelSmall,
                            color = FrostedIceColors.TextMuted
                        )
                    }

                    // Central Red @ Badge
                    Surface(
                        shape = CircleShape,
                        color = FrostedIceColors.RedWingsRed,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "@",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }

                    // Home Team (Detroit)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        AsyncImage(
                            model = getTeamLogoUrl(homeTeam),
                            contentDescription = homeTeam,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = homeTeam,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = FrostedIceColors.TextPrimary
                        )
                        Text(
                            text = homeRecord,
                            style = MaterialTheme.typography.labelSmall,
                            color = FrostedIceColors.TextMuted
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Countdown Monospace Clock
                CountdownClock(
                    remainingSecondsProvider = remainingSecondsProvider
                )
            }
        }

        // Anchored Arena Capsule overlapping bottom border by 50%
        AnchoredArenaCapsule(
            arenaName = "Little Caesars Arena",
            modifier = Modifier.offset(y = 0.dp)
        )
    }
}

/**
 * Last Result Card: DET 4 - 2 BOS with green 'W' badge.
 */
@Composable
fun LastResultCard(
    wingsScore: Int,
    oppScore: Int,
    oppAbbrev: String,
    dateLabel: String,
    modifier: Modifier = Modifier
) {
    FrostedGlassCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "LAST RESULT • $dateLabel",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = FrostedIceColors.TextMuted
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "DET $wingsScore – $oppScore $oppAbbrev",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = FrostedIceColors.TextPrimary
                    )
                }
                Text(
                    text = "Home • Final",
                    style = MaterialTheme.typography.labelSmall,
                    color = FrostedIceColors.TextMuted
                )
            }

            // Green Win Badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = FrostedIceColors.WinGreen,
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "W",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    )
                }
            }
        }
    }
}

/**
 * Upcoming games card with horizontal pager and indicator dots.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UpcomingGamesPagerCard(modifier: Modifier = Modifier) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val games = listOf(
        Triple("MTL", "at Montreal", "Tue, Oct 7 • 7:00 PM"),
        Triple("BOS", "vs Boston", "Thu, Oct 9 • 7:30 PM"),
        Triple("FLA", "at Florida", "Sat, Oct 11 • 7:00 PM")
    )

    FrostedGlassCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "UPCOMING GAMES",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = FrostedIceColors.TextMuted
            )
            Spacer(Modifier.height(10.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                val game = games[page]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = getTeamLogoUrl(game.first),
                        contentDescription = game.first,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = game.second,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = FrostedIceColors.TextPrimary
                        )
                        Text(
                            text = game.third,
                            style = MaterialTheme.typography.labelSmall,
                            color = FrostedIceColors.TextMuted
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Indicator Dots
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(3) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (isSelected) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) FrostedIceColors.RedWingsRed else FrostedIceColors.TextMuted.copy(alpha = 0.35f)
                            )
                    )
                }
            }
        }
    }
}

/**
 * Atlantic Division Standings Card split into "PLAYOFF SEEDS (1-4)" and "IN THE HUNT (5-8)".
 */
@Composable
fun AtlanticStandingsCard(modifier: Modifier = Modifier) {
    val playoffSeeds = listOf(
        Triple("1", "BOS", false),
        Triple("2", "TOR", false),
        Triple("3", "FLA", false),
        Triple("4", "DET", true)
    )
    val inTheHunt = listOf(
        Triple("5", "TBL", false),
        Triple("6", "MTL", false),
        Triple("7", "OTT", false),
        Triple("8", "BUF", false)
    )

    FrostedGlassCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "ATLANTIC DIVISION STANDINGS",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = FrostedIceColors.TextMuted
            )
            Spacer(Modifier.height(12.dp))

            // Section 1: Playoff Seeds (1-4)
            Text(
                text = "PLAYOFF SEEDS (1-4)",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = FrostedIceColors.TextPrimary
            )
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                playoffSeeds.forEach { (seed, team, active) ->
                    GlowingSeedBadge(seed = seed, team = team, isActive = active)
                }
            }

            Spacer(Modifier.height(14.dp))

            // Section 2: In The Hunt (5-8)
            Text(
                text = "IN THE HUNT (5-8)",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = FrostedIceColors.TextPrimary
            )
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                inTheHunt.forEach { (seed, team, active) ->
                    GlowingSeedBadge(seed = seed, team = team, isActive = active)
                }
            }
        }
    }
}

// =================== PREVIEWS ===================

@Preview(showBackground = true)
@Composable
private fun PreviewRedWingsHomeScreen() {
    FrostedIceTheme {
        Column {
            RedWingsHeaderBar(isRefreshing = false, onRefresh = {})
            Spacer(Modifier.height(16.dp))
            PrimaryMatchupCard(
                remainingSecondsProvider = { 7200L },
                awayTeam = "TOR",
                awayRecord = "46-26-10",
                homeTeam = "DET",
                homeRecord = "42-30-10",
                timeLabel = "Sat, Oct 4 • 7:00 PM",
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(16.dp))
            LastResultCard(
                wingsScore = 4,
                oppScore = 2,
                oppAbbrev = "BOS",
                dateLabel = "Tue, Sep 30",
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}
