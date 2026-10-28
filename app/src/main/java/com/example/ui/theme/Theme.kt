package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = TextDark,
    primaryContainer = GoldContainer,
    onPrimaryContainer = GoldLight,
    secondary = ElectricBlue,
    onSecondary = TextDark,
    secondaryContainer = ElectricBlueContainer,
    onSecondaryContainer = ElectricBlue,
    tertiary = GoldSecondary,
    onTertiary = TextDark,
    tertiaryContainer = GoldContainer,
    onTertiaryContainer = GoldLight,
    background = CharcoalBackground,
    onBackground = TextWhite,
    surface = NavySurface,
    onSurface = TextWhite,
    surfaceVariant = NavySurfaceVariant,
    onSurfaceVariant = TextMuted,
    surfaceTint = GoldPrimary,
    outline = NavyCardBorder,
    outlineVariant = GlassBorder,
    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorRedContainer,
    onErrorContainer = Color(0xFFFCA5A5)
)

private val LightColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = TextDark,
    primaryContainer = GoldContainer,
    onPrimaryContainer = GoldLight,
    secondary = ElectricBlue,
    onSecondary = TextDark,
    secondaryContainer = ElectricBlueContainer,
    onSecondaryContainer = ElectricBlue,
    background = CharcoalBackground,
    onBackground = TextWhite,
    surface = NavySurface,
    onSurface = TextWhite,
    surfaceVariant = NavySurfaceVariant,
    onSurfaceVariant = TextMuted,
    outline = NavyCardBorder,
    outlineVariant = GlassBorder,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Exclusively luxury dark charcoal & gold corporate aesthetic
    dynamicColor: Boolean = false, // Keep distinct Al-Bashiri signature identity
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
