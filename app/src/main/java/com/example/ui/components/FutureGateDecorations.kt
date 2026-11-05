package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GoldBorderGlow
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.HologramCyan
import com.example.ui.theme.SciFiBorderGradient
import com.example.ui.theme.SciFiHeroGradient
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

/**
 * BESHEER – FUTURE GATE | Multi-Layered Sci-Fi Command Deck Background.
 *
 * Combines:
 * 1. Deep midnight royal space navy gradient base.
 * 2. Soft cinematic radial light auras:
 *    - Warm metallic gold glow at top-right.
 *    - Holographic electric cyan glow at center-left.
 *    - Subtle futuristic violet aura at bottom-right.
 * 3. Delicate technical grid lines with ultra-low opacity (0.03f) for genuine holographic depth.
 */
@Composable
fun FutureGateAuraBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF02050F),
                        Color(0xFF050D1E),
                        Color(0xFF07142A),
                        Color(0xFF030815)
                    )
                )
            )
    ) {
        // High-performance Canvas rendering of ambient holographic lighting and fine grid
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Top-Right Soft Golden Energy Aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x28F4CD68),
                        Color(0x10F4CD68),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.85f, height * 0.12f),
                    radius = width * 0.65f
                ),
                radius = width * 0.65f,
                center = Offset(width * 0.85f, height * 0.12f)
            )

            // 2. Center-Left Electric Cyan Holographic Aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x2200E5FF),
                        Color(0x0C2997FF),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.15f, height * 0.45f),
                    radius = width * 0.6f
                ),
                radius = width * 0.6f,
                center = Offset(width * 0.15f, height * 0.45f)
            )

            // 3. Bottom Subtle Violet Energy Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x1CBD5BFF),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.7f, height * 0.88f),
                    radius = width * 0.5f
                ),
                radius = width * 0.5f,
                center = Offset(width * 0.7f, height * 0.88f)
            )

            // 4. Fine Technical Sci-Fi Grid Lines (Subtle & Non-distracting, alpha = 0.025f)
            val gridStep = 48.dp.toPx()
            val gridColor = Color(0x08FFFFFF)
            var x = 0f
            while (x < width) {
                drawLine(
                    color = gridColor,
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = 1f
                )
                x += gridStep
            }
            var y = 0f
            while (y < height) {
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f
                )
                y += gridStep
            }
        }

        // Child Content Container
        content()
    }
}

/**
 * BESHEER – FUTURE GATE | Sci-Fi Glassmorphic Card Container.
 */
@Composable
fun FutureGateGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    borderBrush: Brush = SciFiBorderGradient,
    borderWidth: Dp = 1.2.dp,
    backgroundBrush: Brush = Brush.verticalGradient(
        listOf(
            Color(0xE6122244),
            Color(0xF0081329)
        )
    ),
    contentPadding: PaddingValues = PaddingValues(14.dp),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundBrush)
            .border(borderWidth, borderBrush, shape)
            .padding(contentPadding),
        content = content
    )
}

/**
 * BESHEER – FUTURE GATE | Central Pulsing Holographic Intelligence Orb.
 *
 * Dedicated voice assistant core button in the bottom navigation dock:
 * - Animated breathing pulse (scale 1.0 -> 1.08)
 * - Concentric metallic gold and electric cyan orbital rings
 * - Holographic energy aura
 * - Crisp centered mic icon and Cairo label
 */
@Composable
fun BasheerPulsingEnergyOrb(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "basheer_core_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura_alpha"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp)
            .testTag("nav_tab_basheer"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .scale(pulseScale),
            contentAlignment = Alignment.Center
        ) {
            // Outer Holographic Aura Ring
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color(0x66FFD54F),
                                Color(0x4000E5FF),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Inner Orb Capsule with Concentric Gold & Cyan Border Rings
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color(0xFF16294D),
                                Color(0xFF071124),
                                Color(0xFF030713)
                            )
                        )
                    )
                    .border(2.dp, Color(0xFFFFD54F), CircleShape)
                    .padding(2.5.dp)
                    .border(1.dp, Color(0xAA00E5FF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "المساعد الصوتي بشير",
                    modifier = Modifier.size(24.dp),
                    tint = Color(0xFFFFD54F)
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "بشير",
            fontFamily = CairoFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 11.5.sp,
            color = Color(0xFFFFD54F),
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}
