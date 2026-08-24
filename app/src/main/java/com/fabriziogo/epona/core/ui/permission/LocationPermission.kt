package com.fabriziogo.epona.core.ui.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.provider.Settings
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
import com.fabriziogo.epona.core.permission.LocationPermissions
import com.fabriziogo.epona.core.permission.hasLocationPermission

/**
 * Runtime location-permission state for one screen.
 * Create it with [rememberLocationPermissionState].
 */
@Stable
class LocationPermissionState internal constructor(isGranted: Boolean) {

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
 * Tracks the location permission across the screen's lifecycle and hands back a
 * handle for asking. [onGranted] fires whenever the permission flips to granted —
 * from the dialog, or from system settings while the app was in the background.
 * [onDenied] fires when a request the app made comes back without the permission.
 */
@Composable
fun rememberLocationPermissionState(
    onGranted: () -> Unit = {},
    onDenied: () -> Unit = {}
): LocationPermissionState {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val state = remember { LocationPermissionState(context.hasLocationPermission()) }
    val currentOnGranted by rememberUpdatedState(onGranted)
    val currentOnDenied by rememberUpdatedState(onDenied)

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        // Read the permission back from the system instead of the result map: a
        // coarse-only grant still counts, and only one of the two is needed.
        val granted = context.hasLocationPermission()
        state.isGranted = granted
        state.isDenied = !granted
        state.isPermanentlyDenied = !granted && activity != null &&
                !ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
        if (granted) currentOnGranted() else currentOnDenied()
    }

    SideEffect {
        state.launchRequest = { launcher.launch(LocationPermissions) }
        state.launchSettings = { context.openAppSettings() }
    }

    // The permission can also change while the app is away — granted on the settings
    // page we sent the user to, or revoked from system settings.
    LifecycleResumeEffect(Unit) {
        val granted = context.hasLocationPermission()
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
 * Asks for location the first time the calling screen is shown, so opening the
 * screen is itself the trigger. Survives configuration changes, so rotating does
 * not re-prompt; once the system stops showing the dialog the screen's own prompt
 * takes over.
 */
@Composable
fun RequestLocationPermissionOnEntry(state: LocationPermissionState) {
    var hasAsked by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!hasAsked && !state.isGranted) {
            hasAsked = true
            state.request()
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private fun Context.openAppSettings() {
    startActivity(
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
    )
}
