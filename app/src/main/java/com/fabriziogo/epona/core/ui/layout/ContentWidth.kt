package com.fabriziogo.epona.core.ui.layout

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Reading width for forms and settings-style lists. */
val FormMaxWidth: Dp = 600.dp

/** Width for browsing content such as detail pages and card lists. */
val ContentMaxWidth: Dp = 840.dp

/**
 * Horizontal padding that centres content of at most [max] width in the current window.
 * Zero on phones; use it as extra `contentPadding` on a lazy list (or as `start`/`end`
 * padding) so rows stay readable on tablets while the list keeps scrolling edge to edge.
 */
@Composable
fun contentGutter(max: Dp = ContentMaxWidth): Dp {
    val windowWidth = LocalConfiguration.current.screenWidthDp.dp
    return maxOf(0.dp, (windowWidth - max) / 2)
}

/**
 * Lets an item ignore [horizontal] of its container's content padding so a background (a
 * hero, a sheet edge) still runs edge to edge while the rest of the content stays inset.
 */
fun Modifier.fullBleed(horizontal: Dp): Modifier = layout { measurable, constraints ->
    val extra = horizontal.roundToPx() * 2
    val width = constraints.maxWidth + extra
    val placeable = measurable.measure(constraints.copy(minWidth = width, maxWidth = width))
    layout(constraints.maxWidth, placeable.height) {
        placeable.place(-extra / 2, 0)
    }
}

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
