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
import androidx.compose.ui.unit.dp
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.domain.model.Location
import com.fabriziogo.epona.feature.sighting.report.components.LocationPickerCard
import com.fabriziogo.epona.feature.alert.create.steps.components.PhotoCaptureCard
import com.fabriziogo.epona.core.ui.components.EponaTextField
import com.fabriziogo.epona.core.ui.theme.EponaTypography

@Composable
fun PhotoLocationStep(
    photoUris: List<String>,
    location: Location?,
    address: String,
    description: String,
    isLoadingLocation: Boolean,
    locationError: String?,
    onPhotoAdded: (String) -> Unit,
    onPhotoRemoved: (Int) -> Unit,
    onUseCurrentLocation: () -> Unit,
    onLocationPicked: (Location) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(top = 24.dp)) {
        Text(
            stringResource(R.string.create_step_photo_title),
            style = EponaTypography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(4.dp))
        Text(
            stringResource(R.string.create_step_photo_subtitle),
            style = EponaTypography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))

        PhotoCaptureCard(
            photoUris = photoUris,
            onAddPhoto = onPhotoAdded,
            onRemovePhoto = onPhotoRemoved
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
