package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Light Mode Base Colors
val LightFiksBg = Color(0xFFF7F3EB) // Warm cream main background
val LightFiksScoreCard = Color(0xFFFFFFFF) // Crisp white for cards/surfaces
val LightFiksCardBorder = Color(0xFFE8DFD3) // Subtle crisp border
val LightFiksText = Color(0xFF1E1713) // Dark charcoal/espresso
val LightFiksTextMuted = Color(0xFF6B5A4D) // Clean readable warm secondary text

// Dark Mode Base Colors
val DarkFiksBg = Color(0xFF161311) // Dark charcoal background
val DarkFiksScoreCard = Color(0xFF25201C) // Dark surface/card
val DarkFiksCardBorder = Color(0xFF3A322C) // Dark surface border
val DarkFiksText = Color(0xFFF7F3EB) // Cream/light text
val DarkFiksTextMuted = Color(0xFFC4B8AA) // Light muted secondary text

// Core Brand Accents (Consistent across both Light & Dark modes)
val FiksPrimary = Color(0xFFE25A20) // Signature warm orange primary accent
val FiksPrimaryDark = Color(0xFFC0440E)
val FiksPrimaryLight = Color(0xFFFFECE2)
val FiksAccent = Color(0xFFF8B259) // Warm golden amber
val FiksAccentLight = Color(0xFFFFF0DC)
val FiksIconPretest = Color(0xFF238636) // Rich green for pretest
val FiksIconPostest = Color(0xFFE25A20) // Rich orange for posttest
val FiksCapstoneBg = Color(0xFFFFFFFF)

val FiksOnPrimary = Color(0xFFFFFFFF)
val FiksTrack = Color(0xFFEDE4D7)
val FiksPrimaryText = Color(0xFFC0440E)
val FiksDecor = Color(0xFFA58AF0)
val FiksScoreGreen = Color(0xFF238636) // Green secondary accent
val FiksGreenButton = Color(0xFF238636)
val FiksScoreYellow = Color(0xFFE59425)
val FiksScoreRed = Color(0xFFD63B3B)
val FiksLock = Color(0xFF9E8F82)
val FiksCardShadow = Color(0x1F2A180E)

// Dynamic Color Container
data class FiksCustomColors(
    val bg: Color,
    val surface: Color,
    val cardBorder: Color,
    val text: Color,
    val textMuted: Color,
    val isDark: Boolean
)

val LocalFiksColors = staticCompositionLocalOf {
    FiksCustomColors(
        bg = LightFiksBg,
        surface = LightFiksScoreCard,
        cardBorder = LightFiksCardBorder,
        text = LightFiksText,
        textMuted = LightFiksTextMuted,
        isDark = false
    )
}

// Dynamic Composable Color Accessors for seamless Dark/Light adaptation
val FiksBg: Color
    @Composable
    get() = LocalFiksColors.current.bg

val FiksScoreCard: Color
    @Composable
    get() = LocalFiksColors.current.surface

val FiksCardBorder: Color
    @Composable
    get() = LocalFiksColors.current.cardBorder

val FiksText: Color
    @Composable
    get() = LocalFiksColors.current.text

val FiksTextMuted: Color
    @Composable
    get() = LocalFiksColors.current.textMuted

// Legacy aliases for template compatibility
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
val Purple40 = FiksPrimary
val PurpleGrey40 = LightFiksTextMuted
val Pink40 = FiksAccent

// Brand & Feature Colors
val TealPrimary = Color(0xFF0F766E)
val TealLight = Color(0xFF2DD4BF)
val TealDark = Color(0xFF115E59)
val CyanAccent = Color(0xFF06B6D4)
val CyanAccentText = Color(0xFF0891B2)
val FlameOrange = Color(0xFFF97316)
val FlameLight = Color(0xFFFFEDD5)
val AmberGold = Color(0xFFF59E0B)
val SuccessGreen = Color(0xFF10B981)
val ErrorRed = Color(0xFFEF4444)
val PurpleLevel = Color(0xFF8B5CF6)

// Theme Surface & Text Tokens
val LightBg = Color(0xFFFAF7F2)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF5F0E8)
val LightTextPrimary = Color(0xFF1E1713)
val LightTextSecondary = Color(0xFF6B5A4D)
val LightBorder = Color(0xFFE8DFD3)
val LightBorderSubtle = Color(0xFFF0EAE1)

val DarkBg = Color(0xFF161311)
val DarkSurface = Color(0xFF25201C)
val DarkSurfaceVariant = Color(0xFF332B25)
val DarkTextPrimary = Color(0xFFF7F3EB)
val DarkTextSecondary = Color(0xFFC4B8AA)

// Code & Terminal Dark Surfaces
val CodeDarkBg = Color(0xFF0D1117)
val CodeDarkSurface = Color(0xFF161B22)
val CodeDarkBorder = Color(0xFF30363D)

// Chip / Badge Levels
val ChipOrientasiBg = Color(0xFFE0F2FE)
val ChipOrientasiText = Color(0xFF0284C7)
val ChipPemulaBg = Color(0xFFDCFCE7)
val ChipPemulaText = Color(0xFF16A34A)
val ChipMenengahBg = Color(0xFFFEF3C7)
val ChipMenengahText = Color(0xFFD97706)
val ChipLanjutBg = Color(0xFFF3E8FF)
val ChipLanjutText = Color(0xFF9333EA)
val ChipNeutralBg = Color(0xFF1E293B)
val ChipNeutralText = Color(0xFFE2E8F0)



