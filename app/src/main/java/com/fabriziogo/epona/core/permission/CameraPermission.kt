package com.fabriziogo.epona.core.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.MediaStore
import androidx.core.content.ContextCompat

/**
 * Taking a photo goes through the system camera app, which needs no permission of
 * its own. The permission is required only because the app declares CAMERA in its
 * manifest: the system then refuses to start ACTION_IMAGE_CAPTURE until it is
 * granted.
 *
 * Lives outside both `core/ui` and `core/data` so the Compose layer that asks for
 * the permission and the picker that needs it agree on one definition.
 */
const val CameraPermission: String = Manifest.permission.CAMERA

fun Context.hasCameraPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, CameraPermission) ==
            PackageManager.PERMISSION_GRANTED

/**
 * Whether any installed app can service ACTION_IMAGE_CAPTURE. Requires the
 * `<queries>` entry in the manifest to return anything on API 30+.
 */
fun Context.hasCameraApp(): Boolean =
    Intent(MediaStore.ACTION_IMAGE_CAPTURE).resolveActivity(packageManager) != null
