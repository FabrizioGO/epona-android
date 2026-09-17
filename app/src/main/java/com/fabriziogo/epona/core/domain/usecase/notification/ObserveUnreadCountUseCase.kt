package com.fabriziogo.epona.core.domain.usecase.notification

import com.fabriziogo.epona.core.domain.repository.AuthRepository
import com.fabriziogo.epona.core.domain.repository.NotificationRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class ObserveUnreadCountUseCase @Inject constructor(
    private val authRepo: AuthRepository,
    private val notifRepo: NotificationRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<Int> =
        authRepo.observeAuthState().flatMapLatest { isAuthenticated ->
            if (isAuthenticated) notifRepo.observeUnreadCount() else flowOf(0)
        }
}
