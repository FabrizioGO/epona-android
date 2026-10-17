package com.fabriziogo.epona.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Lost / Found / Reward / Adoption roles that Material's scheme has no slot for. Each color has an `on*`
 * partner and a light and a dark set, all checked to at least 4.5:1 for their pairings, so
 * unlike fixed constants they stay legible against dark surfaces.
 *
 * Read through [MaterialTheme.extendedColors].
 */
@Immutable
data class ExtendedColors(
    val lost: Color,
    val onLost: Color,
    val lostContainer: Color,
    val onLostContainer: Color,
    val found: Color,
    val onFound: Color,
    val foundContainer: Color,
    val onFoundContainer: Color,
    val reward: Color,
    val onReward: Color,
    val rewardContainer: Color,
    val onRewardContainer: Color,
    val adoption: Color,
    val onAdoption: Color,
    val adoptionContainer: Color,
    val onAdoptionContainer: Color
)

val LightExtendedColors = ExtendedColors(
    lost = Color(0xFFB91C1C),
    onLost = Color.White,
    lostContainer = Color(0xFFFEE2E2),
    onLostContainer = Color(0xFFB91C1C),
    found = Color(0xFF047857),
    onFound = Color.White,
    foundContainer = Color(0xFFD1FAE5),
    onFoundContainer = Color(0xFF047857),
    reward = Color(0xFFB45309),
    onReward = Color.White,
    rewardContainer = Color(0xFFFEF3C7),
    onRewardContainer = Color(0xFF92400E),
    adoption = Color(0xFF6D28D9),
    onAdoption = Color.White,
    adoptionContainer = Color(0xFFEDE9FE),
    onAdoptionContainer = Color(0xFF5B21B6)
)

val DarkExtendedColors = ExtendedColors(
    lost = Color(0xFFF87171),
    onLost = Color(0xFF450A0A),
    lostContainer = Color(0xFF7F1D1D),
    onLostContainer = Color(0xFFFECACA),
    found = Color(0xFF34D399),
    onFound = Color(0xFF022C22),
    foundContainer = Color(0xFF064E3B),
    onFoundContainer = Color(0xFFA7F3D0),
    reward = Color(0xFFFBBF24),
    onReward = Color(0xFF451A03),
    rewardContainer = Color(0xFF78350F),
    onRewardContainer = Color(0xFFFDE68A),
    adoption = Color(0xFFA78BFA),
    onAdoption = Color(0xFF2E1065),
    adoptionContainer = Color(0xFF4C1D95),
    onAdoptionContainer = Color(0xFFDDD6FE)
)

val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }

val MaterialTheme.extendedColors: ExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalExtendedColors.current
