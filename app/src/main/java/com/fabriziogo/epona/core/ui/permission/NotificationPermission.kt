package com.fabriziogo.epona.core.ui.permission

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.fabriziogo.epona.core.permission.NotificationPermission
import com.fabriziogo.epona.core.permission.hasNotificationPermission
import com.fabriziogo.epona.core.permission.requiresNotificationPermission

/**
 * Runtime notification-permission state for the app. Create it with
 * [rememberNotificationPermissionState]. Mirrors [CameraPermissionState].
 */
@Stable
class NotificationPermissionState internal constructor(isGranted: Boolean) {

    var isGranted: Boolean by mutableStateOf(isGranted)
        internal set

    /** True once the user has turned the request down at least once. */
    var isDenied: Boolean by mutableStateOf(false)
        internal set

    /**
     * True when the system will no longer show the dialog — the user picked
     * "Don't allow" twice, or a device policy blocks it. The app's settings page
     * is then the only way left to grant it.
     */
    var isPermanentlyDenied: Boolean by mutableStateOf(false)
        internal set

    internal var launchRequest: () -> Unit = {}
    internal var launchSettings: () -> Unit = {}

    /** Shows the system permission dialog. No-op below API 33. */
    fun request() = launchRequest()

    /** Opens this app's settings page, where the permission can be granted by hand. */
    fun openAppSettings() = launchSettings()

    /** Takes whichever route can still get the permission granted. */
    fun requestOrOpenAppSettings() {
        if (isPermanentlyDenied) launchSettings() else launchRequest()
    }
}

/**
 * Tracks the notification permission across the app's lifecycle and hands back a
 * handle for asking. [onGranted] fires whenever the permission flips to granted --
 * from the dialog, or from system settings while the app was in the background.
 */
@Composable
fun rememberNotificationPermissionState(
    onGranted: () -> Unit = {},
    onDenied: () -> Unit = {}
): NotificationPermissionState {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val state = remember { NotificationPermissionState(context.hasNotificationPermission()) }
    val currentOnGranted by rememberUpdatedState(onGranted)
    val currentOnDenied by rememberUpdatedState(onDenied)

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        state.isGranted = granted
        state.isDenied = !granted
        state.isPermanentlyDenied = !granted && activity != null &&
                !ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    NotificationPermission
                )
        if (granted) currentOnGranted() else currentOnDenied()
    }

    SideEffect {
        state.launchRequest = {
            if (requiresNotificationPermission) launcher.launch(NotificationPermission)
        }
        state.launchSettings = { context.openAppSettings() }
    }

    // Catches a grant (or a revoke) made on the settings page we sent the user to.
    LifecycleResumeEffect(Unit) {
        val granted = context.hasNotificationPermission()
        if (granted != state.isGranted) {
            state.isGranted = granted
            if (granted) {
                state.isDenied = false
                state.isPermanentlyDenied = false
                currentOnGranted()
            }
        }
        onPauseOrDispose { }
    }

    return state
}

/**
 * Asks once the user is signed in, not at launch.
 *
 * On the login screen there is nothing to explain the request with, and the system
 * only shows the dialog twice ever -- spending one of those on a stranger is how apps
 * end up permanently blocked. Survives configuration changes so rotating does not
 * re-prompt; once the system stops showing the dialog the request is a silent no-op,
 * so no extra guard is needed for the permanently-denied case.
 */
@Composable
fun RequestNotificationPermissionOnceSignedIn(
    state: NotificationPermissionState,
    isAuthenticated: Boolean
) {
    var hasAsked by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated && !hasAsked && !state.isGranted && requiresNotificationPermission) {
            hasAsked = true
            state.request()
        }
    }
}
