package com.fabriziogo.epona.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.fabriziogo.epona.core.domain.model.Pet

/**
 * What to show wherever a pet's name appears. Found alerts are backed by an
 * ownerless pet row with no name, so a blank name falls back to the localized
 * per-species "Found dog" label ("Found %1$s" cannot be a format string —
 * Spanish needs adjective agreement).
 */
@Composable
fun petDisplayName(pet: Pet): String =
    pet.name.ifBlank { stringResource(pet.species.foundLabel) }
