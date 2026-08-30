package com.fabriziogo.epona.feature.profile.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fabriziogo.epona.BuildConfig
import com.fabriziogo.epona.R
import com.fabriziogo.epona.feature.profile.components.SettingsSection
import com.fabriziogo.epona.feature.profile.settings.components.RadiusSlider
import com.fabriziogo.epona.core.ui.components.EponaFilledButton
import com.fabriziogo.epona.core.ui.components.EponaTopAppBar
import com.fabriziogo.epona.core.ui.theme.EponaTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onErrorDismissed()
        }
    }

    Scaffold(
        topBar = {
            EponaTopAppBar(
                title = stringResource(R.string.settings_title),
                onBackClick = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // Alert Radius
            SettingsSection(title = stringResource(R.string.settings_radius_title)) {
                RadiusSlider(
                    radiusKm = state.alertRadiusKm,
                    onRadiusChanged = { viewModel.onRadiusChanged(it) }
                )
                EponaFilledButton(
                    text = stringResource(R.string.settings_radius_save),
                    onClick = { viewModel.onRadiusSaved() },
                    loading = state.isSaving,
                    fullWidth = true
                )
            }

            Spacer(Modifier.height(24.dp))

            // Notifications
            SettingsSection(title = stringResource(R.string.settings_notifications_title)) {
                ListItem(
                    headlineContent = {
                        Text(
                            stringResource(R.string.settings_push_title),
                            style = EponaTypography.bodyLarge
                        )
                    },
                    supportingContent = {
                        Text(
                            stringResource(R.string.settings_push_desc),
                            style = EponaTypography.bodySmall
                        )
                    },
                    trailingContent = {
                        Switch(
                            checked = state.notificationsEnabled,
                            onCheckedChange = { viewModel.onNotificationsToggle(it) }
                        )
                    }
                )
            }

            Spacer(Modifier.height(24.dp))

            // About
            SettingsSection(title = stringResource(R.string.settings_about_title)) {
                ListItem(
                    headlineContent = {
                        Text(stringResource(R.string.settings_version), style = EponaTypography.bodyLarge)
                    },
                    trailingContent = {
                        Text(
                            BuildConfig.VERSION_NAME,
                            style = EponaTypography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
                ListItem(
                    headlineContent = {
                        Text(stringResource(R.string.settings_privacy), style = EponaTypography.bodyLarge)
                    }
                )
                ListItem(
                    headlineContent = {
                        Text(stringResource(R.string.settings_terms), style = EponaTypography.bodyLarge)
                    }
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
