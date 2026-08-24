package com.fabriziogo.epona.core.domain.usecase.notification

import com.fabriziogo.epona.core.domain.model.Notification
import com.fabriziogo.epona.core.domain.repository.AuthRepository
import com.fabriziogo.epona.core.domain.repository.NotificationRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class GetNotificationsUseCase @Inject constructor(
    private val authRepo: AuthRepository,
    private val notifRepo: NotificationRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<List<Notification>> =
        authRepo.observeAuthState().flatMapLatest { isAuthenticated ->
            if (isAuthenticated) {
                val userId = authRepo.currentUserId
                if (userId != null) {
                    notifRepo.observeNotifications(userId)
                } else {
                    flowOf(emptyList())
                }
            } else {
                flowOf(emptyList())
            }
        }
}