package com.fabriziogo.epona.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Brand deviation from the MD3 baseline (4/8/12/16/28): medium and large are rounder (16/24).
 * extraLarge follows the baseline so dialogs, bottom sheets and hero sheet edges match MD3.
 */
val EponaShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)
