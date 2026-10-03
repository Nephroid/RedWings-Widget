package com.redwings.widget.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.redwings.widget.ui.theme.FrostedIceColors
import com.redwings.widget.ui.theme.FrostedIceTheme
import java.util.Locale

/**
 * FrostedGlassCard: Translucent white card with 24.dp rounded corners,
 * ambient soft drop shadow, and a subtle 1.dp vertical gradient white border.
 */
@Composable
fun FrostedGlassCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(24.dp)
    Box(
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.06f),
                spotColor = Color.Black.copy(alpha = 0.06f)
            )
            .clip(shape)
            .background(FrostedIceColors.GlassSurface)
            .border(BorderStroke(1.dp, FrostedIceColors.GlassBorder), shape)
            .padding(16.dp),
        content = content
    )
}

/**
 * AnchoredArenaCapsule: Pill container styled with light ice glass,
 * designed to anchor over bottom borders cleanly (50% vertical overlap).
 */
@Composable
fun AnchoredArenaCapsule(
    arenaName: String,
    modifier: Modifier = Modifier
) {
    val pillShape = CircleShape
    Surface(
        modifier = modifier
            .shadow(
                elevation = 2.dp,
                shape = pillShape,
                ambientColor = Color.Black.copy(alpha = 0.05f)
            )
            .border(BorderStroke(1.dp, FrostedIceColors.GlassBorder), pillShape),
        shape = pillShape,
        color = Color.White.copy(alpha = 0.85f)
    ) {
        Text(
            text = arenaName,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            ),
            color = FrostedIceColors.TextPrimary,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}

/**
 * GlowingSeedBadge: Standings pill; applies RedWingsRed fill and soft outer glow when isActive = true.
 */
@Composable
fun GlowingSeedBadge(
    seed: String,
    team: String,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    val bgColor = if (isActive) FrostedIceColors.RedWingsRed else Color.White.copy(alpha = 0.65f)
    val textColor = if (isActive) Color.White else FrostedIceColors.TextPrimary

    val shadowModifier = if (isActive) {
        modifier.shadow(
            elevation = 6.dp,
            shape = shape,
            ambientColor = FrostedIceColors.ActiveGlowRed,
            spotColor = FrostedIceColors.ActiveGlowRed
        )
    } else {
        modifier
    }

    Box(
        modifier = shadowModifier
            .clip(shape)
            .background(bgColor)
            .then(
                if (!isActive) Modifier.border(
                    BorderStroke(1.dp, FrostedIceColors.GlassBorder),
                    shape
                ) else Modifier
            )
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$seed ",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                ),
                color = if (isActive) Color.White.copy(alpha = 0.85f) else FrostedIceColors.TextMuted
            )
            Text(
                text = team,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp
                ),
                color = textColor
            )
        }
    }
}

/**
 * CountdownClock: Formats remaining seconds to HH:mm:ss using RedWingsRed digital monospace
 * typography without triggering recompositions of outer parent composables.
 */
@Composable
fun CountdownClock(
    remainingSecondsProvider: () -> Long,
    modifier: Modifier = Modifier
) {
    val seconds = remainingSecondsProvider()
    val formattedTime = remember(seconds) {
        val totalSec = seconds.coerceAtLeast(0L)
        val hrs = totalSec / 3600
        val mins = (totalSec % 3600) / 60
        val secs = totalSec % 60
        String.format(Locale.US, "%02d:%02d:%02d", hrs, mins, secs)
    }

    Text(
        text = formattedTime,
        style = MaterialTheme.typography.headlineMedium.copy(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp,
            fontSize = 24.sp
        ),
        color = FrostedIceColors.RedWingsRed,
        modifier = modifier
    )
}

// =================== PREVIEWS ===================

@Preview(showBackground = true, backgroundColor = 0xFFE2EBF1)
@Composable
private fun PreviewFrostedGlassCard() {
    FrostedIceTheme {
        FrostedGlassCard {
            Text(
                text = "Frosted Glass Card Content",
                style = MaterialTheme.typography.bodyLarge,
                color = FrostedIceColors.TextPrimary
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFE2EBF1)
@Composable
private fun PreviewAnchoredArenaCapsule() {
    FrostedIceTheme {
        AnchoredArenaCapsule(arenaName = "Little Caesars Arena")
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFE2EBF1)
@Composable
private fun PreviewGlowingSeedBadge() {
    FrostedIceTheme {
        Row {
            GlowingSeedBadge(seed = "4", team = "DET", isActive = true)
            GlowingSeedBadge(seed = "1", team = "BOS", isActive = false, modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFE2EBF1)
@Composable
private fun PreviewCountdownClock() {
    FrostedIceTheme {
        CountdownClock(remainingSecondsProvider = { 8062L })
    }
}
