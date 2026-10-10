package com.redwings.widget

import android.app.Application
import com.redwings.widget.data.firebase.FanPulseRepository
import com.redwings.widget.data.firebase.FirebaseAiContentEngine
import com.redwings.widget.data.firebase.RemoteConfigManager
import com.redwings.widget.data.local.AppDatabase
import com.redwings.widget.data.repository.HockeyRepository

data class AppContainer(
    val database: AppDatabase,
    val hockeyRepository: HockeyRepository,
    val remoteConfigManager: RemoteConfigManager,
    val aiContentEngine: FirebaseAiContentEngine,
    val fanPulseRepository: FanPulseRepository
)

class RedWingsApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        try {
            com.google.firebase.FirebaseApp.initializeApp(this)
        } catch (t: Throwable) {
            android.util.Log.w("RedWingsApp", "FirebaseApp initializeApp warning: ${t.message}")
        }
        val database = AppDatabase.getDatabase(this)
        val repository = HockeyRepository(
            gameDao = database.gameDao(),
            appContext = applicationContext
        )
        val remoteConfigManager = RemoteConfigManager(applicationContext)
        val aiContentEngine = FirebaseAiContentEngine(applicationContext, remoteConfigManager)
        val fanPulseRepository = FanPulseRepository(applicationContext, database.gameDao())

        container = AppContainer(
            database = database,
            hockeyRepository = repository,
            remoteConfigManager = remoteConfigManager,
            aiContentEngine = aiContentEngine,
            fanPulseRepository = fanPulseRepository
        )
    }
}

