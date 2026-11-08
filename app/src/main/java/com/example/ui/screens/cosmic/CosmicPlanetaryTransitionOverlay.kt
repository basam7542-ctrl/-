package com.example.ui.screens.cosmic

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.CairoFontFamily

/**
 * BESHEER COSMIC FUTURE | Universal Cinematic Planetary Zoom Transition.
 *
 * Simulates traveling across deep space and entering any planetary section:
 * 1. Target planet sphere zooms towards the camera (scale 1x -> 5.5x).
 * 2. Visual focus shifts into the section's glowing orbital corona.
 * 3. Atmospheric halo expands and dissolves into the destination screen.
 * 4. Reverse transition gracefully zooms camera back out to the Solar System.
 */
@Composable
fun CosmicPlanetaryTransitionOverlay(
    isZoomingIn: Boolean,
    targetPlanet: CosmicPlanetDef? = null,
    onTransitionFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(if (isZoomingIn) 1f else 5.5f) }
    val rotation = remember { Animatable(if (isZoomingIn) 0f else 18f) }
    val overlayAlpha = remember { Animatable(if (isZoomingIn) 0f else 1f) }

    val accentColor = targetPlanet?.accentColor ?: Color(0xFFFFD54F)
    val isProducts = targetPlanet?.isProductsPlanet == true

    LaunchedEffect(isZoomingIn, targetPlanet) {
        if (isZoomingIn) {
            // Zoom in towards destination section
            overlayAlpha.animateTo(1f, animationSpec = tween(280, easing = FastOutSlowInEasing))
            scale.animateTo(5.5f, animationSpec = tween(550, easing = FastOutSlowInEasing))
            rotation.animateTo(22f, animationSpec = tween(550, easing = FastOutSlowInEasing))
            onTransitionFinished()
        } else {
            // Reverse Zoom out back to Solar System
            scale.animateTo(1f, animationSpec = tween(500, easing = FastOutSlowInEasing))
            rotation.animateTo(0f, animationSpec = tween(500, easing = FastOutSlowInEasing))
            overlayAlpha.animateTo(0f, animationSpec = tween(250, easing = FastOutSlowInEasing))
            onTransitionFinished()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = overlayAlpha.value * 0.90f)),
        contentAlignment = Alignment.Center
    ) {
        // Atmospheric Expansion Glow Ring
        Box(
            modifier = Modifier
                .size(280.dp)
                .scale(scale.value * 1.15f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            accentColor.copy(alpha = 0.85f),
                            Color(0x4438BDF8),
                            Color.Transparent
                        )
                    )
                )
        )

        if (isProducts) {
            // 3D Products Planet Sphere with Rings
            Image(
                painter = painterResource(id = R.drawable.cosmic_planet_product_zoom),
                contentDescription = "الاقتراب من كوكب المنتجات",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(220.dp)
                    .scale(scale.value)
                    .rotate(rotation.value)
                    .alpha(if (scale.value > 4.5f) (5.5f - scale.value).coerceIn(0f, 1f) else 1f)
            )
        } else {
            // Spherical Planetary Body for any of the other sections
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .scale(scale.value)
                    .rotate(rotation.value)
                    .alpha(if (scale.value > 4.5f) (5.5f - scale.value).coerceIn(0f, 1f) else 1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    accentColor,
                                    accentColor.copy(alpha = 0.85f),
                                    Color(0xFF0F234B),
                                    Color(0xFF020713)
                                ),
                                center = Offset(45f, 45f)
                            )
                        )
                        .border(3.dp, accentColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (targetPlanet != null) {
                        Icon(
                            imageVector = targetPlanet.icon,
                            contentDescription = targetPlanet.name,
                            tint = Color.White,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }

                if (targetPlanet != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xE6081428),
                        border = androidx.compose.foundation.BorderStroke(1.2.dp, accentColor)
                    ) {
                        Text(
                            text = targetPlanet.name,
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
