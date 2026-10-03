package com.fabriziogo.epona.feature.profile.myalerts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.domain.model.Alert
import com.fabriziogo.epona.core.domain.model.AlertStatus
import com.fabriziogo.epona.core.domain.model.AlertType
import com.fabriziogo.epona.core.domain.model.AlertWithDetails
import com.fabriziogo.epona.core.domain.model.Location
import com.fabriziogo.epona.core.domain.model.Pet
import com.fabriziogo.epona.core.domain.model.Species
import com.fabriziogo.epona.core.ui.components.EmptyState
import com.fabriziogo.epona.core.ui.components.EponaTopAppBar
import com.fabriziogo.epona.core.ui.components.LoadingIndicator
import com.fabriziogo.epona.core.ui.theme.EponaTheme
import com.fabriziogo.epona.feature.profile.myalerts.components.MyAlertCard

@Composable
fun MyAlertsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MyAlertsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    MyAlertsContent(
        state = state,
        onNavigateBack = onNavigateBack,
        onNavigateToDetail = onNavigateToDetail,
        onErrorDismissed = viewModel::onErrorDismissed,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MyAlertsContent(
    state: MyAlertsUiState,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onErrorDismissed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            onErrorDismissed()
        }
    }

    Scaffold(
        topBar = {
            EponaTopAppBar(
                title = stringResource(R.string.profile_my_alerts),
                onBackClick = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when {
            state.isLoading -> {
                LoadingIndicator(modifier = Modifier.padding(padding))
            }

            state.alerts.isEmpty() -> {
                EmptyState(
                    icon = Icons.Outlined.Campaign,
                    title = stringResource(R.string.my_alerts_empty_title),
                    description = stringResource(R.string.my_alerts_empty_desc),
                    modifier = Modifier.padding(padding)
                )
            }

            else -> {
                LazyColumn(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = state.alerts,
                        key = { it.alert.id }
                    ) { item ->
                        MyAlertCard(
                            alertWithDetails = item,
                            onClick = { onNavigateToDetail(item.alert.id) }
                        )
                    }
                }
            }
        }
    }
}

private val previewAlerts = listOf(
    AlertWithDetails(
        alert = Alert(
            id = "1",
            type = AlertType.LOST,
            lastSeenLocation = Location(40.4, -3.7, "Retiro Park, Madrid"),
            lastSeenAddress = "Retiro Park, Madrid",
            sightingCount = 3,
            createdAt = System.currentTimeMillis() - 2 * 60 * 60 * 1000L
        ),
        pet = Pet(id = "p1", name = "Luna", species = Species.DOG, breed = "Labrador"),
        ownerName = "Fabrizio"
    ),
    AlertWithDetails(
        alert = Alert(
            id = "2",
            type = AlertType.FOUND,
            status = AlertStatus.RESOLVED,
            lastSeenLocation = Location(40.41, -3.69, "Calle Alcalá, Madrid"),
            lastSeenAddress = "Calle Alcalá, Madrid",
            createdAt = System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000L
        ),
        pet = Pet(id = "p2", name = "Milo", species = Species.CAT),
        ownerName = "Fabrizio"
    )
)

@Preview(showBackground = true, name = "Loading")
@Composable
private fun MyAlertsContentLoadingPreview() {
    EponaTheme {
        MyAlertsContent(
            state = MyAlertsUiState(isLoading = true),
            onNavigateBack = {},
            onNavigateToDetail = {},
            onErrorDismissed = {}
        )
    }
}

@Preview(showBackground = true, name = "Empty")
@Composable
private fun MyAlertsContentEmptyPreview() {
    EponaTheme {
        MyAlertsContent(
            state = MyAlertsUiState(isLoading = false),
            onNavigateBack = {},
            onNavigateToDetail = {},
            onErrorDismissed = {}
        )
    }
}

@Preview(showBackground = true, name = "With alerts")
@Composable
private fun MyAlertsContentListPreview() {
    EponaTheme {
        MyAlertsContent(
            state = MyAlertsUiState(isLoading = false, alerts = previewAlerts),
            onNavigateBack = {},
            onNavigateToDetail = {},
            onErrorDismissed = {}
        )
    }
}
