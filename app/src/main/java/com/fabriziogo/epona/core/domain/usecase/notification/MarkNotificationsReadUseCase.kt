package com.fabriziogo.epona.core.domain.usecase.notification

import com.fabriziogo.epona.core.domain.repository.NotificationRepository
import javax.inject.Inject

class MarkNotificationsReadUseCase @Inject constructor(
    private val notifRepo: NotificationRepository
) {
    suspend operator fun invoke(): Result<Unit> =
        notifRepo.markAllAsRead()
}