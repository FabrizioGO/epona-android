package com.fabriziogo.epona.core.ui.preview

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

/**
 * Renders a preview in both light and dark theme, each on a visible background, so a
 * dark-mode regression shows up in the same pane as the light one.
 *
 * Wrap the preview body in `EponaTheme(dynamicColor = false)`; the theme reads the preview's
 * night mode through `isSystemInDarkTheme()`.
 */
@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
annotation class EponaPreviews
