package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "high_scores")
data class HighScoreEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val score: Int,
    val accuracy: Int,
    val totalWhacks: Int,
    val maxCombo: Int,
    val difficulty: String,
    val timestamp: Long = System.currentTimeMillis()
)
