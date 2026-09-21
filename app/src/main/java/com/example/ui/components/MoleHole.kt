package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.FloatingScore
import com.example.ui.MalletSwing
import com.example.ui.theme.BonkRed
import com.example.ui.theme.GoldenHoneyAccent
import com.example.ui.theme.GoldenHoneyLight
import com.example.ui.theme.HoleCavern
import com.example.ui.theme.MeadowGreenDark
import com.example.ui.theme.MeadowGreenLight
import com.example.ui.theme.MeadowGreenPrimary
import com.example.ui.theme.SoilBrownDark
import com.example.ui.theme.SoilBrownLight
import com.example.ui.theme.SoilBrownSecondary
import com.example.ui.theme.StreakPurple

@Composable
fun MoleHole(
    index: Int,
    isActive: Boolean,
    isWhacked: Boolean,
    floatingScore: FloatingScore?,
    malletSwing: MalletSwing?,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    // Mole pop-up translation animation: 1f = fully popped up, 0f = underground
    val moleElevation by animateFloatAsState(
        targetValue = when {
            isWhacked -> 0.4f
            isActive -> 1f
            else -> 0f
        },
        animationSpec = spring(
            dampingRatio = if (isWhacked) 0.5f else 0.7f,
            stiffness = if (isWhacked) 700f else 500f
        ),
        label = "mole_pop"
    )

    // Dizzy stars rotation when whacked
    val infiniteTransition = rememberInfiniteTransition(label = "stars_spin")
    val starsAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "stars_angle"
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .testTag("mole_hole_$index")
            .clip(RoundedCornerShape(22.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onTap
            ),
        contentAlignment = Alignment.Center
    ) {
        // 1. Hole Environment & Dirt Mound
        MoundBackground(modifier = Modifier.fillMaxSize())

        // 2. Underground Cavern Mask (Clipping container for mole popping out)
        Box(
            modifier = Modifier
                .fillMaxSize(0.85f)
                .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp, bottomStart = 26.dp, bottomEnd = 26.dp)),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Cavern depth
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color.Black, HoleCavern, SoilBrownDark),
                            radius = 180f
                        )
                    )
            )

            // 3. The Animated Mole Character
            MoleCharacter(
                elevation = moleElevation,
                isWhacked = isWhacked,
                modifier = Modifier
                    .fillMaxSize(0.92f)
                    .align(Alignment.BottomCenter)
            )
        }

        // 4. Hole Front Rim (Overlaps the mole's lower paws, giving 3D underground depth)
        HoleRimFront(
            modifier = Modifier
                .fillMaxWidth(0.90f)
                .align(Alignment.BottomCenter)
                .padding(bottom = 6.dp)
        )

        // 5. Bonked Impact Overlay (POW! + Dizzy Stars)
        if (isWhacked) {
            BonkOverlay(
                starsAngle = starsAngle,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 6. Mallet Swing animation
        if (malletSwing != null) {
            MalletStrikeEffect(
                isHit = malletSwing.isHit,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 7. Floating Score popup ("+10", "+20 COMBO!")
        if (floatingScore != null) {
            FloatingScoreText(
                text = floatingScore.text,
                isBonus = floatingScore.isBonus,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

/**
 * Mound background with grass tufts and earthy circular rim
 */
@Composable
private fun MoundBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Outer dirt mound shadow & rim
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(SoilBrownSecondary, SoilBrownDark, MeadowGreenDark),
                center = Offset(width / 2f, height * 0.55f),
                radius = width * 0.5f
            ),
            topLeft = Offset(width * 0.05f, height * 0.12f),
            size = Size(width * 0.9f, height * 0.82f)
        )

        // Decorative pebbles / dirt texture
        drawCircle(
            color = SoilBrownLight.copy(alpha = 0.5f),
            radius = 4f,
            center = Offset(width * 0.18f, height * 0.72f)
        )
        drawCircle(
            color = SoilBrownLight.copy(alpha = 0.5f),
            radius = 3.5f,
            center = Offset(width * 0.82f, height * 0.68f)
        )
        drawCircle(
            color = SoilBrownLight.copy(alpha = 0.4f),
            radius = 5f,
            center = Offset(width * 0.5f, height * 0.88f)
        )

        // Grass blades around hole
        val grassBrush = Brush.verticalGradient(listOf(MeadowGreenLight, MeadowGreenDark))
        val path1 = Path().apply {
            moveTo(width * 0.12f, height * 0.45f)
            lineTo(width * 0.06f, height * 0.35f)
            lineTo(width * 0.15f, height * 0.42f)
            close()
        }
        drawPath(path1, grassBrush)

        val path2 = Path().apply {
            moveTo(width * 0.88f, height * 0.45f)
            lineTo(width * 0.94f, height * 0.35f)
            lineTo(width * 0.85f, height * 0.42f)
            close()
        }
        drawPath(path2, grassBrush)
    }
}

/**
 * Front rim of the hole covering the bottom of the mole
 */
@Composable
private fun HoleRimFront(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.aspectRatio(3.5f)) {
        val width = size.width
        val height = size.height

        // Dirt lip
        drawOval(
            brush = Brush.verticalGradient(
                colors = listOf(SoilBrownLight, SoilBrownDark)
            ),
            topLeft = Offset(0f, 0f),
            size = Size(width, height)
        )

        // Subtle highlight line on lip
        drawArc(
            color = SoilBrownLight.copy(alpha = 0.8f),
            startAngle = 10f,
            sweepAngle = 160f,
            useCenter = false,
            topLeft = Offset(width * 0.05f, 2f),
            size = Size(width * 0.9f, height * 0.8f),
            style = Stroke(width = 3f)
        )
    }
}

/**
 * Expressive Animated Mole Character with Hard Hat, Snout, and Eyes
 */
@Composable
private fun MoleCharacter(
    elevation: Float,
    isWhacked: Boolean,
    modifier: Modifier = Modifier
) {
    // Translation: when elevation is 0, mole is shifted down by 100% of height
    val yOffsetFraction = (1f - elevation).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                translationY = size.height * yOffsetFraction
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Mole Body (Warm Rich Brown)
            drawOval(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF8D5B4C), Color(0xFF5D382B)),
                    startY = h * 0.15f,
                    endY = h
                ),
                topLeft = Offset(w * 0.16f, h * 0.18f),
                size = Size(w * 0.68f, h * 0.85f)
            )

            // Mole Chest / Belly highlight
            drawOval(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFBCAAA4).copy(alpha = 0.6f), Color(0xFF8D6E63).copy(alpha = 0.3f))
                ),
                topLeft = Offset(w * 0.28f, h * 0.48f),
                size = Size(w * 0.44f, h * 0.45f)
            )

            // 2. Yellow Safety Hard Hat
            // Hat Crown
            drawArc(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFEE58), Color(0xFFFBC02D), Color(0xFFF57F17))
                ),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(w * 0.25f, h * 0.08f),
                size = Size(w * 0.50f, h * 0.32f)
            )
            // Hat Brim
            drawRoundRect(
                color = Color(0xFFF57F17),
                topLeft = Offset(w * 0.20f, h * 0.24f),
                size = Size(w * 0.60f, h * 0.08f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
            )

            // 3. Eyes & Cheeks
            if (isWhacked) {
                // Dizzy 'X X' eyes
                val eyeLeftCenter = Offset(w * 0.38f, h * 0.36f)
                val eyeRightCenter = Offset(w * 0.62f, h * 0.36f)
                val strokeW = 4f
                val crossSize = 10f

                // Left X
                drawLine(BonkRed, eyeLeftCenter - Offset(crossSize, crossSize), eyeLeftCenter + Offset(crossSize, crossSize), strokeWidth = strokeW)
                drawLine(BonkRed, eyeLeftCenter - Offset(-crossSize, crossSize), eyeLeftCenter + Offset(-crossSize, crossSize), strokeWidth = strokeW)

                // Right X
                drawLine(BonkRed, eyeRightCenter - Offset(crossSize, crossSize), eyeRightCenter + Offset(crossSize, crossSize), strokeWidth = strokeW)
                drawLine(BonkRed, eyeRightCenter - Offset(-crossSize, crossSize), eyeRightCenter + Offset(-crossSize, crossSize), strokeWidth = strokeW)
            } else {
                // Cheerful big cartoon eyes
                // Left eye
                drawOval(
                    color = Color.White,
                    topLeft = Offset(w * 0.33f, h * 0.32f),
                    size = Size(w * 0.14f, h * 0.16f)
                )
                drawCircle(
                    color = Color(0xFF1E1E1E),
                    radius = w * 0.05f,
                    center = Offset(w * 0.40f, h * 0.40f)
                )
                // Specular highlight
                drawCircle(
                    color = Color.White,
                    radius = w * 0.018f,
                    center = Offset(w * 0.41f, h * 0.38f)
                )

                // Right eye
                drawOval(
                    color = Color.White,
                    topLeft = Offset(w * 0.53f, h * 0.32f),
                    size = Size(w * 0.14f, h * 0.16f)
                )
                drawCircle(
                    color = Color(0xFF1E1E1E),
                    radius = w * 0.05f,
                    center = Offset(w * 0.60f, h * 0.40f)
                )
                // Specular highlight
                drawCircle(
                    color = Color.White,
                    radius = w * 0.018f,
                    center = Offset(w * 0.61f, h * 0.38f)
                )
            }

            // Rosy cheeks
            drawCircle(
                color = Color(0xFFFF8A80).copy(alpha = 0.55f),
                radius = w * 0.07f,
                center = Offset(w * 0.26f, h * 0.46f)
            )
            drawCircle(
                color = Color(0xFFFF8A80).copy(alpha = 0.55f),
                radius = w * 0.07f,
                center = Offset(w * 0.74f, h * 0.46f)
            )

            // 4. Snout and Nose
            // Pink snout oval
            drawOval(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFFFFAB91), Color(0xFFFF8A65))
                ),
                topLeft = Offset(w * 0.36f, h * 0.42f),
                size = Size(w * 0.28f, h * 0.18f)
            )
            // Dark button nose
            drawOval(
                color = Color(0xFF3E2723),
                topLeft = Offset(w * 0.43f, h * 0.43f),
                size = Size(w * 0.14f, h * 0.09f)
            )
            // Smile / mouth
            val mouthPath = Path().apply {
                moveTo(w * 0.44f, h * 0.53f)
                quadraticBezierTo(w * 0.50f, h * 0.57f, w * 0.56f, h * 0.53f)
            }
            drawPath(mouthPath, Color(0xFF3E2723), style = Stroke(width = 3.5f))

            // 5. Cute Paws gripping the hole
            // Left paw
            drawOval(
                brush = Brush.verticalGradient(listOf(Color(0xFFFFAB91), Color(0xFFD7CCC8))),
                topLeft = Offset(w * 0.14f, h * 0.60f),
                size = Size(w * 0.20f, h * 0.14f)
            )
            // Right paw
            drawOval(
                brush = Brush.verticalGradient(listOf(Color(0xFFFFAB91), Color(0xFFD7CCC8))),
                topLeft = Offset(w * 0.66f, h * 0.60f),
                size = Size(w * 0.20f, h * 0.14f)
            )
        }
    }
}

/**
 * Visual Bonk effect with "POW!" badge and spinning golden stars
 */
@Composable
private fun BonkOverlay(
    starsAngle: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Comic impact badge
        Surface(
            modifier = Modifier
                .offset(y = (-14).dp)
                .rotate(-8f),
            shape = RoundedCornerShape(8.dp),
            color = BonkRed,
            border = androidx.compose.foundation.BorderStroke(2.dp, Color.White),
            shadowElevation = 6.dp
        ) {
            Text(
                text = "POW!",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            )
        }

        // Orbiting Dizzy Stars
        Box(
            modifier = Modifier
                .size(76.dp)
                .offset(y = (-10).dp)
                .rotate(starsAngle),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = GoldenHoneyAccent,
                modifier = Modifier
                    .size(18.dp)
                    .align(Alignment.TopCenter)
            )
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = GoldenHoneyLight,
                modifier = Modifier
                    .size(14.dp)
                    .align(Alignment.BottomEnd)
            )
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = GoldenHoneyAccent,
                modifier = Modifier
                    .size(16.dp)
                    .align(Alignment.BottomStart)
            )
        }
    }
}

/**
 * Mallet strike animation that swings onto the hole
 */
@Composable
private fun MalletStrikeEffect(
    isHit: Boolean,
    modifier: Modifier = Modifier
) {
    val swingAngle by animateFloatAsState(
        targetValue = 25f,
        animationSpec = tween(150, easing = FastOutSlowInEasing),
        label = "mallet_angle"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Comic hammer icon / visual
        Surface(
            modifier = Modifier
                .offset(x = 12.dp, y = (-20).dp)
                .rotate(swingAngle),
            shape = RoundedCornerShape(6.dp),
            color = if (isHit) Color(0xFFD84315) else Color(0xFF757575),
            shadowElevation = 8.dp
        ) {
            Text(
                text = if (isHit) "🔨 WHACK!" else "💨 MISS",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }
    }
}

/**
 * Floating score text that moves upwards and fades out
 */
@Composable
private fun FloatingScoreText(
    text: String,
    isBonus: Boolean,
    modifier: Modifier = Modifier
) {
    var offsetY by remember { mutableFloatStateOf(0f) }
    var alpha by remember { mutableFloatStateOf(1f) }

    LaunchedEffect(Unit) {
        animate(
            initialValue = 0f,
            targetValue = -36f,
            animationSpec = tween(400, easing = FastOutSlowInEasing)
        ) { value, _ ->
            offsetY = value
            alpha = (1f - (value / -36f).coerceIn(0f, 1f))
        }
    }

    Text(
        text = text,
        modifier = modifier
            .offset(y = offsetY.dp)
            .background(
                color = if (isBonus) StreakPurple else MeadowGreenDark,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 8.dp, vertical = 3.dp),
        style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            fontSize = if (isBonus) 14.sp else 12.sp
        )
    )
}

private suspend fun animate(
    initialValue: Float,
    targetValue: Float,
    animationSpec: androidx.compose.animation.core.AnimationSpec<Float>,
    block: (Float, Float) -> Unit
) {
    androidx.compose.animation.core.Animatable(initialValue).animateTo(
        targetValue = targetValue,
        animationSpec = animationSpec,
        block = { block(value, velocity) }
    )
}
