package com.fabriziogo.epona.core.ui.permission

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.fabriziogo.epona.core.permission.CameraPermission
import com.fabriziogo.epona.core.permission.hasCameraPermission

/**
 * Runtime camera-permission state for one screen.
 * Create it with [rememberCameraPermissionState].
 *
 * Mirrors [LocationPermissionState]; the two are kept separate rather than
 * generified because each owns a different permanent-denial message and the
 * location one asks for two permissions at once.
 */
@Stable
class CameraPermissionState internal constructor(isGranted: Boolean) {

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

    /** Shows the system permission dialog. */
    fun request() = launchRequest()

    /** Opens this app's settings page, where the permission can be granted by hand. */
    fun openAppSettings() = launchSettings()

    /** Takes whichever route can still get the permission granted. */
    fun requestOrOpenAppSettings() {
        if (isPermanentlyDenied) launchSettings() else launchRequest()
    }
}

/**
 * Tracks the camera permission across the screen's lifecycle and hands back a
 * handle for asking. [onGranted] fires whenever the permission flips to granted —
 * from the dialog, or from system settings while the app was in the background,
 * which is what lets "denied, then granted in Settings" resume the capture.
 */
@Composable
fun rememberCameraPermissionState(
    onGranted: () -> Unit = {},
    onDenied: () -> Unit = {}
): CameraPermissionState {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val state = remember { CameraPermissionState(context.hasCameraPermission()) }
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
                    CameraPermission
                )
        if (granted) currentOnGranted() else currentOnDenied()
    }

    SideEffect {
        state.launchRequest = { launcher.launch(CameraPermission) }
        state.launchSettings = { context.openAppSettings() }
    }

    // Catches a grant made on the settings page we sent the user to.
    LifecycleResumeEffect(Unit) {
        val granted = context.hasCameraPermission()
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
