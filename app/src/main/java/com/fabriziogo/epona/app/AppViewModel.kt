package com.fabriziogo.epona.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fabriziogo.epona.core.domain.usecase.auth.ObserveAuthStateUseCase
import com.fabriziogo.epona.core.domain.usecase.notification.ObserveUnreadCountUseCase
import com.fabriziogo.epona.core.domain.usecase.user.SyncFcmTokenUseCase
import com.fabriziogo.epona.core.domain.usecase.user.SyncUserLocaleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    observeAuthState: ObserveAuthStateUseCase,
    observeUnreadCount: ObserveUnreadCountUseCase,
    private val syncFcmToken: SyncFcmTokenUseCase,
    private val syncUserLocale: SyncUserLocaleUseCase
) : ViewModel() {

    val authState: StateFlow<Boolean> = observeAuthState()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    val unreadCount: StateFlow<Int> = observeUnreadCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )

    init {
        /**
         * Registers the device with the server once a session exists.
         *
         * Collected directly rather than off [authState]: that one is
         * `WhileSubscribed`, so it stops whenever the UI is not looking, and device
         * registration must not depend on who happens to be watching.
         *
         * Both writes are idempotent, so the redundant run on process restart is
         * harmless -- and a locale change recreates the activity, which recreates this
         * ViewModel, which is how a language switch reaches `users.locale` at all.
         */
        viewModelScope.launch {
            observeAuthState()
                .distinctUntilChanged()
                .filter { isAuthenticated -> isAuthenticated }
                .collect {
                    syncUserLocale().onFailure { Timber.w(it, "Locale sync failed") }
                    syncFcmToken().onFailure { Timber.w(it, "FCM token sync failed") }
                }
        }
    }
}
