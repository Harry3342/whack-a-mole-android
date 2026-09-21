package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BonkRed
import com.example.ui.theme.GoldenHoneyAccent
import com.example.ui.theme.GoldenHoneyLight
import com.example.ui.theme.MeadowGreenPrimary
import com.example.ui.theme.StreakPurple

@Composable
fun GameHud(
    score: Int,
    bestScore: Int,
    timeLeftSeconds: Int,
    totalTimeSeconds: Int = 60,
    currentCombo: Int,
    hapticsEnabled: Boolean,
    onPauseClick: () -> Unit,
    onToggleHaptics: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Pulse animation for low time warning (< 10s)
    val isTimeLow = timeLeftSeconds <= 10
    val infiniteTransition = rememberInfiniteTransition(label = "hud_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isTimeLow) 1.08f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "time_pulse"
    )

    val timerProgress = (timeLeftSeconds.toFloat() / totalTimeSeconds).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = timerProgress,
        animationSpec = tween(300),
        label = "timer_progress"
    )

    val timerColor by animateColorAsState(
        targetValue = when {
            timeLeftSeconds <= 10 -> BonkRed
            timeLeftSeconds <= 20 -> GoldenHoneyAccent
            else -> MeadowGreenPrimary
        },
        animationSpec = tween(300),
        label = "timer_color"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("game_hud_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Row: Best Score & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Best score chip
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GoldenHoneyAccent.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldenHoneyAccent.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Best Score",
                            tint = GoldenHoneyAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "BEST: $bestScore",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = GoldenHoneyAccent
                            )
                        )
                    }
                }

                // Action icons: Haptics and Pause
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onToggleHaptics,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("haptic_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (hapticsEnabled) Icons.Filled.Vibration else Icons.Outlined.Vibration,
                            contentDescription = "Toggle Haptics",
                            tint = if (hapticsEnabled) MaterialTheme.colorScheme.primary else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onPauseClick,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("pause_game_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = "Pause Game",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Middle Row: Score & Timer Displays
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Score Column
                Column {
                    Text(
                        text = "SCORE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "$score",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("current_score_text")
                        )

                        // Combo Badge with animation
                        AnimatedVisibility(
                            visible = currentCombo >= 3,
                            enter = scaleIn(spring(dampingRatio = 0.6f)) + fadeIn(),
                            exit = scaleOut() + fadeOut()
                        ) {
                            val comboMultiplier = if (currentCombo >= 7) "x3" else "x2"
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = StreakPurple,
                                modifier = Modifier.testTag("combo_badge")
                            ) {
                                Text(
                                    text = "🔥 $comboMultiplier ($currentCombo STREAK)",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                )
                            }
                        }
                    }
                }

                // Time Left Capsule
                Surface(
                    modifier = Modifier
                        .scale(if (isTimeLow) pulseScale else 1f)
                        .testTag("time_capsule"),
                    shape = RoundedCornerShape(16.dp),
                    color = timerColor.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, timerColor.copy(alpha = 0.7f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Timer",
                            tint = timerColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "${timeLeftSeconds}s",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                color = timerColor
                            ),
                            modifier = Modifier.testTag("time_left_text")
                        )
                    }
                }
            }

            // Timer Progress Bar along bottom of HUD
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(timerColor.copy(alpha = 0.7f), timerColor)
                            )
                        )
                )
            }
        }
    }
}
