package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// =========================================================================
// Luxury Corporate Glassmorphism Palette for "البشيري للأجهزة الكهربائية"
// Sampled with pixel-level precision directly from the reference mockup
// Deep Midnight Navy + Metallic Warm Gold + Electric Cyan + Emerald + Purple
// =========================================================================

// Backgrounds & Surfaces (Deep Midnight Navy & Charcoal)
val CharcoalBackground = Color(0xFF020612)
val NavyDarkBackground = Color(0xFF040916)
val NavySurface = Color(0xFF0C1830)
val NavySurfaceVariant = Color(0xFF11203D)
val NavySurfaceElevated = Color(0xFF122244)
val NavyCardBorder = Color(0xFF1D355F)

// Metallic Warm Gold Brand Accents (Exact Sampled Values)
val GoldPrimary = Color(0xFFF4CD68)      // Warm Polished Gold
val GoldSecondary = Color(0xFFE5B63E)    // Deep Metallic Gold
val GoldLight = Color(0xFFFDF0CD)        // Light Champagne Gold
val GoldDark = Color(0xFF997A15)         // Deep Burnished Gold
val GoldContainer = Color(0x33F4CD68)    // Gold Tint Container
val GoldBorderGlow = Color(0x80F4CD68)   // 50% Opacity Glow
val GoldSubtleGlow = Color(0x26F4CD68)   // 15% Subtle Glow

// Electric Blue & Modern Cyber Accents
val ElectricBlue = Color(0xFF2997FF)     // Pure Electric Cyan-Blue
val ElectricBlueDark = Color(0xFF0284C7)
val ElectricBlueContainer = Color(0x332997FF)
val ElectricBlueGlow = Color(0x662997FF) // 40% Opacity Glow
val ElectricBlueBorder = Color(0xFF1E88E5)

// Glassmorphism & Highlights
val GlassBorder = Color(0x33FFFFFF)      // 20% White for frosted edges
val GlassSurface = Color(0xDE0E1A33)     // Semi-transparent surface
val GlassSurfaceLight = Color(0xEE132448)

// Typography & Contrast
val TextWhite = Color(0xFFFFFFFF)
val TextMuted = Color(0xFFA6B7D4)
val TextSubtle = Color(0xFF64748B)
val TextDark = Color(0xFF070B14)
val TextGold = Color(0xFFFDE68A)
val TextBlue = Color(0xFF7DD3FC)

// Status & Feedback Colors (Exact Sampled Values)
val SuccessGreen = Color(0xFF00E676)     // Emerald Trend Green
val SuccessGreenContainer = Color(0xFF063023)
val WarningAmber = Color(0xFFF59E0B)
val WarningAmberContainer = Color(0xFF332005)
val ErrorRed = Color(0xFFEF4444)
val ErrorRedContainer = Color(0xFF360D0D)
val PurpleAccent = Color(0xFFBD5BFF)     // Lavender Invoices Purple

// Reusable Luxury Gradients
val MetallicGoldGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFFF5CF6B),
        Color(0xFFF4CD68),
        Color(0xFFE5B63E)
    )
)

val ElectricBlueGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF38BDF8),
        Color(0xFF2997FF)
    )
)

val CardGlassGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xE6122244),
        Color(0xF2071124)
    )
)

val HeaderLuxuryGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF020716),
        Color(0xFF071228),
        Color(0xFF030919)
    )
)

// =========================================================================
// BESHEER – FUTURE GATE | PREMIUM SCI-FI SYSTEM TOKENS
// =========================================================================
val SciFiBackground = Color(0xFF030816)
val SciFiSurface = Color(0xFF0A152C)
val SciFiSurfaceElevated = Color(0xFF102142)
val SciFiSurfaceHighlight = Color(0xFF162C58)

// Hologram Cyan & Electric Blue
val HologramCyan = Color(0xFF00E5FF)
val HologramCyanGlow = Color(0x6600E5FF)
val HologramCyanSubtle = Color(0x2600E5FF)

// Metallic Gold & Champagne
val ChampagneGold = Color(0xFFFBE4A0)
val CyberGold = Color(0xFFFFD54F)

// Cyber Emerald
val CyberEmerald = Color(0xFF00E676)
val CyberEmeraldGlow = Color(0x6600E676)

// Future Violet
val FutureViolet = Color(0xFFBD5BFF)
val FutureVioletGlow = Color(0x66BD5BFF)

// Alert Coral
val AlertCoral = Color(0xFFFF4757)

// Sci-Fi Gradients
val SciFiHeroGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF142750),
        Color(0xFF09142B),
        Color(0xFF040A18)
    )
)

val SciFiBorderGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0x99F4CD68),
        Color(0x9900E5FF),
        Color(0x80BD5BFF),
        Color(0x99F4CD68)
    )
)

val HologramCyanGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF00E5FF),
        Color(0xFF2997FF)
    )
)

