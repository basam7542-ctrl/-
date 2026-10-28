package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// =========================================================================
// Luxury Corporate Dark Palette for "البشيري للأجهزة الكهربائية"
// Deep Charcoal & Navy + Metallic Gold + Electric Blue
// =========================================================================

// Backgrounds & Surfaces (Deep Charcoal & Midnight Navy)
val CharcoalBackground = Color(0xFF070B14)
val NavyDarkBackground = Color(0xFF0A0F1D)
val NavySurface = Color(0xFF10182A)
val NavySurfaceVariant = Color(0xFF172238)
val NavySurfaceElevated = Color(0xFF1E2D4A)
val NavyCardBorder = Color(0xFF263756)

// Metallic Gold Brand Accents
val GoldPrimary = Color(0xFFD4AF37)      // Classic Metallic Gold
val GoldSecondary = Color(0xFFF3C64F)    // Warm Polished Gold
val GoldLight = Color(0xFFFDF0CD)        // Light Champagne Gold
val GoldDark = Color(0xFF997A15)         // Deep Burnished Gold
val GoldContainer = Color(0xFF2A2008)    // Deep Gold Tint Container
val GoldBorderGlow = Color(0x66D4AF37)   // 40% Opacity Glow
val GoldSubtleGlow = Color(0x22D4AF37)   // 13% Subtle Glow

// Electric Blue & Modern Cyber Accents
val ElectricBlue = Color(0xFF38BDF8)     // Sky Electric Blue
val ElectricBlueDark = Color(0xFF0284C7)
val ElectricBlueContainer = Color(0xFF0C243E)
val ElectricBlueGlow = Color(0x5538BDF8) // 33% Opacity Glow
val ElectricBlueBorder = Color(0xFF1E40AF)

// Glassmorphism & Highlights
val GlassBorder = Color(0x26FFFFFF)      // 15% White for frosted edges
val GlassSurface = Color(0xDE10182A)     // Semi-transparent surface
val GlassSurfaceLight = Color(0xEE16223B)

// Typography & Contrast
val TextWhite = Color(0xFFF8FAFC)
val TextMuted = Color(0xFF94A3B8)
val TextSubtle = Color(0xFF64748B)
val TextDark = Color(0xFF070B14)
val TextGold = Color(0xFFFDE68A)
val TextBlue = Color(0xFF7DD3FC)

// Status & Feedback Colors
val SuccessGreen = Color(0xFF10B981)
val SuccessGreenContainer = Color(0xFF063023)
val WarningAmber = Color(0xFFF59E0B)
val WarningAmberContainer = Color(0xFF332005)
val ErrorRed = Color(0xFFEF4444)
val ErrorRedContainer = Color(0xFF360D0D)
val PurpleAccent = Color(0xFFA855F7)

// Reusable Luxury Gradients
val MetallicGoldGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFFE5C058),
        Color(0xFFD4AF37),
        Color(0xFFB88E1C)
    )
)

val ElectricBlueGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF38BDF8),
        Color(0xFF0284C7)
    )
)

val CardGlassGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF16233B),
        Color(0xFF0E1729)
    )
)

val HeaderLuxuryGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF0F172A),
        Color(0xFF0A0F1D)
    )
)
