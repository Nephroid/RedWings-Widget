package com.redwings.widget.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.redwings.widget.MainActivity
import com.redwings.widget.R
import com.redwings.widget.data.local.AppDatabase
import com.redwings.widget.data.repository.HockeyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/** Lean provider (<300 lines per REDWINGS.md): scheduling, theming, and responsive layouts. */
class RedWingsWidgetProvider : AppWidgetProvider() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        scheduleNextUpdate(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        cancelUpdate(context)
    }

    override fun onUpdate(context: Context, mgr: AppWidgetManager, ids: IntArray) {
        val pending = goAsync()
        scope.launch { try { updateAllWidgets(context, mgr, ids) } finally { pending.finish() } }
        scheduleNextUpdate(context)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context, mgr: AppWidgetManager, id: Int, opts: android.os.Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, mgr, id, opts)
        val pending = goAsync()
        scope.launch {
            try { updateAllWidgets(context, mgr, intArrayOf(id), opts) } finally { pending.finish() }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_TOGGLE_THEME -> {
                val prefs = context.getSharedPreferences(WidgetBinder.PREFS, Context.MODE_PRIVATE)
                val next = (prefs.getInt(KEY_THEME, WidgetTheme.HOME.id) + 1) % WidgetTheme.values().size
                prefs.edit().putInt(KEY_THEME, next).apply()
                triggerUpdate(context)
            }
            AppWidgetManager.ACTION_APPWIDGET_UPDATE, Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED, ACTION_AUTO_UPDATE -> {
                val mgr = AppWidgetManager.getInstance(context)
                val ids = mgr.getAppWidgetIds(ComponentName(context, RedWingsWidgetProvider::class.java))
                if (ids.isNotEmpty()) {
                    val pending = goAsync()
                    scope.launch {
                        try { updateAllWidgets(context, mgr, ids) } finally { pending.finish() }
                    }
                    scheduleNextUpdate(context)
                } else cancelUpdate(context)
            }
        }
    }

    private suspend fun updateAllWidgets(
        context: Context, mgr: AppWidgetManager, ids: IntArray, opts: android.os.Bundle? = null
    ) {
        try {
            val prefs = context.getSharedPreferences(WidgetBinder.PREFS, Context.MODE_PRIVATE)
            val theme = WidgetTheme.fromIndex(prefs.getInt(KEY_THEME, WidgetTheme.HOME.id))

            val game = try {
                val dao = AppDatabase.getDatabase(context).gameDao()
                var nextGame = dao.getNextGame()
                if (nextGame == null) {
                    try {
                        HockeyRepository(dao, context).refreshGames(context)
                        nextGame = dao.getNextGame()
                    } catch (e: Exception) {
                        Log.w(TAG, "initial refresh failed: ${e.message}")
                    }
                }
                nextGame?.let {
                    val detRecord = prefs.getString("team_record_DET", null)
                        ?: prefs.getString("wings_summary", null)?.substringBefore(" •")
                        ?: ""
                    val oppRecord = prefs.getString("team_record_${it.opponentAbbrev}", "") ?: ""
                    WidgetBinder.WidgetGame(
                        opponentName = it.opponentName,
                        opponentAbbrev = it.opponentAbbrev,
                        gameTimeMillis = it.gameTimeMillis,
                        isHomeGame = it.isHomeGame,
                        venue = it.venueName,
                        standingLine = it.standingsSummary,
                        h2hLine = it.headToHead,
                        awayRecord = if (it.isHomeGame) oppRecord else detRecord,
                        homeRecord = if (it.isHomeGame) detRecord else oppRecord
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "room read failed: ${e.message}")
                null
            }

            val oppAbbrev = game?.opponentAbbrev
                ?: prefs.getString("next_game_opponent_abbrev", null)
                ?: prefs.getString("next_game_opp", null)
                ?: "--"

            val (oppBitmap, detBitmap) = coroutineScope {
                val oppDeferred = async(Dispatchers.IO) {
                    if (oppAbbrev != "--") WidgetBinder.loadLogoBitmap(context, oppAbbrev) else null
                }
                val detDeferred = async(Dispatchers.IO) {
                    WidgetBinder.loadLogoBitmap(context, "DET")
                }
                oppDeferred.await() to detDeferred.await()
            }

            ids.forEach { id ->
                val options = opts ?: mgr.getAppWidgetOptions(id)
                val w = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 180)?.takeIf { it > 0 } ?: 180
                val h = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 110)?.takeIf { it > 0 } ?: 110

                val views = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val sizeMap = mapOf(
                        android.util.SizeF(280f, 110f) to buildWidgetViews(context, R.layout.red_wings_widget_layout, game, theme, prefs, id, 320, 140, oppBitmap, detBitmap),
                        android.util.SizeF(460f, 130f) to buildWidgetViews(context, R.layout.red_wings_widget_wide, game, theme, prefs, id, 540, 170, oppBitmap, detBitmap)
                    )
                    RemoteViews(sizeMap)
                } else {
                    val layout = if (w >= 450) R.layout.red_wings_widget_wide else R.layout.red_wings_widget_layout
                    buildWidgetViews(context, layout, game, theme, prefs, id, w, h, oppBitmap, detBitmap)
                }

                if (WidgetCrashGuard.validateRemoteViews(views, id)) {
                    mgr.updateAppWidget(id, views)
                } else {
                    Log.w(TAG, "Oversized RemoteViews detected for widget $id, applying emergency fallback")
                    mgr.updateAppWidget(id, WidgetCrashGuard.buildEmergencyViews(context))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "update failed: ${e.message}", e)
        }
    }

    private suspend fun buildWidgetViews(
        context: Context,
        layoutResId: Int,
        game: WidgetBinder.WidgetGame?,
        theme: WidgetTheme,
        prefs: android.content.SharedPreferences,
        widgetId: Int,
        minW: Int,
        minH: Int,
        oppBitmap: Bitmap? = null,
        detBitmap: Bitmap? = null
    ): RemoteViews {
        val views = RemoteViews(context.packageName, layoutResId)
        views.setOnClickPendingIntent(R.id.widget_theme_toggle, togglePI(context, widgetId))
        views.setTextViewText(R.id.widget_theme_toggle, theme.buttonLabel)
        applyWidgetTheme(context, views, theme)

        if (game != null) WidgetBinder.bindGameData(context, views, game, theme, oppBitmap, detBitmap)
        else WidgetBinder.bindEmptyState(context, views, theme, oppBitmap, detBitmap)

        bindStandings(context, views, prefs, theme)
        applyResponsiveLayout(context, views, minW, minH)
        views.setOnClickPendingIntent(R.id.widget_root, openAppPI(context))
        return views
    }

    internal fun bindStandings(
        context: Context, views: RemoteViews,
        prefs: android.content.SharedPreferences, theme: WidgetTheme
    ) {
        val rows = StandingsFormatter.formatAtlanticLine(prefs.getString(KEY_ATLANTIC, null))
        val ids = listOf(
            R.id.widget_team_1, R.id.widget_team_2, R.id.widget_team_3, R.id.widget_team_4,
            R.id.widget_team_5, R.id.widget_team_6, R.id.widget_team_7, R.id.widget_team_8
        )
        val teamColor = ContextCompat.getColor(context, theme.teamColorRes)
        val detColor = ContextCompat.getColor(context, theme.teamDetRes)
        val subColor = ContextCompat.getColor(context, theme.subColorRes)
        ids.forEachIndexed { i, id ->
            val raw = rows.getOrNull(i)?.toString() ?: "${i + 1}. --"
            val clean = raw.replace(Regex("^\\d+[\\.\\s]\\s*"), "").trim()
            val abbr = Regex("([A-Za-z]{2,3})").find(clean)?.value?.uppercase() ?: clean.take(3).uppercase()
            val isDet = (raw.contains("DET", ignoreCase = true) || abbr == "DET") && abbr != "--"
            val seedNum = i + 1

            if (isDet) {
                views.setTextViewText(id, "$seedNum $abbr")
                views.setTextColor(id, detColor)
                views.setInt(id, "setBackgroundResource", theme.detHighlightPillRes)
                val padH = (6 * context.resources.displayMetrics.density).toInt()
                val padV = (2 * context.resources.displayMetrics.density).toInt()
                views.setViewPadding(id, padH, padV, padH, padV)
            } else if (abbr == "--" || abbr.isEmpty()) {
                views.setTextViewText(id, "$seedNum --")
                views.setTextColor(id, teamColor)
                views.setInt(id, "setBackgroundResource", android.R.color.transparent)
                views.setViewPadding(id, 0, 0, 0, 0)
            } else {
                val seedHex = String.format("#%06X", 0xFFFFFF and subColor)
                val teamHex = String.format("#%06X", 0xFFFFFF and teamColor)
                val htmlString = "<font color='$seedHex'>$seedNum</font> <font color='$teamHex'><b>$abbr</b></font>"
                val spanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    android.text.Html.fromHtml(htmlString, android.text.Html.FROM_HTML_MODE_LEGACY)
                } else {
                    @Suppress("DEPRECATION")
                    android.text.Html.fromHtml(htmlString)
                }
                views.setTextViewText(id, spanned)
                views.setInt(id, "setBackgroundResource", android.R.color.transparent)
                views.setViewPadding(id, 0, 0, 0, 0)
            }
        }
    }

    internal fun applyWidgetTheme(context: Context, views: RemoteViews, theme: WidgetTheme) {
        views.setInt(R.id.widget_root, "setBackgroundResource", theme.bgDrawableRes)
        views.setInt(R.id.widget_tag, "setBackgroundResource", theme.tagDrawableRes)
        views.setInt(R.id.widget_theme_toggle, "setBackgroundResource", theme.tagDrawableRes)
        views.setInt(R.id.widget_header_layout, "setBackgroundResource", theme.headerDrawableRes)
        views.setInt(R.id.widget_matchup_layout, "setBackgroundResource", theme.matchupCardDrawableRes)
        views.setInt(R.id.widget_standings_table, "setBackgroundResource", theme.standingsCardDrawableRes)
        views.setInt(R.id.widget_info_card, "setBackgroundResource", theme.venuePillDrawableRes)

        val stripeColor = ContextCompat.getColor(context, theme.stripeColorRes)
        views.setInt(R.id.widget_header_stripe_l1, "setBackgroundColor", stripeColor)
        views.setInt(R.id.widget_header_stripe_l2, "setBackgroundColor", stripeColor)
        views.setInt(R.id.widget_header_stripe_r1, "setBackgroundColor", stripeColor)
        views.setInt(R.id.widget_header_stripe_r2, "setBackgroundColor", stripeColor)

        views.setInt(R.id.widget_divider_top, "setBackgroundColor",
            ContextCompat.getColor(context, theme.dividerColorRes))
        views.setTextColor(R.id.widget_title, ContextCompat.getColor(context, theme.titleColorRes))
        views.setTextColor(R.id.widget_countdown, ContextCompat.getColor(context, theme.countdownColorRes))
        views.setTextColor(R.id.widget_opponent, ContextCompat.getColor(context, theme.opponentColorRes))
        views.setTextColor(R.id.widget_venue_info, ContextCompat.getColor(context, theme.subColorRes))
        views.setTextColor(R.id.widget_standing_h2h, ContextCompat.getColor(context, theme.standingColorRes))
        views.setTextColor(R.id.widget_tag, ContextCompat.getColor(context, theme.tagTextColorRes))
        views.setTextColor(R.id.widget_theme_toggle, ContextCompat.getColor(context, theme.tagTextColorRes))
        views.setTextColor(R.id.widget_away_name, ContextCompat.getColor(context, theme.opponentColorRes))
        views.setTextColor(R.id.widget_home_name, ContextCompat.getColor(context, theme.opponentColorRes))
        views.setTextColor(R.id.widget_away_tag, ContextCompat.getColor(context, theme.subColorRes))
        views.setTextColor(R.id.widget_home_tag, ContextCompat.getColor(context, theme.subColorRes))
        views.setTextColor(R.id.widget_away_record, ContextCompat.getColor(context, theme.subColorRes))
        views.setTextColor(R.id.widget_home_record, ContextCompat.getColor(context, theme.subColorRes))
        views.setTextColor(R.id.widget_seeds_header, ContextCompat.getColor(context, theme.standingColorRes))
        views.setTextColor(R.id.widget_hunt_header, ContextCompat.getColor(context, theme.standingColorRes))
        views.setTextColor(R.id.widget_matchup_badge, ContextCompat.getColor(context, theme.matchupBadgeTextColorRes))
    }

    internal fun applyResponsiveLayout(context: Context, views: RemoteViews, minW: Int, minH: Int) {
        when {
            minH < 72 -> {
                views.setViewVisibility(R.id.widget_header_layout, View.GONE)
                views.setViewVisibility(R.id.widget_divider_top, View.GONE)
                views.setViewVisibility(R.id.widget_info_card, View.GONE)
                views.setViewVisibility(R.id.widget_standings_table, View.GONE)
            }
            minH < 90 -> {
                views.setViewVisibility(R.id.widget_header_layout, View.VISIBLE)
                views.setViewVisibility(R.id.widget_divider_top, View.VISIBLE)
                views.setViewVisibility(R.id.widget_info_card, View.VISIBLE)
                views.setViewVisibility(R.id.widget_standings_table, View.GONE)
            }
            else -> {
                views.setViewVisibility(R.id.widget_header_layout, View.VISIBLE)
                views.setViewVisibility(R.id.widget_divider_top, View.VISIBLE)
                views.setViewVisibility(R.id.widget_info_card, View.VISIBLE)
                views.setViewVisibility(R.id.widget_standings_table, View.VISIBLE)
            }
        }
        val showLogos = minW >= 130
        views.setViewVisibility(R.id.widget_away_logo, if (showLogos) View.VISIBLE else View.GONE)
        views.setViewVisibility(R.id.widget_home_logo, if (showLogos) View.VISIBLE else View.GONE)

        if (minH < 100) {
            val h = minH.toFloat()
            views.setTextViewTextSize(R.id.widget_title, TypedValue.COMPLEX_UNIT_SP, (h * 0.105f).coerceIn(8.5f, 15f))
            views.setTextViewTextSize(R.id.widget_opponent, TypedValue.COMPLEX_UNIT_SP, (h * 0.12f).coerceIn(10f, 18f))
            views.setTextViewTextSize(R.id.widget_countdown, TypedValue.COMPLEX_UNIT_SP, (h * 0.22f).coerceIn(16f, 36f))
            views.setTextViewTextSize(R.id.widget_venue_info, TypedValue.COMPLEX_UNIT_SP, (h * 0.09f).coerceIn(8f, 14f))
            views.setTextViewTextSize(R.id.widget_standing_h2h, TypedValue.COMPLEX_UNIT_SP, (h * 0.085f).coerceIn(7.5f, 13f))
        }
    }

    private fun piFlags() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        else PendingIntent.FLAG_UPDATE_CURRENT

    private fun togglePI(context: Context, id: Int): PendingIntent =
        PendingIntent.getBroadcast(context, id, Intent(context, RedWingsWidgetProvider::class.java).apply { action = ACTION_TOGGLE_THEME }, piFlags())

    private fun openAppPI(context: Context): PendingIntent =
        PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }, piFlags())

    private fun scheduleNextUpdate(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as? android.app.AlarmManager ?: return
        val pi = PendingIntent.getBroadcast(
            context, ALARM_CODE,
            Intent(context, RedWingsWidgetProvider::class.java).apply { action = ACTION_AUTO_UPDATE },
            piFlags()
        )
        try {
            val app = context.applicationContext as? com.redwings.widget.RedWingsApp
            val rc = app?.container?.remoteConfigManager?.configState?.value
                ?: com.redwings.widget.data.firebase.RemoteConfigValues()

            val prefs = context.getSharedPreferences(com.redwings.widget.data.repository.HockeyRepository.PREFS, Context.MODE_PRIVATE)
            val nextGameTime = prefs.getLong("next_game_time_millis", 0L)
            val now = System.currentTimeMillis()

            val intervalMs = when {
                // Game in progress or near start (15m before start to 3.5h after start)
                nextGameTime > 0L && now in (nextGameTime - 15 * 60 * 1000L)..(nextGameTime + 3 * 3600 * 1000L + 1800 * 1000L) -> {
                    rc.widgetLivePollMs
                }
                // Pre-game day window (within 6 hours of puck drop)
                nextGameTime > 0L && now in (nextGameTime - 6 * 3600 * 1000L)..nextGameTime -> {
                    rc.widgetGameDayPrePollMs
                }
                // Off-day or far out
                else -> {
                    rc.widgetOffDayPollMs
                }
            }

            am.setInexactRepeating(
                android.app.AlarmManager.ELAPSED_REALTIME,
                android.os.SystemClock.elapsedRealtime() + intervalMs, intervalMs, pi
            )
        } catch (e: Exception) {
            Log.e(TAG, "schedule failed: ${e.message}")
        }
    }

    private fun cancelUpdate(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as? android.app.AlarmManager ?: return
        val pi = PendingIntent.getBroadcast(context, ALARM_CODE,
            Intent(context, RedWingsWidgetProvider::class.java).apply { action = ACTION_AUTO_UPDATE }, piFlags())
        am.cancel(pi)
    }

    companion object {
        const val ACTION_AUTO_UPDATE = "com.redwings.widget.ACTION_AUTO_UPDATE"
        const val ACTION_TOGGLE_THEME = "com.redwings.widget.ACTION_TOGGLE_THEME"
        private const val ALARM_CODE = 7741
        private const val INTERVAL_MS = 30 * 60 * 1000L
        private const val KEY_THEME = "widget_theme_index"
        private const val KEY_ATLANTIC = "atlantic_standings"
        private const val KEY_WCGB = "games_back_wild_card"
        private const val KEY_PO_STATUS = "playoff_status"
        private const val TAG = "WingsWidget"

        fun triggerUpdate(context: Context) {
            context.sendBroadcast(Intent(context, RedWingsWidgetProvider::class.java).apply { action = ACTION_AUTO_UPDATE })
        }
    }
}
