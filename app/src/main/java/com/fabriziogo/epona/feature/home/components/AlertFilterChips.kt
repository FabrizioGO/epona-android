package com.fabriziogo.epona.feature.home.components


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fabriziogo.epona.R
import com.fabriziogo.epona.feature.home.AlertFilter
import com.fabriziogo.epona.core.ui.components.EponaFilterChip

@Composable
fun AlertFilterChips(
    selectedFilter: AlertFilter,
    lostCount: Int,
    foundCount: Int,
    totalCount: Int,
    onFilterSelected: (AlertFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        EponaFilterChip(
            label = stringResource(R.string.home_filter_all_count, totalCount),
            selected = selectedFilter == AlertFilter.ALL,
            onClick = { onFilterSelected(AlertFilter.ALL) }
        )
        EponaFilterChip(
            label = stringResource(R.string.home_filter_lost_count, lostCount),
            selected = selectedFilter == AlertFilter.LOST,
            onClick = { onFilterSelected(AlertFilter.LOST) }
        )
        EponaFilterChip(
            label = stringResource(R.string.home_filter_found_count, foundCount),
            selected = selectedFilter == AlertFilter.FOUND,
            onClick = { onFilterSelected(AlertFilter.FOUND) }
        )
    }
}