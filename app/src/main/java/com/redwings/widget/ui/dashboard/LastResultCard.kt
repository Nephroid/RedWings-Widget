package com.redwings.widget.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.redwings.widget.data.model.GameStarUi
import com.redwings.widget.data.model.getTeamLogoUrlFallback
import com.redwings.widget.ui.LastGameUi
import com.redwings.widget.ui.theme.LocalJerseyPalette
import java.util.Locale

/**
 * Frosted Ice glassmorphism card for the last game result.
 * Displays specular acrylic finish, score clash with team crests, bold W/L badge, recap pills,
 * and the 3 Players / Stars of the Game with headshots.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LastResultCard(
    lastGame: LastGameUi,
    modifier: Modifier = Modifier,
    showFormGuide: Boolean = false,
    formHistory: List<String> = emptyList(),
    recapPills: List<String> = emptyList(),
    threeStars: List<GameStarUi> = lastGame.threeStars
) {
    val palette = LocalJerseyPalette.current
    val oppAbbrev = lastGame.opponentAbbrev.ifBlank { "OPP" }
    val oppName = lastGame.opponent.ifBlank { "Opponent" }
    val dateText = lastGame.dateLabel.ifBlank { "Recent" }

    val detScore = lastGame.wingsScore
    val oppScore = lastGame.oppScore
    val isWin = lastGame.isWinner || (detScore > oppScore)

    val pills = when {
        lastGame.recapPills.isNotEmpty() -> lastGame.recapPills
        recapPills.isNotEmpty() -> recapPills
        else -> emptyList()
    }

    FrostedGlassCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: "LAST RESULT • Tue, Sep 30" + Bold W/L badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LAST RESULT • $dateText",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = palette.secondaryText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp
                    )
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isWin) WinGreenSolid else LossRedSolid,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                    shadowElevation = 2.dp,
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (isWin) "W" else "L",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                        )
                    }
                }
            }

            // Center score clash: DET 4 – 2 BOS with Winged Wheel crest & Opponent crest
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Detroit side
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_redwings_logo),
                        contentDescription = "Detroit Red Wings logo",
                        modifier = Modifier.size(42.dp)
                    )
                    Column {
                        Text(
                            text = "DET",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, fontSize = 20.sp, color = palette.primaryText)
                        )
                        Text(
                            text = if (lastGame.isHome) "HOME" else "AWAY",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp, color = if (lastGame.isHome) palette.accentRed else palette.secondaryText)
                        )
                    }
                }

                // Center clash: 4 – 2
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "$detScore",
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black, fontSize = 30.sp, color = if (detScore >= oppScore) palette.primaryText else palette.secondaryText)
                    )
                    Text(
                        text = "–",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp, color = palette.secondaryText.copy(alpha = 0.5f))
                    )
                    Text(
                        text = "$oppScore",
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black, fontSize = 30.sp, color = if (oppScore >= detScore) palette.primaryText else palette.secondaryText)
                    )
                }

                // Opponent side
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = oppAbbrev,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, fontSize = 20.sp, color = palette.primaryText)
                        )
                        Text(
                            text = if (!lastGame.isHome) "HOME" else "AWAY",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp, color = if (!lastGame.isHome) palette.accentRed else palette.secondaryText)
                        )
                    }
                    TeamLogo(
                        abbrev = oppAbbrev,
                        size = 40.dp
                    )
                }
            }

            // Clean goal scorer / recap pills
            if (pills.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    pills.forEach { pillText ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = palette.cardBorder.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, palette.cardBorder.copy(alpha = 0.7f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(palette.accentRed))
                                Text(
                                    text = pillText,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp, color = palette.primaryText),
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }

            // Form Guide (optional)
            if (showFormGuide && formHistory.isNotEmpty()) {
                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Form:",
                        style = MaterialTheme.typography.labelSmall.copy(color = palette.secondaryText, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    )
                    formHistory.forEach { outcome ->
                        val outcomeWin = outcome.equals("W", ignoreCase = true)
                        Box(
                            modifier = Modifier.size(20.dp).clip(CircleShape).background(if (outcomeWin) WinGreenSolid else LossRedSolid),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = outcome.take(1).uppercase(Locale.US),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 10.sp, color = Color.White)
                            )
                        }
                    }
                }
            }

            // 3 Players of the Game (Three Stars)
            if (threeStars.isNotEmpty()) {
                HorizontalDivider(
                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp),
                    color = palette.cardBorder.copy(alpha = 0.5f),
                    thickness = 1.dp
                )
                ThreeStarsSection(stars = threeStars)
            }
        }
    }
}

/**
 * ThreeStarsSection: Displays the 3 Players / Stars of the Game with their
 * headshot photos, player names, star indicators, and relevant game stats.
 */
@Composable
fun ThreeStarsSection(
    stars: List<GameStarUi>,
    modifier: Modifier = Modifier
) {
    val palette = LocalJerseyPalette.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("three_stars_section"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Section title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "★",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFFFFD700),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                )
                Text(
                    text = "3 PLAYERS OF THE GAME",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = palette.secondaryText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                )
            }
            Text(
                text = "THREE STARS",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = palette.secondaryText.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                )
            )
        }

        // 3 player star cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            stars.take(3).forEach { star ->
                StarPlayerCard(
                    star = star,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * StarPlayerCard: High-density frosted glass player card displaying
 * star rank badge (1st/2nd/3rd), circular headshot, player name, team/position, and stat line.
 */
@Composable
fun StarPlayerCard(
    star: GameStarUi,
    modifier: Modifier = Modifier
) {
    val palette = LocalJerseyPalette.current

    val (starColor, starBg, starLabel) = when (star.star) {
        1 -> Triple(Color(0xFFFFD700), Color(0xFFFFD700).copy(alpha = 0.15f), "1ST")
        2 -> Triple(Color(0xFFC0C0C0), Color(0xFFC0C0C0).copy(alpha = 0.15f), "2ND")
        3 -> Triple(Color(0xFFCD7F32), Color(0xFFCD7F32).copy(alpha = 0.15f), "3RD")
        else -> Triple(palette.secondaryText, palette.cardBorder.copy(alpha = 0.2f), "${star.star}TH")
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = palette.cardBorder.copy(alpha = 0.18f),
        border = BorderStroke(1.dp, palette.cardBorder.copy(alpha = 0.55f)),
        modifier = modifier.testTag("star_player_card_${star.star}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Star Badge Pill
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = starBg,
                border = BorderStroke(0.5.dp, starColor.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "★",
                        color = starColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = starLabel,
                        color = starColor,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 9.sp,
                            letterSpacing = 0.3.sp
                        )
                    )
                }
            }

            // Headshot photo
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(palette.cardSurface)
                    .border(
                        BorderStroke(
                            1.5.dp,
                            if (star.star == 1) starColor.copy(alpha = 0.85f) else palette.cardBorder.copy(alpha = 0.7f)
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(star.headshotUrl.ifBlank { null })
                        .crossfade(true)
                        .build(),
                    contentDescription = "${star.name} photo",
                    placeholder = painterResource(R.drawable.ic_puck_vector),
                    fallback = painterResource(R.drawable.ic_puck_vector),
                    error = painterResource(R.drawable.ic_puck_vector),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Player Name
            Text(
                text = star.name,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = palette.primaryText
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            // Team & Jersey/Position details
            val meta = buildString {
                if (star.teamAbbrev.isNotBlank()) append(star.teamAbbrev)
                if (star.position.isNotBlank()) {
                    if (isNotEmpty()) append(" • ")
                    append(star.position)
                }
                if (star.sweaterNo != null && star.sweaterNo > 0) {
                    if (isNotEmpty()) append(" • #") else append("#")
                    append(star.sweaterNo)
                }
            }
            if (meta.isNotBlank()) {
                Text(
                    text = meta,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp,
                        color = if (star.teamAbbrev.equals("DET", ignoreCase = true)) palette.accentRed else palette.secondaryText
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            // Stat line pill
            if (star.statLine.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = palette.cardSurface.copy(alpha = 0.85f),
                    border = BorderStroke(0.5.dp, palette.cardBorder.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = star.statLine,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = palette.primaryText
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

