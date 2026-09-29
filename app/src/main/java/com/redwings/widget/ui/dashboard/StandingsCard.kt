package com.redwings.widget.ui.dashboard

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.redwings.widget.ui.theme.LocalJerseyPalette
import com.redwings.widget.widget.StandingsFormatter

/**
 * Frosted Ice Standings Matrix (Concept 2 Template) for the Atlantic Division.
 * Dual-column matrix layout for seeds 1-4 and 5-8 with Detroit Red Wings glowing crimson pill,
 * specular white border, and tap-to-expand full division statistics table.
 */
@Composable
fun StandingsCard(
    standings: List<StandingsRowUi>,
    playoffChaseText: String = "",
    summary: String = "",
    atlanticLine: String = "",
    modifier: Modifier = Modifier,
    showExtendedStats: Boolean = false
) {
    val palette = LocalJerseyPalette.current
    var isExpanded by remember(showExtendedStats) { mutableStateOf(showExtendedStats) }
    val rows = if (standings.size >= 4) standings else StandingsFormatter.parseAtlanticRowsForUi(atlanticLine)

    val detRow = rows.find { it.isRedWings || it.teamAbbrev.equals("DET", ignoreCase = true) }
    val rankBadgeText = if (detRow != null) "${detRow.rank}TH • ${detRow.points} PTS" else summary.substringBefore(" •")

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = palette.cardSurface),
        border = BorderStroke(1.5.dp, palette.cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: "ATLANTIC DIVISION STANDINGS" with subtle division rank indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ATLANTIC DIVISION STANDINGS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = palette.secondaryText,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontSize = 12.sp
                    )
                )

                if (rankBadgeText.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = palette.accentRed.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, palette.accentRed.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = rankBadgeText.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.accentRed,
                                letterSpacing = 0.5.sp
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Dual-Column Matrix Layout: Playoff Seeds (1-4) vs In The Hunt (5-8)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(palette.cardBorder.copy(alpha = 0.08f))
                    .clickable { isExpanded = !isExpanded }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Column 1: Playoff Seeds (1-4)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(
                        text = "PLAYOFF SEEDS (1-4)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            color = palette.secondaryText
                        ),
                        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                    )
                    rows.take(4).forEach { row -> MatrixSeedRow(row = row) }
                }

                // Vertical divider line between columns
                Box(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .width(1.dp)
                        .height(115.dp)
                        .background(palette.cardBorder.copy(alpha = 0.5f))
                )

                // Column 2: In The Hunt (5-8)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(
                        text = "IN THE HUNT (5-8)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            color = palette.secondaryText
                        ),
                        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                    )
                    rows.drop(4).take(4).forEach { row -> MatrixSeedRow(row = row) }
                }
            }

            // Interactive Footer: Playoff Chase status + Tap-to-expand button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val chase = playoffChaseText.ifBlank { "CLINCHED PLAYOFF SPOT" }
                val isClinched = chase.contains("CLINCHED", ignoreCase = true)
                Text(
                    text = if (isClinched) "CLINCHED PLAYOFF SPOT" else chase.uppercase(),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        color = if (isClinched) ClinchedGreen else palette.highlightGold
                    )
                )

                Surface(
                    modifier = Modifier.clickable { isExpanded = !isExpanded },
                    shape = RoundedCornerShape(6.dp),
                    color = palette.cardBorder.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, palette.cardBorder.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = if (isExpanded) "HIDE STATS ▲" else "FULL STATS ▼",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.secondaryText
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Expandable full division stats table
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    HorizontalDivider(color = palette.cardBorder, thickness = 1.dp)
                    Spacer(Modifier.height(2.dp))

                    // Table Column Headers
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("#", modifier = Modifier.width(20.dp), style = MaterialTheme.typography.labelSmall, color = palette.secondaryText)
                        Text("Team", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = palette.secondaryText)
                        listOf("GP" to 26.dp, "W" to 26.dp, "L" to 26.dp, "OT" to 26.dp, "DIFF" to 34.dp).forEach { (h, w) ->
                            Text(h, modifier = Modifier.width(w), textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall, color = palette.secondaryText)
                        }
                        Text("PTS", modifier = Modifier.width(32.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = palette.secondaryText)
                    }

                    HorizontalDivider(color = palette.cardBorder.copy(alpha = 0.5f), thickness = 1.dp)

                    rows.forEachIndexed { index, row ->
                        if (index == 3) PlayoffCutoffDivider()
                        StatsTableRow(row = row)
                    }
                }
            }
        }
    }
}

/** Compact seed item inside the dual-column matrix */
@Composable
private fun MatrixSeedRow(row: StandingsRowUi) {
    val palette = LocalJerseyPalette.current
    val isDet = row.isRedWings || row.teamAbbrev.equals("DET", ignoreCase = true)
    val rowModifier = if (isDet) {
        Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(palette.accentRed)
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)), RoundedCornerShape(8.dp))
            .padding(horizontal = 7.dp, vertical = 3.5.dp)
    } else {
        Modifier.fillMaxWidth().padding(horizontal = 7.dp, vertical = 3.5.dp)
    }
    val contentColor = if (isDet) Color.White else palette.primaryText
    val ptsColor = if (isDet) Color.White.copy(alpha = 0.95f) else palette.secondaryText

    Row(modifier = rowModifier, verticalAlignment = Alignment.CenterVertically) {
        Text("${row.rank}", modifier = Modifier.width(14.dp), style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = if (isDet) FontWeight.Black else FontWeight.Bold, color = contentColor))
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(getTeamLogoUrlFallback(row.teamAbbrev)).crossfade(true).build(),
            placeholder = painterResource(R.drawable.ic_puck_vector),
            error = painterResource(R.drawable.ic_puck_vector),
            contentDescription = "${row.teamAbbrev} logo",
            modifier = Modifier.size(15.dp).padding(end = 4.dp)
        )
        Text(row.teamAbbrev, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp, fontWeight = if (isDet) FontWeight.Black else FontWeight.Bold, color = contentColor))
        Text("${row.points} PTS", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp, fontWeight = if (isDet) FontWeight.Black else FontWeight.Medium, color = ptsColor))
    }
}

/** Complete statistics table row */
@Composable
private fun StatsTableRow(row: StandingsRowUi) {
    val palette = LocalJerseyPalette.current
    val isDet = row.isRedWings || row.teamAbbrev.equals("DET", ignoreCase = true)
    val bg = if (isDet) palette.accentRed.copy(alpha = 0.15f) else Color.Transparent
    val textColor = if (isDet) palette.accentRed else palette.primaryText
    val diffStr = if (row.goalDiff > 0) "+${row.goalDiff}" else "${row.goalDiff}"
    val diffColor = if (row.goalDiff > 0) ClinchedGreen else if (row.goalDiff < 0) LossRedSolid else textColor

    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(bg).padding(vertical = 3.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("${row.rank}", modifier = Modifier.width(20.dp), style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, fontWeight = if (isDet) FontWeight.Bold else FontWeight.Normal, color = textColor))
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(getTeamLogoUrlFallback(row.teamAbbrev)).crossfade(true).build(),
                placeholder = painterResource(R.drawable.ic_puck_vector),
                error = painterResource(R.drawable.ic_puck_vector),
                contentDescription = "${row.teamAbbrev} logo",
                modifier = Modifier.size(18.dp).padding(end = 6.dp)
            )
            Text(row.teamAbbrev, style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, fontWeight = if (isDet) FontWeight.Black else FontWeight.Bold, color = textColor))
        }
        Text("${row.gamesPlayed}", modifier = Modifier.width(26.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, color = textColor))
        Text("${row.wins}", modifier = Modifier.width(26.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, color = textColor))
        Text("${row.losses}", modifier = Modifier.width(26.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, color = textColor))
        Text("${row.otLosses}", modifier = Modifier.width(26.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, color = textColor))
        Text(diffStr, modifier = Modifier.width(34.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = diffColor))
        Text("${row.points}", modifier = Modifier.width(32.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Black, color = textColor))
    }
}

@Composable
private fun PlayoffCutoffDivider() {
    val gold = LocalJerseyPalette.current.highlightGold
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = gold.copy(alpha = 0.6f), thickness = 1.dp)
        Text(" PLAYOFF CUTOFF ", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = gold))
        HorizontalDivider(modifier = Modifier.weight(1f), color = gold.copy(alpha = 0.6f), thickness = 1.dp)
    }
}

/** Backward-compatible overload for tests and previews */
@Composable
fun StandingsCard(summary: String, atlanticLine: String, modifier: Modifier = Modifier) = StandingsCard(
    standings = emptyList(),
    playoffChaseText = "CLINCHED PLAYOFF SPOT",
    summary = summary,
    atlanticLine = atlanticLine,
    modifier = modifier
)
