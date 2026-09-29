package com.redwings.widget.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.redwings.widget.BuildConfig
import com.redwings.widget.R
import com.redwings.widget.ui.theme.AppJersey
import com.redwings.widget.ui.theme.LocalJerseyPalette
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Custom palette matching mockup */
val AppDarkBg = Color(0xFF0F1014); val CardDarkSurface = Color(0xFF1B1D22)
val HeroGradientTop = Color(0xFFCE1126); val HeroGradientBottom = Color(0xFF750713)
val FlipTileBg = Color(0xFF1F0407); val FlipTileBorder = Color(0xFF4A0A10)
val WinGreenSolid = Color(0xFF388E3C); val LossRedSolid = Color(0xFFD32F2F)
val PillDarkBg = Color(0xFF26282E); val StandingsDetHighlight = Color(0xFF521319)
val ClinchedGreen = Color(0xFF4CAF50); val PlayoffCutoffLineColor = Color(0xFFE5A823)

/** Dual horizontal white racing stripes framing the Home Red sweater stripe */
@Composable
private fun DualRacingStripes(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(Color.White))
        Box(modifier = Modifier.fillMaxWidth().height(1.5.dp).background(Color.White.copy(alpha = 0.85f)))
    }
}

/**
 * Top Header Banner styled after the official Detroit Red Wings Home Red sweater stripe.
 * Framed by dual horizontal white racing stripes, an embossed Winged Wheel crest,
 * bold athletic wordmark, and a frosted glass jersey toggle pill.
 */
@Composable
fun AppHeaderBanner(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    activeJersey: AppJersey = AppJersey.HERITAGE,
    onJerseyThemeToggle: () -> Unit = {}
) {
    val palette = LocalJerseyPalette.current
    val sweaterBrush = remember(palette.headerBackground, palette.heroBottom) {
        Brush.verticalGradient(listOf(palette.headerBackground, palette.heroBottom))
    }

    Column(modifier = modifier.fillMaxWidth().background(sweaterBrush)) {
        DualRacingStripes() // Dual horizontal white racing stripes framing top

        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Controls Bar: Frosted Glass Jersey Theme Pill & Refresh Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onJerseyThemeToggle,
                    shape = RoundedCornerShape(999.dp),
                    color = Color.White.copy(alpha = 0.20f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.50f)),
                    shadowElevation = 2.dp,
                    modifier = Modifier.testTag("jersey_theme_chip")
                ) {
                    Text(
                        text = activeJersey.chipLabel,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 0.5.sp
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Box(modifier = Modifier.size(32.dp), contentAlignment = Alignment.Center) {
                    if (isRefreshing) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                    } else {
                        IconButton(onClick = onRefresh, modifier = Modifier.testTag("refresh_button").size(32.dp)) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh schedule", tint = Color.White)
                        }
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // Center: Embossed Winged Wheel Crest + Bold Athletic Wordmark DETROIT RED WINGS
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .shadow(elevation = 6.dp, shape = CircleShape)
                        .background(Brush.radialGradient(listOf(Color.White.copy(alpha = 0.25f), Color.Transparent)), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_redwings_logo),
                        contentDescription = "Detroit Red Wings Crest",
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "DETROIT RED WINGS",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Black, fontSize = 22.sp, letterSpacing = 2.5.sp, color = Color.White
                    ),
                    maxLines = 1
                )
            }

            if (!subtitle.isNullOrBlank()) {
                Spacer(Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White.copy(alpha = 0.90f), letterSpacing = 1.2.sp, fontWeight = FontWeight.Bold, fontSize = 11.sp
                    ),
                    maxLines = 1
                )
            }
        }

        DualRacingStripes() // Dual horizontal white racing stripes framing bottom
    }
}

/**
 * Photorealistic frosted ice rink surface with subtle red center line,
 * blue lines, and center faceoff circle under frosted acrylic glass.
 */
@Composable
fun IceRinkBackground(modifier: Modifier = Modifier) {
    val palette = LocalJerseyPalette.current

    Canvas(modifier = modifier.fillMaxSize()) {
        val centerX = size.width / 2f
        val centerY = size.height * 0.42f
        val circleRadius = size.width * 0.38f
        val neutralZoneSpacing = size.height * 0.18f

        val rinkRed = palette.accentRed.copy(alpha = 0.15f)
        val rinkBlue = Color(0xFF1565C0).copy(alpha = 0.16f)

        // 1. Ice base subtle temperature shading
        drawRect(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.04f), Color(0xFFE3F2FD).copy(alpha = 0.03f), Color.Transparent)))

        // 2. Neutral zone blue lines
        val topBlueY = centerY - neutralZoneSpacing
        val bottomBlueY = centerY + neutralZoneSpacing
        if (topBlueY > 0f) drawLine(rinkBlue, Offset(0f, topBlueY), Offset(size.width, topBlueY), 3.dp.toPx())
        if (bottomBlueY < size.height) drawLine(rinkBlue, Offset(0f, bottomBlueY), Offset(size.width, bottomBlueY), 3.dp.toPx())

        // 3. Center red line
        drawLine(rinkRed, Offset(0f, centerY), Offset(size.width, centerY), 3.dp.toPx())

        // 4. Center faceoff circle (NHL regulation blue ring)
        drawCircle(rinkBlue, circleRadius, Offset(centerX, centerY), style = Stroke(width = 2.5.dp.toPx()))

        // 5. Center ice red dot
        drawCircle(rinkRed, radius = 6.dp.toPx(), center = Offset(centerX, centerY))

        // 6. Neutral zone faceoff spots
        val dotOffset = circleRadius * 0.72f
        val dotRadius = 3.dp.toPx()
        drawCircle(rinkRed, dotRadius, Offset(centerX - dotOffset, centerY - neutralZoneSpacing * 0.5f))
        drawCircle(rinkRed, dotRadius, Offset(centerX + dotOffset, centerY - neutralZoneSpacing * 0.5f))
        drawCircle(rinkRed, dotRadius, Offset(centerX - dotOffset, centerY + neutralZoneSpacing * 0.5f))
        drawCircle(rinkRed, dotRadius, Offset(centerX + dotOffset, centerY + neutralZoneSpacing * 0.5f))

        // 7. Zamboni skate blade etchings
        val etchColor = Color.White.copy(alpha = 0.04f)
        drawLine(etchColor, Offset(size.width * 0.1f, centerY - 60.dp.toPx()), Offset(size.width * 0.4f, centerY - 10.dp.toPx()), 1.dp.toPx())
        drawLine(etchColor, Offset(size.width * 0.6f, centerY + 20.dp.toPx()), Offset(size.width * 0.9f, centerY + 70.dp.toPx()), 1.dp.toPx())
        drawLine(etchColor, Offset(size.width * 0.2f, centerY + 40.dp.toPx()), Offset(size.width * 0.5f, centerY + 90.dp.toPx()), 1.dp.toPx())

        // 8. Frosted acrylic glass specular sheen & diffusion overlay
        drawRect(
            brush = Brush.radialGradient(
                listOf(Color.White.copy(alpha = 0.12f), Color.White.copy(alpha = 0.03f), Color.Transparent),
                center = Offset(centerX, centerY),
                radius = size.width * 0.85f
            )
        )
    }
}

@Composable
fun formatGameTime(millis: Long): String {
    if (millis <= 0L) return "Sat, Oct 4 • 7:00 PM"
    return remember(millis) { SimpleDateFormat("EEE, MMM d • h:mm a", Locale.US).format(Date(millis)) }
}

fun formatUpcomingDate(millis: Long): String {
    if (millis <= 0L) return "Sat, Oct 4"
    return SimpleDateFormat("EEE, MMM d", Locale.US).format(Date(millis))
}

@Composable
fun AppFooter(modifier: Modifier = Modifier) {
    val palette = LocalJerseyPalette.current
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Detroit Red Wings Widget v${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.labelSmall,
            color = palette.secondaryText,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = "Automated Build • Atlantic Division",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = palette.secondaryText.copy(alpha = 0.8f)
        )
    }
}
