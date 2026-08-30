package com.fabriziogo.epona.feature.detail

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fabriziogo.epona.feature.detail.components.DetailActionButtons
import com.fabriziogo.epona.feature.detail.components.DetailDescription
import com.fabriziogo.epona.feature.detail.components.DetailHero
import com.fabriziogo.epona.feature.detail.components.DetailInfoCards
import com.fabriziogo.epona.feature.detail.components.DetailMapPreview
import com.fabriziogo.epona.feature.detail.components.DetailSightingsSection
import com.fabriziogo.epona.feature.detail.components.ResolveDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.domain.model.AlertStatus
import com.fabriziogo.epona.core.domain.model.AlertType
import com.fabriziogo.epona.core.domain.model.AlertWithDetails
import com.fabriziogo.epona.core.ui.components.AlertStatusBadge
import com.fabriziogo.epona.core.ui.components.AlertTypeBadge
import com.fabriziogo.epona.core.ui.components.EmptyState
import com.fabriziogo.epona.core.ui.components.EponaFilledButton
import com.fabriziogo.epona.core.ui.components.LoadingIndicator
import com.fabriziogo.epona.core.ui.components.RewardBadge
import com.fabriziogo.epona.core.ui.theme.EponaTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToReportSighting: (String) -> Unit,
    onNavigateToMap: (Double, Double) -> Unit,
    onShareAlert: (String, String) -> Unit,
    onDialPhone: (String) -> Unit,
    confirmationMessage: String?,
    onConfirmationShown: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AlertDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.navEvents.collect { event ->
            when (event) {
                DetailNavEvent.NavigateBack -> onNavigateBack()
                is DetailNavEvent.NavigateToReportSighting ->
                    onNavigateToReportSighting(event.alertId)
                is DetailNavEvent.NavigateToMap ->
                    onNavigateToMap(event.lat, event.lng)
                is DetailNavEvent.ShareAlert ->
                    onShareAlert(event.text, event.url)
                is DetailNavEvent.DialPhone ->
                    onDialPhone(event.phone)
            }
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(AlertDetailEvent.ErrorDismissed)
        }
    }

    // Confirmation handed back by the report-sighting screen, which otherwise just pops.
    LaunchedEffect(confirmationMessage) {
        confirmationMessage?.let {
            snackbarHostState.showSnackbar(it)
            onConfirmationShown()
        }
    }

    // Resolve confirmation dialog
    if (state.showResolveDialog) {
        ResolveDialog(
            petName = state.alertDetail?.pet?.name ?: stringResource(R.string.resolve_default_pet_name),
            onConfirm = { viewModel.onEvent(AlertDetailEvent.ResolveConfirmed) },
            onDismiss = { viewModel.onEvent(AlertDetailEvent.ResolveDismissed) }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        when {
            state.isLoading -> {
                LoadingIndicator(modifier = Modifier.padding(paddingValues))
            }

            state.alertDetail == null && !state.isLoading -> {
                EmptyState(
                    icon = Icons.Outlined.Warning,
                    title = stringResource(R.string.detail_not_found_title),
                    description = stringResource(R.string.detail_not_found_desc),
                    modifier = Modifier.padding(paddingValues),
                    action = {
                        EponaFilledButton(
                            text = stringResource(R.string.action_go_back),
                            onClick = { viewModel.onEvent(AlertDetailEvent.BackClicked) }
                        )
                    }
                )
            }

            else -> {
                val detail = state.alertDetail!!
                val alert = detail.alert
                val pet = detail.pet
                val isResolved = alert.status == AlertStatus.RESOLVED

                LazyColumn(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    // Hero image with back/share buttons
                    item(key = "hero") {
                        DetailHero(
                            pet = pet,
                            alertType = alert.type,
                            onBackClick = {
                                viewModel.onEvent(AlertDetailEvent.BackClicked)
                            },
                            onShareClick = {
                                viewModel.onEvent(AlertDetailEvent.ShareClicked)
                            }
                        )
                    }

                    // Pet name, breed, status badge, reward
                    item(key = "header") {
                        DetailHeaderContent(
                            detail = detail,
                            isResolved = isResolved
                        )
                    }

                    // Last seen location + time cards
                    item(key = "info_cards") {
                        DetailInfoCards(
                            lastSeenAddress = alert.lastSeenAddress ?: stringResource(R.string.detail_address_unknown),
                            lastSeenAt = alert.lastSeenAt,
                            sightingCount = alert.sightingCount,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }

                    // Map preview
                    item(key = "map") {
                        DetailMapPreview(
                            latitude = alert.lastSeenLocation.latitude,
                            longitude = alert.lastSeenLocation.longitude,
                            onClick = {
                                viewModel.onEvent(AlertDetailEvent.ViewOnMapClicked)
                            },
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }

                    // Description
                    if (alert.description != null || pet.description != null) {
                        item(key = "description") {
                            DetailDescription(
                                alertDescription = alert.description,
                                petDescription = pet.description,
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }
                    }

                    // Sightings trail
                    item(key = "sightings") {
                        DetailSightingsSection(
                            sightings = state.sightings,
                            isLoading = state.isSightingsLoading,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }

                    // Action buttons
                    item(key = "actions") {
                        DetailActionButtons(
                            isOwner = state.isCurrentUserOwner,
                            isResolved = isResolved,
                            isLost = alert.type == AlertType.LOST,
                            isResolving = state.isResolving,
                            onContactClick = {
                                viewModel.onEvent(AlertDetailEvent.ContactOwnerClicked)
                            },
                            onReportSightingClick = {
                                viewModel.onEvent(AlertDetailEvent.ReportSightingClicked)
                            },
                            onResolveClick = {
                                viewModel.onEvent(AlertDetailEvent.ResolveClicked)
                            },
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                }
            }
        }
    }
}


@Composable
internal fun DetailHeaderContent(
    detail: AlertWithDetails,
    isResolved: Boolean,
    modifier: Modifier = Modifier
) {
    val alert = detail.alert
    val pet = detail.pet

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Name + badges row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = pet.name,
                    style = EponaTypography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (isResolved) {
                    AlertStatusBadge(status = AlertStatus.RESOLVED)
                } else {
                    AlertTypeBadge(type = alert.type)
                }
            }

            alert.reward?.let { reward ->
                if (reward > 0 && !isResolved) {
                    RewardBadge(amount = reward)
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        // Breed, size, color
        Text(
            text = listOfNotNull(
                pet.breed,
                pet.size.label,
                pet.color
            ).joinToString(" · "),
            style = EponaTypography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Owner info
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.detail_posted_by, detail.ownerName),
            style = EponaTypography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AlertDetailScreenPreview() {
    AlertDetailScreen(
        onNavigateBack = {},
        onNavigateToReportSighting = {},
        onNavigateToMap = { _, _ -> },
        onShareAlert = { _, _ -> },
        onDialPhone = {},
        confirmationMessage = null,
        onConfirmationShown = {}
    )
}
