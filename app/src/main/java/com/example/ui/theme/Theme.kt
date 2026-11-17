package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Standardized Design Tokens for Shapes & Buttons
val AppShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(20.dp)
)

val AppButtonHeight = 50.dp
val AppButtonCornerRadius = 12.dp
val AppButtonShape = RoundedCornerShape(AppButtonCornerRadius)


private val LightColorScheme = lightColorScheme(
    primary = FiksPrimary,
    onPrimary = FiksOnPrimary,
    primaryContainer = FiksAccent,
    onPrimaryContainer = LightFiksText,
    secondary = FiksScoreGreen,
    onSecondary = Color.White,
    secondaryContainer = LightFiksScoreCard,
    onSecondaryContainer = LightFiksText,
    tertiary = FiksScoreGreen,
    onTertiary = LightFiksText,
    background = LightFiksBg,
    onBackground = LightFiksText,
    surface = LightFiksBg,
    onSurface = LightFiksText,
    surfaceVariant = LightFiksScoreCard,
    onSurfaceVariant = LightFiksTextMuted,
    outline = FiksLock,
    outlineVariant = LightFiksCardBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = FiksPrimary,
    onPrimary = FiksOnPrimary,
    primaryContainer = Color(0xFF3D271E),
    onPrimaryContainer = DarkFiksText,
    secondary = Color(0xFF2EA043),
    onSecondary = Color.White,
    secondaryContainer = DarkFiksScoreCard,
    onSecondaryContainer = DarkFiksText,
    tertiary = Color(0xFF2EA043),
    onTertiary = DarkFiksText,
    background = DarkFiksBg,
    onBackground = DarkFiksText,
    surface = DarkFiksBg,
    onSurface = DarkFiksText,
    surfaceVariant = DarkFiksScoreCard,
    onSurfaceVariant = DarkFiksTextMuted,
    outline = Color(0xFF7A6E64),
    outlineVariant = DarkFiksCardBorder
)

@Composable
fun FiksFahamTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val customColors = if (darkTheme) {
        FiksCustomColors(
            bg = DarkFiksBg,
            surface = DarkFiksScoreCard,
            cardBorder = DarkFiksCardBorder,
            text = DarkFiksText,
            textMuted = DarkFiksTextMuted,
            isDark = true
        )
    } else {
        FiksCustomColors(
            bg = LightFiksBg,
            surface = LightFiksScoreCard,
            cardBorder = LightFiksCardBorder,
            text = LightFiksText,
            textMuted = LightFiksTextMuted,
            isDark = false
        )
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalFiksColors provides customColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

// Backwards compatibility alias for template
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    FiksFahamTheme(darkTheme = darkTheme, content = content)
}



