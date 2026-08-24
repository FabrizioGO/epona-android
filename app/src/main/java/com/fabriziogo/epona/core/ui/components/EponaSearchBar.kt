package com.fabriziogo.epona.core.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fabriziogo.epona.core.ui.theme.EponaTypography

@Composable
fun EponaSearchBar(
    placeholder: String = "Search pets, breeds, locations...",
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onFilterClick: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = placeholder,
                style = EponaTypography.bodyLarge,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            )
            onFilterClick?.let {
                Icon(
                    Icons.Filled.Settings,
                    contentDescription = "Filters",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable(onClick = it)
                )
            }
        }
    }
}

@Preview
@Composable
fun EponaSearchBarPreview() {
    EponaSearchBar(onClick = {})
}
