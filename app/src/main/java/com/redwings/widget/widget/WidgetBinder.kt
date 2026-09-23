package com.redwings.widget.widget

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.widget.RemoteViews
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.redwings.widget.R
import com.redwings.widget.data.model.getTeamLogoUrlFallback
import java.util.concurrent.TimeUnit

/**
 * Binds game data into RemoteViews. Seam type [WidgetGame] decouples the
 * widget layer from the Room entity: provider maps RedWingsGame -> WidgetGame.
 * Logo URLs come from data-layer TeamUtils (ESPN PNG fallback, no SVG decoder
 * needed); venue/standings prefer entity fields with "RedWingsPrefs" fallback.
 */
object WidgetBinder {

    data class WidgetGame(
        val opponentName: String,
        val opponentAbbrev: String,
        val gameTimeMillis: Long,
        val isHomeGame: Boolean,
        val venue: String = "",
        val standingLine: String = "",
        val h2hLine: String = ""
    )

    const val PREFS = "RedWingsPrefs"
    private const val TAG = "WingsWidget"
    private const val DET_ABBR = "DET"

    /** "3d 04h 12m" / "04h 12m" / "PUCK DROP! LIVE" (<=0 within 3h) / "Final". */
    fun formatCountdown(gameTimeMillis: Long, now: Long = System.currentTimeMillis()): String {
        val diff = gameTimeMillis - now
        if (diff <= 0) {
            return if (diff > -TimeUnit.HOURS.toMillis(3)) "PUCK DROP! LIVE" else "Final"
        }
        val days = TimeUnit.MILLISECONDS.toDays(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff) % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60
        return buildString {
            if (days > 0) append("${days}d ")
            append(String.format("%02dh %02dm", hours, minutes))
        }
    }

    suspend fun bindGameData(context: Context, views: RemoteViews, game: WidgetGame, theme: WidgetTheme) {
        val prefix = if (game.isHomeGame) "vs." else "at"
        views.setTextViewText(R.id.widget_opponent, "$prefix ${game.opponentName}")
        views.setTextViewText(R.id.widget_countdown, formatCountdown(game.gameTimeMillis))

        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val venue = game.venue.ifEmpty {
            prefs.getString("next_game_venue", "Little Caesars Arena") ?: "Little Caesars Arena"
        }
        val homeAway = if (game.isHomeGame) "Home" else "Away"
        views.setTextViewText(R.id.widget_venue_info, "$venue • $homeAway")

        val standing = game.standingLine.ifEmpty {
            prefs.getString("redwings_standing_summary", null) ?: "Atlantic Division"
        }
        val h2h = game.h2hLine.ifEmpty {
            prefs.getString("head_to_head_record", null) ?: ""
        }
        views.setTextViewText(
            R.id.widget_standing_h2h,
            if (h2h.isNotEmpty()) "$standing • $h2h" else standing
        )

        // Away logo left, home logo right (ESPN PNGs need no SVG decoder).
        val oppBitmap = loadLogoBitmap(context, getTeamLogoUrlFallback(game.opponentAbbrev))
        val detBitmap = loadLogoBitmap(context, getTeamLogoUrlFallback(DET_ABBR))
        val (awayBmp, awayFallback) = if (game.isHomeGame) {
            oppBitmap to R.drawable.ic_puck_placeholder
        } else {
            detBitmap to R.drawable.ic_redwings_logo
        }
        val (homeBmp, homeFallback) = if (game.isHomeGame) {
            detBitmap to R.drawable.ic_redwings_logo
        } else {
            oppBitmap to R.drawable.ic_puck_placeholder
        }
        if (awayBmp != null) views.setImageViewBitmap(R.id.widget_away_logo, awayBmp)
        else views.setImageViewResource(R.id.widget_away_logo, awayFallback)
        if (homeBmp != null) views.setImageViewBitmap(R.id.widget_home_logo, homeBmp)
        else views.setImageViewResource(R.id.widget_home_logo, homeFallback)
    }

    fun bindEmptyState(context: Context, views: RemoteViews, theme: WidgetTheme) {
        views.setTextViewText(R.id.widget_opponent, "No Scheduled Games")
        views.setTextViewText(R.id.widget_countdown, "--")
        views.setTextViewText(R.id.widget_venue_info, "Little Caesars Arena")
        views.setTextViewText(R.id.widget_standing_h2h, "Standings unavailable")
        try {
            views.setImageViewResource(R.id.widget_away_logo, R.drawable.ic_redwings_logo)
            views.setImageViewResource(R.id.widget_home_logo, R.drawable.ic_puck_placeholder)
        } catch (e: Exception) {
            Log.e(TAG, "empty-state logos missing: ${e.message}")
        }
    }

    private suspend fun loadLogoBitmap(context: Context, url: String): Bitmap? {
        return try {
            val result = context.imageLoader.execute(
                ImageRequest.Builder(context).data(url).allowHardware(false).build()
            )
            if (result is SuccessResult) {
                val raw = result.drawable.toBitmap()
                if (raw.width <= 0 || raw.height <= 0) return null
                val maxDim = 120 // avoid TransactionTooLargeException
                if (raw.width > maxDim || raw.height > maxDim) {
                    val ratio = raw.width.toFloat() / raw.height.toFloat()
                    val (w, h) = if (raw.width > raw.height) {
                        maxDim to (maxDim / ratio).toInt().coerceAtLeast(1)
                    } else {
                        (maxDim * ratio).toInt().coerceAtLeast(1) to maxDim
                    }
                    Bitmap.createScaledBitmap(raw, w, h, true)
                } else raw
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "logo load failed $url: ${e.message}")
            null
        }
    }
}
