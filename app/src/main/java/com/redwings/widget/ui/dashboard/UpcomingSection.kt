package com.redwings.widget.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.redwings.widget.R
import com.redwings.widget.data.model.getTeamLogoUrlFallback
import com.redwings.widget.ui.NextGameUi
import com.redwings.widget.ui.theme.LocalJerseyPalette

/** Single upcoming row: logo + vs/at team + date + HOME/AWAY pill */
@Composable
fun UpcomingRow(game: NextGameUi, modifier: Modifier = Modifier) {
    val palette = LocalJerseyPalette.current
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
                    color = palette.primaryText
                )
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = formatUpcomingDate(game.startTimeMillis),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = palette.secondaryText,
                    fontSize = 14.sp
                ),
                modifier = Modifier.padding(end = 10.dp)
            )

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = palette.cardBorder.copy(alpha = 0.5f)
            ) {
                Text(
                    text = if (game.isHome) "HOME" else "AWAY",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = palette.primaryText
                    )
                )
            }
        }
    }
}

/** Card for horizontal carousel on wide or foldable screens */
@Composable
fun UpcomingCard(game: NextGameUi, modifier: Modifier = Modifier) {
    val palette = LocalJerseyPalette.current
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = palette.cardSurface),
        border = BorderStroke(1.dp, palette.cardBorder),
        modifier = modifier.width(135.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = formatUpcomingDate(game.startTimeMillis),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.secondaryText
                )
            )
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(getTeamLogoUrlFallback(game.opponentAbbrev))
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(R.drawable.ic_puck_vector),
                error = painterResource(R.drawable.ic_puck_vector),
                contentDescription = "${game.opponent} logo",
                modifier = Modifier.size(36.dp)
            )
            Text(
                text = if (game.isHome) "vs ${game.opponentAbbrev}" else "@ ${game.opponentAbbrev}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = palette.primaryText
                )
            )
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = palette.cardBorder.copy(alpha = 0.5f)
            ) {
                Text(
                    text = if (game.isHome) "HOME" else "AWAY",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.secondaryText
                    )
                )
            }
        }
    }
}

/** Upcoming games section: vertical list on compact, horizontal carousel on expanded */
@Composable
fun UpcomingSection(
    games: List<NextGameUi>,
    modifier: Modifier = Modifier,
    isExpanded: Boolean = false
) {
    val palette = LocalJerseyPalette.current
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Upcoming (${games.take(7).size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = palette.primaryText,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (isExpanded) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(games.take(7), key = { it.startTimeMillis to it.opponent }) { game ->
                    UpcomingCard(game = game)
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                games.take(7).forEach { game ->
                    UpcomingRow(game = game)
                }
            }
        }
    }
}

/** Backward-compatible list */
@Composable
fun UpcomingList(games: List<NextGameUi>, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(games.take(7), key = { it.startTimeMillis to it.opponent }) { game ->
            UpcomingRow(game = game)
        }
    }
}
