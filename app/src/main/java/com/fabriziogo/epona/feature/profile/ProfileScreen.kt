package com.fabriziogo.epona.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fabriziogo.epona.BuildConfig
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.domain.model.UserStats
import com.fabriziogo.epona.core.ui.components.EponaConfirmDialog
import com.fabriziogo.epona.core.ui.components.HeroIconButton
import com.fabriziogo.epona.core.ui.components.HeroSheetEdge
import com.fabriziogo.epona.core.ui.components.LoadingIndicator
import com.fabriziogo.epona.core.ui.components.TabHero
import com.fabriziogo.epona.core.ui.media.PhotoSourceSheet
import com.fabriziogo.epona.core.ui.media.rememberMediaPickerState
import com.fabriziogo.epona.core.ui.theme.EponaTheme
import com.fabriziogo.epona.core.ui.theme.EponaTypography
import com.fabriziogo.epona.core.ui.theme.StatusBarIcons
import com.fabriziogo.epona.feature.profile.components.DeleteAccountDialog
import com.fabriziogo.epona.feature.profile.components.ProfileHeader
import com.fabriziogo.epona.feature.profile.components.SettingsItem
import com.fabriziogo.epona.feature.profile.components.SettingsSection
import com.fabriziogo.epona.feature.profile.components.StatsRow
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    onNavigateToMyPets: () -> Unit,
    onNavigateToMyAlerts: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToAuth: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val cameraDeniedMessage = stringResource(R.string.photo_camera_denied)

    // White glyphs read against the teal hero regardless of the app theme.
    StatusBarIcons(darkIcons = false)

    // Owned by the screen rather than by the stateless header below, so previews
    // of the header do not need an ActivityResultRegistry to render. Single slot:
    // a profile holds one avatar, and the single-select contract covers it.
    val avatarPicker = rememberMediaPickerState(
        remainingSlots = 1,
        onUrisPicked = { uris ->
            uris.firstOrNull()?.let { viewModel.onEvent(ProfileEvent.AvatarPicked(it)) }
        },
        onCameraDenied = {
            scope.launch { snackbarHostState.showSnackbar(cameraDeniedMessage) }
        }
    )

    if (avatarPicker.isSheetVisible) {
        PhotoSourceSheet(
            onDismiss = avatarPicker::dismiss,
            onGalleryClick = avatarPicker::pickFromGallery,
            onCameraClick = avatarPicker::takePhoto,
            isCameraAvailable = avatarPicker.isCameraAvailable
        )
    }

    LaunchedEffect(Unit) {
        viewModel.navEvents.collect { event ->
            when (event) {
                ProfileNavEvent.NavigateToMyPets -> onNavigateToMyPets()
                ProfileNavEvent.NavigateToMyAlerts -> onNavigateToMyAlerts()
                ProfileNavEvent.NavigateToSettings -> onNavigateToSettings()
                ProfileNavEvent.NavigateToHelp -> onNavigateToHelp()
                ProfileNavEvent.NavigateToAuth -> onNavigateToAuth()
            }
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(ProfileEvent.ErrorDismissed)
        }
    }

    if (state.showSignOutDialog) {
        EponaConfirmDialog(
            title = stringResource(R.string.auth_sign_out_title),
            message = stringResource(R.string.auth_sign_out_message),
            confirmText = stringResource(R.string.auth_sign_out),
            dismissText = stringResource(R.string.action_cancel),
            onConfirm = { viewModel.onEvent(ProfileEvent.SignOutConfirmed) },
            onDismiss = { viewModel.onEvent(ProfileEvent.SignOutDismissed) }
        )
    }

    if (state.showDeleteAccountDialog) {
        DeleteAccountDialog(
            onConfirm = { viewModel.onEvent(ProfileEvent.DeleteAccountConfirmed) },
            onDismiss = { viewModel.onEvent(ProfileEvent.DeleteAccountDismissed) }
        )
    }

    Scaffold(
        modifier = modifier,
        // The hero draws its own status-bar padding so it can sit under the transparent bar.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        ProfileScreenContent(
            state = state,
            paddingValues = padding,
            onEvent = viewModel::onEvent,
            onAvatarClick = avatarPicker::open
        )
    }
}

@Composable
fun ProfileScreenContent(
    state: ProfileUiState,
    paddingValues: PaddingValues,
    onEvent: (ProfileEvent) -> Unit,
    onAvatarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(MaterialTheme.colorScheme.surface),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item(key = "hero") {
            TabHero(
                title = stringResource(R.string.profile_title),
                actions = {
                    HeroIconButton(
                        icon = Icons.Outlined.Settings,
                        contentDescription = stringResource(R.string.profile_settings),
                        onClick = { onEvent(ProfileEvent.SettingsClicked) }
                    )
                }
            ) {
                Spacer(Modifier.height(16.dp))
                ProfileHeader(
                    displayName = state.user?.displayName
                        ?: stringResource(R.string.profile_default_name),
                    email = state.user?.email ?: "",
                    avatarUrl = state.user?.avatarUrl,
                    previewUri = state.avatarPreviewUri,
                    isUploading = state.isUploadingAvatar,
                    onAvatarClick = onAvatarClick
                )
                Spacer(Modifier.height(20.dp))
                StatsRow(stats = state.stats)
            }
        }

        item(key = "sheet") {
            HeroSheetEdge {
                if (state.isLoading || state.isDeletingAccount) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                    ) {
                        LoadingIndicator()
                    }
                } else {
                    ProfileSections(state = state, onEvent = onEvent)
                }
            }
        }
    }
}

@Composable
private fun ProfileSections(
    state: ProfileUiState,
    onEvent: (ProfileEvent) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        SettingsSection(title = stringResource(R.string.profile_section_my_stuff)) {
            SettingsItem(
                icon = Icons.Outlined.Pets,
                label = stringResource(R.string.profile_my_pets),
                subtitle = stringResource(R.string.profile_pets_registered, state.stats.totalPets),
                onClick = { onEvent(ProfileEvent.MyPetsClicked) }
            )
            SettingsItem(
                icon = Icons.Outlined.Campaign,
                label = stringResource(R.string.profile_my_alerts),
                subtitle = stringResource(R.string.profile_alerts_active, state.stats.activeAlerts),
                onClick = { onEvent(ProfileEvent.MyAlertsClicked) }
            )
        }

        Spacer(Modifier.height(16.dp))

        SettingsSection(title = stringResource(R.string.profile_section_app)) {
            SettingsItem(
                icon = Icons.Outlined.Settings,
                label = stringResource(R.string.profile_settings),
                subtitle = stringResource(R.string.profile_settings_subtitle),
                onClick = { onEvent(ProfileEvent.SettingsClicked) }
            )
            SettingsItem(
                icon = Icons.AutoMirrored.Outlined.HelpOutline,
                label = stringResource(R.string.profile_help),
                onClick = { onEvent(ProfileEvent.HelpClicked) }
            )
        }

        Spacer(Modifier.height(16.dp))

        SettingsSection(title = stringResource(R.string.profile_section_account)) {
            SettingsItem(
                icon = Icons.AutoMirrored.Outlined.Logout,
                label = stringResource(R.string.auth_sign_out),
                isDestructive = true,
                showChevron = false,
                onClick = { onEvent(ProfileEvent.SignOutClicked) }
            )
            SettingsItem(
                icon = Icons.Outlined.DeleteForever,
                label = stringResource(R.string.profile_delete_account),
                isDestructive = true,
                showChevron = false,
                onClick = { onEvent(ProfileEvent.DeleteAccountClicked) }
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.profile_version, BuildConfig.VERSION_NAME),
            style = EponaTypography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(bottom = 24.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    EponaTheme(dynamicColor = false) {
        ProfileScreenContent(
            state = ProfileUiState(
                isLoading = false,
                stats = UserStats(totalPets = 2, activeAlerts = 1, sightingsReported = 5)
            ),
            paddingValues = PaddingValues(),
            onEvent = {},
            onAvatarClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenLoadingPreview() {
    EponaTheme(dynamicColor = false) {
        ProfileScreenContent(
            state = ProfileUiState(isLoading = true),
            paddingValues = PaddingValues(),
            onEvent = {},
            onAvatarClick = {}
        )
    }
}
