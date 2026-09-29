package com.redwings.widget.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.redwings.widget.BuildConfig
import com.redwings.widget.ui.theme.WingsRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Custom palette matching mockup */
val AppDarkBg = Color(0xFF0F1014)
val CardDarkSurface = Color(0xFF1B1D22)
val HeroGradientTop = Color(0xFFCE1126)
val HeroGradientBottom = Color(0xFF750713)
val FlipTileBg = Color(0xFF1F0407)
val FlipTileBorder = Color(0xFF4A0A10)
val WinGreenSolid = Color(0xFF388E3C)
val LossRedSolid = Color(0xFFD32F2F)
val PillDarkBg = Color(0xFF26282E)
val StandingsDetHighlight = Color(0xFF521319)
val ClinchedGreen = Color(0xFF4CAF50)
val PlayoffCutoffLineColor = Color(0xFFE5A823)

/** Top Header Banner with massive bold block typography matching mockup */
@Composable
fun AppHeaderBanner(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(WingsRed)
            .padding(top = 16.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "RED WINGS",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 32.sp,
                    letterSpacing = 4.sp,
                    color = Color.White
                )
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White.copy(alpha = 0.9f),
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        Box(modifier = Modifier.align(Alignment.CenterEnd)) {
            if (isRefreshing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.5.dp,
                    color = Color.White
                )
            } else {
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.testTag("refresh_button").size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Refresh schedule",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

/** Subtle ice-rink background geometry */
@Composable
fun IceRinkBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val centerX = size.width / 2f
        val centerY = size.height * 0.42f
        val radius = size.width * 0.42f
        val faintRed = WingsRed.copy(alpha = 0.05f)

        // Center line
        drawLine(
            color = faintRed,
            start = Offset(0f, centerY),
            end = Offset(size.width, centerY),
            strokeWidth = 2.dp.toPx()
        )
        // Center ice circle
        drawCircle(
            color = faintRed,
            radius = radius,
            center = Offset(centerX, centerY),
            style = Stroke(width = 2.dp.toPx())
        )
        // Center ice dot
        drawCircle(
            color = faintRed,
            radius = 6.dp.toPx(),
            center = Offset(centerX, centerY)
        )
    }
}

@Composable
fun formatGameTime(millis: Long): String {
    if (millis <= 0L) return "Sat, Oct 4 • 7:00 PM"
    return remember(millis) {
        SimpleDateFormat("EEE, MMM d • h:mm a", Locale.US).format(Date(millis))
    }
}

fun formatUpcomingDate(millis: Long): String {
    if (millis <= 0L) return "Sat, Oct 4"
    return SimpleDateFormat("EEE, MMM d", Locale.US).format(Date(millis))
}

@Composable
fun AppFooter(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Detroit Red Wings Widget v${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF6E727A),
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = "Automated Build • Atlantic Division",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = Color(0xFF555960)
        )
    }
}
