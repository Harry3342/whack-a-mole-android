package com.example.ui

enum class GameState {
    IDLE,
    COUNTDOWN,
    PLAYING,
    PAUSED,
    GAME_OVER
}

enum class GameDifficulty(val displayName: String, val moleIntervalMs: Long, val description: String) {
    CASUAL("Casual", 700L, "Relaxed pace • 700ms"),
    CLASSIC("Classic", 500L, "Original arcade • 500ms"),
    BLITZ("Blitz", 360L, "Super fast • 360ms")
}

data class FloatingScore(
    val id: Long,
    val holeIndex: Int,
    val text: String,
    val isBonus: Boolean
)

data class MalletSwing(
    val id: Long,
    val holeIndex: Int,
    val isHit: Boolean
)

data class GameUiState(
    val gameState: GameState = GameState.IDLE,
    val difficulty: GameDifficulty = GameDifficulty.CLASSIC,
    val score: Int = 0,
    val timeLeftSeconds: Int = 60,
    val countdownNumber: Int = 3,
    val activeHoleIndex: Int? = null,
    val whackedHoleIndex: Int? = null,
    val currentCombo: Int = 0,
    val maxCombo: Int = 0,
    val totalHits: Int = 0,
    val totalTaps: Int = 0,
    val isNewRecord: Boolean = false,
    val floatingScores: List<FloatingScore> = emptyList(),
    val activeMallet: MalletSwing? = null,
    val hapticsEnabled: Boolean = true
) {
    val accuracyPercentage: Int
        get() = if (totalTaps > 0) ((totalHits.toFloat() / totalTaps) * 100).toInt() else 100
}
