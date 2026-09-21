package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.GameState
import com.example.ui.GameViewModel
import com.example.ui.components.CountdownOverlay
import com.example.ui.components.GameHud
import com.example.ui.components.GameOverDialog
import com.example.ui.components.GameStartCard
import com.example.ui.components.LeaderboardSheet
import com.example.ui.components.MoleGrid
import com.example.ui.components.PauseDialog
import com.example.ui.theme.GoldenHoneyAccent
import com.example.ui.theme.MeadowBackgroundDark
import com.example.ui.theme.MeadowBackgroundLight
import com.example.ui.theme.MeadowGreenDark
import com.example.ui.theme.MeadowGreenLight
import com.example.ui.theme.MeadowGreenPrimary
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                WhackAMoleApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhackAMoleApp(
    viewModel: GameViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val topScores by viewModel.topScores.collectAsStateWithLifecycle()
    val bestScoreEntity by viewModel.bestScore.collectAsStateWithLifecycle()
    val bestScore = bestScoreEntity?.score ?: 0

    var showLeaderboard by remember { mutableStateOf(false) }

    // Background meadow gradient
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            MaterialTheme.colorScheme.background
        )
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Whack A Mole",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showLeaderboard = true },
                        modifier = Modifier.testTag("topbar_leaderboard_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Leaderboard",
                            tint = GoldenHoneyAccent
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 500.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (uiState.gameState) {
                        GameState.IDLE -> {
                            GameStartCard(
                                bestScore = bestScore,
                                selectedDifficulty = uiState.difficulty,
                                onDifficultySelected = { viewModel.setDifficulty(it) },
                                onStartGame = { viewModel.startGame() },
                                onOpenLeaderboard = { showLeaderboard = true }
                            )
                        }

                        GameState.COUNTDOWN,
                        GameState.PLAYING,
                        GameState.PAUSED,
                        GameState.GAME_OVER -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Dynamic Game HUD
                                GameHud(
                                    score = uiState.score,
                                    bestScore = maxOf(bestScore, uiState.score),
                                    timeLeftSeconds = uiState.timeLeftSeconds,
                                    totalTimeSeconds = 60,
                                    currentCombo = uiState.currentCombo,
                                    hapticsEnabled = uiState.hapticsEnabled,
                                    onPauseClick = { viewModel.pauseGame() },
                                    onToggleHaptics = { viewModel.toggleHaptics() }
                                )

                                // Interactive 3x3 Mole Grid
                                MoleGrid(
                                    activeHoleIndex = uiState.activeHoleIndex,
                                    whackedHoleIndex = uiState.whackedHoleIndex,
                                    floatingScores = uiState.floatingScores,
                                    activeMallet = uiState.activeMallet,
                                    onHoleTapped = { index -> viewModel.onHoleTapped(index) }
                                )

                                // Helpful gameplay hint pill
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                ) {
                                    Text(
                                        text = "⚡ Tap moles fast! Consecutive hits trigger x2 and x3 combos",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Overlays: Countdown
            if (uiState.gameState == GameState.COUNTDOWN) {
                CountdownOverlay(count = uiState.countdownNumber)
            }

            // Overlays: Pause
            if (uiState.gameState == GameState.PAUSED) {
                PauseDialog(
                    onResume = { viewModel.resumeGame() },
                    onRestart = { viewModel.startGame() },
                    onMainMenu = { viewModel.quitToMenu() }
                )
            }

            // Overlays: Game Over Scorecard Dialog
            if (uiState.gameState == GameState.GAME_OVER) {
                GameOverDialog(
                    score = uiState.score,
                    accuracy = uiState.accuracyPercentage,
                    totalHits = uiState.totalHits,
                    maxCombo = uiState.maxCombo,
                    isNewRecord = uiState.isNewRecord,
                    onPlayAgain = { viewModel.startGame() },
                    onViewLeaderboard = { showLeaderboard = true },
                    onMainMenu = { viewModel.quitToMenu() }
                )
            }

            // Leaderboard Bottom Sheet
            if (showLeaderboard) {
                LeaderboardSheet(
                    scores = topScores,
                    onDismiss = { showLeaderboard = false }
                )
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
