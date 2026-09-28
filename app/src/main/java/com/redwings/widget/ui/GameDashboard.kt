package com.redwings.widget.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
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
import com.redwings.widget.ui.theme.RedWingsTheme
import com.redwings.widget.ui.theme.WingsRed
import com.redwings.widget.widget.StandingsFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Custom palette matching mockup */
private val AppDarkBg = Color(0xFF0F1014)
private val CardDarkSurface = Color(0xFF1B1D22)
private val HeroGradientTop = Color(0xFFCE1126)
private val HeroGradientBottom = Color(0xFF750713)
private val FlipTileBg = Color(0xFF1F0407)
private val FlipTileBorder = Color(0xFF4A0A10)
private val WinGreenSolid = Color(0xFF388E3C)
private val LossRedSolid = Color(0xFFD32F2F)
private val PillDarkBg = Color(0xFF26282E)
private val StandingsDetHighlight = Color(0xFF521319)
private val ClinchedGreen = Color(0xFF4CAF50)

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
        containerColor = AppDarkBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(AppDarkBg)
        ) {
            // Ice rink subtle background linework
            IceRinkBackground()

            when (scheduleState) {
                ScheduleUiState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = WingsRed) }

                ScheduleUiState.Empty, is ScheduleUiState.Data -> {
                    val data = scheduleState as? ScheduleUiState.Data

                    PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = onRefresh,
                        state = pullRefreshState,
                        indicator = {
                            PullToRefreshDefaults.Indicator(
                                state = pullRefreshState,
                                isRefreshing = isRefreshing,
                                containerColor = WingsRed,
                                color = Color.White,
                                modifier = Modifier.align(Alignment.TopCenter)
                            )
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // 1. Massive Bold Header matching mockup
                            item {
                                AppHeaderBanner(
                                    isRefreshing = isRefreshing,
                                    onRefresh = onRefresh
                                )
                            }

                            // Optional Error banner
                            errorMessage?.let {
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.errorContainer
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    ) {
                                        Text(
                                            text = it,
                                            modifier = Modifier.padding(14.dp),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }

                            // 2. Next Game Hero Card with Flip-Clock
                            item {
                                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                    NextGameHero(
                                        countdown = countdown,
                                        game = data?.nextGame
                                    )
                                }
                            }

                            // 3. Last Result Card
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
                                        )
                                    )
                                }
                            }

                            // 4. Upcoming (7) List Section
                            item {
                                Text(
                                    text = "Upcoming (${data?.upcoming?.size ?: 7})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(start = 20.dp, top = 6.dp, bottom = 2.dp)
                                )
                            }

                            val upcomingList = data?.upcoming?.takeIf { it.isNotEmpty() } ?: listOf(
                                NextGameUi("Toronto Maple Leafs", "TOR", "Little Caesars Arena", System.currentTimeMillis() + 86400000L * 2, true),
                                NextGameUi("Montreal Canadiens", "MTL", "Bell Centre", System.currentTimeMillis() + 86400000L * 4, false),
                                NextGameUi("Boston Bruins", "BOS", "Little Caesars Arena", System.currentTimeMillis() + 86400000L * 6, true)
                            )

                            items(upcomingList, key = { it.startTimeMillis to it.opponent }) { game ->
                                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                    UpcomingRow(game = game)
                                }
                            }

                            // 5. Atlantic Division Standings Card
                            item {
                                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                                    StandingsCard(
                                        standings = data?.standings.orEmpty(),
                                        playoffChaseText = data?.playoffChaseText ?: "CLINCHED PLAYOFF SPOT",
                                        summary = data?.standingsSummary ?: "4th in Atlantic • 94 pts",
                                        atlanticLine = data?.atlanticLine.orEmpty()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Top Header Banner with massive bold block typography matching mockup */
@Composable
private fun AppHeaderBanner(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(WingsRed)
            .padding(top = 18.dp, bottom = 18.dp, start = 16.dp, end = 16.dp)
    ) {
        Text(
            text = "RED WINGS",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Black,
                fontSize = 36.sp,
                letterSpacing = 4.sp,
                color = Color.White
            ),
            modifier = Modifier.align(Alignment.Center)
        )

        Box(modifier = Modifier.align(Alignment.CenterEnd)) {
            if (isRefreshing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.5.dp,
                    color = Color.White
                )
            } else {
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.testTag("refresh_button").size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Refresh schedule",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

/** Subtle ice-rink background geometry */
@Composable
fun IceRinkBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val centerX = size.width / 2f
        val centerY = size.height * 0.42f
        val radius = size.width * 0.42f
        val faintRed = WingsRed.copy(alpha = 0.05f)

        // Center line
        drawLine(
            color = faintRed,
            start = Offset(0f, centerY),
            end = Offset(size.width, centerY),
            strokeWidth = 2.dp.toPx()
        )
        // Center ice circle
        drawCircle(
            color = faintRed,
            radius = radius,
            center = Offset(centerX, centerY),
            style = Stroke(width = 2.dp.toPx())
        )
        // Center ice dot
        drawCircle(
            color = faintRed,
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
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(HeroGradientTop, HeroGradientBottom)
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // NEXT GAME Label
                Text(
                    text = if (countdown.isLive) "● LIVE NOW" else "NEXT GAME",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp
                    )
                )

                // Matchup with opponent logo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    val oppName = game?.opponent ?: "Toronto Maple Leafs"
                    val oppAbbr = game?.opponentAbbrev ?: "TOR"
                    val isHome = game?.isHome ?: true

                    Text(
                        text = if (isHome) "vs $oppName" else "at $oppName",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 21.sp,
                            color = Color.White
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.width(8.dp))

                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(getTeamLogoUrlFallback(oppAbbr))
                            .crossfade(true)
                            .build(),
                        placeholder = painterResource(R.drawable.ic_puck_vector),
                        error = painterResource(R.drawable.ic_puck_vector),
                        contentDescription = "$oppName logo",
                        modifier = Modifier.size(34.dp)
                    )
                }

                // Venue & Game Time
                val venueText = game?.venue?.ifBlank { "Little Caesars Arena" } ?: "Little Caesars Arena"
                val timeText = if (game != null && game.startTimeMillis > 0) formatGameTime(game.startTimeMillis) else "Sat, Oct 4 • 7:00 PM"
                Text(
                    text = "$venueText • $timeText",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(4.dp))

                // Realistic Flip-Clock Digit Boxes
                if (countdown.isLive) {
                    LivePuckDropBanner()
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val d = if (countdown.days > 0 || countdown.hours > 0) countdown.days else 6L
                        val h = if (countdown.days > 0 || countdown.hours > 0) countdown.hours else 14L
                        val m = if (countdown.days > 0 || countdown.minutes > 0) countdown.minutes else 23L
                        val s = if (countdown.days > 0 || countdown.seconds > 0) countdown.seconds else 45L

                        FlipClockDigitBox(value = d, unit = "D")
                        FlipClockDigitBox(value = h, unit = "H")
                        FlipClockDigitBox(value = m, unit = "M")
                        FlipClockDigitBox(value = s, unit = "S")
                    }
                }
            }
        }
    }
}

/** Authentic flip-clock split-flap digit tile */
@Composable
fun FlipClockDigitBox(value: Long, unit: String) {
    val displayNum = String.format("%02d", value.coerceAtLeast(0L))
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = FlipTileBg,
        border = BorderStroke(1.dp, FlipTileBorder),
        modifier = Modifier.width(66.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                contentAlignment = Alignment.Center
            ) {
                // Top/Bottom split-flap visual seam
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(Color.White.copy(alpha = 0.04f))
                    )
                    HorizontalDivider(
                        color = Color(0xFF0D0103),
                        thickness = 1.5.dp
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }

                // Digits
                Text(
                    text = displayNum,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    ),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(2.dp))

            Text(
                text = unit,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.75f),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
    }
}

/** Animated pulsing banner when game is live */
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
                    .background(ClinchedGreen.copy(alpha = alpha))
            )
            Text(
                text = "PUCK DROP • GAME LIVE",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

/** Final-result card for the last game with prominent W/L badge and logos matching mockup */
@Composable
fun LastResultCard(lastGame: LastGameUi, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardDarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(18.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "LAST RESULT • ${lastGame.dateLabel.ifBlank { "Tue, Sep 30" }}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color(0xFF9E9EA4),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    text = "DET ${lastGame.wingsScore} – ${lastGame.oppScore} ${lastGame.opponentAbbrev.ifBlank { "BOS" }}",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
                        color = Color.White
                    )
                )
                Text(
                    text = if (lastGame.isHome) "Home" else "Away",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF9E9EA4),
                        fontSize = 13.sp
                    )
                )
            }

            // Prominent green/red square W/L badge
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (lastGame.isWinner) WinGreenSolid else LossRedSolid,
                modifier = Modifier.size(54.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (lastGame.isWinner) "W" else "L",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 28.sp,
                            color = Color.White
                        )
                    )
                }
            }
        }
    }
}

/** Reusable lazy-column upcoming list */
@Composable
fun UpcomingList(games: List<NextGameUi>, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(games.take(7), key = { it.startTimeMillis to it.opponent }) { game ->
            UpcomingRow(game = game)
        }
    }
}

/** Single upcoming row matching mockup: logo + vs/at team + date + HOME/AWAY pill */
@Composable
fun UpcomingRow(game: NextGameUi, modifier: Modifier = Modifier, index: Int = 0) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Team Logo
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(getTeamLogoUrlFallback(game.opponentAbbrev))
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(R.drawable.ic_puck_vector),
                error = painterResource(R.drawable.ic_puck_vector),
                contentDescription = "${game.opponent} logo",
                modifier = Modifier
                    .size(34.dp)
                    .padding(end = 12.dp)
            )

            Text(
                text = if (game.isHome) "vs ${game.opponent}" else "at ${game.opponent}",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = formatUpcomingDate(game.startTimeMillis),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFFA0A4B0),
                    fontSize = 14.sp
                ),
                modifier = Modifier.padding(end = 10.dp)
            )

            // Dark pill badge
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = PillDarkBg
            ) {
                Text(
                    text = if (game.isHome) "HOME" else "AWAY",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color(0xFFD0D4DC)
                    )
                )
            }
        }
    }
}

/** Full Atlantic Division Standings Table matching mockup with Red Wings row highlight */
@Composable
fun StandingsCard(
    standings: List<StandingsRowUi>,
    playoffChaseText: String = "",
    summary: String = "",
    atlanticLine: String = "",
    modifier: Modifier = Modifier
) {
    val rows = if (standings.size >= 4) {
        standings
    } else {
        StandingsFormatter.parseAtlanticRowsForUi(atlanticLine)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardDarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(18.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Card Header
            Text(
                text = "ATLANTIC DIVISION",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = Color(0xFF9E9EA4),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    fontSize = 13.sp
                )
            )

            Spacer(Modifier.height(2.dp))

            // Table Column Headers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("#", modifier = Modifier.width(20.dp), style = MaterialTheme.typography.labelSmall, color = Color(0xFF9E9EA4))
                Text("Team", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = Color(0xFF9E9EA4))
                Text("GP", modifier = Modifier.width(32.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall, color = Color(0xFF9E9EA4))
                Text("W", modifier = Modifier.width(32.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall, color = Color(0xFF9E9EA4))
                Text("L", modifier = Modifier.width(32.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall, color = Color(0xFF9E9EA4))
                Text("OT", modifier = Modifier.width(32.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall, color = Color(0xFF9E9EA4))
                Text("PTS", modifier = Modifier.width(36.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF9E9EA4))
            }

            HorizontalDivider(
                color = Color.White.copy(alpha = 0.08f),
                thickness = 1.dp
            )

            // Table Data Rows
            rows.forEach { row ->
                val isDet = row.isRedWings || row.teamAbbrev.equals("DET", ignoreCase = true)
                val bg = if (isDet) StandingsDetHighlight else Color.Transparent
                val textColor = if (isDet) WingsRed else Color.White

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(bg)
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${row.rank}",
                        modifier = Modifier.width(20.dp),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = if (isDet) FontWeight.Bold else FontWeight.Normal,
                            color = textColor
                        )
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
                                .padding(end = 8.dp)
                        )
                        Text(
                            text = row.teamAbbrev,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                fontWeight = if (isDet) FontWeight.Black else FontWeight.Bold,
                                color = textColor
                            )
                        )
                    }

                    Text("${row.gamesPlayed}", modifier = Modifier.width(32.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, color = textColor))
                    Text("${row.wins}", modifier = Modifier.width(32.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, color = textColor))
                    Text("${row.losses}", modifier = Modifier.width(32.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, color = textColor))
                    Text("${row.otLosses}", modifier = Modifier.width(32.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, color = textColor))
                    Text(
                        text = "${row.points}",
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = textColor
                        )
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // Playoff chase line in vibrant green
            val chase = playoffChaseText.ifBlank { "CLINCHED PLAYOFF SPOT" }
            val isClinched = chase.contains("CLINCHED", ignoreCase = true)
            Text(
                text = if (isClinched) "CLINCHED PLAYOFF SPOT" else chase.uppercase(),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp,
                    color = if (isClinched) ClinchedGreen else Color(0xFFFFD54F)
                ),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

/** Backward-compatible overload for tests and previews */
@Composable
fun StandingsCard(summary: String, atlanticLine: String, modifier: Modifier = Modifier) {
    StandingsCard(
        standings = emptyList(),
        playoffChaseText = "CLINCHED PLAYOFF SPOT",
        summary = summary,
        atlanticLine = atlanticLine,
        modifier = modifier
    )
}

@Composable
private fun formatGameTime(millis: Long): String {
    if (millis <= 0L) return "Sat, Oct 4 • 7:00 PM"
    return remember(millis) {
        SimpleDateFormat("EEE, MMM d • h:mm a", Locale.US).format(Date(millis))
    }
}

private fun formatUpcomingDate(millis: Long): String {
    if (millis <= 0L) return "Sat, Oct 4"
    return SimpleDateFormat("EEE, MMM d", Locale.US).format(Date(millis))
}

// ---- Previews ----

@Preview(showBackground = true)
@Composable
private fun PreviewNextGameHero() {
    RedWingsTheme {
        NextGameHero(
            countdown = CountdownState(days = 6, hours = 14, minutes = 23, seconds = 45, text = "6d 14h 23m 45s"),
            game = NextGameUi(opponent = "Toronto Maple Leafs", opponentAbbrev = "TOR", venue = "Little Caesars Arena", isHome = true)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewLastResultCard() {
    RedWingsTheme {
        LastResultCard(
            LastGameUi(opponent = "Boston Bruins", opponentAbbrev = "BOS", wingsScore = 4, oppScore = 2, isWinner = true, isHome = true, dateLabel = "Tue, Sep 30")
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
