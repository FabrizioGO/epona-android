package com.fabriziogo.epona.core.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.ui.theme.EponaTheme
import com.fabriziogo.epona.core.ui.theme.EponaTypography

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
                        style = EponaTypography.bodyLarge,
                        color = contentColor.copy(alpha = 0.7f)
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = EponaTypography.bodyLarge.copy(color = contentColor),
                    singleLine = true,
                    cursorBrush = SolidColor(contentColor),
                    interactionSource = remember { MutableInteractionSource() }
                )
            }

            if (value.isNotEmpty()) {
                Icon(
                    Icons.Filled.Clear,
                    contentDescription = stringResource(R.string.cd_clear_search),
                    tint = contentColor,
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .clickable(onClick = { onValueChange("") })
                )
                Spacer(Modifier.size(12.dp))
            }

            Icon(
                Icons.Filled.Search,
                contentDescription = stringResource(R.string.action_search),
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
