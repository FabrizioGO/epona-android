package com.fabriziogo.epona.core.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

/**
 * POST_NOTIFICATIONS only exists from API 33. Below it the manifest grant is the whole
 * story, but the user can still switch notifications off from system settings -- which
 * is why the pre-33 branch asks the notification manager rather than returning true.
 *
 * Lives outside both `core/ui` and `core/data` so the Compose layer that asks for the
 * permission and anything that reports on it agree on one definition.
 */
const val NotificationPermission: String = Manifest.permission.POST_NOTIFICATIONS

val requiresNotificationPermission: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

fun Context.hasNotificationPermission(): Boolean =
    if (requiresNotificationPermission) {
        ContextCompat.checkSelfPermission(this, NotificationPermission) ==
                PackageManager.PERMISSION_GRANTED
    } else {
        NotificationManagerCompat.from(this).areNotificationsEnabled()
    }
