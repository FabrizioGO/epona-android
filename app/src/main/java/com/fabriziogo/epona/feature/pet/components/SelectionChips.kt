package com.fabriziogo.epona.feature.pet.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fabriziogo.epona.core.domain.model.Species
import com.fabriziogo.epona.core.ui.components.EponaFilterChip
import com.fabriziogo.epona.core.ui.theme.EponaTheme

/**
 * Single-choice chip row over an enum's entries.
 *
 * The caller passes [label] rather than the component reaching into the enum, so it
 * works for any enum without knowing which ones carry a display name.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T : Enum<T>> SelectionChips(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            EponaFilterChip(
                label = label(option),
                selected = option == selected,
                onClick = { onSelected(option) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SelectionChipsPreview() {
    EponaTheme {
        SelectionChips(
            options = Species.entries,
            selected = Species.DOG,
            label = { it.label },
            onSelected = {}
        )
    }
}
