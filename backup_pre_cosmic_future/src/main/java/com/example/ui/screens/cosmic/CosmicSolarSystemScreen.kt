package com.example.ui.screens.cosmic

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.MainTab
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.HologramCyan
import kotlin.math.cos
import kotlin.math.sin

/**
 * BESHEER COSMIC FUTURE | Phase 1 Main Screen: The Solar System (النظام الشمسي).
 *
 * Requirements fulfilled:
 * 1. Center glowing Sun (نواة شمسية مضيئة) carrying Al-Bashiri royal branding with solar flares.
 * 2. Elliptical orbital rings with 10+ 3D spherical interactive planets representing app sections.
 * 3. Dedicated Products Planet (كوكب المنتجات) with golden orbital rings, triggering cinematic zoom.
 * 4. Earth horizon at the bottom with city lights and atmosphere.
 * 5. Integrated Voice Assistant «بشير» glowing core with audio visualizer waveforms.
 * 6. Strict Safe Area respect (No overlap with Android status bar or navigation bar).
 */
@Composable
fun CosmicSolarSystemScreen(
    onEnterProductsPlanet: () -> Unit,
    onNavigateToTab: (MainTab) -> Unit,
    onBasheerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Infinite transitions for calm organic breathing and orbital pulsation
    val infiniteTransition = rememberInfiniteTransition(label = "cosmic_solar_system")

    val sunPulse by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sun_pulse"
    )

    val sunRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(45000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sun_rotation"
    )

    val waveAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF02040C),
                        Color(0xFF050B1B),
                        Color(0xFF071228),
                        Color(0xFF040816),
                        Color(0xFF010309)
                    )
                )
            )
    ) {
        // Starfield & Cosmic Dust Canvas (Subtle ambient stars)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val starPositions = listOf(
                Offset(size.width * 0.12f, size.height * 0.15f),
                Offset(size.width * 0.88f, size.height * 0.18f),
                Offset(size.width * 0.22f, size.height * 0.32f),
                Offset(size.width * 0.78f, size.height * 0.38f),
                Offset(size.width * 0.08f, size.height * 0.52f),
                Offset(size.width * 0.92f, size.height * 0.55f),
                Offset(size.width * 0.35f, size.height * 0.68f),
                Offset(size.width * 0.65f, size.height * 0.72f),
                Offset(size.width * 0.15f, size.height * 0.82f),
                Offset(size.width * 0.85f, size.height * 0.84f)
            )
            starPositions.forEachIndexed { idx, pos ->
                val starRadius = if (idx % 3 == 0) 1.8f else 1.1f
                drawCircle(
                    color = if (idx % 2 == 0) Color(0xDDF4CD68) else Color(0xCC00E5FF),
                    radius = starRadius,
                    center = pos
                )
            }
        }

        // Main Responsive Layout Container
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val screenWidth = maxWidth
            val screenHeight = maxHeight
            val centerX = screenWidth / 2
            val centerY = screenHeight * 0.44f

            // 1. Orbital Ring Paths Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height * 0.44f

                // Inner Orbit (Golden Luminous Ellipse)
                drawOval(
                    color = Color(0x35F4CD68),
                    topLeft = Offset(cx - size.width * 0.36f, cy - size.height * 0.18f),
                    size = Size(size.width * 0.72f, size.height * 0.36f),
                    style = Stroke(
                        width = 1.4f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                    )
                )

                // Middle Orbit (Cyan Electric Ellipse)
                drawOval(
                    color = Color(0x2800E5FF),
                    topLeft = Offset(cx - size.width * 0.44f, cy - size.height * 0.24f),
                    size = Size(size.width * 0.88f, size.height * 0.48f),
                    style = Stroke(
                        width = 1.2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 10f), 0f)
                    )
                )

                // Outer Orbit (Deep Space Orbit)
                drawOval(
                    color = Color(0x18BD5BFF),
                    topLeft = Offset(cx - size.width * 0.48f, cy - size.height * 0.29f),
                    size = Size(size.width * 0.96f, size.height * 0.58f),
                    style = Stroke(width = 1.0f)
                )
            }

            // 2. Central Glowing Sun Core (نواة شمسية مضيئة - البشيري)
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .offset(x = centerX - 65.dp, y = centerY - 65.dp)
                    .scale(sunPulse),
                contentAlignment = Alignment.Center
            ) {
                // Solar Flare Corona Rings
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .rotate(sunRotation)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    Color(0x99FFC837),
                                    Color(0x55F4CD68),
                                    Color(0x22FF8C00),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Sun Core Image with Arabic "البشيري" Calligraphy
                Image(
                    painter = painterResource(id = R.drawable.cosmic_sun_core),
                    contentDescription = "شمس البشيري المركزية",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(92.dp)
                        .clip(CircleShape)
                )
            }

            // 3. Interactive Orbiting Planets System (الكواكب والأقسام)

            // Planet 1: [المنتجات] (The Star Planet - Feature Focus of Phase 1)
            // Positioned at top-right orbit (x: +30%, y: -16%)
            CosmicPlanetItem(
                name = "المنتجات",
                icon = Icons.Default.ViewInAr,
                accentColor = Color(0xFFFFD54F),
                isFeaturedWithRings = true,
                xOffset = centerX + screenWidth * 0.26f,
                yOffset = centerY - screenHeight * 0.17f,
                onClick = onEnterProductsPlanet,
                testTag = "cosmic_planet_products"
            )

            // Planet 2: [لوحة التحكم] (Dashboard / Speedometer)
            // Positioned at top center (x: -5%, y: -23%)
            CosmicPlanetItem(
                name = "لوحة التحكم",
                icon = Icons.Default.Speed,
                accentColor = Color(0xFFF4CD68),
                isFeaturedWithRings = false,
                xOffset = centerX - screenWidth * 0.08f,
                yOffset = centerY - screenHeight * 0.24f,
                onClick = { onNavigateToTab(MainTab.CATALOG) },
                testTag = "cosmic_planet_dashboard"
            )

            // Planet 3: [المخزون] (Warehouse / Inventory)
            // Positioned at mid-right (x: +36%, y: +1%)
            CosmicPlanetItem(
                name = "المخزون",
                icon = Icons.Default.Inventory2,
                accentColor = Color(0xFF38BDF8),
                isFeaturedWithRings = false,
                xOffset = centerX + screenWidth * 0.33f,
                yOffset = centerY - screenHeight * 0.01f,
                onClick = { onNavigateToTab(MainTab.INVENTORY) },
                testTag = "cosmic_planet_inventory"
            )

            // Planet 4: [المبيعات والفواتير] (Invoices & Sales Log)
            // Positioned at lower-right (x: +22%, y: +16%)
            CosmicPlanetItem(
                name = "المبيعات والفواتير",
                icon = Icons.Default.ReceiptLong,
                accentColor = Color(0xFFFFC837),
                isFeaturedWithRings = false,
                xOffset = centerX + screenWidth * 0.21f,
                yOffset = centerY + screenHeight * 0.15f,
                onClick = { onNavigateToTab(MainTab.SALES_LOG) },
                testTag = "cosmic_planet_sales"
            )

            // Planet 5: [العملاء] (Customers)
            // Positioned at bottom-right (x: +8%, y: +22%)
            CosmicPlanetItem(
                name = "العملاء",
                icon = Icons.Default.People,
                accentColor = Color(0xFF00E676),
                isFeaturedWithRings = false,
                xOffset = centerX + screenWidth * 0.04f,
                yOffset = centerY + screenHeight * 0.21f,
                onClick = { onNavigateToTab(MainTab.ACCOUNTS) },
                testTag = "cosmic_planet_customers"
            )

            // Planet 6: [الموردون] (Suppliers)
            // Positioned at bottom-center-left (x: -18%, y: +22%)
            CosmicPlanetItem(
                name = "الموردون",
                icon = Icons.Default.LocalShipping,
                accentColor = Color(0xFF38BDF8),
                isFeaturedWithRings = false,
                xOffset = centerX - screenWidth * 0.18f,
                yOffset = centerY + screenHeight * 0.21f,
                onClick = { onNavigateToTab(MainTab.ACCOUNTS) },
                testTag = "cosmic_planet_suppliers"
            )

            // Planet 7: [تجار الجملة] (Wholesalers)
            // Positioned at lower-left (x: -33%, y: +15%)
            CosmicPlanetItem(
                name = "تجار الجملة",
                icon = Icons.Default.Store,
                accentColor = Color(0xFFBD5BFF),
                isFeaturedWithRings = false,
                xOffset = centerX - screenWidth * 0.33f,
                yOffset = centerY + screenHeight * 0.15f,
                onClick = { onNavigateToTab(MainTab.ACCOUNTS) },
                testTag = "cosmic_planet_merchants"
            )

            // Planet 8: [المدفوعات والتحصيلات] (Payments & Collections)
            // Positioned at mid-left (x: -38%, y: +0%)
            CosmicPlanetItem(
                name = "المدفوعات والتحصيل",
                icon = Icons.Default.Payments,
                accentColor = Color(0xFF2997FF),
                isFeaturedWithRings = false,
                xOffset = centerX - screenWidth * 0.38f,
                yOffset = centerY + screenHeight * 0.01f,
                onClick = { onNavigateToTab(MainTab.ACCOUNTS) },
                testTag = "cosmic_planet_payments"
            )

            // Planet 9: [المصروفات والخزينة] (Expenses & Vault)
            // Positioned at upper-mid-left (x: -36%, y: -14%)
            CosmicPlanetItem(
                name = "المصروفات والخزينة",
                icon = Icons.Default.AccountBalance,
                accentColor = Color(0xFFFFB300),
                isFeaturedWithRings = false,
                xOffset = centerX - screenWidth * 0.36f,
                yOffset = centerY - screenHeight * 0.14f,
                onClick = { onNavigateToTab(MainTab.ACCOUNTS) },
                testTag = "cosmic_planet_expenses"
            )

            // Planet 10: [التقارير] (Reports & Statistics)
            // Positioned at upper-left (x: -24%, y: -22%)
            CosmicPlanetItem(
                name = "التقارير",
                icon = Icons.Default.Assessment,
                accentColor = Color(0xFF00E5FF),
                isFeaturedWithRings = false,
                xOffset = centerX - screenWidth * 0.24f,
                yOffset = centerY - screenHeight * 0.22f,
                onClick = { onNavigateToTab(MainTab.ACCOUNTS) },
                testTag = "cosmic_planet_reports"
            )

            // Planet 11: [الإعدادات] (Settings & Store Profile)
            // Positioned at top-left-center (x: -12%, y: -16%)
            CosmicPlanetItem(
                name = "الإعدادات",
                icon = Icons.Default.Settings,
                accentColor = Color(0xFFF4CD68),
                isFeaturedWithRings = false,
                xOffset = centerX - screenWidth * 0.12f,
                yOffset = centerY - screenHeight * 0.16f,
                onClick = { onNavigateToTab(MainTab.SETTINGS) },
                testTag = "cosmic_planet_settings"
            )

            // 4. Earth Horizon & Atmosphere at Bottom (أفق كوكب الأرض)
            Image(
                painter = painterResource(id = R.drawable.cosmic_earth_horizon),
                contentDescription = "أفق كوكب الأرض",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .height(150.dp)
            )

            // 5. Central Glowing Voice Assistant «بشير» (نواة ذكية بتموجات صوتية)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Waveform Audio Visualizer line around Basheer button
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val waveHeights = listOf(4, 8, 12, 16, 22, 16, 12, 8, 4)
                    // Left Sound Bars
                    waveHeights.forEach { h ->
                        Box(
                            modifier = Modifier
                                .width(2.5.dp)
                                .height((h * waveAlpha).dp)
                                .background(Color(0xFFFFD54F).copy(alpha = waveAlpha), RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                    }

                    Spacer(modifier = Modifier.width(68.dp)) // Space for central Basheer orb

                    // Right Sound Bars
                    waveHeights.reversed().forEach { h ->
                        Box(
                            modifier = Modifier
                                .width(2.5.dp)
                                .height((h * waveAlpha).dp)
                                .background(Color(0xFFFFD54F).copy(alpha = waveAlpha), RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // The Central Glowing Golden Orb of Bashir
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    Color(0xFFFFD54F),
                                    Color(0xFFE5B63E),
                                    Color(0xFF0C1830)
                                )
                            )
                        )
                        .border(2.5.dp, Color(0xFFFFD54F), CircleShape)
                        .clickable(onClick = onBasheerClick)
                        .testTag("cosmic_basheer_mic_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "المساعد الصوتي بشير",
                        tint = Color(0xFF040916),
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "بشير",
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp,
                    color = Color(0xFFFFD54F),
                    textAlign = TextAlign.Center
                )
            }
        }

        // 6. Top Header with Al-Bashiri Crescent Logo (Strictly respects status bar safe area)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 8.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.albashiri_crescent_header),
                contentDescription = "شعار البشيري للأجهزة الكهربائية",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth(0.68f)
                    .height(68.dp)
            )
        }
    }
}

/**
 * 3D Spherical Interactive Planet Component for the Solar System.
 */
@Composable
private fun CosmicPlanetItem(
    name: String,
    icon: ImageVector,
    accentColor: Color,
    isFeaturedWithRings: Boolean,
    xOffset: Dp,
    yOffset: Dp,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .offset(x = xOffset - 32.dp, y = yOffset - 32.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(if (isFeaturedWithRings) 64.dp else 48.dp),
            contentAlignment = Alignment.Center
        ) {
            // Golden Orbital Planetary Ring (Exclusive for Products Planet)
            if (isFeaturedWithRings) {
                Canvas(modifier = Modifier.size(64.dp)) {
                    drawOval(
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color(0x22FFD54F),
                                Color(0xFFFFD54F),
                                Color(0x66FFD54F),
                                Color.Transparent
                            )
                        ),
                        topLeft = Offset(0f, size.height * 0.35f),
                        size = Size(size.width, size.height * 0.3f),
                        style = Stroke(width = 2.4f)
                    )
                }
            }

            // 3D Spherical Planet Body
            Box(
                modifier = Modifier
                    .size(if (isFeaturedWithRings) 48.dp else 38.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                accentColor,
                                accentColor.copy(alpha = 0.8f),
                                Color(0xFF071428),
                                Color(0xFF020612)
                            ),
                            center = Offset(16f, 16f)
                        )
                    )
                    .border(
                        width = if (isFeaturedWithRings) 2.dp else 1.2.dp,
                        brush = Brush.linearGradient(
                            listOf(accentColor, accentColor.copy(alpha = 0.3f))
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = name,
                    tint = if (isFeaturedWithRings) Color(0xFF030713) else Color.White,
                    modifier = Modifier.size(if (isFeaturedWithRings) 24.dp else 18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Planet Label in Cairo
        Text(
            text = name,
            fontFamily = CairoFontFamily,
            fontWeight = if (isFeaturedWithRings) FontWeight.ExtraBold else FontWeight.Bold,
            fontSize = if (isFeaturedWithRings) 11.5.sp else 9.5.sp,
            color = if (isFeaturedWithRings) Color(0xFFFFD54F) else Color(0xFFD4E2F5),
            textAlign = TextAlign.Center,
            lineHeight = 11.sp
        )
    }
}
