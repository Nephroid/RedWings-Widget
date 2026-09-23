package com.redwings.widget

import android.app.Application
import com.redwings.widget.data.local.AppDatabase
import com.redwings.widget.data.repository.HockeyRepository

data class AppContainer(
    val database: AppDatabase,
    val hockeyRepository: HockeyRepository
)

class RedWingsApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.getDatabase(this)
        val repository = HockeyRepository(
            gameDao = database.gameDao(),
            appContext = applicationContext
        )
        container = AppContainer(
            database = database,
            hockeyRepository = repository
        )
    }
}
