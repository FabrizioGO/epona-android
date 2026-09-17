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
    /**
     * Restarts on every sign-in and sign-out. The user id is resolved inside the
     * repository, behind `awaitReady()`: reading the non-suspend `currentUserId` here
     * instead is not ordered against `observeAuthState()` emitting true, so a cold start
     * could see null and yield an empty list forever.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<List<Notification>> =
        authRepo.observeAuthState().flatMapLatest { isAuthenticated ->
            if (isAuthenticated) notifRepo.observeNotifications() else flowOf(emptyList())
        }
}
