package com.redwings.widget.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.redwings.widget.R
import com.redwings.widget.data.model.getTeamLogoUrlFallback
import com.redwings.widget.ui.StandingsRowUi
import com.redwings.widget.ui.theme.WingsRed
import com.redwings.widget.widget.StandingsFormatter

/** Full Atlantic Division Standings Table matching mockup with Red Wings row highlight */
@Composable
fun StandingsCard(
    standings: List<StandingsRowUi>,
    playoffChaseText: String = "",
    summary: String = "",
    atlanticLine: String = "",
    modifier: Modifier = Modifier,
    showExtendedStats: Boolean = false
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
            verticalArrangement = Arrangement.spacedBy(4.dp)
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
                Text("GP", modifier = Modifier.width(28.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall, color = Color(0xFF9E9EA4))
                Text("W", modifier = Modifier.width(28.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall, color = Color(0xFF9E9EA4))
                Text("L", modifier = Modifier.width(28.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall, color = Color(0xFF9E9EA4))
                Text("OT", modifier = Modifier.width(28.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall, color = Color(0xFF9E9EA4))
                if (showExtendedStats) {
                    Text("DIFF", modifier = Modifier.width(36.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall, color = Color(0xFF9E9EA4))
                }
                Text("PTS", modifier = Modifier.width(34.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF9E9EA4))
            }

            HorizontalDivider(
                color = Color.White.copy(alpha = 0.08f),
                thickness = 1.dp
            )

            // Table Data Rows with Playoff Cutoff line after Rank 3
            rows.forEachIndexed { index, row ->
                if (index == 3) {
                    PlayoffCutoffDivider()
                }

                val isDet = row.isRedWings || row.teamAbbrev.equals("DET", ignoreCase = true)
                val bg = if (isDet) StandingsDetHighlight else Color.Transparent
                val textColor = if (isDet) WingsRed else Color.White

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(bg)
                        .padding(vertical = 5.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${row.rank}",
                        modifier = Modifier.width(20.dp),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
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
                                fontSize = 13.sp,
                                fontWeight = if (isDet) FontWeight.Black else FontWeight.Bold,
                                color = textColor
                            )
                        )
                    }

                    Text("${row.gamesPlayed}", modifier = Modifier.width(28.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, color = textColor))
                    Text("${row.wins}", modifier = Modifier.width(28.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, color = textColor))
                    Text("${row.losses}", modifier = Modifier.width(28.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, color = textColor))
                    Text("${row.otLosses}", modifier = Modifier.width(28.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, color = textColor))
                    if (showExtendedStats) {
                        val diffStr = if (row.goalDiff > 0) "+${row.goalDiff}" else "${row.goalDiff}"
                        val diffColor = if (row.goalDiff > 0) ClinchedGreen else if (row.goalDiff < 0) LossRedSolid else textColor
                        Text(diffStr, modifier = Modifier.width(36.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp, color = diffColor))
                    }
                    Text(
                        text = "${row.points}",
                        modifier = Modifier.width(34.dp),
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
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

@Composable
private fun PlayoffCutoffDivider() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = PlayoffCutoffLineColor.copy(alpha = 0.6f),
            thickness = 1.dp
        )
        Text(
            text = " PLAYOFF CUTOFF ",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                color = PlayoffCutoffLineColor
            )
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = PlayoffCutoffLineColor.copy(alpha = 0.6f),
            thickness = 1.dp
        )
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
