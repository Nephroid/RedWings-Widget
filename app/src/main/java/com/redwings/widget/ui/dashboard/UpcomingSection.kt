package com.redwings.widget.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.redwings.widget.R
import com.redwings.widget.data.model.getTeamLogoUrlFallback
import com.redwings.widget.ui.NextGameUi
import com.redwings.widget.ui.theme.JerseyPalette
import com.redwings.widget.ui.theme.LocalJerseyPalette
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Determines TV broadcast network pill */
fun getBroadcastChannel(game: NextGameUi): String =
    if (game.broadcast.isNotBlank()) game.broadcast
    else if (game.opponentAbbrev.uppercase(Locale.US) in setOf("BOS", "TOR", "NYR", "COL")) "TNT"
    else "FDSN-DET"

/** Formats game start time (e.g. 7:00 PM) */
private fun formatTileTime(millis: Long): String =
    if (millis <= 0L) "TBD" else SimpleDateFormat("h:mm a", Locale.US).format(Date(millis))

/** Formats combined game date and time */
fun formatUpcomingDateTime(millis: Long): String =
    if (millis <= 0L) "TBD" else SimpleDateFormat("EEE, MMM d • h:mm a", Locale.US).format(Date(millis))

@Composable
private fun BroadcastPill(broadcast: String, palette: JerseyPalette) {
    val isTnt = broadcast == "TNT"
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (isTnt) Color(0xFF1B1D22) else palette.accentRed.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, if (isTnt) palette.cardBorder else palette.accentRed.copy(alpha = 0.35f))
    ) {
        Text(
            text = broadcast,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp, fontWeight = FontWeight.Black,
                color = if (isTnt) palette.primaryText else palette.accentRed
            ),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun TeamCrest(abbr: String, name: String, isDet: Boolean, size: Dp = 32.dp) {
    if (isDet) {
        Image(
            painter = painterResource(R.drawable.ic_redwings_logo),
            contentDescription = name,
            modifier = Modifier.size(size)
        )
    } else {
        TeamLogo(abbrev = abbr, size = size)
    }
}

/**
 * Frosted acrylic game card tile for carousel / card matrix.
 * Displays team crests (DET vs BOS), date/time, venue tag, and TV broadcast pill.
 */
@Composable
fun UpcomingCard(game: NextGameUi, modifier: Modifier = Modifier) {
    val palette = LocalJerseyPalette.current
    val broadcast = getBroadcastChannel(game)
    val matchup = if (game.isHome) "DET vs ${game.opponentAbbrev}" else "DET @ ${game.opponentAbbrev}"
    val venue = if (game.isHome) "Little Caesars Arena" else game.venue.ifBlank { "Away" }

    FrostedGlassCard(
        modifier = modifier.width(172.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatUpcomingDate(game.startTimeMillis),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = palette.secondaryText)
                )
                BroadcastPill(broadcast = broadcast, palette = palette)
            }

            // Center Team Crests: DET vs OPP
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TeamCrest(abbr = "DET", name = "Detroit Red Wings", isDet = true, size = 32.dp)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = palette.cardBorder.copy(alpha = 0.45f),
                    modifier = Modifier.padding(horizontal = 6.dp)
                ) {
                    Text(
                        text = if (game.isHome) "vs" else "@",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 10.sp, color = palette.primaryText),
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                }
                TeamCrest(abbr = game.opponentAbbrev, name = game.opponent, isDet = false, size = 32.dp)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = matchup,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black, fontSize = 14.sp, color = palette.primaryText),
                    maxLines = 1
                )
                Text(
                    text = formatTileTime(game.startTimeMillis),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = palette.secondaryText)
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = palette.cardBorder.copy(alpha = 0.35f),
                border = BorderStroke(0.5.dp, palette.cardBorder.copy(alpha = 0.6f))
            ) {
                Text(
                    text = venue,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Medium, color = palette.secondaryText),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}

/** Frosted acrylic row for vertical list backward-compatibility */
@Composable
fun UpcomingRow(game: NextGameUi, modifier: Modifier = Modifier) {
    val palette = LocalJerseyPalette.current
    val broadcast = getBroadcastChannel(game)
    val matchup = if (game.isHome) "DET vs ${game.opponentAbbrev}" else "DET @ ${game.opponentAbbrev}"

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = palette.cardSurface),
        border = BorderStroke(1.dp, palette.cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                TeamCrest(abbr = "DET", name = "DET", isDet = true, size = 26.dp)
                Text(text = if (game.isHome) "vs" else "@", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp, color = palette.secondaryText))
                TeamCrest(abbr = game.opponentAbbrev, name = game.opponent, isDet = false, size = 26.dp)
                Column {
                    Text(text = matchup, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp, color = palette.primaryText))
                    Text(
                        text = if (game.isHome) "Little Caesars Arena" else game.venue.ifBlank { "Away" },
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, color = palette.secondaryText),
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = formatUpcomingDateTime(game.startTimeMillis),
                    style = MaterialTheme.typography.bodySmall.copy(color = palette.secondaryText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                )
                BroadcastPill(broadcast = broadcast, palette = palette)
            }
        }
    }
}

/**
 * Upcoming games section: Frosted Ice horizontal carousel or card matrix.
 */
@Composable
fun UpcomingSection(
    games: List<NextGameUi>,
    modifier: Modifier = Modifier,
    isExpanded: Boolean = false
) {
    val palette = LocalJerseyPalette.current
    val displayGames = games.take(7)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "UPCOMING GAMES",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black, letterSpacing = 0.8.sp, fontSize = 15.sp, color = palette.primaryText
                )
            )
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = palette.cardBorder.copy(alpha = 0.35f),
                border = BorderStroke(1.dp, palette.cardBorder.copy(alpha = 0.6f))
            ) {
                Text(
                    text = "${displayGames.size} GAMES",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp, color = palette.secondaryText),
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        if (displayGames.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = palette.cardSurface,
                border = BorderStroke(1.dp, palette.cardBorder)
            ) {
                Text(
                    text = "No upcoming games scheduled",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(color = palette.secondaryText)
                )
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(displayGames, key = { "${it.startTimeMillis}_${it.opponentAbbrev}" }) { game ->
                    UpcomingCard(game = game)
                }
            }
        }
    }
}

/** Backward-compatible list */
@Composable
fun UpcomingList(games: List<NextGameUi>, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(games.take(7), key = { "${it.startTimeMillis}_${it.opponentAbbrev}" }) { game ->
            UpcomingRow(game = game)
        }
    }
}
