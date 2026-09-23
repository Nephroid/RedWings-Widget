package com.redwings.widget.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class RedWingsGame(
    @PrimaryKey val gameId: Int,
    val gameTimeMillis: Long,
    val opponentName: String,
    val opponentAbbrev: String,
    val isHomeGame: Boolean,
    val venueName: String,
    val gameState: String,
    val wingsScore: Int = 0,
    val opponentScore: Int = 0,
    val isFinal: Boolean = false,
    val isSimulated: Boolean = false,
    val standingsSummary: String = "",
    val headToHead: String = ""
)
