package com.fabriziogo.epona.feature.home.components

import com.fabriziogo.epona.core.ui.preview.EponaPreviews
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.ui.theme.EponaTheme
import com.fabriziogo.epona.feature.home.AlertFilter

private data class Category(
    val filter: AlertFilter,
    val label: Int,
    val count: Int
)

/**
 * All / Lost / Found / Adoption selector for the home hero, driven by [AlertFilter]. Colors are
 * derived from the theme so it reads on the hero's `primaryContainer` surface.
 *
 * Four segments leave about 80dp each on a 360dp phone, so only the selected segment spells out
 * its count and the icon slot carries just the selection check.
 */
@Composable
fun AlertCategoryRow(
    selectedFilter: AlertFilter,
    totalCount: Int,
    lostCount: Int,
    foundCount: Int,
    adoptionCount: Int,
    onFilterSelected: (AlertFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf(
        Category(AlertFilter.ALL, R.string.home_filter_all, totalCount),
        Category(AlertFilter.LOST, R.string.home_filter_lost, lostCount),
        Category(AlertFilter.FOUND, R.string.home_filter_found, foundCount),
        Category(AlertFilter.ADOPTION, R.string.home_filter_adoption, adoptionCount)
    )

    val onHero = MaterialTheme.colorScheme.onPrimaryContainer
    val colors = SegmentedButtonDefaults.colors(
        activeContainerColor = MaterialTheme.colorScheme.secondaryContainer,
        activeContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        activeBorderColor = onHero.copy(alpha = 0.38f),
        inactiveContainerColor = Color.Transparent,
        inactiveContentColor = onHero,
        inactiveBorderColor = onHero.copy(alpha = 0.38f)
    )

    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        categories.forEachIndexed { index, category ->
            val selected = category.filter == selectedFilter
            SegmentedButton(
                selected = selected,
                onClick = { onFilterSelected(category.filter) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = categories.size),
                colors = colors,
                icon = { SegmentedButtonDefaults.Icon(active = selected) },
                label = {
                    val name = stringResource(category.label)
                    Text(
                        text = if (selected) "$name · ${category.count}" else name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
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
                totalCount = 12,
                lostCount = 8,
                foundCount = 4,
                adoptionCount = 3,
                onFilterSelected = {}
            )
        }
    }
}
