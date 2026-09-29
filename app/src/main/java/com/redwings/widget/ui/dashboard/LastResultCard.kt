package com.redwings.widget.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.redwings.widget.ui.LastGameUi
import com.redwings.widget.ui.theme.LocalJerseyPalette

/** Final-result card for the last game with prominent W/L badge and optional form guide */
@Composable
fun LastResultCard(
    lastGame: LastGameUi,
    modifier: Modifier = Modifier,
    showFormGuide: Boolean = false,
    formHistory: List<String> = listOf("W", "W", "L", "W", "W")
) {
    val palette = LocalJerseyPalette.current

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = palette.cardSurface),
        border = BorderStroke(1.dp, palette.cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(18.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "LAST RESULT • ${lastGame.dateLabel.ifBlank { "Tue, Sep 30" }}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = palette.secondaryText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Text(
                        text = "DET ${lastGame.wingsScore} – ${lastGame.oppScore} ${lastGame.opponentAbbrev.ifBlank { "BOS" }}",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp,
                            color = palette.primaryText
                        )
                    )
                    Text(
                        text = if (lastGame.isHome) "Home" else "Away",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = palette.secondaryText,
                            fontSize = 13.sp
                        )
                    )
                }

                // Prominent green/red square W/L badge
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (lastGame.isWinner) WinGreenSolid else LossRedSolid,
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (lastGame.isWinner) "W" else "L",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 28.sp,
                                color = Color.White
                            )
                        )
                    }
                }
            }

            if (showFormGuide && formHistory.isNotEmpty()) {
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Form:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = palette.secondaryText,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    formHistory.forEach { outcome ->
                        val isWin = outcome.equals("W", ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(if (isWin) WinGreenSolid else LossRedSolid),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = outcome.take(1).uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
