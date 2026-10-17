package com.fabriziogo.epona.feature.home.components

import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.heading
import com.fabriziogo.epona.core.ui.preview.EponaPreviews
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.ui.theme.EponaTheme

/**
 * Section header for the nearby-alerts grid. [horizontalPadding] should match the grid's
 * content gutter so the title lines up with the first card column.
 */
@Composable
fun AlertFeedSection(
    onViewMapClick: () -> Unit,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 16.dp,
    showViewMap: Boolean = true,
    title: String = stringResource(
        if (showViewMap) R.string.home_nearby_alerts else R.string.home_adoption_section
    )
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            // Same height with or without the map button, so switching segments does not jump.
            .heightIn(min = 48.dp)
            // TextButton carries its own 12dp inner padding; trim the trailing gutter so the
            // arrow still lines up with the grid's right edge.
            .padding(start = horizontalPadding, end = (horizontalPadding - 12.dp).coerceAtLeast(0.dp)),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.semantics { heading() }
        )
        if (showViewMap) {
            TextButton(
                onClick = onViewMapClick,
                contentPadding = ButtonDefaults.TextButtonWithIconContentPadding
            ) {
                Text(text = stringResource(R.string.home_view_map))
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
            }
        }
    }
}

@EponaPreviews
@Composable
private fun AlertFeedSectionPreview() {
    EponaTheme(dynamicColor = false) {
        AlertFeedSection(onViewMapClick = {})
    }
}
