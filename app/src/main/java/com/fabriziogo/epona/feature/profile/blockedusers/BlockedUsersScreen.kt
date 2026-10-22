@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.fabriziogo.epona.feature.profile.blockedusers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.domain.model.BlockedUser
import com.fabriziogo.epona.core.ui.components.EmptyState
import com.fabriziogo.epona.core.ui.components.EponaAvatar
import com.fabriziogo.epona.core.ui.components.EponaTopAppBar
import com.fabriziogo.epona.core.ui.components.FullScreenProgress
import com.fabriziogo.epona.core.ui.text.ErrorSnackbarEffect
import com.fabriziogo.epona.core.ui.theme.EponaTheme

@Composable
fun BlockedUsersScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BlockedUsersViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    ErrorSnackbarEffect(state.error, snackbarHostState) {
        viewModel.onEvent(BlockedUsersEvent.ErrorDismissed)
    }

    BlockedUsersContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        onUnblock = { viewModel.onEvent(BlockedUsersEvent.UnblockClicked(it)) },
        modifier = modifier
    )
}

@Composable
private fun BlockedUsersContent(
    state: BlockedUsersUiState,
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    onUnblock: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            EponaTopAppBar(
                title = stringResource(R.string.blocked_users_title),
                onBackClick = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> FullScreenProgress()

                state.users.isEmpty() -> EmptyState(
                    icon = Icons.Outlined.Block,
                    title = stringResource(R.string.blocked_users_empty_title),
                    description = stringResource(R.string.blocked_users_empty_description),
                    modifier = Modifier.align(Alignment.Center)
                )

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(state.users, key = { it.id }) { user ->
                        BlockedUserRow(
                            user = user,
                            isUnblocking = user.id in state.unblocking,
                            onUnblock = { onUnblock(user.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BlockedUserRow(
    user: BlockedUser,
    isUnblocking: Boolean,
    onUnblock: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        EponaAvatar(url = user.avatarUrl, name = user.displayName, size = 44.dp)
        Text(
            text = user.displayName.ifBlank { stringResource(R.string.blocked_users_unknown_name) },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
        )
        TextButton(onClick = onUnblock, enabled = !isUnblocking) {
            Text(stringResource(R.string.blocked_users_unblock))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BlockedUsersContentPreview() {
    EponaTheme(dynamicColor = false) {
        BlockedUsersContent(
            state = BlockedUsersUiState(
                isLoading = false,
                users = listOf(BlockedUser("1", "Ana"), BlockedUser("2", "Luis"))
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onNavigateBack = {},
            onUnblock = {}
        )
    }
}
