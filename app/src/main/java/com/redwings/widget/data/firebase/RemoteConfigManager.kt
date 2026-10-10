package com.redwings.widget.data.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.redwings.widget.BuildConfig
import com.redwings.widget.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RemoteConfigValues(
    val widgetOffDayPollMs: Long = 14400000L,        // 4 hours
    val widgetGameDayPrePollMs: Long = 1800000L,     // 30 minutes
    val widgetLivePollMs: Long = 60000L,             // 60 seconds
    val isSeasonLeadersEnabled: Boolean = true,
    val isPlayoffChaseEnabled: Boolean = true,
    val emergencyWidgetCircuitBreaker: Boolean = false,
    val maxLogoDimPx: Int = 120
)

class RemoteConfigManager(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {

    private val _configState = MutableStateFlow(RemoteConfigValues())
    val configState: StateFlow<RemoteConfigValues> = _configState.asStateFlow()

    private var remoteConfig: FirebaseRemoteConfig? = null

    init {
        try {
            val rc = FirebaseRemoteConfig.getInstance()
            remoteConfig = rc

            val fetchIntervalSeconds = if (BuildConfig.DEBUG) 0L else 3600L
            val settings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(fetchIntervalSeconds)
                .build()

            rc.setConfigSettingsAsync(settings)
            rc.setDefaultsAsync(R.xml.remote_config_defaults).addOnCompleteListener {
                updateStateFromConfig(rc)
            }

            // Real-time server config updates
            rc.addOnConfigUpdateListener(object : ConfigUpdateListener {
                override fun onUpdate(configUpdate: ConfigUpdate) {
                    rc.activate().addOnCompleteListener {
                        updateStateFromConfig(rc)
                    }
                }

                override fun onError(error: FirebaseRemoteConfigException) {
                    Log.w("RemoteConfigManager", "Real-time config update error: ${error.message}")
                }
            })

            fetchAndActivate()
        } catch (e: Exception) {
            Log.w("RemoteConfigManager", "Firebase Remote Config initialization fallback: ${e.message}")
        }
    }

    fun fetchAndActivate() {
        val rc = remoteConfig ?: return
        scope.launch {
            try {
                rc.fetchAndActivate().addOnSuccessListener {
                    updateStateFromConfig(rc)
                }.addOnFailureListener { e ->
                    Log.w("RemoteConfigManager", "Remote Config fetch failed: ${e.message}")
                }
            } catch (e: Exception) {
                Log.w("RemoteConfigManager", "Fetch exception: ${e.message}")
            }
        }
    }

    private fun updateStateFromConfig(rc: FirebaseRemoteConfig) {
        val values = RemoteConfigValues(
            widgetOffDayPollMs = rc.getLong("widget_off_day_poll_ms").takeIf { it > 0 } ?: 14400000L,
            widgetGameDayPrePollMs = rc.getLong("widget_game_day_pre_poll_ms").takeIf { it > 0 } ?: 1800000L,
            widgetLivePollMs = rc.getLong("widget_live_poll_ms").takeIf { it > 0 } ?: 60000L,
            isSeasonLeadersEnabled = rc.getBoolean("feature_card_season_leaders_enabled"),
            isPlayoffChaseEnabled = rc.getBoolean("feature_card_playoff_chase_enabled"),
            emergencyWidgetCircuitBreaker = rc.getBoolean("emergency_widget_circuit_breaker"),
            maxLogoDimPx = rc.getLong("max_logo_dim_px").toInt().takeIf { it > 0 } ?: 120
        )
        _configState.value = values
    }
}
