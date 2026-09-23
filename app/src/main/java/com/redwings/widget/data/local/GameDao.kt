package com.redwings.widget.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.redwings.widget.data.model.RedWingsGame
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM games ORDER BY gameTimeMillis ASC LIMIT 8")
    fun getGames(): Flow<List<RedWingsGame>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGames(games: List<RedWingsGame>)

    @Query("DELETE FROM games")
    suspend fun clearGames()

    @Transaction
    suspend fun replaceGames(games: List<RedWingsGame>) {
        clearGames()
        insertGames(games)
    }

    @Query(
        "SELECT * FROM games WHERE isFinal = 0 " +
            "AND gameTimeMillis > :now - 14400000 " +
            "ORDER BY gameTimeMillis ASC LIMIT 1"
    )
    suspend fun getNextGame(now: Long = System.currentTimeMillis()): RedWingsGame?
}
