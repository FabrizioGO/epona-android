package com.fabriziogo.epona.feature.home.components

import com.fabriziogo.epona.core.ui.preview.EponaPreviews
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.ui.theme.EponaTheme
import com.fabriziogo.epona.feature.home.AlertFilter

private data class Category(
    val filter: AlertFilter,
    val label: Int
)

private val categories = listOf(
    Category(AlertFilter.ALL, R.string.home_filter_all),
    Category(AlertFilter.LOST, R.string.home_filter_lost),
    Category(AlertFilter.FOUND, R.string.home_filter_found),
    Category(AlertFilter.ADOPTION, R.string.home_filter_adoption)
)

/**
 * All / Lost / Found / Adoption selector for the home hero, driven by [AlertFilter]. Colors are
 * derived from the theme so it reads on the hero's `primaryContainer` surface.
 *
 * Chips size to their label and the row scrolls horizontally, so longer translations or larger
 * font scales never truncate. [contentPadding] keeps the chips aligned with the hero content at
 * rest while letting them scroll edge to edge.
 */
@Composable
fun AlertCategoryRow(
    selectedFilter: AlertFilter,
    onFilterSelected: (AlertFilter) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val onHero = MaterialTheme.colorScheme.onPrimaryContainer
    val colors = FilterChipDefaults.filterChipColors(
        containerColor = Color.Transparent,
        labelColor = onHero,
        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(contentPadding)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            val selected = category.filter == selectedFilter
            FilterChip(
                selected = selected,
                onClick = { onFilterSelected(category.filter) },
                label = { Text(stringResource(category.label)) },
                colors = colors,
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selected,
                    borderColor = onHero.copy(alpha = 0.38f),
                    selectedBorderColor = Color.Transparent
                )
            )
        }
    }
}

@EponaPreviews
@Composable
private fun AlertCategoryRowPreview() {
    EponaTheme(dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.primaryContainer) {
            AlertCategoryRow(
                selectedFilter = AlertFilter.ALL,
                onFilterSelected = {},
                contentPadding = PaddingValues(horizontal = 20.dp)
            )
        }
    }
}

@EponaPreviews
@Composable
private fun AlertCategoryRowAdoptionPreview() {
    EponaTheme(dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.primaryContainer) {
            AlertCategoryRow(
                selectedFilter = AlertFilter.ADOPTION,
                onFilterSelected = {},
                contentPadding = PaddingValues(horizontal = 20.dp)
            )
        }
    }
}
