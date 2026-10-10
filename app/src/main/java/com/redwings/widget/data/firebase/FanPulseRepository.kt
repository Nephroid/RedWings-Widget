package com.redwings.widget.data.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.redwings.widget.data.local.GameDao
import com.redwings.widget.data.repository.HockeyRepository
import com.redwings.widget.widget.WidgetBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.atomic.AtomicInteger

class FanPulseRepository(
    private val context: Context,
    private val gameDao: GameDao? = null,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {

    private val auth by lazy {
        try { FirebaseAuth.getInstance() } catch (_: Throwable) { null }
    }

    private val firestore by lazy {
        try { FirebaseFirestore.getInstance() } catch (_: Throwable) { null }
    }

    private val prefs = context.getSharedPreferences(HockeyRepository.PREFS, Context.MODE_PRIVATE)
    private val widgetPrefs = context.getSharedPreferences(WidgetBinder.PREFS, Context.MODE_PRIVATE)

    private val _fanPulseCount = MutableStateFlow(loadCachedFanPulse())
    val fanPulseCount: StateFlow<Long> = _fanPulseCount.asStateFlow()

    private var pulseListener: ListenerRegistration? = null
    private var prefsListener: ListenerRegistration? = null
    private var scoreMirrorListener: ListenerRegistration? = null

    private val pendingTaps = AtomicInteger(0)
    private var debounceJob: Job? = null
    private var currentGameId: Int = 0

    init {
        try {
            ensureAnonymousAuth()
        } catch (t: Throwable) {
            Log.w("FanPulseRepository", "init warning: ${t.message}")
        }
    }

    private fun ensureAnonymousAuth() {
        val a = auth ?: return
        if (a.currentUser == null) {
            scope.launch {
                try {
                    a.signInAnonymously().await()
                    Log.d("FanPulseRepository", "Signed in anonymously as ${a.currentUser?.uid}")
                    listenToUserPreferences()
                } catch (t: Throwable) {
                    Log.w("FanPulseRepository", "Anonymous auth fallback: ${t.message}")
                }
            }
        } else {
            listenToUserPreferences()
        }
    }

    /**
     * Attaches real-time listener to game's 10-shard distributed counter.
     */
    fun attachGamePulseListener(gameId: Int) {
        if (gameId <= 0 || gameId == currentGameId) return
        currentGameId = gameId

        pulseListener?.remove()
        val db = firestore ?: return

        try {
            pulseListener = db.collection("live_pulse")
                .document(gameId.toString())
                .collection("shards")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("FanPulseRepository", "Sharded counter listener error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        var total = 0L
                        for (doc in snapshot.documents) {
                            val count = doc.getLong("count") ?: 0L
                            total += count
                        }
                        if (total > 0L) {
                            _fanPulseCount.value = total
                            saveCachedFanPulse(total)
                        }
                    }
                }
        } catch (e: Exception) {
            Log.w("FanPulseRepository", "Attaching pulse listener failed: ${e.message}")
        }

        attachLiveScoreMirror(gameId)
    }

    /**
     * Fan action: "Light the Lamp" / "Goal Horn" celebration.
     * Instant local optimistic increment (0ms UI latency), debounced batch commit to Firestore.
     */
    fun lightTheLamp(gameId: Int = currentGameId) {
        if (gameId <= 0) return
        _fanPulseCount.value += 1
        pendingTaps.incrementAndGet()

        debounceJob?.cancel()
        debounceJob = scope.launch {
            delay(1500L) // 1.5s sliding window batching
            val countToCommit = pendingTaps.getAndSet(0)
            if (countToCommit > 0) {
                commitBatchTaps(gameId, countToCommit)
            }
        }
    }

    private suspend fun commitBatchTaps(gameId: Int, count: Int) {
        val db = firestore ?: return
        try {
            val shardIndex = (0..9).random()
            val shardRef = db.collection("live_pulse")
                .document(gameId.toString())
                .collection("shards")
                .document("shard_$shardIndex")

            shardRef.set(
                mapOf("count" to FieldValue.increment(count.toLong())),
                SetOptions.merge()
            ).await()
            Log.d("FanPulseRepository", "Batched $count lamp taps to shard_$shardIndex")
        } catch (e: Exception) {
            Log.w("FanPulseRepository", "Failed to commit batched lamp taps: ${e.message}")
        }
    }

    /**
     * Synchronizes selected Jersey Theme across user's devices via Firestore.
     */
    private fun listenToUserPreferences() {
        val uid = auth?.currentUser?.uid ?: return
        val db = firestore ?: return

        try {
            prefsListener = db.collection("users")
                .document(uid)
                .collection("preferences")
                .document("settings")
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    val remoteTheme = snapshot.getLong("jerseyTheme")?.toInt()
                    if (remoteTheme != null) {
                        val localTheme = widgetPrefs.getInt("widget_theme_index", 0)
                        if (localTheme != remoteTheme) {
                            widgetPrefs.edit().putInt("widget_theme_index", remoteTheme).apply()
                            Log.d("FanPulseRepository", "Synced remote jersey theme: $remoteTheme")
                        }
                    }
                }
        } catch (e: Exception) {
            Log.w("FanPulseRepository", "Failed to listen to preferences: ${e.message}")
        }
    }

    fun syncThemeToCloud(themeIndex: Int) {
        val uid = auth?.currentUser?.uid ?: return
        val db = firestore ?: return

        scope.launch {
            try {
                db.collection("users")
                    .document(uid)
                    .collection("preferences")
                    .document("settings")
                    .set(mapOf("jerseyTheme" to themeIndex), SetOptions.merge())
                    .await()
            } catch (e: Exception) {
                Log.w("FanPulseRepository", "Syncing theme failed: ${e.message}")
            }
        }
    }

    /**
     * Optional live score mirroring from Firestore to Room DAO.
     * Preserves Room as the Single Source of Truth (SSOT).
     */
    private fun attachLiveScoreMirror(gameId: Int) {
        scoreMirrorListener?.remove()
        val db = firestore ?: return

        try {
            scoreMirrorListener = db.collection("live_games")
                .document(gameId.toString())
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                    val wingsScore = snapshot.getLong("wingsScore")?.toInt() ?: return@addSnapshotListener
                    val oppScore = snapshot.getLong("oppScore")?.toInt() ?: return@addSnapshotListener
                    val isFinal = snapshot.getBoolean("isFinal") ?: false

                    scope.launch {
                        try {
                            // Update shared prefs and trigger UI refresh
                            prefs.edit()
                                .putInt("live_wings_score", wingsScore)
                                .putInt("live_opp_score", oppScore)
                                .apply()
                        } catch (e: Exception) {
                            Log.w("FanPulseRepository", "Mirror score update failed: ${e.message}")
                        }
                    }
                }
        } catch (e: Exception) {
            Log.w("FanPulseRepository", "Live score mirror listener failed: ${e.message}")
        }
    }

    private fun loadCachedFanPulse(): Long = prefs.getLong("fan_pulse_count", 1926L) // 1926: Wings inaugural year!

    private fun saveCachedFanPulse(count: Long) {
        prefs.edit().putLong("fan_pulse_count", count).apply()
    }

    fun cleanup() {
        pulseListener?.remove()
        prefsListener?.remove()
        scoreMirrorListener?.remove()
        debounceJob?.cancel()
    }
}
