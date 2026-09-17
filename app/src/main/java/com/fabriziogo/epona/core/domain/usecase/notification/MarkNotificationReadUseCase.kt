package com.fabriziogo.epona.core.domain.usecase.notification

import com.fabriziogo.epona.core.domain.repository.NotificationRepository
import javax.inject.Inject

/** Singular counterpart to [MarkNotificationsReadUseCase], which clears the whole list. */
class MarkNotificationReadUseCase @Inject constructor(
    private val notifRepo: NotificationRepository
) {
    suspend operator fun invoke(notificationId: String): Result<Unit> =
        notifRepo.markAsRead(notificationId)
}
