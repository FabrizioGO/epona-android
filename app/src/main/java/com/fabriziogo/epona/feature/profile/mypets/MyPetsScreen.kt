package com.fabriziogo.epona.feature.profile.mypets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Pets
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fabriziogo.epona.R
import com.fabriziogo.epona.feature.profile.mypets.components.MyPetCard
import com.fabriziogo.epona.core.ui.components.EmptyState
import com.fabriziogo.epona.core.ui.components.EponaConfirmDialog
import com.fabriziogo.epona.core.ui.components.EponaFAB
import com.fabriziogo.epona.core.ui.components.EponaFilledButton
import com.fabriziogo.epona.core.ui.components.EponaTopAppBar
import com.fabriziogo.epona.core.ui.components.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPetsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddPet: () -> Unit,
    onNavigateToEditPet: (String) -> Unit,
    confirmationMessage: String?,
    onConfirmationShown: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MyPetsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onErrorDismissed()
        }
    }

    // Confirmation handed back by the add/edit screens, which otherwise just pop.
    LaunchedEffect(confirmationMessage) {
        confirmationMessage?.let {
            snackbarHostState.showSnackbar(it)
            onConfirmationShown()
        }
    }

    if (state.showDeleteDialog) {
        EponaConfirmDialog(
            title = stringResource(R.string.my_pets_delete_title, state.petToDelete?.name ?: ""),
            message = stringResource(R.string.my_pets_delete_message),
            confirmText = stringResource(R.string.action_delete),
            dismissText = stringResource(R.string.action_cancel),
            onConfirm = { viewModel.onDeleteConfirmed() },
            onDismiss = { viewModel.onDeleteDismissed() }
        )
    }

    Scaffold(
        topBar = {
            EponaTopAppBar(
                title = stringResource(R.string.my_pets_title),
                onBackClick = onNavigateBack
            )
        },
        floatingActionButton = {
            EponaFAB(
                onClick = onNavigateToAddPet,
                icon = Icons.Filled.Add,
                contentDescription = stringResource(R.string.my_pets_add)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when {
            state.isLoading -> {
                LoadingIndicator(modifier = Modifier.padding(padding))
            }

            state.pets.isEmpty() -> {
                EmptyState(
                    icon = Icons.Outlined.Pets,
                    title = stringResource(R.string.my_pets_empty_title),
                    description = stringResource(R.string.my_pets_empty_desc),
                    modifier = Modifier.padding(padding),
                    action = {
                        EponaFilledButton(
                            text = stringResource(R.string.create_add_pet),
                            onClick = onNavigateToAddPet,
                            icon = Icons.Filled.Add
                        )
                    }
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
                        items = state.pets,
                        key = { it.id }
                    ) { pet ->
                        MyPetCard(
                            pet = pet,
                            onEditClick = { onNavigateToEditPet(pet.id) },
                            onDeleteClick = { viewModel.onDeleteRequest(pet) }
                        )
                    }
                }
            }
        }
    }
}
