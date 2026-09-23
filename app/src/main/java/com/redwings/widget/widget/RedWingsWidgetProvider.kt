package com.redwings.widget.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.redwings.widget.MainActivity
import com.redwings.widget.R
import com.redwings.widget.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Lean provider (<350 lines): scheduling + theming + responsive tweaks.
 * Game binding -> WidgetBinder, standings text -> StandingsFormatter.
 *
 * Room contract: AppDatabase.getDatabase(ctx).gameDao().getNextGame()
 * returns the next RedWingsGame? (suspend). Entity fields mapped:
 * opponentName / opponentAbbrev / gameTimeMillis / isHomeGame /
 * venueName / standingsSummary / headToHead.
 */
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
                val next = (prefs.getInt(KEY_THEME, 0) + 1) % WidgetTheme.values().size
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
            val theme = WidgetTheme.fromIndex(prefs.getInt(KEY_THEME, 0))

            val game = try {
                AppDatabase.getDatabase(context).gameDao().getNextGame()?.let {
                    WidgetBinder.WidgetGame(
                        opponentName = it.opponentName,
                        opponentAbbrev = it.opponentAbbrev,
                        gameTimeMillis = it.gameTimeMillis,
                        isHomeGame = it.isHomeGame,
                        venue = it.venueName,
                        standingLine = it.standingsSummary,
                        h2hLine = it.headToHead
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "room read failed: ${e.message}")
                null
            }

            ids.forEach { id ->
                val views = RemoteViews(context.packageName, R.layout.red_wings_widget_layout)
                val options = opts ?: mgr.getAppWidgetOptions(id)
                val w = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 180)?.takeIf { it > 0 } ?: 180
                val h = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 110)?.takeIf { it > 0 } ?: 110

                views.setOnClickPendingIntent(R.id.widget_theme_toggle, togglePI(context, id))
                views.setTextViewText(R.id.widget_theme_toggle, theme.buttonLabel)
                applyWidgetTheme(context, views, theme)

                if (game != null) WidgetBinder.bindGameData(context, views, game, theme)
                else WidgetBinder.bindEmptyState(context, views, theme)
                bindStandings(context, views, prefs, theme)
                applyResponsiveLayout(context, views, w, h)

                views.setOnClickPendingIntent(R.id.widget_root, openAppPI(context))
                mgr.updateAppWidget(id, views)
            }
        } catch (e: Exception) {
            Log.e(TAG, "update failed: ${e.message}", e)
        }
    }

    private fun bindStandings(
        context: Context, views: RemoteViews,
        prefs: android.content.SharedPreferences, theme: WidgetTheme
    ) {
        val rows = StandingsFormatter.formatAtlanticLine(prefs.getString(KEY_ATLANTIC, null))
        val ids = listOf(R.id.widget_team_1, R.id.widget_team_2, R.id.widget_team_3,
            R.id.widget_team_4, R.id.widget_team_5)
        val teamColor = ContextCompat.getColor(context, theme.teamColorRes)
        val detColor = ContextCompat.getColor(context, theme.teamDetRes)
        ids.forEachIndexed { i, id ->
            val row = rows.getOrNull(i) ?: "${i + 1}. --"
            views.setTextViewText(id, row)
            views.setTextColor(id, if (row.contains("DET", ignoreCase = true)) detColor else teamColor)
        }
        val wcBack = prefs.getString(KEY_WCGB, "--") ?: "--"
        val inPlayoffs = prefs.getString(KEY_PO_STATUS, "OUT").let {
            it.equals("IN", true) || it.contains("CLINCHED", true)
        }
        views.setTextViewText(R.id.widget_team_6, StandingsFormatter.formatWcLine(wcBack, inPlayoffs))
        views.setTextColor(R.id.widget_team_6,
            ContextCompat.getColor(context, if (inPlayoffs) theme.teamDetRes else theme.wcgbColorRes))
    }

    private fun applyWidgetTheme(context: Context, views: RemoteViews, theme: WidgetTheme) {
        views.setInt(R.id.widget_root, "setBackgroundResource", theme.bgDrawableRes)
        views.setInt(R.id.widget_tag, "setBackgroundResource", theme.tagDrawableRes)
        views.setInt(R.id.widget_theme_toggle, "setBackgroundResource", theme.tagDrawableRes)
        views.setInt(R.id.widget_divider_top, "setBackgroundColor",
            ContextCompat.getColor(context, theme.dividerColorRes))
        views.setTextColor(R.id.widget_title, ContextCompat.getColor(context, theme.titleColorRes))
        views.setTextColor(R.id.widget_countdown, ContextCompat.getColor(context, theme.countdownColorRes))
        views.setTextColor(R.id.widget_opponent, ContextCompat.getColor(context, theme.opponentColorRes))
        views.setTextColor(R.id.widget_venue_info, ContextCompat.getColor(context, theme.subColorRes))
        views.setTextColor(R.id.widget_standing_h2h, ContextCompat.getColor(context, theme.standingColorRes))
        views.setTextColor(R.id.widget_tag, ContextCompat.getColor(context, theme.tagTextColorRes))
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

        val h = minH.toFloat()
        val bonus = if (minW >= 260 && minH >= 90) ((minW - 260) * 0.015f).coerceIn(0f, 3.5f) else 0f
        views.setTextViewTextSize(R.id.widget_title, TypedValue.COMPLEX_UNIT_SP, (h * 0.105f).coerceIn(8.5f, 15f))
        views.setTextViewTextSize(R.id.widget_opponent, TypedValue.COMPLEX_UNIT_SP, (h * 0.12f + bonus * 0.5f).coerceIn(10f, 18f))
        views.setTextViewTextSize(R.id.widget_countdown, TypedValue.COMPLEX_UNIT_SP, (h * 0.22f + bonus).coerceIn(16f, 36f))
        views.setTextViewTextSize(R.id.widget_venue_info, TypedValue.COMPLEX_UNIT_SP, (h * 0.09f).coerceIn(8f, 14f))
        views.setTextViewTextSize(R.id.widget_standing_h2h, TypedValue.COMPLEX_UNIT_SP, (h * 0.085f).coerceIn(7.5f, 13f))
        val teamSp = (h * 0.088f).coerceIn(7.5f, 13f)
        listOf(R.id.widget_team_1, R.id.widget_team_2, R.id.widget_team_3,
            R.id.widget_team_4, R.id.widget_team_5, R.id.widget_team_6).forEach {
            views.setTextViewTextSize(it, TypedValue.COMPLEX_UNIT_SP, teamSp)
        }
        val d = context.resources.displayMetrics.density
        val padH = ((minW * 0.025f).coerceIn(6f, 12f) * d).toInt()
        val padV = ((minH * 0.035f).coerceIn(4f, 10f) * d).toInt()
        views.setViewPadding(R.id.widget_root, padH, padV, padH, padV)
    }

    private fun piFlags() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else PendingIntent.FLAG_UPDATE_CURRENT

    private fun togglePI(context: Context, id: Int): PendingIntent {
        val i = Intent(context, RedWingsWidgetProvider::class.java).apply { action = ACTION_TOGGLE_THEME }
        return PendingIntent.getBroadcast(context, id, i, piFlags())
    }

    private fun openAppPI(context: Context): PendingIntent {
        val i = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(context, 0, i, piFlags())
    }

    private fun scheduleNextUpdate(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as? android.app.AlarmManager ?: return
        val pi = PendingIntent.getBroadcast(
            context, ALARM_CODE,
            Intent(context, RedWingsWidgetProvider::class.java).apply { action = ACTION_AUTO_UPDATE },
            piFlags()
        )
        try {
            // Hockey: 30-min inexact polling is plenty (cf. baseball 15-min).
            am.setInexactRepeating(
                android.app.AlarmManager.ELAPSED_REALTIME,
                android.os.SystemClock.elapsedRealtime() + INTERVAL_MS, INTERVAL_MS, pi
            )
        } catch (e: Exception) {
            Log.e(TAG, "schedule failed: ${e.message}")
        }
    }

    private fun cancelUpdate(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as? android.app.AlarmManager ?: return
        val pi = PendingIntent.getBroadcast(
            context, ALARM_CODE,
            Intent(context, RedWingsWidgetProvider::class.java).apply { action = ACTION_AUTO_UPDATE },
            piFlags()
        )
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
            context.sendBroadcast(
                Intent(context, RedWingsWidgetProvider::class.java).apply { action = ACTION_AUTO_UPDATE }
            )
        }
    }
}
