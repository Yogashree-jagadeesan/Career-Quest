package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.navigation.ScreenRoute
import com.example.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Premium Krishna-Radha Inspired Career App Background.
 * Displays clearly visible Lord Krishna and Radha in sacred Vrindavan grove with trees,
 * sacred Yamuna water, flowers and soft greenery.
 * Carefully balanced translucent gradient scrim ensures all text & glass cards remain crisp and readable
 * while letting the divine artwork shine through brightly.
 */
@Composable
fun KrishnaAppBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Full screen Krishna and Radha in Vrindavan wallpaper
        Image(
            painter = painterResource(id = R.drawable.img_krishna_radha_bg),
            contentDescription = "Lord Krishna and Radha in sacred Vrindavan with trees and lotus flowers",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Soft gradient scrim: leaves Krishna and Radha clearly visible while giving top/bottom contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            PeacockBlueDark.copy(alpha = 0.45f), // Soft top for status/top bar
                            Color.Transparent,                  // Clear center so Radha-Krishna are prominent
                            PeacockBlueNight.copy(alpha = 0.25f),
                            RoyalBlueDeep.copy(alpha = 0.65f)   // Soft bottom for navigation
                        )
                    )
                )
        )

        // Floating small golden sparkles, lotuses & peacock feathers
        FloatingSparklesAndLotusesBackground(modifier = Modifier.fillMaxSize())

        // Top level app content
        content()
    }
}

/**
 * Floating small golden sparkles, gentle lotus petals, and peacock feathers drifting in the Vrindavan breeze.
 */
@Composable
fun FloatingSparklesAndLotusesBackground(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "VrindavanAmbience")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "VrindavanPhase"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 1. Small Golden Sparkle Stars
        val sparkles = listOf(
            Triple(0.12f, 0.22f, 3.dp),
            Triple(0.88f, 0.16f, 2.5.dp),
            Triple(0.24f, 0.48f, 3.5.dp),
            Triple(0.78f, 0.38f, 2.5.dp),
            Triple(0.48f, 0.70f, 4.dp),
            Triple(0.90f, 0.78f, 3.dp),
            Triple(0.15f, 0.82f, 2.5.dp),
            Triple(0.62f, 0.18f, 3.dp),
            Triple(0.35f, 0.32f, 2.dp)
        )

        sparkles.forEachIndexed { i, s ->
            val baseY = s.second
            val currentY = ((baseY - (phase * 0.35f) + 1f) % 1f) * h
            val currentX = s.first * w
            val alpha = (0.35f + 0.65f * kotlin.math.sin((phase * 2 * Math.PI + i).toFloat()).coerceIn(0f, 1f))
            val starColor = if (i % 2 == 0) DivineGold.copy(alpha = alpha) else TurquoiseAccent.copy(alpha = alpha)

            val r = s.third.toPx()
            val starPath = Path().apply {
                moveTo(currentX, currentY - r * 1.5f)
                lineTo(currentX + r * 0.35f, currentY - r * 0.35f)
                lineTo(currentX + r * 1.5f, currentY)
                lineTo(currentX + r * 0.35f, currentY + r * 0.35f)
                lineTo(currentX, currentY + r * 1.5f)
                lineTo(currentX - r * 0.35f, currentY + r * 0.35f)
                lineTo(currentX - r * 1.5f, currentY)
                lineTo(currentX - r * 0.35f, currentY - r * 0.35f)
                close()
            }
            drawPath(starPath, starColor)
        }

        // 2. Gentle Floating Lotus Petals
        val petals = listOf(
            Triple(0.20f, 0.30f, 0.8f),
            Triple(0.82f, 0.50f, 1.0f),
            Triple(0.40f, 0.85f, 0.9f),
            Triple(0.70f, 0.25f, 0.7f)
        )

        petals.forEachIndexed { idx, p ->
            val startY = p.second
            val yPos = ((startY + phase * 0.25f) % 1f) * h
            val sway = kotlin.math.sin((phase * 6.28f + idx).toDouble()).toFloat() * 16.dp.toPx()
            val xPos = (p.first * w) + sway
            val petalColor = if (idx % 2 == 0) LotusPink.copy(alpha = 0.55f) else LotusPetalPink.copy(alpha = 0.45f)

            val pw = (8.dp * p.third).toPx()
            val ph = (14.dp * p.third).toPx()
            val petalPath = Path().apply {
                moveTo(xPos, yPos - ph * 0.5f)
                cubicTo(xPos + pw, yPos - ph * 0.2f, xPos + pw * 0.8f, yPos + ph * 0.5f, xPos, yPos + ph * 0.5f)
                cubicTo(xPos - pw * 0.8f, yPos + ph * 0.5f, xPos - pw, yPos - ph * 0.2f, xPos, yPos - ph * 0.5f)
                close()
            }
            drawPath(petalPath, petalColor)
        }
    }
}

/**
 * Backward compatibility alias for FloatingSparklesBackground.
 */
@Composable
fun FloatingSparklesBackground(
    modifier: Modifier = Modifier
) {
    FloatingSparklesAndLotusesBackground(modifier = modifier)
}

/**
 * Elegant Glass Card with Gold Borders.
 * Translucent peacock blue body allows Vrindavan background to shine through softly,
 * framed by a radiant double gold and turquoise border with top shimmer accent.
 */
@Composable
fun KrishnaGlassCard(
    modifier: Modifier = Modifier,
    borderColor: Color = DivineGold,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xD907243E), // Translucent dark peacock blue
                        Color(0xE605172A),
                        Color(0xD90B1D34)  // Royal blue depth
                    )
                )
            )
            .border(
                1.5.dp,
                Brush.linearGradient(
                    listOf(
                        DivineGold,
                        TurquoiseAccent.copy(alpha = 0.65f),
                        EmeraldGreen.copy(alpha = 0.55f),
                        DivineGold
                    )
                ),
                RoundedCornerShape(18.dp)
            )
    ) {
        // Decorative golden, turquoise & emerald top hairline glow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.5.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            TurquoiseAccent.copy(alpha = 0.6f),
                            DivineGold,
                            EmeraldGreen.copy(alpha = 0.7f),
                            GoldYellowWarm,
                            TurquoiseAccent.copy(alpha = 0.6f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            content()
        }
    }
}

/**
 * Backward compatibility alias for HeritageOrnamentalCard.
 */
@Composable
fun HeritageOrnamentalCard(
    modifier: Modifier = Modifier,
    borderColor: Color = DivineGold,
    content: @Composable ColumnScope.() -> Unit
) {
    KrishnaGlassCard(
        modifier = modifier,
        borderColor = borderColor,
        content = content
    )
}

/**
 * Royal Palace Door Opening Animation Overlay.
 * Sequence when a tab is clicked:
 * 1. Two ornate royal palace doors close toward the center seam
 * 2. When they meet, a radiant golden sunburst glow + sparkles erupts from the center
 * 3. The destination screen loads underneath
 * 4. The palace doors swing open wide to reveal the new screen!
 */
@Composable
fun RoyalPalaceDoorOverlay(
    doorProgress: Float, // 0f = doors open offscreen, 1f = doors fully closed in center
    glowAlpha: Float,    // 0f to 1f golden sunburst glow
    modifier: Modifier = Modifier
) {
    if (doorProgress <= 0.001f && glowAlpha <= 0.001f) return

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            val halfW = w / 2f
            val openFraction = (1f - doorProgress).coerceIn(0f, 1f)
            val leftOffset = -halfW * openFraction
            val rightOffset = halfW * openFraction

            // --- LEFT GRAND PALACE WOODEN DOOR ---
            val leftDoorRect = Rect(leftOffset, 0f, leftOffset + halfW, h)
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF1B0C06),
                        Color(0xFF2C1810),
                        Color(0xFF3E2723),
                        Color(0xFF4E342E)
                    ),
                    startX = leftDoorRect.left,
                    endX = leftDoorRect.right
                ),
                topLeft = Offset(leftDoorRect.left, 0f),
                size = Size(halfW, h)
            )

            // Ornate Antique Gold Carved Panel Frame on Left Door
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(DivineGold, GoldYellowWarm, DivineGold, GoldTrim)
                ),
                topLeft = Offset(leftDoorRect.left + 16.dp.toPx(), 24.dp.toPx()),
                size = Size(halfW - 22.dp.toPx(), h - 48.dp.toPx()),
                style = Stroke(width = 3.5.dp.toPx())
            )

            // Inner carved wood panel border
            drawRect(
                color = DivineGold.copy(alpha = 0.6f),
                topLeft = Offset(leftDoorRect.left + 26.dp.toPx(), 36.dp.toPx()),
                size = Size(halfW - 42.dp.toPx(), h - 72.dp.toPx()),
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Traditional South Indian Temple Arch Carving on Left Door
            val leftArchPath = Path().apply {
                val archL = leftDoorRect.left + 36.dp.toPx()
                val archR = leftDoorRect.right - 16.dp.toPx()
                val archW = archR - archL
                val archTop = h * 0.20f
                val archBottom = h * 0.80f

                moveTo(archL, archBottom)
                lineTo(archL, archTop + archW * 0.4f)
                cubicTo(archL, archTop + archW * 0.1f, archL + archW * 0.35f, archTop, archL + archW * 0.5f, archTop)
                cubicTo(archL + archW * 0.65f, archTop, archR, archTop + archW * 0.1f, archR, archTop + archW * 0.4f)
                lineTo(archR, archBottom)
                close()
            }
            drawPath(
                path = leftArchPath,
                brush = Brush.verticalGradient(listOf(DivineGold.copy(alpha = 0.85f), GoldTrim.copy(alpha = 0.6f))),
                style = Stroke(width = 2.5.dp.toPx())
            )

            // Grand Antique Gold Ring Handle / Knockers on Left Door
            val leftCenterY = h * 0.50f
            val leftCenterX = leftDoorRect.right - 36.dp.toPx()
            drawCircle(
                brush = Brush.radialGradient(listOf(DivineGoldShimmer, DivineGold, GoldTrim)),
                radius = 22.dp.toPx(),
                center = Offset(leftCenterX, leftCenterY),
                style = Stroke(width = 4.dp.toPx())
            )
            drawCircle(
                color = DivineGold,
                radius = 7.dp.toPx(),
                center = Offset(leftCenterX, leftCenterY)
            )

            // --- RIGHT GRAND PALACE WOODEN DOOR ---
            val rightDoorLeft = halfW + rightOffset
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF4E342E),
                        Color(0xFF3E2723),
                        Color(0xFF2C1810),
                        Color(0xFF1B0C06)
                    ),
                    startX = rightDoorLeft,
                    endX = rightDoorLeft + halfW
                ),
                topLeft = Offset(rightDoorLeft, 0f),
                size = Size(halfW, h)
            )

            // Ornate Antique Gold Carved Panel Frame on Right Door
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(DivineGold, GoldYellowWarm, DivineGold, GoldTrim)
                ),
                topLeft = Offset(rightDoorLeft + 6.dp.toPx(), 24.dp.toPx()),
                size = Size(halfW - 22.dp.toPx(), h - 48.dp.toPx()),
                style = Stroke(width = 3.5.dp.toPx())
            )

            // Inner carved wood panel border
            drawRect(
                color = DivineGold.copy(alpha = 0.6f),
                topLeft = Offset(rightDoorLeft + 16.dp.toPx(), 36.dp.toPx()),
                size = Size(halfW - 42.dp.toPx(), h - 72.dp.toPx()),
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Traditional South Indian Temple Arch Carving on Right Door
            val rightArchPath = Path().apply {
                val archL = rightDoorLeft + 16.dp.toPx()
                val archR = rightDoorLeft + halfW - 36.dp.toPx()
                val archW = archR - archL
                val archTop = h * 0.20f
                val archBottom = h * 0.80f

                moveTo(archL, archBottom)
                lineTo(archL, archTop + archW * 0.4f)
                cubicTo(archL, archTop + archW * 0.1f, archL + archW * 0.35f, archTop, archL + archW * 0.5f, archTop)
                cubicTo(archL + archW * 0.65f, archTop, archR, archTop + archW * 0.1f, archR, archTop + archW * 0.4f)
                lineTo(archR, archBottom)
                close()
            }
            drawPath(
                path = rightArchPath,
                brush = Brush.verticalGradient(listOf(DivineGold.copy(alpha = 0.85f), GoldTrim.copy(alpha = 0.6f))),
                style = Stroke(width = 2.5.dp.toPx())
            )

            // Grand Antique Gold Ring Handle / Knockers on Right Door
            val rightCenterX = rightDoorLeft + 36.dp.toPx()
            drawCircle(
                brush = Brush.radialGradient(listOf(DivineGoldShimmer, DivineGold, GoldTrim)),
                radius = 22.dp.toPx(),
                center = Offset(rightCenterX, leftCenterY),
                style = Stroke(width = 4.dp.toPx())
            )
            drawCircle(
                color = DivineGold,
                radius = 7.dp.toPx(),
                center = Offset(rightCenterX, leftCenterY)
            )

            // --- DIVINE GOLDEN GLOW WHEN DOORS MEET AT CENTER ---
            if (glowAlpha > 0.01f) {
                // Vertical radiant beam along center seam
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            DivineGold.copy(alpha = 0.85f * glowAlpha),
                            DivineGoldShimmer.copy(alpha = 1.0f * glowAlpha),
                            DivineGold.copy(alpha = 0.85f * glowAlpha),
                            Color.Transparent
                        ),
                        startX = halfW - 45.dp.toPx(),
                        endX = halfW + 45.dp.toPx()
                    ),
                    topLeft = Offset(halfW - 45.dp.toPx(), 0f),
                    size = Size(90.dp.toPx(), h)
                )

                // Large radial burst at center doorway
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            DivineGoldShimmer.copy(alpha = 0.95f * glowAlpha),
                            DivineGold.copy(alpha = 0.80f * glowAlpha),
                            GoldYellowWarm.copy(alpha = 0.50f * glowAlpha),
                            Color.Transparent
                        ),
                        center = Offset(halfW, h * 0.5f),
                        radius = w * 0.75f
                    ),
                    center = Offset(halfW, h * 0.5f),
                    radius = w * 0.75f
                )
            }
        }

        // Shimmering Golden Sparkles and Sacred Lotus at Center when Glow is active
        if (glowAlpha > 0.1f) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LotusIcon(size = 56.dp, tint = DivineGold)
                    Text(
                        "✨ 🪷 ✨",
                        fontSize = 28.sp,
                        color = DivineGold
                    )
                }
            }
        }
    }
}

/**
 * Lotus Petal Shower + Golden Sparkles Overlay when an Achievement is Completed.
 */
@Composable
fun LotusPetalShowerWithSparkles(
    title: String,
    description: String = "",
    xpAwarded: Int = 0,
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "PetalShower")
    val fallPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PetalFallPhase"
    )

    LaunchedEffect(title) {
        delay(4000)
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onDismiss)
            .background(PeacockBlueDark.copy(alpha = 0.70f)),
        contentAlignment = Alignment.Center
    ) {
        // Falling Lotus Petals and Sparkles
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 28 Falling Pink Lotus Petals
            for (i in 0 until 28) {
                val seed = (i * 37) % 100
                val startX = (seed / 100f) * w
                val speed = 0.75f + (i % 5) * 0.15f
                val currentY = ((fallPhase * speed + (i * 0.07f)) % 1f) * (h + 120f) - 60f
                val sway = kotlin.math.sin((fallPhase * 6.28f + i).toDouble()).toFloat() * 26.dp.toPx()
                val petalX = startX + sway

                val petalColor = when (i % 3) {
                    0 -> LotusPink
                    1 -> LotusPetalPink
                    else -> LotusPinkLight
                }

                val pw = 12.dp.toPx()
                val ph = 20.dp.toPx()
                val petalPath = Path().apply {
                    moveTo(petalX, currentY - ph * 0.5f)
                    cubicTo(petalX + pw, currentY - ph * 0.2f, petalX + pw * 0.8f, currentY + ph * 0.5f, petalX, currentY + ph * 0.5f)
                    cubicTo(petalX - pw * 0.8f, currentY + ph * 0.5f, petalX - pw, currentY - ph * 0.2f, petalX, currentY - ph * 0.5f)
                    close()
                }
                drawPath(petalPath, petalColor.copy(alpha = 0.88f))
            }

            // Small Golden Sparkle Stars
            for (j in 0 until 20) {
                val sx = ((j * 47) % 100 / 100f) * w
                val sy = ((j * 61) % 100 / 100f) * h
                val sparkleAlpha = (0.3f + 0.7f * kotlin.math.sin((fallPhase * 12.56f + j).toDouble()).toFloat()).coerceIn(0f, 1f)
                val sr = (3.dp + (j % 3).dp).toPx()

                val starPath = Path().apply {
                    moveTo(sx, sy - sr * 1.5f)
                    lineTo(sx + sr * 0.35f, sy - sr * 0.35f)
                    lineTo(sx + sr * 1.5f, sy)
                    lineTo(sx + sr * 0.35f, sy + sr * 0.35f)
                    lineTo(sx, sy + sr * 1.5f)
                    lineTo(sx - sr * 0.35f, sy + sr * 0.35f)
                    lineTo(sx - sr * 1.5f, sy)
                    lineTo(sx - sr * 0.35f, sy - sr * 0.35f)
                    close()
                }
                drawPath(starPath, DivineGold.copy(alpha = sparkleAlpha))
            }
        }

        // Center Divine Achievement Glass Card
        Box(
            modifier = Modifier
                .padding(28.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xF2072A40),
                            Color(0xFA041926)
                        )
                    )
                )
                .border(
                    2.dp,
                    Brush.linearGradient(
                        listOf(DivineGold, TurquoiseAccent, DivineGold, GoldYellowWarm)
                    ),
                    RoundedCornerShape(24.dp)
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("✨", fontSize = 26.sp)
                    LotusIcon(size = 40.dp, tint = LotusPink)
                    Text("✨", fontSize = 26.sp)
                }

                Text(
                    "DIVINE ACHIEVEMENT COMPLETED",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DivineGold,
                    letterSpacing = 1.sp
                )

                Text(
                    title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                if (description.isNotBlank()) {
                    Text(
                        description,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center
                    )
                }

                if (xpAwarded > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x33FBBF24))
                            .border(1.2.dp, DivineGold, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "+$xpAwarded XP Milestone",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = DivineGold
                        )
                    }
                }

                Text(
                    "Tap anywhere to dismiss",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}

/**
 * Small Lotus Celebration + Golden Glow when All Daily Goals are Completed.
 */
@Composable
fun SmallLotusCelebrationWithGlow(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "LotusCelebration")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LotusPulse"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xD9072A40),
                        Color(0xF00A3D62),
                        Color(0xD9072A40)
                    )
                )
            )
            .border(
                1.5.dp,
                Brush.horizontalGradient(
                    listOf(DivineGold, TurquoiseAccent, DivineGold)
                ),
                RoundedCornerShape(18.dp)
            )
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Blooming Lotus with Animated Concentric Golden Glow Rings
            Box(
                modifier = Modifier.size(50.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    drawCircle(
                        color = DivineGold.copy(alpha = 0.25f * glowAlpha),
                        radius = (w * 0.48f) * pulse,
                        center = Offset(w * 0.5f, h * 0.5f)
                    )
                    drawCircle(
                        color = DivineGold.copy(alpha = 0.50f * glowAlpha),
                        radius = (w * 0.38f),
                        center = Offset(w * 0.5f, h * 0.5f)
                    )
                }
                LotusIcon(size = 32.dp, tint = LotusPink)
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("🪷", fontSize = 14.sp)
                    Text(
                        "All Daily Goals Completed!",
                        color = DivineGold,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text("✨", fontSize = 14.sp)
                }
                Text(
                    "Divine Lotus Celebration • 100% Day Mastery (+100 XP)",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Traditional Palace Doorway Bottom Navigation Bar.
 * Tabs: Today | Progress | To-Do | Settings
 */
@Composable
fun TraditionalPalaceDoorwayNavigation(
    currentRoute: String,
    onNavigate: (ScreenRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    val navItems = listOf(
        ScreenRoute.DASHBOARD,
        ScreenRoute.PROGRESS,
        ScreenRoute.TODOS,
        ScreenRoute.SETTINGS
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xF2061A2D),
                        Color(0xFA041220)
                    )
                )
            )
            .border(
                1.5.dp,
                Brush.horizontalGradient(
                    listOf(
                        GoldYellowWarm.copy(alpha = 0.4f),
                        DivineGold,
                        TurquoiseAccent.copy(alpha = 0.6f),
                        DivineGold,
                        GoldYellowWarm.copy(alpha = 0.4f)
                    )
                ),
                RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            .padding(horizontal = 6.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { screen ->
                val isSelected = currentRoute == screen.route
                PalaceGateNavItem(
                    screen = screen,
                    isSelected = isSelected,
                    onClick = { onNavigate(screen) }
                )
            }
        }
    }
}

/**
 * Single Palace Gate Navigation Item.
 * Traditional South Indian palace wooden gate design with antique gold carvings.
 */
@Composable
fun PalaceGateNavItem(
    screen: ScreenRoute,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val gateAnim = remember { Animatable(if (isSelected) 0.28f else 0f) }
    val glowAnim = remember { Animatable(if (isSelected) 1f else 0f) }

    LaunchedEffect(isSelected) {
        if (isSelected) {
            gateAnim.animateTo(0f, tween(60, easing = LinearEasing))
            glowAnim.snapTo(0.2f)
            glowAnim.animateTo(1f, tween(120, easing = FastOutSlowInEasing))
            gateAnim.animateTo(0.28f, tween(150, easing = FastOutSlowInEasing))
        } else {
            glowAnim.animateTo(0f, tween(120))
            gateAnim.animateTo(0f, tween(180, easing = FastOutSlowInEasing))
        }
    }

    val openFraction = gateAnim.value
    val glowIntensity = glowAnim.value

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .width(56.dp)
                .height(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Traditional South Indian Temple / Palace Arch Path
                val archPath = Path().apply {
                    moveTo(w * 0.12f, h)
                    lineTo(w * 0.12f, h * 0.40f)
                    cubicTo(w * 0.12f, h * 0.10f, w * 0.38f, 0f, w * 0.5f, 0f)
                    cubicTo(w * 0.62f, 0f, w * 0.88f, h * 0.10f, w * 0.88f, h * 0.40f)
                    lineTo(w * 0.88f, h)
                    close()
                }

                // 1. Subtle Golden Glow behind Arch when selected/opening
                if (glowIntensity > 0.05f) {
                    drawPath(
                        path = archPath,
                        brush = Brush.verticalGradient(
                            listOf(
                                DivineGold.copy(alpha = 0.65f * glowIntensity),
                                GoldYellowWarm.copy(alpha = 0.4f * glowIntensity),
                                Color(0xFF2C1810).copy(alpha = 0.9f)
                            )
                        )
                    )
                }

                // 2. Open Sanctum Radiant Light when slightly open
                if (openFraction > 0.02f) {
                    drawPath(
                        path = archPath,
                        brush = Brush.radialGradient(
                            colors = listOf(
                                DivineGold.copy(alpha = 0.75f * openFraction),
                                GoldYellowWarm.copy(alpha = 0.45f * openFraction),
                                Color.Transparent
                            ),
                            center = Offset(w * 0.5f, h * 0.5f),
                            radius = w * 0.55f
                        )
                    )
                }

                // 3. Antique Gold Arch Trim & Pillars
                val frameColor = if (isSelected) DivineGold else Color(0xFFD4AF37).copy(alpha = 0.55f)
                drawPath(
                    path = archPath,
                    color = frameColor,
                    style = Stroke(width = if (isSelected) 2.2.dp.toPx() else 1.2.dp.toPx())
                )

                // 4. Handcrafted Brownish Dark Wood Doors with Carvings & Antique-Gold Highlights
                val closedDoorWidth = (w * 0.38f)
                val currentDoorWidth = closedDoorWidth * (1f - openFraction).coerceIn(0.04f, 1f)

                if (openFraction < 0.95f) {
                    // Left Wooden Door
                    val leftDoorX = w * 0.12f
                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF2C1810),
                                Color(0xFF3E2723),
                                Color(0xFF4E342E)
                            )
                        ),
                        topLeft = Offset(leftDoorX, h * 0.28f),
                        size = Size(currentDoorWidth, h * 0.72f),
                        cornerRadius = CornerRadius(3f, 3f),
                        alpha = (1f - openFraction).coerceIn(0.2f, 1f)
                    )
                    // Antique-gold carved panel border on left door
                    drawRoundRect(
                        color = DivineGold.copy(alpha = 0.75f),
                        topLeft = Offset(leftDoorX + 2.dp.toPx(), h * 0.32f),
                        size = Size(currentDoorWidth - 4.dp.toPx(), h * 0.64f),
                        cornerRadius = CornerRadius(2f, 2f),
                        style = Stroke(width = 1.dp.toPx()),
                        alpha = (1f - openFraction).coerceIn(0.2f, 1f)
                    )
                    // Lotus motif / floral carving on left door
                    drawCircle(
                        color = DivineGold.copy(alpha = 0.7f),
                        radius = 2.dp.toPx(),
                        center = Offset(leftDoorX + currentDoorWidth * 0.5f, h * 0.5f),
                        alpha = (1f - openFraction).coerceIn(0.2f, 1f)
                    )
                    // Brass knob / ring handle
                    drawCircle(
                        color = DivineGold,
                        radius = 2.dp.toPx(),
                        center = Offset(leftDoorX + currentDoorWidth * 0.75f, h * 0.62f),
                        alpha = (1f - openFraction).coerceIn(0.2f, 1f)
                    )

                    // Right Wooden Door
                    val rightDoorX = (w * 0.88f) - currentDoorWidth
                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF4E342E),
                                Color(0xFF3E2723),
                                Color(0xFF2C1810)
                            )
                        ),
                        topLeft = Offset(rightDoorX, h * 0.28f),
                        size = Size(currentDoorWidth, h * 0.72f),
                        cornerRadius = CornerRadius(3f, 3f),
                        alpha = (1f - openFraction).coerceIn(0.2f, 1f)
                    )
                    // Antique-gold carved panel border on right door
                    drawRoundRect(
                        color = DivineGold.copy(alpha = 0.75f),
                        topLeft = Offset(rightDoorX + 2.dp.toPx(), h * 0.32f),
                        size = Size(currentDoorWidth - 4.dp.toPx(), h * 0.64f),
                        cornerRadius = CornerRadius(2f, 2f),
                        style = Stroke(width = 1.dp.toPx()),
                        alpha = (1f - openFraction).coerceIn(0.2f, 1f)
                    )
                    // Lotus motif / floral carving on right door
                    drawCircle(
                        color = DivineGold.copy(alpha = 0.7f),
                        radius = 2.dp.toPx(),
                        center = Offset(rightDoorX + currentDoorWidth * 0.5f, h * 0.5f),
                        alpha = (1f - openFraction).coerceIn(0.2f, 1f)
                    )
                    // Brass knob / ring handle
                    drawCircle(
                        color = DivineGold,
                        radius = 2.dp.toPx(),
                        center = Offset(rightDoorX + currentDoorWidth * 0.25f, h * 0.62f),
                        alpha = (1f - openFraction).coerceIn(0.2f, 1f)
                    )
                }

                // 5. Crown Kalash Finial at Top
                drawCircle(
                    color = if (isSelected) DivineGold else GoldYellowWarm.copy(alpha = 0.7f),
                    radius = 3.dp.toPx(),
                    center = Offset(w * 0.5f, 1.5.dp.toPx())
                )
            }

            Icon(
                imageVector = screen.icon,
                contentDescription = screen.title,
                tint = if (isSelected) DivineGold else Color.White.copy(alpha = 0.65f),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = screen.title,
            color = if (isSelected) DivineGold else Color.White.copy(alpha = 0.75f),
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal
        )
    }
}

/**
 * 100% Completion Celebration Golden Glitter Overlay.
 */
@Composable
fun FullGoldenGlitterOverlay(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "GlitterCelebration")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "GlitterPhase"
    )

    Canvas(modifier = modifier.fillMaxWidth().height(140.dp)) {
        val w = size.width
        val h = size.height

        val glitterSpots = listOf(
            Offset(w * 0.15f, h * ((0.9f - phase) % 1f)),
            Offset(w * 0.32f, h * ((0.75f - phase * 0.8f) % 1f)),
            Offset(w * 0.50f, h * ((1.0f - phase * 1.1f) % 1f)),
            Offset(w * 0.68f, h * ((0.85f - phase * 0.9f) % 1f)),
            Offset(w * 0.85f, h * ((0.95f - phase) % 1f)),
            Offset(w * 0.25f, h * ((0.6f - phase * 0.7f) % 1f)),
            Offset(w * 0.78f, h * ((0.65f - phase * 0.75f) % 1f))
        )

        glitterSpots.forEachIndexed { i, pos ->
            val color = when (i % 3) {
                0 -> DivineGold
                1 -> GoldYellowWarm
                else -> TurquoiseAccent
            }
            val alpha = (0.4f + 0.6f * kotlin.math.sin((phase * 2 * Math.PI + i).toFloat())).coerceIn(0.2f, 1f)
            drawCircle(
                color = color.copy(alpha = alpha),
                radius = (3.dp + (i % 3).dp).toPx(),
                center = pos
            )
        }
    }
}

@Composable
fun CelebrationParticleOverlay(
    modifier: Modifier = Modifier
) {
    FullGoldenGlitterOverlay(modifier = modifier)
}

/**
 * Sacred Lotus Vector Icon.
 */
@Composable
fun LotusIcon(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = LotusPink
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        val centerPetal = Path().apply {
            moveTo(w * 0.5f, h * 0.15f)
            cubicTo(w * 0.65f, h * 0.45f, w * 0.6f, h * 0.85f, w * 0.5f, h * 0.88f)
            cubicTo(w * 0.4f, h * 0.85f, w * 0.35f, h * 0.45f, w * 0.5f, h * 0.15f)
            close()
        }
        drawPath(centerPetal, tint)

        val leftPetal = Path().apply {
            moveTo(w * 0.45f, h * 0.28f)
            cubicTo(w * 0.15f, h * 0.50f, w * 0.25f, h * 0.80f, w * 0.48f, h * 0.88f)
            cubicTo(w * 0.35f, h * 0.70f, w * 0.35f, h * 0.45f, w * 0.45f, h * 0.28f)
            close()
        }
        drawPath(leftPetal, tint.copy(alpha = 0.82f))

        val rightPetal = Path().apply {
            moveTo(w * 0.55f, h * 0.28f)
            cubicTo(w * 0.85f, h * 0.50f, w * 0.75f, h * 0.80f, w * 0.52f, h * 0.88f)
            cubicTo(w * 0.65f, h * 0.70f, w * 0.65f, h * 0.45f, w * 0.55f, h * 0.28f)
            close()
        }
        drawPath(rightPetal, tint.copy(alpha = 0.82f))

        val calyx = Path().apply {
            moveTo(w * 0.2f, h * 0.86f)
            quadraticTo(w * 0.5f, h * 0.98f, w * 0.8f, h * 0.86f)
            quadraticTo(w * 0.5f, h * 0.90f, w * 0.2f, h * 0.86f)
            close()
        }
        drawPath(calyx, EmeraldGreen)
    }
}

/**
 * Sacred Peacock Eye Vector Icon.
 */
@Composable
fun PeacockEyeIcon(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        drawOval(
            color = TurquoiseAccent,
            topLeft = Offset(w * 0.1f, h * 0.1f),
            size = Size(w * 0.8f, h * 0.8f)
        )
        drawOval(
            color = EmeraldGreen,
            topLeft = Offset(w * 0.22f, h * 0.22f),
            size = Size(w * 0.56f, h * 0.56f)
        )
        drawOval(
            color = PeacockBlueDark,
            topLeft = Offset(w * 0.35f, h * 0.35f),
            size = Size(w * 0.30f, h * 0.30f)
        )
        drawCircle(
            color = DivineGold,
            radius = w * 0.07f,
            center = Offset(w * 0.5f, h * 0.5f)
        )
    }
}

/**
 * Sacred Peacock Feather Graphic.
 */
@Composable
fun PeacockFeatherGraphic(
    modifier: Modifier = Modifier,
    size: Dp = 36.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        val stemPath = Path().apply {
            moveTo(w * 0.3f, h)
            cubicTo(w * 0.38f, h * 0.68f, w * 0.46f, h * 0.38f, w * 0.5f, h * 0.12f)
        }
        drawPath(stemPath, WarmNeutralCream, style = Stroke(width = 1.6.dp.toPx()))

        for (i in 0 until 8) {
            val progress = i / 8f
            val y = h * (0.22f + progress * 0.58f)
            val barbColor = if (i % 2 == 0) EmeraldGreen else TurquoiseAccent
            drawLine(
                color = barbColor.copy(alpha = 0.75f),
                start = Offset(w * (0.36f + progress * 0.08f), y),
                end = Offset(w * 0.12f, y - 5.dp.toPx()),
                strokeWidth = 1.2.dp.toPx()
            )
            drawLine(
                color = barbColor.copy(alpha = 0.75f),
                start = Offset(w * (0.36f + progress * 0.08f), y),
                end = Offset(w * 0.86f, y - 4.dp.toPx()),
                strokeWidth = 1.2.dp.toPx()
            )
        }

        // Peacock Eye
        val eyeCenter = Offset(w * 0.5f, h * 0.22f)
        val eyeR = w * 0.22f

        drawOval(
            color = TurquoiseAccent,
            topLeft = Offset(eyeCenter.x - eyeR, eyeCenter.y - eyeR * 1.2f),
            size = Size(eyeR * 2f, eyeR * 2.4f)
        )
        drawOval(
            color = EmeraldGreen,
            topLeft = Offset(eyeCenter.x - eyeR * 0.72f, eyeCenter.y - eyeR * 0.88f),
            size = Size(eyeR * 1.44f, eyeR * 1.76f)
        )
        drawOval(
            color = PeacockBlueDark,
            topLeft = Offset(eyeCenter.x - eyeR * 0.46f, eyeCenter.y - eyeR * 0.56f),
            size = Size(eyeR * 0.92f, eyeR * 1.12f)
        )
        drawCircle(
            color = DivineGold,
            radius = eyeR * 0.26f,
            center = eyeCenter
        )
    }
}

/**
 * Floating +XP Notification Badge.
 */
@Composable
fun FloatingXpBadge(
    xp: Int,
    label: String = "",
    onDismiss: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(xp) {
        visible = true
        delay(2200)
        visible = false
        delay(300)
        onDismiss()
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(250)) + slideInVertically(initialOffsetY = { -it / 2 }),
        exit = fadeOut(tween(350)) + slideOutVertically(targetOffsetY = { -it })
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .shadow(16.dp, RoundedCornerShape(24.dp), ambientColor = DivineGold, spotColor = DivineGold)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            PeacockBlueNight,
                            PeacockBlueDark,
                            RoyalBlueDeep
                        )
                    )
                )
                .border(
                    2.dp,
                    Brush.horizontalGradient(
                        listOf(DivineGold, GoldYellowWarm, DivineGold)
                    ),
                    RoundedCornerShape(24.dp)
                )
                .padding(horizontal = 20.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("✨", fontSize = 18.sp)
                Text(
                    text = "+$xp XP",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DivineGold
                )
                if (label.isNotBlank()) {
                    Text(
                        text = "• $label",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun LevelBadge(
    level: Int,
    title: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xD9061A2D),
                        Color(0xD90B1A30)
                    )
                )
            )
            .border(1.5.dp, DivineGold, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text("⭐", fontSize = 13.sp)
        Text(
            text = "Lvl $level",
            color = DivineGold,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "•",
            color = DivineGold.copy(alpha = 0.6f),
            fontSize = 11.sp
        )
        Text(
            text = title,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun StreakBadge(
    streakDays: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xD90B1A30),
                        Color(0xD9061A2D)
                    )
                )
            )
            .border(1.5.dp, GoldYellowWarm, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text("🔥", fontSize = 13.sp)
        Text(
            text = "$streakDays d",
            color = GoldYellowWarm,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun XPBadge(
    totalXP: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xD9061A2D),
                        Color(0xD90A243D)
                    )
                )
            )
            .border(1.5.dp, DivineGold, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text("✨", fontSize = 13.sp)
        Text(
            text = "$totalXP XP",
            color = DivineGold,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun KrishnaFluteHeroBanner(
    modifier: Modifier = Modifier,
    onDismissOrCollapse: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(
                1.5.dp,
                Brush.horizontalGradient(
                    listOf(
                        TurquoiseAccent.copy(alpha = 0.6f),
                        DivineGold,
                        EmeraldGreen.copy(alpha = 0.6f)
                    )
                ),
                RoundedCornerShape(20.dp)
            )
    ) {
        Image(
            painter = painterResource(id = R.drawable.krishna_flute_hero),
            contentDescription = "Lord Krishna playing flute with peacock feather and lotus",
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            PeacockBlueDark.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PeacockEyeIcon(size = 18.dp)
                    Text(
                        text = "Career Quest • Divine Focus",
                        color = DivineGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Perform your duty with dedication and focus.",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
