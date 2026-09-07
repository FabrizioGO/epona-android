package com.fabriziogo.epona.core.ui.components

import com.fabriziogo.epona.core.domain.model.Species

/**
 * Placeholder artwork for a [Species] when no pet photo is available.
 */
fun Species.emoji(): String = when (this.value) {
    "dog" -> "🐕"
    "cat" -> "🐈"
    "bird" -> "🐦"
    "rabbit" -> "🐇"
    else -> "🐾"
}
