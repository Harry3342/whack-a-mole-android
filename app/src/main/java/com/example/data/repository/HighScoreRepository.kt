package com.example.data.repository

import com.example.data.db.HighScoreDao
import com.example.data.db.HighScoreEntity
import kotlinx.coroutines.flow.Flow

class HighScoreRepository(private val dao: HighScoreDao) {
    val topScores: Flow<List<HighScoreEntity>> = dao.getTopScores()
    val bestScore: Flow<HighScoreEntity?> = dao.getBestScore()

    suspend fun saveScore(score: HighScoreEntity): Long {
        return dao.insertScore(score)
    }
}
