package com.redwings.widget.ui.dashboard

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
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

/** Countdown hero for the next game with gradient backdrop, logos, and flip-clock digit boxes. */
@Composable
fun NextGameHero(
    countdown: CountdownState,
    game: NextGameUi?,
    modifier: Modifier = Modifier,
    tileWidth: Dp = 66.dp,
    showFlankingBadges: Boolean = false
) {
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

                    if (showFlankingBadges) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(R.drawable.ic_redwings_logo)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Detroit Red Wings logo",
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                    }

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

                        FlipClockDigitBox(value = d, unit = "D", width = tileWidth)
                        FlipClockDigitBox(value = h, unit = "H", width = tileWidth)
                        FlipClockDigitBox(value = m, unit = "M", width = tileWidth)
                        FlipClockDigitBox(value = s, unit = "S", width = tileWidth)
                    }
                }
            }
        }
    }
}

/** Authentic flip-clock split-flap digit tile */
@Composable
fun FlipClockDigitBox(value: Long, unit: String, width: Dp = 66.dp) {
    val displayNum = String.format("%02d", value.coerceAtLeast(0L))
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = FlipTileBg,
        border = BorderStroke(1.dp, FlipTileBorder),
        modifier = Modifier.width(width)
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
fun LivePuckDropBanner() {
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
