package com.redwings.widget.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color

import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.util.Log
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.redwings.widget.R
import com.redwings.widget.data.model.getTeamColor
import com.redwings.widget.data.model.getTeamLogoUrl
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.io.FileOutputStream
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

    suspend fun bindGameData(
        context: Context,
        views: RemoteViews,
        game: WidgetGame,
        theme: WidgetTheme,
        preloadedOppBitmap: Bitmap? = null,
        preloadedDetBitmap: Bitmap? = null
    ) {
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

        // Away logo left, home logo right (ESPN PNGs with persistent disk cache & badge fallback).
        val oppBitmap = preloadedOppBitmap ?: loadLogoBitmap(context, game.opponentAbbrev)
        val detBitmap = preloadedDetBitmap ?: loadLogoBitmap(context, DET_ABBR)
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

    fun bindEmptyState(
        context: Context,
        views: RemoteViews,
        theme: WidgetTheme,
        preloadedOppBitmap: Bitmap? = null,
        preloadedDetBitmap: Bitmap? = null
    ) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val detRecord = prefs.getString("team_record_DET", null)
            ?: prefs.getString("wings_summary", null)?.substringBefore(" •")
            ?: ""

        val oppRecord = prefs.getString("team_record_OPP", "") ?: ""
        val oppName = prefs.getString("next_game_opponent_abbrev", null)
            ?: prefs.getString("next_game_opp", null)
            ?: "--"
        val isHome = prefs.getBoolean("next_game_is_home", true)

        val awayName = if (isHome) oppName else "DET"
        val homeName = if (isHome) "DET" else oppName
        val awayRec = if (isHome) oppRecord else detRecord
        val homeRec = if (isHome) detRecord else oppRecord

        views.setTextViewText(R.id.widget_away_name, awayName)
        views.setTextViewText(R.id.widget_home_name, homeName)
        views.setTextViewText(R.id.widget_away_record, awayRec)
        views.setTextViewText(R.id.widget_home_record, homeRec)
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
            val oppBmp = preloadedOppBitmap ?: if (oppName != "--") {
                val diskFile = getLogoDiskFile(context, oppName)
                if (diskFile.exists() && diskFile.length() > 0) {
                    BitmapFactory.decodeFile(diskFile.absolutePath)
                } else {
                    generateTeamBadge(oppName)
                }
            } else null

            val detBmp = preloadedDetBitmap ?: run {
                val diskFile = getLogoDiskFile(context, "DET")
                if (diskFile.exists() && diskFile.length() > 0) {
                    BitmapFactory.decodeFile(diskFile.absolutePath)
                } else null
            }

            val awayBmp = if (isHome) oppBmp else detBmp
            val homeBmp = if (isHome) detBmp else oppBmp

            if (awayBmp != null) views.setImageViewBitmap(R.id.widget_away_logo, awayBmp)
            else views.setImageViewResource(R.id.widget_away_logo, if (isHome) R.drawable.ic_puck_vector else R.drawable.ic_redwings_logo)

            if (homeBmp != null) views.setImageViewBitmap(R.id.widget_home_logo, homeBmp)
            else views.setImageViewResource(R.id.widget_home_logo, if (isHome) R.drawable.ic_redwings_logo else R.drawable.ic_puck_vector)
        } catch (e: Exception) {
            Log.e(TAG, "empty-state logos missing: ${e.message}")
        }
    }

    internal var logoBitmapLoader: (suspend (Context, String) -> Bitmap?)? = null

    fun getLogoDiskFile(context: Context, abbrev: String): File {
        val upper = abbrev.uppercase().takeIf { it.isNotBlank() } ?: "DET"
        val dir = File(context.filesDir, "logos").apply { if (!exists()) mkdirs() }
        return File(dir, "${upper.lowercase()}.png")
    }

    fun generateTeamBadge(abbrev: String?, size: Int = 120): Bitmap {
        val upper = abbrev?.uppercase()?.takeIf { it.isNotBlank() } ?: "DET"
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val teamColorInt = getTeamColor(upper).toInt()
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = teamColorInt
            style = Paint.Style.FILL
        }
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = (size * 0.05f).coerceAtLeast(2f)
        }
        val radius = (size / 2f) - (strokePaint.strokeWidth / 2f)
        canvas.drawCircle(size / 2f, size / 2f, radius, bgPaint)
        canvas.drawCircle(size / 2f, size / 2f, radius, strokePaint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = size * (if (upper.length > 3) 0.30f else 0.36f)
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val textBounds = Rect()
        textPaint.getTextBounds(upper, 0, upper.length, textBounds)
        val y = (size / 2f) + (textBounds.height() / 2f) - textBounds.bottom
        canvas.drawText(upper, size / 2f, y, textPaint)
        return bmp
    }

    suspend fun loadLogoBitmap(context: Context, identifier: String): Bitmap? {
        val upper = if (identifier.startsWith("http")) {
            val slug = identifier.substringAfterLast("/").substringBefore(".").uppercase()
            when (slug) {
                "LA" -> "LAK"
                "SJ" -> "SJS"
                "TB" -> "TBL"
                else -> slug
            }
        } else {
            identifier.uppercase()
        }
        val url = if (identifier.startsWith("http")) identifier else getTeamLogoUrl(upper)

        logoBitmapLoader?.let { loader ->
            return loader(context, url)
        }

        val diskFile = getLogoDiskFile(context, upper)
        if (diskFile.exists() && diskFile.length() > 0) {
            try {
                val diskBmp = BitmapFactory.decodeFile(diskFile.absolutePath)
                if (diskBmp != null && diskBmp.width > 0) return diskBmp
            } catch (e: Exception) {
                Log.w(TAG, "failed to decode disk cached logo for $upper: ${e.message}")
            }
        }

        val netBmp = withTimeoutOrNull(4000L) {
            try {
                val result = context.imageLoader.execute(
                    ImageRequest.Builder(context).data(url).allowHardware(false).build()
                )
                if (result is SuccessResult) {
                    val raw = result.drawable.toBitmap()
                    if (raw.width <= 0 || raw.height <= 0) return@withTimeoutOrNull null
                    val maxDim = 120 // avoid TransactionTooLargeException
                    val scaled = if (raw.width > maxDim || raw.height > maxDim) {
                        val ratio = raw.width.toFloat() / raw.height.toFloat()
                        val (w, h) = if (raw.width > raw.height) {
                            maxDim to (maxDim / ratio).toInt().coerceAtLeast(1)
                        } else {
                            (maxDim * ratio).toInt().coerceAtLeast(1) to maxDim
                        }
                        Bitmap.createScaledBitmap(raw, w, h, true)
                    } else raw

                    try {
                        FileOutputStream(diskFile).use { out ->
                            scaled.compress(Bitmap.CompressFormat.PNG, 100, out)
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "failed to cache logo to disk for $upper: ${e.message}")
                    }
                    scaled
                } else null
            } catch (e: Exception) {
                Log.e(TAG, "logo load failed $url: ${e.message}")
                null
            }
        }

        return netBmp ?: generateTeamBadge(upper)
    }
}
