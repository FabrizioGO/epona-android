package com.fabriziogo.epona.core.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.ui.theme.EponaTheme

/**
 * A pill-shaped inline search field. Filters the caller's list as [value] changes.
 */
@Composable
fun EponaSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = stringResource(R.string.home_search_placeholder),
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = MaterialTheme.shapes.extraLarge,
        color = containerColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyLarge,
                        color = contentColor
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    // The placeholder is a separate node, so name the field itself.
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = placeholder },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = contentColor),
                    singleLine = true,
                    cursorBrush = SolidColor(contentColor),
                    interactionSource = remember { MutableInteractionSource() }
                )
            }

            if (value.isNotEmpty()) {
                // IconButton gives the clear action its 48dp touch target.
                IconButton(onClick = { onValueChange("") }) {
                    Icon(
                        Icons.Filled.Clear,
                        contentDescription = stringResource(R.string.cd_clear_search),
                        tint = contentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Decorative: the field above already carries the label.
            Icon(
                Icons.Filled.Search,
                contentDescription = null,
                tint = contentColor
            )
        }
    }
}

@Preview
@Composable
private fun EponaSearchBarPreview() {
    EponaTheme(dynamicColor = false) {
        EponaSearchBar(value = "", onValueChange = {})
    }
}

@Preview
@Composable
private fun EponaSearchBarFilledPreview() {
    EponaTheme(dynamicColor = false) {
        EponaSearchBar(value = "beagle", onValueChange = {})
    }
}
