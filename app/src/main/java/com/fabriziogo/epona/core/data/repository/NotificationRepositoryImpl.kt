package com.fabriziogo.epona.core.data.repository

import com.fabriziogo.epona.core.data.mapper.toDomain
import com.fabriziogo.epona.core.data.mapper.toEntity
import com.fabriziogo.epona.core.database.dao.NotificationDao
import com.fabriziogo.epona.core.domain.model.Notification
import com.fabriziogo.epona.core.domain.repository.NotificationRepository
import com.fabriziogo.epona.core.network.service.AuthService
import com.fabriziogo.epona.core.network.service.NotificationService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val authService: AuthService,
    private val notificationService: NotificationService,
    private val notificationDao: NotificationDao
) : NotificationRepository {

    private suspend fun currentUserId(): String = authService.requireUserId()

    /**
     * The user id once session restoration has finished, or null if nobody is signed in.
     *
     * [currentUserId] throws, which is right for a one-shot write and wrong for a Flow
     * the app collects for its whole lifetime: `AppViewModel` and `HomeViewModel` both
     * collect the unread count from their `viewModelScope`, and an exception there takes
     * the scope -- and the screen -- down.
     */
    private suspend fun currentUserIdOrNull(): String? {
        authService.awaitReady()
        return authService.getCurrentUserId()
    }

    /**
     * Cached notifications for the signed-in user, kept fresh from the server.
     *
     * Rows are only ever created by Postgres triggers, so the client has no write path
     * that would seed the cache: without the refresh here a fresh install, a new device
     * or a destructive migration shows an empty list forever, which is exactly the
     * state the screen was stuck in.
     *
     * Realtime is a *tick*, not a payload. Nothing in this codebase decodes a
     * PostgresAction record; the change is re-read through the normal query so one
     * decode path covers the socket and the cache alike.
     */
    @OptIn(FlowPreview::class)
    override fun observeNotifications(): Flow<List<Notification>> = channelFlow {
        val userId = currentUserIdOrNull()
        if (userId == null) {
            send(emptyList())
            return@channelFlow
        }

        val initialRefresh = launch { refreshQuietly(userId) }

        // With rows already cached, show them now and let the refresh update them --
        // offline that means the list appears instead of waiting out a network timeout.
        // With nothing cached there is nothing worth showing, so wait for the fetch
        // rather than flashing the empty state.
        if (notificationDao.getNotifications(userId).isEmpty()) initialRefresh.join()

        launch {
            notificationService.observeNotifications(userId)
                // `markAllAsRead` updates N rows and so produces N realtime events.
                // Without this, tapping "mark all read" on a 50-row list fires 50 full
                // re-fetches. One fetch once the burst settles is the same answer.
                .debounce(REALTIME_SETTLE_MS)
                // Realtime is an enhancement here, not the source of the list: if the
                // channel cannot be joined -- the table is not in the publication, the
                // socket is down -- the collector must simply stop receiving updates,
                // not take the screen down.
                .catch { e -> Timber.w(e, "Realtime notification channel closed") }
                .collect { refreshQuietly(userId) }
        }

        notificationDao.observeNotifications(userId)
            .map { entities -> entities.map { it.toDomain() } }
            .collect { notifications -> send(notifications) }
    }

    /**
     * Unread count straight from Room, with no network and no realtime channel.
     *
     * Three surfaces collect this app-wide (bottom-nav badge, home hero bell,
     * notifications screen). Giving it a realtime channel would open one channel per
     * consumer, and because `Realtime.channel(topic)` de-duplicates by topic they would
     * share a single channel object and tear each other's subscription down. Freshness
     * comes from whatever writes Room instead: [observeNotifications] while the list is
     * open, the FCM service while it is not, and the mark-read paths.
     */
    override fun observeUnreadCount(): Flow<Int> = flow {
        val userId = currentUserIdOrNull()
        if (userId == null) {
            emit(0)
            return@flow
        }
        emitAll(notificationDao.observeUnreadCount(userId))
    }

    override suspend fun refreshNotifications(): Result<Unit> = runCatching {
        refresh(currentUserId())
    }

    override suspend fun markAllAsRead(): Result<Unit> = runCatching {
        val userId = currentUserId()
        notificationService.markAllAsRead(userId)
        notificationDao.markAllAsRead(userId)
    }

    override suspend fun markAsRead(notificationId: String): Result<Unit> =
        runCatching {
            notificationService.markAsRead(notificationId)
            notificationDao.markAsRead(notificationId)
        }

    override suspend fun deleteNotification(
        notificationId: String
    ): Result<Unit> = runCatching {
        notificationService.deleteNotification(notificationId)
        notificationDao.deleteNotification(notificationId)
    }

    private suspend fun refresh(userId: String) {
        val dtos = notificationService.getNotifications(userId)
        notificationDao.insertNotifications(
            // `NotificationDto.id` is nullable and the mapper defaults it to "". A row
            // without an id would take the empty-string primary key and REPLACE the
            // previous one; drop it instead of collapsing the list into one row.
            dtos.filter { !it.id.isNullOrBlank() }.map { it.toEntity() }
        )
    }

    /**
     * Runs a refresh for its side effect only. A transient network error must not blank
     * out a list that is still valid on screen, and must not cancel the Room flow that
     * is serving it.
     */
    private suspend fun refreshQuietly(userId: String) {
        try {
            refresh(userId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.w(e, "Notification refresh failed; serving the cached list")
        }
    }

    private companion object {
        const val REALTIME_SETTLE_MS = 300L
    }
}
