package com.redwings.widget.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.redwings.widget.R
import com.redwings.widget.data.model.TeamLeaderPlayerUi
import com.redwings.widget.data.model.TeamLeadersUi
import com.redwings.widget.ui.theme.LocalJerseyPalette

/**
 * Category enum for DRW Season Stat Leaders. Order: Goals, Assists, Points.
 */
enum class LeaderCategory(val label: String) {
    GOALS("GOALS"),
    ASSISTS("ASSISTS"),
    POINTS("POINTS")
}

/**
 * Standalone Frosted Glass card bubble displaying season stats for the Top 5
 * Detroit Red Wings in Goals, Assists, and Points.
 * Includes interactive segmented pill switcher, rank medal badges (#1 Gold, #2 Silver, #3 Bronze),
 * circular headshots, position/sweater info, and stat lines.
 */
@Composable
fun DrwSeasonLeadersCard(
    leaders: TeamLeadersUi,
    modifier: Modifier = Modifier,
    pacing: Map<Int, String> = emptyMap()
) {
    val palette = LocalJerseyPalette.current
    var selectedCategory by remember { mutableStateOf(LeaderCategory.GOALS) }

    val currentPlayers = when (selectedCategory) {
        LeaderCategory.GOALS -> leaders.topGoals
        LeaderCategory.ASSISTS -> leaders.topAssists
        LeaderCategory.POINTS -> leaders.topPoints
    }

    FrostedGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("drw_season_leaders_card")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Winged Wheel Logo + Title + Top 5 badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_redwings_logo),
                        contentDescription = "Detroit Red Wings",
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "DRW SEASON LEADERS",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            letterSpacing = 0.5.sp,
                            color = palette.primaryText
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = palette.cardBorder.copy(alpha = 0.20f),
                    border = BorderStroke(1.dp, palette.cardBorder.copy(alpha = 0.45f))
                ) {
                    Text(
                        text = "TOP 5",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = palette.secondaryText
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Interactive Segmented Switcher Pill
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = palette.cardBorder.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, palette.cardBorder.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    LeaderCategory.values().forEach { category ->
                        val isSelected = selectedCategory == category
                        Surface(
                            onClick = { selectedCategory = category },
                            shape = RoundedCornerShape(9.dp),
                            color = if (isSelected) palette.accentRed else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("drw_leaders_tab_${category.name.lowercase()}")
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = category.label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                        fontSize = 11.sp,
                                        letterSpacing = 0.5.sp,
                                        color = if (isSelected) Color.White else palette.secondaryText
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(2.dp))

            // Top 5 Player List
            if (currentPlayers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No season stats available",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = palette.secondaryText
                        )
                    )
                }
            } else {
                currentPlayers.take(5).forEachIndexed { index, player ->
                    DrwLeaderPlayerRow(
                        player = player,
                        modifier = Modifier.fillMaxWidth(),
                        pacingText = pacing[player.playerId]
                    )
                    if (index < currentPlayers.take(5).lastIndex) {
                        HorizontalDivider(
                            color = palette.cardBorder.copy(alpha = 0.25f),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * High-density player row showing rank medal badge (#1 Gold, #2 Silver, #3 Bronze, #4-#5 Frosted),
 * circular headshot photo, player name, position/sweater number, secondary stat line,
 * and primary stat badge.
 */
@Composable
fun DrwLeaderPlayerRow(
    player: TeamLeaderPlayerUi,
    modifier: Modifier = Modifier,
    pacingText: String? = null
) {
    val palette = LocalJerseyPalette.current
    val context = LocalContext.current

    val (rankTextColor, rankBgColor, rankBorderColor) = when (player.rank) {
        1 -> Triple(
            Color(0xFFFFD700),
            Color(0xFFFFD700).copy(alpha = 0.18f),
            Color(0xFFFFD700).copy(alpha = 0.6f)
        )
        2 -> Triple(
            Color(0xFFC0C0C0),
            Color(0xFFC0C0C0).copy(alpha = 0.18f),
            Color(0xFFC0C0C0).copy(alpha = 0.6f)
        )
        3 -> Triple(
            Color(0xFFCD7F32),
            Color(0xFFCD7F32).copy(alpha = 0.18f),
            Color(0xFFCD7F32).copy(alpha = 0.6f)
        )
        else -> Triple(
            palette.secondaryText,
            palette.cardBorder.copy(alpha = 0.18f),
            palette.cardBorder.copy(alpha = 0.40f)
        )
    }

    Row(
        modifier = modifier
            .padding(vertical = 4.dp)
            .testTag("drw_leader_row_${player.rank}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Rank Badge
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = rankBgColor,
            border = BorderStroke(1.dp, rankBorderColor),
            modifier = Modifier.size(28.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "#${player.rank}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        color = rankTextColor
                    )
                )
            }
        }

        // Circular Headshot
        Surface(
            shape = CircleShape,
            color = palette.cardBorder.copy(alpha = 0.20f),
            border = BorderStroke(1.dp, palette.cardBorder.copy(alpha = 0.50f)),
            modifier = Modifier.size(38.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(player.headshotUrl.ifBlank { "https://assets.nhle.com/mugs/nhl/latest/${player.playerId}.png" })
                    .crossfade(true)
                    .build(),
                contentDescription = player.name,
                placeholder = painterResource(R.drawable.ic_puck_vector),
                error = painterResource(R.drawable.ic_puck_vector),
                fallback = painterResource(R.drawable.ic_puck_vector),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
            )
        }

        // Player Name & Detail Column
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = player.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = palette.primaryText
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (player.position.isNotBlank()) {
                    val posTag = buildString {
                        append(player.position)
                        if (player.sweaterNo != null && player.sweaterNo > 0) {
                            append(" • #").append(player.sweaterNo)
                        }
                    }
                    Text(
                        text = posTag,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = palette.secondaryText
                        )
                    )
                }
            }

            if (!pacingText.isNullOrBlank()) {
                Text(
                    text = "✦ $pacingText",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = palette.accentRed
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else if (player.secondaryStat.isNotBlank()) {
                Text(
                    text = player.secondaryStat,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = palette.secondaryText.copy(alpha = 0.85f)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Primary Stat Badge (PTS / G / A)
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = palette.cardBorder.copy(alpha = 0.22f),
            border = BorderStroke(1.dp, palette.cardBorder.copy(alpha = 0.50f))
        ) {
            Text(
                text = player.primaryStat,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    color = palette.accentRed
                ),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}
