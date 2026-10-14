package com.redwings.widget

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelProvider
import com.redwings.widget.ui.GameDashboard
import com.redwings.widget.ui.GameViewModel
import com.redwings.widget.ui.ScheduleUiState
import com.redwings.widget.ui.theme.AppJersey
import com.redwings.widget.ui.theme.RedWingsTheme
import com.redwings.widget.ui.theme.resolveAppJersey
import com.redwings.widget.widget.RedWingsWidgetProvider
import com.redwings.widget.widget.ThemeMode
import com.redwings.widget.widget.WidgetBinder

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val viewModel = ViewModelProvider(this)[GameViewModel::class.java]

        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val legacyPrefs = getSharedPreferences(WidgetBinder.PREFS, Context.MODE_PRIVATE)

        setContent {
            val scheduleState by viewModel.scheduleState.collectAsState()
            val isHomeGame = (scheduleState as? ScheduleUiState.Data)?.nextGame?.isHome
                ?: legacyPrefs.getBoolean("next_game_is_home", true)

            var activeThemeMode by remember {
                val initialIndex = if (prefs.contains(KEY_THEME)) {
                    prefs.getInt(KEY_THEME, -1)
                } else {
                    legacyPrefs.getInt(KEY_THEME, -1)
                }
                mutableStateOf(ThemeMode.fromId(initialIndex))
            }

            fun toggleThemeMode() {
                val nextMode = activeThemeMode.next()
                activeThemeMode = nextMode
                prefs.edit().putInt(KEY_THEME, nextMode.id).apply()
                legacyPrefs.edit().putInt(KEY_THEME, nextMode.id).apply()
                RedWingsWidgetProvider.triggerUpdate(this@MainActivity)
                try {
                    (application as? RedWingsApp)?.container?.fanPulseRepository?.syncThemeToCloud(nextMode.id)
                } catch (_: Throwable) { }
            }

            val activeJersey = activeThemeMode.resolveAppJersey(isHomeGame)
            val themeChipLabel = if (activeThemeMode == ThemeMode.AUTO) {
                "⚡ AUTO (${if (isHomeGame) "HOME" else "AWAY"})"
            } else {
                activeThemeMode.chipLabel
            }

            RedWingsTheme(jersey = activeJersey) {
                GameDashboard(
                    viewModel = viewModel,
                    activeJersey = activeJersey,
                    themeChipLabel = themeChipLabel,
                    onJerseyThemeToggle = ::toggleThemeMode
                )
            }
        }
    }

    override fun onStop() {
        super.onStop()
        RedWingsWidgetProvider.triggerUpdate(this)
    }


    companion object {
        const val PREFS_NAME = "com.redwings.widget.PREFS"
        const val KEY_THEME = "widget_theme_index"
    }
}
