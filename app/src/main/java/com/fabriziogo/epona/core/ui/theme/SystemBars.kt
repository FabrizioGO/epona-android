package com.fabriziogo.epona.core.ui.theme

import android.view.Window
import androidx.activity.compose.LocalActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import java.util.WeakHashMap

/** One screen's claim on the status-bar glyph colour. */
private class IconClaim(var darkIcons: Boolean)

/**
 * Claims per window, oldest first. A save/restore pair is not enough here: during a
 * NavHost crossfade the entering screen's effect runs before the exiting screen disposes,
 * so "restoring" the previous value would overwrite the screen that is staying. Instead
 * the most recently entered screen that is still composed always decides.
 */
private val claims = WeakHashMap<Window, MutableList<IconClaim>>()

private fun Window.applyClaims(themeDefaultDark: Boolean, view: android.view.View) {
    val dark = claims[this]?.lastOrNull()?.darkIcons ?: themeDefaultDark
    WindowCompat.getInsetsController(this, view).isAppearanceLightStatusBars = dark
}

/**
 * Forces the status-bar glyph colour for as long as this composable stays in the
 * composition; when the last claim leaves, the glyphs fall back to the theme default.
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

    val themeDefaultDark = MaterialTheme.colorScheme.surface.luminance() > 0.5f
    val claim = remember { IconClaim(darkIcons) }

    SideEffect {
        claim.darkIcons = darkIcons
        activity.window.applyClaims(themeDefaultDark, view)
    }

    DisposableEffect(activity) {
        val window = activity.window
        claims.getOrPut(window) { mutableListOf() }.add(claim)
        window.applyClaims(themeDefaultDark, view)
        onDispose {
            claims[window]?.remove(claim)
            window.applyClaims(themeDefaultDark, view)
        }
    }
}
