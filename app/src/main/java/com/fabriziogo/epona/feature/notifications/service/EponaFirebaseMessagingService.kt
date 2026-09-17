package com.fabriziogo.epona.feature.notifications.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.fabriziogo.epona.R
import com.fabriziogo.epona.app.MainActivity
import com.fabriziogo.epona.core.domain.repository.UserRepository
import com.fabriziogo.epona.core.domain.usecase.notification.RefreshNotificationsUseCase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class EponaFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var refreshNotifications: RefreshNotificationsUseCase

    /**
     * Deliberately not cancelled in `onDestroy`. The work has to outlive the service
     * instance -- the system tears the service down as soon as `onMessageReceived`
     * returns, and cancelling here would abort the refresh mid-flight.
     */
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        const val CHANNEL_ID = "epona_alerts"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        serviceScope.launch {
            userRepository.updateFcmToken(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title
            ?: message.data["title"]
            ?: getString(R.string.notification_default_title)
        val body = message.notification?.body
            ?: message.data["body"]
            ?: ""
        val alertId = message.data["alert_id"]
        val type = message.data["type"] ?: "new_alert"

        createNotificationChannel()
        showNotification(title, body, alertId, type)

        // The tray is only half of it. The in-app list and all three unread badges read
        // Room, and while the app is backgrounded nothing else writes it -- the realtime
        // channel only exists while the notifications screen is collecting.
        //
        // A re-fetch rather than an insert from the payload: the payload carries no row
        // id or created_at, so an insert would need a synthetic primary key and would
        // duplicate the row on the next refresh.
        serviceScope.launch {
            refreshNotifications()
                .onFailure { Timber.w(it, "Push-triggered notification refresh failed") }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.notification_channel_desc)
                enableVibration(true)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(
        title: String,
        body: String,
        alertId: String?,
        type: String
    ) {
        // Explicit component, not `getLaunchIntentForPackage`. That returns an
        // ACTION_MAIN + CATEGORY_LAUNCHER intent, and when the task already exists the
        // system treats it as "tapped the launcher icon": it brings the task forward and
        // drops the extras, so `alert_id` never reaches onNewIntent. Fixing onNewIntent
        // without this fixes nothing.
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            alertId?.let { putExtra(MainActivity.EXTRA_ALERT_ID, it) }
            putExtra(MainActivity.EXTRA_NOTIFICATION_TYPE, type)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            alertId?.hashCode() ?: 0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val icon = when (type) {
            "new_alert" -> android.R.drawable.ic_dialog_alert
            "sighting" -> android.R.drawable.ic_menu_compass
            "resolved" -> android.R.drawable.star_on
            else -> android.R.drawable.ic_popup_reminder
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(
            alertId?.hashCode() ?: System.currentTimeMillis().toInt(),
            notification
        )
    }
}
