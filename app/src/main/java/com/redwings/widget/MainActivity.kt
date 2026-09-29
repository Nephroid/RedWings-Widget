package com.redwings.widget

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelProvider
import com.redwings.widget.ui.GameDashboard
import com.redwings.widget.ui.GameViewModel
import com.redwings.widget.ui.theme.AppJersey
import com.redwings.widget.ui.theme.RedWingsTheme
import com.redwings.widget.widget.RedWingsWidgetProvider
import com.redwings.widget.widget.WidgetBinder

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val viewModel = ViewModelProvider(this)[GameViewModel::class.java]

        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val legacyPrefs = getSharedPreferences(WidgetBinder.PREFS, Context.MODE_PRIVATE)

        setContent {
            var activeJersey by remember {
                val initialIndex = if (prefs.contains(KEY_THEME)) {
                    prefs.getInt(KEY_THEME, 0)
                } else {
                    legacyPrefs.getInt(KEY_THEME, 0)
                }
                mutableStateOf(AppJersey.fromIndex(initialIndex))
            }

            fun toggleJersey() {
                val nextJersey = AppJersey.fromIndex(activeJersey.id + 1)
                activeJersey = nextJersey
                // Persist to SharedPreferences ("com.redwings.widget.PREFS", key "widget_theme_index")
                prefs.edit().putInt(KEY_THEME, nextJersey.id).apply()
                // Synchronize with widget preferences and trigger widget update
                legacyPrefs.edit().putInt(KEY_THEME, nextJersey.id).apply()
                RedWingsWidgetProvider.triggerUpdate(this@MainActivity)
            }

            RedWingsTheme(jersey = activeJersey) {
                GameDashboard(
                    viewModel = viewModel,
                    activeJersey = activeJersey,
                    onJerseyThemeToggle = { toggleJersey() }
                )
            }
        }
    }

    companion object {
        const val PREFS_NAME = "com.redwings.widget.PREFS"
        const val KEY_THEME = "widget_theme_index"
    }
}
