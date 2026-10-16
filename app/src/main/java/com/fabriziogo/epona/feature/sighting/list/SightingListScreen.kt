@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.fabriziogo.epona.feature.sighting.list

import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.fabriziogo.epona.core.ui.layout.contentGutter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.ui.components.EmptyState
import com.fabriziogo.epona.core.ui.components.EponaTopAppBar
import com.fabriziogo.epona.core.ui.components.FullScreenProgress
import com.fabriziogo.epona.core.ui.components.SightingCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SightingListScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SightingListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Pinned bar: picks up its tonal 'scrolled' color as the content scrolls under it.

    val topBarScroll = TopAppBarDefaults.pinnedScrollBehavior()


    Scaffold(
        modifier = Modifier.nestedScroll(topBarScroll.nestedScrollConnection),
        topBar = {
            EponaTopAppBar(
                scrollBehavior = topBarScroll,
                title = stringResource(R.string.sighting_list_title, state.sightings.size),
                onBackClick = onNavigateBack
            )
        }
    ) { padding ->
        when {
            state.isLoading -> {
                FullScreenProgress(modifier = Modifier.padding(padding))
            }

            state.sightings.isEmpty() -> {
                EmptyState(
                    icon = Icons.Outlined.Visibility,
                    title = stringResource(R.string.sighting_empty_title),
                    description = stringResource(R.string.sighting_empty_desc),
                    modifier = Modifier.padding(padding)
                )
            }

            else -> {
                LazyColumn(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(
                        horizontal = 16.dp + contentGutter(),
                        vertical = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = state.sightings,
                        key = { it.sighting.id }
                    ) { sighting ->
                        SightingCard(sighting = sighting)
                    }
                }
            }
        }
    }
}