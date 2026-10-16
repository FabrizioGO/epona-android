package com.fabriziogo.epona.core.ui.layout

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Reading width for forms and settings-style lists. */
val FormMaxWidth: Dp = 600.dp

/** Width for browsing content such as detail pages and card lists. */
val ContentMaxWidth: Dp = 840.dp

/**
 * Caps the width at [max] and centres the result, so forms and text lines do not stretch
 * across a tablet or an unfolded foldable. On phones (narrower than [max]) it changes nothing.
 *
 * Apply it to the content *inside* a scroll container, not to the scrollable itself, so the
 * whole window width still scrolls and edge-to-edge backgrounds keep working.
 */
fun Modifier.constrainedWidth(max: Dp = FormMaxWidth): Modifier =
    this
        .fillMaxWidth()
        .wrapContentWidth(Alignment.CenterHorizontally)
        .widthIn(max = max)
        .fillMaxWidth()
