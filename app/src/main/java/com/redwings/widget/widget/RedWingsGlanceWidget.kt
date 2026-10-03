package com.redwings.widget.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.unit.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.redwings.widget.MainActivity
import com.redwings.widget.R

/**
 * Helper to produce a stable ColorProvider across day and night themes.
 */
private fun solidColor(color: Color): ColorProvider = ColorProvider(color)

/**
 * Android Glance Widget for Red Wings:
 * Implements the 16:9 frosted glass card layout featuring:
 * 1. Embedded top RedWingsRed header with stripes & wordmark
 * 2. Matchup section with records, @ badge, countdown timer, and arena pill
 * 3. Bottom horizontal division standings ticker dividing seeds 1-4 and 5-8, highlighting '4 DET'.
 */
class RedWingsGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                WidgetContent(context)
            }
        }
    }

    @Composable
    private fun WidgetContent(context: Context) {
        val prefs = context.getSharedPreferences("RedWingsPrefs", Context.MODE_PRIVATE)
        val opponent = prefs.getString("next_game_opponent_abbrev", "TOR") ?: "TOR"
        val wingsRecord = "42-30-10"
        val oppRecord = "46-26-10"
        val countdownText = "02:14:22"

        val frostedBg = Color(0xFFF0F5F8)
        val wingsRed = Color(0xFFC8102E)
        val textPrimary = Color(0xFF111827)
        val textMuted = Color(0xFF4B5563)

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .cornerRadius(20.dp)
                .background(frostedBg)
                .clickable(actionStartActivity<MainActivity>())
        ) {
            Column(modifier = GlanceModifier.fillMaxSize()) {
                // 1. Embedded Red Wings Header Bar
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .background(wingsRed)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            provider = ImageProvider(R.drawable.ic_redwings_logo),
                            contentDescription = "Red Wings Logo",
                            modifier = GlanceModifier.size(18.dp)
                        )
                        Spacer(modifier = GlanceModifier.width(6.dp))
                        Text(
                            text = "DETROIT RED WINGS",
                            style = TextStyle(
                                color = solidColor(Color.White),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = GlanceModifier.defaultWeight())
                        Text(
                            text = "NEXT GAME",
                            style = TextStyle(
                                color = solidColor(Color.White.copy(alpha = 0.9f)),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }

                // 2. Compact Matchup Layout
                Column(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Away Team
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = opponent,
                                style = TextStyle(
                                    color = solidColor(textPrimary),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = oppRecord,
                                style = TextStyle(
                                    color = solidColor(textMuted),
                                    fontSize = 9.sp
                                )
                            )
                        }

                        Spacer(modifier = GlanceModifier.width(12.dp))

                        // Matchup @ badge
                        Box(
                            modifier = GlanceModifier
                                .size(20.dp)
                                .cornerRadius(10.dp)
                                .background(wingsRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "@",
                                style = TextStyle(
                                    color = solidColor(Color.White),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Spacer(modifier = GlanceModifier.width(12.dp))

                        // Home Team
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "DET",
                                style = TextStyle(
                                    color = solidColor(wingsRed),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = wingsRecord,
                                style = TextStyle(
                                    color = solidColor(textMuted),
                                    fontSize = 9.sp
                                )
                            )
                        }

                        Spacer(modifier = GlanceModifier.width(16.dp))

                        // Countdown
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = countdownText,
                                style = TextStyle(
                                    color = solidColor(wingsRed),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "Little Caesars Arena",
                                style = TextStyle(
                                    color = solidColor(textMuted),
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }

                // 3. Atlantic Division Standings Ticker (Playoff Seeds 1-4 vs In The Hunt 5-8)
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SEEDS 1-4: ",
                            style = TextStyle(
                                color = solidColor(textMuted),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "1.BOS  2.TOR  3.FLA ",
                            style = TextStyle(
                                color = solidColor(textPrimary),
                                fontSize = 8.5.sp
                            )
                        )
                        Box(
                            modifier = GlanceModifier
                                .cornerRadius(4.dp)
                                .background(wingsRed)
                                .padding(horizontal = 3.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "4.DET",
                                style = TextStyle(
                                    color = solidColor(Color.White),
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Spacer(modifier = GlanceModifier.width(6.dp))
                        Text(
                            text = "| HUNT: 5.TBL 6.MTL 7.OTT 8.BUF",
                            style = TextStyle(
                                color = solidColor(textMuted),
                                fontSize = 8.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

class RedWingsGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = RedWingsGlanceWidget()
}
