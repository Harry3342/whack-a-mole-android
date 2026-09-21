package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HighScoreDao {
    @Query("SELECT * FROM high_scores ORDER BY score DESC, timestamp DESC LIMIT :limit")
    fun getTopScores(limit: Int = 10): Flow<List<HighScoreEntity>>

    @Query("SELECT * FROM high_scores ORDER BY score DESC LIMIT 1")
    fun getBestScore(): Flow<HighScoreEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: HighScoreEntity): Long
}
