package com.fabriziogo.epona.feature.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fabriziogo.epona.BuildConfig
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.ui.media.PhotoSourceSheet
import com.fabriziogo.epona.core.ui.media.rememberMediaPickerState
import com.fabriziogo.epona.feature.profile.components.ProfileHeader
import com.fabriziogo.epona.feature.profile.components.SettingsItem
import com.fabriziogo.epona.feature.profile.components.SettingsSection
import com.fabriziogo.epona.feature.profile.components.StatsRow
import com.fabriziogo.epona.core.ui.components.EponaConfirmDialog
import com.fabriziogo.epona.core.ui.components.EponaLargeTopAppBar
import com.fabriziogo.epona.core.ui.components.LoadingIndicator
import com.fabriziogo.epona.core.ui.theme.EponaTypography
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
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
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState()
    )

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

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            EponaLargeTopAppBar(
                title = stringResource(R.string.profile_title),
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        if (state.isLoading) {
            LoadingIndicator(modifier = Modifier.padding(padding))
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // Avatar + name + email
            ProfileHeader(
                displayName = state.user?.displayName ?: stringResource(R.string.profile_default_name),
                email = state.user?.email ?: "",
                avatarUrl = state.user?.avatarUrl,
                previewUri = state.avatarPreviewUri,
                isUploading = state.isUploadingAvatar,
                onAvatarClick = avatarPicker::open
            )

            Spacer(Modifier.height(20.dp))

            // Stats cards
            StatsRow(stats = state.stats)

            Spacer(Modifier.height(24.dp))

            // My Stuff section
            SettingsSection(title = stringResource(R.string.profile_section_my_stuff)) {
                SettingsItem(
                    icon = Icons.Outlined.Pets,
                    label = stringResource(R.string.profile_my_pets),
                    subtitle = stringResource(R.string.profile_pets_registered, state.stats.totalPets),
                    onClick = { viewModel.onEvent(ProfileEvent.MyPetsClicked) }
                )
                SettingsItem(
                    icon = Icons.Outlined.Campaign,
                    label = stringResource(R.string.profile_my_alerts),
                    subtitle = stringResource(R.string.profile_alerts_active, state.stats.activeAlerts),
                    onClick = { viewModel.onEvent(ProfileEvent.MyAlertsClicked) }
                )
            }

            Spacer(Modifier.height(16.dp))

            // App section
            SettingsSection(title = stringResource(R.string.profile_section_app)) {
                SettingsItem(
                    icon = Icons.Outlined.Settings,
                    label = stringResource(R.string.profile_settings),
                    subtitle = stringResource(R.string.profile_settings_subtitle),
                    onClick = { viewModel.onEvent(ProfileEvent.SettingsClicked) }
                )
                SettingsItem(
                    icon = Icons.AutoMirrored.Outlined.HelpOutline,
                    label = stringResource(R.string.profile_help),
                    onClick = { viewModel.onEvent(ProfileEvent.HelpClicked) }
                )
            }

            Spacer(Modifier.height(16.dp))

            // Account section
            SettingsSection(title = stringResource(R.string.profile_section_account)) {
                SettingsItem(
                    icon = Icons.AutoMirrored.Outlined.Logout,
                    label = stringResource(R.string.auth_sign_out),
                    isDestructive = true,
                    showChevron = false,
                    onClick = { viewModel.onEvent(ProfileEvent.SignOutClicked) }
                )
            }

            Spacer(Modifier.height(24.dp))

            // App version
            androidx.compose.material3.Text(
                text = stringResource(R.string.profile_version, BuildConfig.VERSION_NAME),
                style = EponaTypography.bodySmall,
                color = androidx.compose.material3.MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview(){
    ProfileScreen(
        onNavigateToMyPets = {},
        onNavigateToMyAlerts = {},
        onNavigateToSettings = {},
        onNavigateToHelp = {},
        onNavigateToAuth = {}
    )
}
