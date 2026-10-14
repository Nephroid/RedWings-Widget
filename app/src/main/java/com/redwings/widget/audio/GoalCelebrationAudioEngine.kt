package com.redwings.widget.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.redwings.widget.R
import kotlinx.coroutines.*
import java.util.Locale

class GoalCelebrationAudioEngine private constructor(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var audioFocusRequest: AudioFocusRequest? = null

    init {
        initTts()
    }

    private fun initTts() {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.let { engine ->
                    engine.language = Locale.US
                    engine.setPitch(0.95f)
                    engine.setSpeechRate(1.05f)
                    isTtsReady = true
                }
            } else {
                Log.w(TAG, "TTS Initialization failed with status: $status")
            }
        }
    }

    fun playCelebration(
        scorerName: String = "Dylan Larkin",
        sweaterNumber: Int = 71,
        isOvertime: Boolean = false
    ) {
        scope.launch {
            requestAudioFocus()

            // Step 1: Play authentic Goal Horn
            playGoalHorn()

            // Step 2: Speak Arena Goal Announcement
            val announcement = if (isOvertime) {
                "Detroit Red Wings Overtime Winner! Scored by number $sweaterNumber, $scorerName!"
            } else {
                "Detroit Red Wings goal! Scored by number $sweaterNumber, $scorerName!"
            }

            speakAnnouncement(announcement)
        }
    }

    private suspend fun playGoalHorn(): Long = suspendCancellableCoroutine { continuation ->
        try {
            val mediaPlayer = MediaPlayer.create(context, R.raw.red_wings_goal_horn)
            if (mediaPlayer == null) {
                continuation.resume(0L) {}
                return@suspendCancellableCoroutine
            }

            mediaPlayer.setOnCompletionListener { mp ->
                val duration = mp.duration.toLong()
                mp.release()
                if (continuation.isActive) {
                    continuation.resume(duration) {}
                }
            }

            mediaPlayer.setOnErrorListener { mp, _, _ ->
                mp.release()
                if (continuation.isActive) {
                    continuation.resume(0L) {}
                }
                true
            }

            mediaPlayer.start()
        } catch (e: Exception) {
            Log.e(TAG, "Error playing goal horn: ${e.message}")
            if (continuation.isActive) {
                continuation.resume(0L) {}
            }
        }
    }

    private suspend fun speakAnnouncement(text: String) = suspendCancellableCoroutine { continuation ->
        val engine = tts
        if (engine == null || !isTtsReady) {
            abandonAudioFocus()
            continuation.resume(Unit) {}
            return@suspendCancellableCoroutine
        }

        val utteranceId = "GOAL_${System.currentTimeMillis()}"
        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}

            override fun onDone(utteranceId: String?) {
                abandonAudioFocus()
                if (continuation.isActive) {
                    continuation.resume(Unit) {}
                }
            }

            override fun onError(utteranceId: String?) {
                abandonAudioFocus()
                if (continuation.isActive) {
                    continuation.resume(Unit) {}
                }
            }
        })

        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val playbackAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()

            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(playbackAttributes)
                .setAcceptsDelayedFocusGain(false)
                .build()

            audioFocusRequest?.let { audioManager.requestAudioFocus(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                null,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
            )
        }
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(null)
        }
    }

    companion object {
        private const val TAG = "GoalCelebrationAudio"

        @Volatile
        private var instance: GoalCelebrationAudioEngine? = null

        fun getInstance(context: Context): GoalCelebrationAudioEngine {
            return instance ?: synchronized(this) {
                instance ?: GoalCelebrationAudioEngine(context.applicationContext).also { instance = it }
            }
        }
    }
}
