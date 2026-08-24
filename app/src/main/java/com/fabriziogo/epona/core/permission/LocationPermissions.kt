package com.fabriziogo.epona.core.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

/**
 * Fine and coarse are requested together: the system shows a single dialog with a
 * precise/approximate choice, and either grant is enough for what the app does.
 *
 * Lives outside both `core/ui` and `core/data` so the Compose layer that asks for
 * the permission and the repository that needs it agree on one definition.
 */
val LocationPermissions = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

fun Context.hasLocationPermission(): Boolean = LocationPermissions.any {
    ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
}
