package com.fabriziogo.epona.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.ui.extensions.pressScale
import com.fabriziogo.epona.core.ui.theme.EponaAmber
import com.fabriziogo.epona.core.ui.theme.EponaTeal
import com.fabriziogo.epona.core.ui.theme.EponaTheme
import com.fabriziogo.epona.core.ui.theme.EponaTypography
import com.fabriziogo.epona.feature.home.AlertFilter

private data class Category(
    val filter: AlertFilter,
    val icon: ImageVector,
    val label: Int,
    val count: Int
)

/**
 * Circular category selector for the home hero — All / Lost / Found, driven by [AlertFilter].
 * Always rendered on the dark hero background, so colors are hardcoded to white rather than
 * theme-derived.
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

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        categories.forEach { category ->
            val selected = category.filter == selectedFilter
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(if (selected) MaterialTheme.colorScheme.secondary else Color.White.copy(alpha = 0.12f))
                        .pressScale(onClick = { onFilterSelected(category.filter) }),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = stringResource(category.label),
                        tint = Color.White
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(category.label),
                    style = EponaTypography.labelMedium,
                    color = Color.White.copy(alpha = if (selected) 1f else 0.7f)
                )
                Text(
                    text = category.count.toString(),
                    style = EponaTypography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F4F44)
@Composable
private fun AlertCategoryRowPreview() {
    EponaTheme(dynamicColor = false) {
        AlertCategoryRow(
            selectedFilter = AlertFilter.ALL,
            totalCount = 12,
            lostCount = 8,
            foundCount = 4,
            onFilterSelected = {}
        )
    }
}
