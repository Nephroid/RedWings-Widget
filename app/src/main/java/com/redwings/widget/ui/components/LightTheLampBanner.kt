package com.redwings.widget.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.redwings.widget.ui.theme.LocalJerseyPalette
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@Composable
fun LightTheLampBanner(
    pulseCount: Long,
    onLightTheLamp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalJerseyPalette.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "lampScale"
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = palette.cardSurface.copy(alpha = 0.90f),
        border = BorderStroke(1.2.dp, palette.accentRed.copy(alpha = 0.65f)),
        shadowElevation = 3.dp,
        modifier = modifier
            .fillMaxWidth()
            .scale(scale),
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            isPressed = true
            onLightTheLamp()
            scope.launch {
                delay(150)
                isPressed = false
            }
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = palette.accentRed,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🚨",
                            fontSize = 18.sp
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "LIGHT THE LAMP",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            letterSpacing = 0.6.sp,
                            color = palette.primaryText
                        )
                    )
                    Text(
                        text = "Sound the Little Caesars Arena goal horn",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = palette.secondaryText
                        )
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = palette.accentRed.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, palette.accentRed.copy(alpha = 0.4f))
            ) {
                Text(
                    text = "🔥 ${NumberFormat.getNumberInstance(Locale.US).format(pulseCount)}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        color = palette.accentRed
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
