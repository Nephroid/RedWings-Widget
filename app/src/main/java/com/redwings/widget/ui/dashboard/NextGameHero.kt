package com.redwings.widget.ui.dashboard

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.redwings.widget.R
import com.redwings.widget.data.model.getTeamLogoUrlFallback
import com.redwings.widget.ui.CountdownState
import com.redwings.widget.ui.NextGameUi
import com.redwings.widget.ui.theme.JerseyPalette
import com.redwings.widget.ui.theme.LocalJerseyPalette
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Frosted Ice Hero Matchup Card for the next game (Concept 2 template).
 */
@Composable
fun NextGameHero(
    countdown: CountdownState,
    game: NextGameUi?,
    modifier: Modifier = Modifier,
    tileWidth: Dp = 66.dp,
    showFlankingBadges: Boolean = false
) {
    val palette = LocalJerseyPalette.current
    val oppName = game?.opponent ?: "Toronto Maple Leafs"
    val oppAbbr = game?.opponentAbbrev ?: "TOR"
    val isDetroitHome = game?.isHome ?: true

    val awayAbbr = if (isDetroitHome) oppAbbr else "DET"
    val homeAbbr = if (isDetroitHome) "DET" else oppAbbr
    val awayName = if (isDetroitHome) oppName else "Detroit Red Wings"
    val homeName = if (isDetroitHome) "Detroit Red Wings" else oppName
    val awayRecord = if (isDetroitHome) "20-8-6" else "22-7-5"
    val homeRecord = if (isDetroitHome) "22-7-5" else "20-8-6"

    val dateFormatted = formatHeroDate(game?.startTimeMillis ?: 0L)
    val venueText = game?.venue?.ifBlank { "Little Caesars Arena" } ?: "Little Caesars Arena"

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = palette.cardSurface),
                border = BorderStroke(1.5.dp, palette.cardBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TeamCol(abbr = awayAbbr, name = awayName, isHome = false, record = awayRecord, isDet = awayAbbr == "DET", palette = palette)

                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = dateFormatted.uppercase(Locale.US),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold, letterSpacing = 1.sp, fontSize = 12.sp, color = palette.primaryText
                            ),
                            textAlign = TextAlign.Center
                        )
                        if (countdown.isLive) {
                            LivePulseBadge(palette = palette)
                        } else {
                            Text(
                                text = formatTimer(countdown),
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontWeight = FontWeight.Black, letterSpacing = 1.5.sp, fontSize = 28.sp, color = palette.accentRed
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    TeamCol(abbr = homeAbbr, name = homeName, isHome = true, record = homeRecord, isDet = homeAbbr == "DET", palette = palette)
                }
            }

            Surface(
                modifier = Modifier.size(24.dp),
                shape = RoundedCornerShape(7.dp),
                color = palette.accentRed,
                shadowElevation = 3.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("@", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 13.sp, color = Color.White))
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = palette.cardSurface,
            border = BorderStroke(1.dp, palette.cardBorder),
            shadowElevation = 1.dp
        ) {
            Text(
                text = venueText,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = palette.secondaryText),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
            )
        }
    }
}

@Composable
private fun TeamCol(abbr: String, name: String, isHome: Boolean, record: String, isDet: Boolean, palette: JerseyPalette) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (!isHome) Crest(abbr = abbr, isDet = isDet, name = name)
        Column(horizontalAlignment = if (isHome) Alignment.End else Alignment.Start) {
            Text(if (isHome) "HOME" else "AWAY", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp, color = if (isHome) palette.accentRed else palette.secondaryText))
            Text(abbr, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, fontSize = 20.sp, color = palette.primaryText))
            Text(record, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium, fontSize = 11.sp, color = palette.secondaryText))
        }
        if (isHome) Crest(abbr = abbr, isDet = isDet, name = name)
    }
}

@Composable
private fun Crest(abbr: String, isDet: Boolean, name: String) {
    if (isDet) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(R.drawable.ic_redwings_logo).crossfade(true).build(),
            contentDescription = "Detroit Red Wings logo",
            modifier = Modifier.size(42.dp)
        )
    } else {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(getTeamLogoUrlFallback(abbr)).crossfade(true).build(),
            placeholder = painterResource(R.drawable.ic_puck_vector),
            error = painterResource(R.drawable.ic_puck_vector),
            contentDescription = "$name logo",
            modifier = Modifier.size(40.dp)
        )
    }
}

private fun formatHeroDate(millis: Long): String {
    if (millis <= 0L) return "Saturday • 7:00 PM"
    return SimpleDateFormat("EEEE • h:mm a", Locale.US).format(Date(millis))
}

private fun formatTimer(countdown: CountdownState): String {
    val h = if (countdown.days > 0 || countdown.hours > 0) countdown.hours else 2L
    val m = if (countdown.days > 0 || countdown.hours > 0 || countdown.minutes > 0) countdown.minutes else 14L
    val s = if (countdown.days > 0 || countdown.hours > 0 || countdown.minutes > 0 || countdown.seconds > 0) countdown.seconds else 22L
    return if (countdown.days > 0) {
        String.format(Locale.US, "%dd %02dh %02dm", countdown.days, countdown.hours, countdown.minutes)
    } else {
        String.format(Locale.US, "%02d : %02d : %02d", h, m, s)
    }
}

@Composable
private fun LivePulseBadge(palette: JerseyPalette) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val alpha by transition.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(800), repeatMode = RepeatMode.Reverse), label = "alpha"
    )
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = palette.accentRed.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, palette.accentRed),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(palette.accentRed.copy(alpha = alpha)))
            Text("PUCK DROP LIVE", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp, color = palette.accentRed))
        }
    }
}

@Composable
fun FlipClockDigitBox(value: Long, unit: String, width: Dp = 66.dp) {}
