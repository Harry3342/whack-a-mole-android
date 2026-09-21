package com.example

import androidx.test.core.app.ApplicationProvider
import com.example.ui.GameDifficulty
import com.example.ui.GameState
import com.example.ui.GameViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class WhackGameLogicTest {

    @Test
    fun testInitialGameState() {
        val viewModel = GameViewModel(ApplicationProvider.getApplicationContext())
        val state = viewModel.uiState.value

        assertEquals(GameState.IDLE, state.gameState)
        assertEquals(0, state.score)
        assertEquals(60, state.timeLeftSeconds)
        assertEquals(GameDifficulty.CLASSIC, state.difficulty)
    }

    @Test
    fun testDifficultySelection() {
        val viewModel = GameViewModel(ApplicationProvider.getApplicationContext())
        viewModel.setDifficulty(GameDifficulty.BLITZ)
        assertEquals(GameDifficulty.BLITZ, viewModel.uiState.value.difficulty)

        viewModel.setDifficulty(GameDifficulty.CASUAL)
        assertEquals(GameDifficulty.CASUAL, viewModel.uiState.value.difficulty)
    }

    @Test
    fun testAccuracyCalculation() {
        val viewModel = GameViewModel(ApplicationProvider.getApplicationContext())
        val state = viewModel.uiState.value
        // When no taps yet, accuracy should be 100%
        assertEquals(100, state.accuracyPercentage)
    }
}
