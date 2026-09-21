package com.example.ui

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.HighScoreEntity
import com.example.data.db.WhackDatabase
import com.example.data.repository.HighScoreRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.random.Random

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HighScoreRepository
    val topScores: StateFlow<List<HighScoreEntity>>
    val bestScore: StateFlow<HighScoreEntity?>

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var gameTimerJob: Job? = null
    private var moleTimerJob: Job? = null
    private var countdownJob: Job? = null

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    init {
        val database = WhackDatabase.getDatabase(application)
        repository = HighScoreRepository(database.highScoreDao())

        topScores = repository.topScores.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        bestScore = repository.bestScore.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    }

    fun setDifficulty(difficulty: GameDifficulty) {
        if (_uiState.value.gameState == GameState.IDLE) {
            _uiState.update { it.copy(difficulty = difficulty) }
        }
    }

    fun toggleHaptics() {
        _uiState.update { it.copy(hapticsEnabled = !it.hapticsEnabled) }
    }

    fun startGame() {
        cancelAllJobs()
        _uiState.update {
            it.copy(
                gameState = GameState.COUNTDOWN,
                score = 0,
                timeLeftSeconds = 60,
                countdownNumber = 3,
                activeHoleIndex = null,
                whackedHoleIndex = null,
                currentCombo = 0,
                maxCombo = 0,
                totalHits = 0,
                totalTaps = 0,
                isNewRecord = false,
                floatingScores = emptyList(),
                activeMallet = null
            )
        }

        countdownJob = viewModelScope.launch {
            for (count in 3 downTo 1) {
                _uiState.update { it.copy(countdownNumber = count) }
                vibrateTick()
                delay(800L)
            }
            _uiState.update { it.copy(gameState = GameState.PLAYING) }
            vibrateSuccess()
            beginPlaying()
        }
    }

    private fun beginPlaying() {
        startMoleSpawner()
        startGameCountdown()
    }

    private fun startMoleSpawner() {
        moleTimerJob?.cancel()
        moleTimerJob = viewModelScope.launch {
            val interval = _uiState.value.difficulty.moleIntervalMs
            while (_uiState.value.gameState == GameState.PLAYING) {
                // Select a random hole (0..8)
                val current = _uiState.value.activeHoleIndex
                var next = Random.nextInt(9)
                if (next == current) {
                    next = (next + Random.nextInt(1, 9)) % 9
                }

                _uiState.update {
                    it.copy(
                        activeHoleIndex = next,
                        whackedHoleIndex = null
                    )
                }

                delay(interval)

                // If not hit, mole hides briefly before next pop up
                if (_uiState.value.activeHoleIndex == next) {
                    _uiState.update { it.copy(activeHoleIndex = null) }
                    delay(100L)
                }
            }
        }
    }

    private fun startGameCountdown() {
        gameTimerJob?.cancel()
        gameTimerJob = viewModelScope.launch {
            while (_uiState.value.gameState == GameState.PLAYING && _uiState.value.timeLeftSeconds > 0) {
                delay(1000L)
                if (_uiState.value.gameState == GameState.PLAYING) {
                    val remaining = _uiState.value.timeLeftSeconds - 1
                    _uiState.update { it.copy(timeLeftSeconds = remaining) }
                    if (remaining <= 5 && remaining > 0) {
                        vibrateTick()
                    }
                    if (remaining <= 0) {
                        onGameOver()
                    }
                }
            }
        }
    }

    fun onHoleTapped(holeIndex: Int) {
        val currentState = _uiState.value
        if (currentState.gameState != GameState.PLAYING) return

        val isHit = (currentState.activeHoleIndex == holeIndex)
        val newTotalTaps = currentState.totalTaps + 1

        // Trigger Mallet swing visual
        val malletSwing = MalletSwing(System.currentTimeMillis(), holeIndex, isHit)

        if (isHit) {
            val newTotalHits = currentState.totalHits + 1
            val newCombo = currentState.currentCombo + 1
            val newMaxCombo = max(currentState.maxCombo, newCombo)

            val multiplier = when {
                newCombo >= 7 -> 3
                newCombo >= 4 -> 2
                else -> 1
            }
            val pointsEarned = 10 * multiplier
            val newScore = currentState.score + pointsEarned

            val floatText = if (multiplier > 1) "+$pointsEarned (x$multiplier!)" else "+$pointsEarned"
            val newFloat = FloatingScore(
                id = System.currentTimeMillis(),
                holeIndex = holeIndex,
                text = floatText,
                isBonus = multiplier > 1
            )

            _uiState.update {
                it.copy(
                    score = newScore,
                    totalHits = newTotalHits,
                    totalTaps = newTotalTaps,
                    currentCombo = newCombo,
                    maxCombo = newMaxCombo,
                    activeHoleIndex = null,
                    whackedHoleIndex = holeIndex,
                    activeMallet = malletSwing,
                    floatingScores = (it.floatingScores + newFloat).takeLast(4)
                )
            }

            vibrateBonk()

            // Remove whacked state after brief bonk animation
            viewModelScope.launch {
                delay(320L)
                _uiState.update {
                    if (it.whackedHoleIndex == holeIndex) it.copy(whackedHoleIndex = null) else it
                }
            }
        } else {
            // Missed! Reset combo
            _uiState.update {
                it.copy(
                    currentCombo = 0,
                    totalTaps = newTotalTaps,
                    activeMallet = malletSwing
                )
            }
            vibrateMiss()
        }

        // Auto remove mallet swing after quick strike
        viewModelScope.launch {
            delay(220L)
            _uiState.update {
                if (it.activeMallet?.id == malletSwing.id) it.copy(activeMallet = null) else it
            }
        }
    }

    fun pauseGame() {
        if (_uiState.value.gameState == GameState.PLAYING) {
            cancelAllJobs()
            _uiState.update { it.copy(gameState = GameState.PAUSED) }
        }
    }

    fun resumeGame() {
        if (_uiState.value.gameState == GameState.PAUSED) {
            _uiState.update { it.copy(gameState = GameState.PLAYING) }
            beginPlaying()
        }
    }

    fun quitToMenu() {
        cancelAllJobs()
        _uiState.update { it.copy(gameState = GameState.IDLE) }
    }

    private fun onGameOver() {
        cancelAllJobs()
        val finalState = _uiState.value
        val best = bestScore.value?.score ?: 0
        val isRecord = finalState.score > 0 && finalState.score > best

        _uiState.update {
            it.copy(
                gameState = GameState.GAME_OVER,
                isNewRecord = isRecord,
                activeHoleIndex = null,
                whackedHoleIndex = null
            )
        }

        vibrateGameOver()

        // Persist to Room Database
        viewModelScope.launch {
            repository.saveScore(
                HighScoreEntity(
                    score = finalState.score,
                    accuracy = finalState.accuracyPercentage,
                    totalWhacks = finalState.totalHits,
                    maxCombo = finalState.maxCombo,
                    difficulty = finalState.difficulty.displayName,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    private fun cancelAllJobs() {
        countdownJob?.cancel()
        gameTimerJob?.cancel()
        moleTimerJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        cancelAllJobs()
    }

    private fun vibrateBonk() {
        if (!_uiState.value.hapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(45L, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(45L)
            }
        } catch (_: Exception) {}
    }

    private fun vibrateMiss() {
        if (!_uiState.value.hapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(20L, 100))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(20L)
            }
        } catch (_: Exception) {}
    }

    private fun vibrateTick() {
        if (!_uiState.value.hapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(25L, 120))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(25L)
            }
        } catch (_: Exception) {}
    }

    private fun vibrateSuccess() {
        if (!_uiState.value.hapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 40, 50, 60), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(70L)
            }
        } catch (_: Exception) {}
    }

    private fun vibrateGameOver() {
        if (!_uiState.value.hapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 80, 80, 120), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(150L)
            }
        } catch (_: Exception) {}
    }
}
