package com.redwings.widget.ui.dashboard

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.redwings.widget.ui.CountdownState
import com.redwings.widget.ui.LastGameUi
import com.redwings.widget.ui.NextGameUi
import com.redwings.widget.ui.ScheduleUiState
import com.redwings.widget.ui.theme.AppJersey
import com.redwings.widget.ui.theme.LocalJerseyPalette

/**
 * Tabletop / Stand Mode Dashboard for Google Pixel 10 Fold / Pixel 9 Pro Fold (Hinge folded ~90°-120°)
 * and ASUS Chromebook CM34 Flip (Tent / Stand / Touch Console).
 *
 * Upper Screen (Angled Upward / Desk View): Broadcast Scoreboard Hero.
 * Lower Screen (Flat on Surface / Touch Base): Interactive Console (Standings + Last Game Box Score).
 */
@Composable
fun TabletopDashboard(
    scheduleState: ScheduleUiState,
    countdownProvider: () -> CountdownState,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    activeJersey: AppJersey = AppJersey.HOME,
    onJerseyThemeToggle: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val data = scheduleState as? ScheduleUiState.Data
    val bottomScrollState = rememberScrollState()
    val palette = LocalJerseyPalette.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
    ) {
        // ================= UPPER HALF: UPRIGHT BROADCAST SCOREBOARD =================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(palette.background)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            IceRinkBackground()

            Column(
                modifier = Modifier
                    .widthIn(max = 900.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "GAME NIGHT LIVE • DETROIT RED WINGS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = palette.accentRed,
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Black
                        )
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            onClick = onJerseyThemeToggle,
                            shape = RoundedCornerShape(16.dp),
                            color = palette.chipBg,
                            border = BorderStroke(1.dp, palette.cardBorder),
                            modifier = Modifier.testTag("jersey_theme_chip")
                        ) {
                            Text(
                                text = activeJersey.chipLabel,
                                color = palette.chipText,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        if (isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = palette.accentRed
                            )
                        } else {
                            IconButton(
                                onClick = onRefresh,
                                modifier = Modifier.testTag("refresh_button").size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "Refresh schedule",
                                    tint = palette.primaryText
                                )
                            }
                        }
                    }
                }

                NextGameHero(
                    countdownProvider = countdownProvider,
                    game = data?.nextGame,
                    tileWidth = 72.dp,
                    showFlankingBadges = true
                )
            }
        }

        // ================= PHYSICAL HINGE ACCENT DIVIDER =================
        HorizontalDivider(
            color = palette.accentRed.copy(alpha = 0.35f),
            thickness = 2.dp
        )

        // ================= LOWER HALF: FLAT TOUCH CONSOLE =================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.05f)
                .background(palette.background)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Row(
                modifier = Modifier
                    .widthIn(max = 1400.dp)
                    .fillMaxSize()
                    .verticalScroll(bottomScrollState),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left console column: Standings
                Column(
                    modifier = Modifier.weight(1.1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StandingsCard(
                        standings = data?.standings.orEmpty(),
                        playoffChaseText = data?.playoffChaseText ?: "CLINCHED PLAYOFF SPOT",
                        summary = data?.standingsSummary ?: "4th in Atlantic • 94 pts",
                        atlanticLine = data?.atlanticLine.orEmpty(),
                        showExtendedStats = true
                    )
                }

                // Right console column: Last Result & Upcoming
                Column(
                    modifier = Modifier.weight(0.9f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
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
                        leaders = data?.teamLeaders ?: com.redwings.widget.data.model.TeamLeadersUi()
                    )

                    val upcomingList = data?.upcoming?.takeIf { it.isNotEmpty() } ?: listOf(
                        NextGameUi("Toronto Maple Leafs", "TOR", "Little Caesars Arena", System.currentTimeMillis() + 86400000L * 2, true),
                        NextGameUi("Montreal Canadiens", "MTL", "Bell Centre", System.currentTimeMillis() + 86400000L * 4, false)
                    )

                    UpcomingSection(games = upcomingList, isExpanded = true)

                    Spacer(Modifier.height(8.dp))
                    AppFooter()
                }
            }
        }
    }
}
