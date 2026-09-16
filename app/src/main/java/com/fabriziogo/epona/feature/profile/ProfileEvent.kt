package com.fabriziogo.epona.feature.profile

import android.net.Uri

sealed interface ProfileEvent {
    data object MyPetsClicked : ProfileEvent
    data object MyAlertsClicked : ProfileEvent
    data object SettingsClicked : ProfileEvent
    data object HelpClicked : ProfileEvent
    data object SignOutClicked : ProfileEvent
    data object SignOutConfirmed : ProfileEvent
    data object SignOutDismissed : ProfileEvent
    data class AvatarPicked(val uri: Uri) : ProfileEvent
    data object Refresh : ProfileEvent
    data object ErrorDismissed : ProfileEvent
}

sealed interface ProfileNavEvent {
    data object NavigateToMyPets : ProfileNavEvent
    data object NavigateToMyAlerts : ProfileNavEvent
    data object NavigateToSettings : ProfileNavEvent
    data object NavigateToHelp : ProfileNavEvent
    data object NavigateToAuth : ProfileNavEvent
}
