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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R

/**
 * BESHEER COSMIC FUTURE | Cinematic Planetary Zoom Transition.
 *
 * Simulates traveling across deep space and entering the atmosphere of the Products Planet:
 * 1. Planet sphere zooms towards the camera (scale 1x -> 6x).
 * 2. Visual focus shifts into the golden orbital ring and clouds.
 * 3. Atmospheric halo expands and dissolves into the Products City.
 */
@Composable
fun CosmicPlanetaryTransitionOverlay(
    isZoomingIn: Boolean,
    onTransitionFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(if (isZoomingIn) 1f else 5.5f) }
    val rotation = remember { Animatable(if (isZoomingIn) 0f else 18f) }
    val overlayAlpha = remember { Animatable(if (isZoomingIn) 0f else 1f) }

    LaunchedEffect(isZoomingIn) {
        if (isZoomingIn) {
            // Zoom in towards Products Planet
            overlayAlpha.animateTo(1f, animationSpec = tween(300, easing = FastOutSlowInEasing))
            scale.animateTo(5.5f, animationSpec = tween(650, easing = FastOutSlowInEasing))
            rotation.animateTo(22f, animationSpec = tween(650, easing = FastOutSlowInEasing))
            onTransitionFinished()
        } else {
            // Reverse Zoom out back to Solar System
            scale.animateTo(1f, animationSpec = tween(600, easing = FastOutSlowInEasing))
            rotation.animateTo(0f, animationSpec = tween(600, easing = FastOutSlowInEasing))
            overlayAlpha.animateTo(0f, animationSpec = tween(300, easing = FastOutSlowInEasing))
            onTransitionFinished()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = overlayAlpha.value * 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        // Atmospheric Expansion Glow Ring
        Box(
            modifier = Modifier
                .size(260.dp)
                .scale(scale.value * 1.15f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color(0x99FFD54F),
                            Color(0x5538BDF8),
                            Color.Transparent
                        )
                    )
                )
        )

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
    }
}
