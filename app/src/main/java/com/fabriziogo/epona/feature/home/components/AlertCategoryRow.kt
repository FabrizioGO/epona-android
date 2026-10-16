package com.fabriziogo.epona.feature.home.components

import com.fabriziogo.epona.core.ui.preview.EponaPreviews
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.ui.theme.EponaTheme
import com.fabriziogo.epona.feature.home.AlertFilter

private data class Category(
    val filter: AlertFilter,
    val icon: ImageVector,
    val label: Int,
    val count: Int
)

/**
 * All / Lost / Found selector for the home hero, driven by [AlertFilter]. Colors are derived
 * from the theme so it reads on the hero's `primaryContainer` surface.
 */
@Composable
fun AlertCategoryRow(
    selectedFilter: AlertFilter,
    totalCount: Int,
    lostCount: Int,
    foundCount: Int,
    onFilterSelected: (AlertFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf(
        Category(AlertFilter.ALL, Icons.Outlined.Pets, R.string.home_filter_all, totalCount),
        Category(AlertFilter.LOST, Icons.Outlined.Search, R.string.home_filter_lost, lostCount),
        Category(AlertFilter.FOUND, Icons.Outlined.CheckCircle, R.string.home_filter_found, foundCount)
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
                icon = {
                    SegmentedButtonDefaults.Icon(active = selected) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = null,
                            modifier = Modifier.size(SegmentedButtonDefaults.IconSize)
                        )
                    }
                },
                label = {
                    Text(
                        text = "${stringResource(category.label)} · ${category.count}",
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
                onFilterSelected = {}
            )
        }
    }
}
