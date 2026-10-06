package com.redwings.widget.widget

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.redwings.widget.R
import com.redwings.widget.data.model.getTeamLogoUrl
import kotlinx.coroutines.withTimeoutOrNull
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
        val h2hLine: String = "",
        val awayRecord: String = "",
        val homeRecord: String = ""
    )

    const val PREFS = "RedWingsPrefs"
    private const val TAG = "WingsWidget"
    private const val DET_ABBR = "DET"

    /** "3d 04h 12m" / "04h 12m" / "PUCK DROP! LIVE" (<=0 within 3h) / "Final". */
    fun formatCountdown(gameTimeMillis: Long, now: Long = System.currentTimeMillis()): String {
        if (gameTimeMillis <= 0L) return "-- : --"
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

    /** Formats timestamp to 'SAT • 7:00 PM' or 'TODAY • 7:00 PM'. */
    fun formatGameDateTime(gameTimeMillis: Long, now: Long = System.currentTimeMillis()): String {
        if (gameTimeMillis <= 0) return "NEXT GAME"
        val gameCal = java.util.Calendar.getInstance().apply { timeInMillis = gameTimeMillis }
        val nowCal = java.util.Calendar.getInstance().apply { timeInMillis = now }
        val isSameDay = gameCal.get(java.util.Calendar.YEAR) == nowCal.get(java.util.Calendar.YEAR) &&
            gameCal.get(java.util.Calendar.DAY_OF_YEAR) == nowCal.get(java.util.Calendar.DAY_OF_YEAR)
        val timeFmt = java.text.SimpleDateFormat("h:mm a", java.util.Locale.US).format(java.util.Date(gameTimeMillis))
        return if (isSameDay) "TODAY • ${timeFmt.uppercase()}"
        else java.text.SimpleDateFormat("EEEE • h:mm a", java.util.Locale.US).format(java.util.Date(gameTimeMillis)).uppercase()
    }

    suspend fun bindGameData(context: Context, views: RemoteViews, game: WidgetGame, theme: WidgetTheme) {
        val awayAbbr = if (game.isHomeGame) game.opponentAbbrev else DET_ABBR
        val homeAbbr = if (game.isHomeGame) DET_ABBR else game.opponentAbbrev
        views.setTextViewText(R.id.widget_away_name, awayAbbr)
        views.setTextViewText(R.id.widget_home_name, homeAbbr)

        val gameTimeText = formatGameDateTime(game.gameTimeMillis)
        views.setTextViewText(R.id.widget_opponent, gameTimeText)
        views.setTextViewText(R.id.widget_countdown, formatCountdown(game.gameTimeMillis))

        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val venue = game.venue.ifEmpty {
            prefs.getString("next_game_venue", "Little Caesars Arena") ?: "Little Caesars Arena"
        }
        val homeAway = if (game.isHomeGame) "Home" else "Away"
        views.setTextViewText(R.id.widget_venue_info, "$venue • $homeAway")

        val standing = game.standingLine.ifEmpty {
            prefs.getString("standings_summary", null) ?: ""
        }
        val wcBack = prefs.getString("games_back_wild_card", "-") ?: "-"
        val poStatus = prefs.getString("playoff_status", "OUT") ?: "OUT"
        val inPlayoffs = poStatus.equals("IN", ignoreCase = true) ||
            poStatus.contains("CLINCHED", ignoreCase = true)
        val chase = StandingsFormatter.formatPlayoffChase(wcBack, inPlayoffs).toString()

        val cleanStanding = if (standing.isNotBlank()) {
            Regex("(\\d+st ATL|\\d+nd ATL|\\d+rd ATL|\\d+th ATL)").find(standing)?.value ?: ""
        } else ""

        val contextLine = venue
        views.setTextViewText(R.id.widget_standing_h2h, contextLine)

        val detRecord = prefs.getString("team_record_DET", null)
            ?: prefs.getString("wings_summary", null)?.substringBefore(" •")
            ?: ""
        val oppRecord = prefs.getString("team_record_${game.opponentAbbrev}", "") ?: ""

        val awayRec = if (game.awayRecord.isNotBlank()) game.awayRecord
            else if (game.isHomeGame) oppRecord
            else detRecord

        val homeRec = if (game.homeRecord.isNotBlank()) game.homeRecord
            else if (game.isHomeGame) detRecord
            else oppRecord

        views.setTextViewText(R.id.widget_away_record, awayRec)
        views.setTextViewText(R.id.widget_home_record, homeRec)

        // Away logo left, home logo right (ESPN PNGs need no SVG decoder).
        val oppBitmap = loadLogoBitmap(context, getTeamLogoUrl(game.opponentAbbrev))
        val detBitmap = loadLogoBitmap(context, getTeamLogoUrl(DET_ABBR))
        val (awayBmp, awayFallback) = if (game.isHomeGame) {
            oppBitmap to R.drawable.ic_puck_vector
        } else {
            detBitmap to R.drawable.ic_redwings_logo
        }
        val (homeBmp, homeFallback) = if (game.isHomeGame) {
            detBitmap to R.drawable.ic_redwings_logo
        } else {
            oppBitmap to R.drawable.ic_puck_vector
        }
        if (awayBmp != null) views.setImageViewBitmap(R.id.widget_away_logo, awayBmp)
        else views.setImageViewResource(R.id.widget_away_logo, awayFallback)
        if (homeBmp != null) views.setImageViewBitmap(R.id.widget_home_logo, homeBmp)
        else views.setImageViewResource(R.id.widget_home_logo, homeFallback)
    }

    fun bindEmptyState(context: Context, views: RemoteViews, theme: WidgetTheme) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val detRecord = prefs.getString("team_record_DET", null)
            ?: prefs.getString("wings_summary", null)?.substringBefore(" •")
            ?: ""

        val oppRecord = prefs.getString("team_record_OPP", "") ?: ""
        val oppName = prefs.getString("next_game_opp", "--") ?: "--"

        views.setTextViewText(R.id.widget_away_name, oppName)
        views.setTextViewText(R.id.widget_home_name, "DET")
        views.setTextViewText(R.id.widget_away_record, oppRecord)
        views.setTextViewText(R.id.widget_home_record, detRecord)
        views.setTextViewText(R.id.widget_opponent, "NO UPCOMING GAMES")
        views.setTextViewText(R.id.widget_countdown, "-- : --")
        views.setTextViewText(R.id.widget_standing_h2h, "Little Caesars Arena")

        val rawAtlantic = prefs.getString("atlantic_standings", null)
        val rows = if (!rawAtlantic.isNullOrBlank()) {
            StandingsFormatter.formatAtlanticLine(rawAtlantic)
        } else null

        val teamIds = listOf(
            R.id.widget_team_1, R.id.widget_team_2, R.id.widget_team_3, R.id.widget_team_4,
            R.id.widget_team_5, R.id.widget_team_6, R.id.widget_team_7, R.id.widget_team_8
        )
        val teamColor = ContextCompat.getColor(context, theme.teamColorRes)
        val detColor = ContextCompat.getColor(context, theme.teamDetRes)
        val subColor = ContextCompat.getColor(context, theme.subColorRes)

        teamIds.forEachIndexed { i, id ->
            val num = i + 1
            val teamAbbr = if (rows != null && i < rows.size) {
                val raw = rows[i].toString()
                val clean = raw.replace(Regex("^\\d+[\\.\\s]\\s*"), "").trim()
                Regex("([A-Za-z]{2,3})").find(clean)?.value?.uppercase() ?: clean.take(3).uppercase()
            } else {
                "--"
            }
            val isDet = teamAbbr.equals("DET", ignoreCase = true)

            if (isDet) {
                views.setTextViewText(id, "$num $teamAbbr")
                views.setTextColor(id, detColor)
                views.setInt(id, "setBackgroundResource", theme.detHighlightPillRes)
                val padH = (6 * context.resources.displayMetrics.density).toInt()
                val padV = (2 * context.resources.displayMetrics.density).toInt()
                views.setViewPadding(id, padH, padV, padH, padV)
            } else if (teamAbbr != "--") {
                val seedHex = String.format("#%06X", 0xFFFFFF and subColor)
                val teamHex = String.format("#%06X", 0xFFFFFF and teamColor)
                val htmlString = "<font color='$seedHex'>$num</font> <font color='$teamHex'><b>$teamAbbr</b></font>"
                val spanned = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    android.text.Html.fromHtml(htmlString, android.text.Html.FROM_HTML_MODE_LEGACY)
                } else {
                    @Suppress("DEPRECATION")
                    android.text.Html.fromHtml(htmlString)
                }
                views.setTextViewText(id, spanned)
                views.setInt(id, "setBackgroundResource", android.R.color.transparent)
                views.setViewPadding(id, 0, 0, 0, 0)
            } else {
                views.setTextViewText(id, "$num --")
                views.setTextColor(id, teamColor)
                views.setInt(id, "setBackgroundResource", android.R.color.transparent)
                views.setViewPadding(id, 0, 0, 0, 0)
            }
        }
        try {
            views.setImageViewResource(R.id.widget_away_logo, R.drawable.ic_puck_vector)
            views.setImageViewResource(R.id.widget_home_logo, R.drawable.ic_redwings_logo)
        } catch (e: Exception) {
            Log.e(TAG, "empty-state logos missing: ${e.message}")
        }
    }

    internal var logoBitmapLoader: (suspend (Context, String) -> Bitmap?)? = null

    private suspend fun loadLogoBitmap(context: Context, url: String): Bitmap? {
        logoBitmapLoader?.let { return it(context, url) }
        return withTimeoutOrNull(4000L) {
            try {
                val result = context.imageLoader.execute(
                    ImageRequest.Builder(context).data(url).allowHardware(false).build()
                )
                if (result is SuccessResult) {
                    val raw = result.drawable.toBitmap()
                    if (raw.width <= 0 || raw.height <= 0) return@withTimeoutOrNull null
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
}
