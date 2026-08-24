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
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fabriziogo.epona.feature.profile.components.ProfileHeader
import com.fabriziogo.epona.feature.profile.components.SettingsItem
import com.fabriziogo.epona.feature.profile.components.SettingsSection
import com.fabriziogo.epona.feature.profile.components.StatsRow
import com.fabriziogo.epona.core.ui.components.EponaConfirmDialog
import com.fabriziogo.epona.core.ui.components.EponaLargeTopAppBar
import com.fabriziogo.epona.core.ui.components.LoadingIndicator
import com.fabriziogo.epona.core.ui.theme.EponaTypography

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
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState()
    )

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
            title = "Sign out?",
            message = "You'll need to sign in again to access your alerts and pets.",
            confirmText = "Sign Out",
            dismissText = "Cancel",
            onConfirm = { viewModel.onEvent(ProfileEvent.SignOutConfirmed) },
            onDismiss = { viewModel.onEvent(ProfileEvent.SignOutDismissed) }
        )
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            EponaLargeTopAppBar(
                title = "Profile",
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
                displayName = state.user?.displayName ?: "User",
                email = state.user?.email ?: "",
                avatarUrl = state.user?.avatarUrl
            )

            Spacer(Modifier.height(20.dp))

            // Stats cards
            StatsRow(stats = state.stats)

            Spacer(Modifier.height(24.dp))

            // My Stuff section
            SettingsSection(title = "My Stuff") {
                SettingsItem(
                    icon = Icons.Outlined.Pets,
                    label = "My Pets",
                    subtitle = "${state.stats.totalPets} registered",
                    onClick = { viewModel.onEvent(ProfileEvent.MyPetsClicked) }
                )
                SettingsItem(
                    icon = Icons.Outlined.Campaign,
                    label = "My Alerts",
                    subtitle = "${state.stats.activeAlerts} active",
                    onClick = { viewModel.onEvent(ProfileEvent.MyAlertsClicked) }
                )
            }

            Spacer(Modifier.height(16.dp))

            // App section
            SettingsSection(title = "App") {
                SettingsItem(
                    icon = Icons.Outlined.Settings,
                    label = "Settings",
                    subtitle = "Notifications, radius, preferences",
                    onClick = { viewModel.onEvent(ProfileEvent.SettingsClicked) }
                )
                SettingsItem(
                    icon = Icons.AutoMirrored.Outlined.HelpOutline,
                    label = "Help & Support",
                    onClick = { viewModel.onEvent(ProfileEvent.HelpClicked) }
                )
            }

            Spacer(Modifier.height(16.dp))

            // Account section
            SettingsSection(title = "Account") {
                SettingsItem(
                    icon = Icons.AutoMirrored.Outlined.Logout,
                    label = "Sign Out",
                    isDestructive = true,
                    showChevron = false,
                    onClick = { viewModel.onEvent(ProfileEvent.SignOutClicked) }
                )
            }

            Spacer(Modifier.height(24.dp))

            // App version
            androidx.compose.material3.Text(
                text = "Epona v1.0.0",
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
