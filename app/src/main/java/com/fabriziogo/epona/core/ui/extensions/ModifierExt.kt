package com.fabriziogo.epona.core.ui.extensions

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.Role

/**
 * Clickable with press-scale animation (M3 feel).
 */
fun Modifier.pressScale(
    onClick: () -> Unit,
    scaleFactor: Float = 0.97f
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleFactor else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "pressScale"
    )
    this
        .scale(scale)
        .clickable(
            interactionSource = interactionSource,
            // Keep the default state layer so press, focus and hover stay visible
            // (keyboard and D-pad users get no other cue), on top of the scale.
            indication = LocalIndication.current,
            role = Role.Button,
            onClick = onClick
        )
}

/**
 * Clickable without ripple (for custom touch handling).
 */
fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
    clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() },
        onClick = onClick
    )
}