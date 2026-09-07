package com.fabriziogo.epona.core.ui.theme

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Forces the status-bar glyph colour for as long as this composable stays in the
 * composition, restoring the previous value on the way out.
 *
 * `enableEdgeToEdge()` picks one app-wide appearance from the light/dark theme, which is
 * the wrong answer for screens that draw their own content under the transparent bar: a
 * photo hero or an always-light map decides legibility by its pixels, not by the theme.
 *
 * @param darkIcons true for dark glyphs, i.e. light content sits behind the bar.
 */
@Composable
fun StatusBarIcons(darkIcons: Boolean) {
    val view = LocalView.current
    val activity = LocalActivity.current
    if (view.isInEditMode || activity == null) return

    DisposableEffect(view, activity, darkIcons) {
        val controller = WindowCompat.getInsetsController(activity.window, view)
        val previous = controller.isAppearanceLightStatusBars
        controller.isAppearanceLightStatusBars = darkIcons
        onDispose { controller.isAppearanceLightStatusBars = previous }
    }
}
