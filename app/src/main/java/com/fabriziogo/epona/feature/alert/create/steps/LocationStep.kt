package com.fabriziogo.epona.feature.alert.create.steps

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.domain.model.Location
import com.fabriziogo.epona.core.ui.components.EponaTextField
import com.fabriziogo.epona.core.ui.theme.EponaTheme
import com.fabriziogo.epona.core.ui.theme.EponaTypography
import com.fabriziogo.epona.feature.sighting.report.components.LocationPickerCard

/**
 * Step 3 (shared): where the pet was lost or found, plus the alert
 * description. Extracted from PhotoLocationStep by deleting the PhotoCaptureCard
 * (dead data: photoUris were never read by publish()); photos now live on the
 * pet — the registered pet for LOST, the FoundPetStep picker for FOUND.
 */
@Composable
fun LocationStep(
    location: Location?,
    address: String,
    description: String,
    isLoadingLocation: Boolean,
    locationError: String?,
    onUseCurrentLocation: () -> Unit,
    onLocationPicked: (Location) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(top = 24.dp)) {
        Text(
            stringResource(R.string.create_step_location_title),
            style = EponaTypography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(4.dp))
        Text(
            stringResource(R.string.create_step_location_subtitle),
            style = EponaTypography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))

        LocationPickerCard(
            location = location,
            address = address,
            isLoading = isLoadingLocation,
            errorText = locationError,
            onUseCurrentLocation = onUseCurrentLocation,
            onLocationPicked = onLocationPicked
        )

        Spacer(Modifier.height(24.dp))

        EponaTextField(
            value = description,
            onValueChange = onDescriptionChanged,
            label = stringResource(R.string.create_description),
            placeholder = stringResource(R.string.create_description_placeholder),
            singleLine = false,
            maxLines = 5,
            imeAction = ImeAction.Done
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LocationStepPreview() {
    EponaTheme {
        LocationStep(
            location = Location(40.4, -3.7, "Retiro Park, Madrid"),
            address = "Retiro Park, Madrid",
            description = "",
            isLoadingLocation = false,
            locationError = null,
            onUseCurrentLocation = {},
            onLocationPicked = {},
            onDescriptionChanged = {}
        )
    }
}
