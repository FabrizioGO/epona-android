package com.fabriziogo.epona.core.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController

/** Key the one-shot confirmation travels under, on the destination's saved state. */
private const val SNACKBAR_MESSAGE_KEY = "snackbar_message"

/**
 * Pops back to the previous destination and hands it a message to confirm what just
 * happened.
 *
 * Without this, a save and a cancel are the same gesture from the user's side — both
 * end as a plain back navigation with nothing to show the work landed.
 */
fun NavController.popBackStackWithMessage(message: String) {
    previousBackStackEntry?.savedStateHandle?.set(SNACKBAR_MESSAGE_KEY, message)
    popBackStack()
}

/**
 * The message this destination was returned to with, or null. Read it in the
 * navigation layer — the back stack entry is what carries the result — and hand it to
 * the screen, which shows it and then calls [clearSnackbarMessage].
 */
@Composable
fun NavBackStackEntry.snackbarMessageAsState(): State<String?> =
    savedStateHandle
        .getStateFlow<String?>(SNACKBAR_MESSAGE_KEY, null)
        .collectAsStateWithLifecycle()

/** Drops the delivered message so returning to this destination does not replay it. */
fun NavBackStackEntry.clearSnackbarMessage() {
    savedStateHandle[SNACKBAR_MESSAGE_KEY] = null
}
