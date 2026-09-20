package com.fabriziogo.epona.feature.alert.create.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.domain.model.AlertType
import com.fabriziogo.epona.core.domain.model.FoundCustody
import com.fabriziogo.epona.core.ui.components.EponaTextField
import com.fabriziogo.epona.core.ui.theme.EponaTypography

@Composable
fun ContactReviewStep(
    phone: String,
    reward: String,
    alertType: AlertType,
    petName: String,
    address: String,
    custody: FoundCustody?,
    onPhoneChanged: (String) -> Unit,
    onRewardChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(top = 24.dp)) {
        Text(
            stringResource(R.string.create_step_contact_title),
            style = EponaTypography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(4.dp))
        Text(
            stringResource(R.string.create_step_contact_subtitle),
            style = EponaTypography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))

        EponaTextField(
            value = phone,
            onValueChange = onPhoneChanged,
            label = stringResource(R.string.create_contact_phone),
            placeholder = stringResource(R.string.create_contact_phone_placeholder),
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Next
        )

        if (alertType == AlertType.LOST) {
            Spacer(Modifier.height(12.dp))
            EponaTextField(
                value = reward,
                onValueChange = onRewardChanged,
                label = stringResource(R.string.create_reward),
                placeholder = stringResource(R.string.create_reward_placeholder),
                supportingText = stringResource(R.string.create_reward_hint),
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            )
        }

        Spacer(Modifier.height(24.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Outlined.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        stringResource(R.string.create_ready_title),
                        style = EponaTypography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(Modifier.height(8.dp))
                val typeLabel = if (alertType == AlertType.LOST) {
                    stringResource(R.string.alert_type_lost)
                } else {
                    stringResource(R.string.alert_type_found)
                }
                val summaryText = if (address.isNotBlank()) {
                    stringResource(R.string.create_ready_summary, typeLabel, petName, address)
                } else {
                    stringResource(R.string.create_ready_summary_no_location, typeLabel, petName)
                }
                Text(
                    text = summaryText,
                    style = EponaTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                )

                // Spelled out here because it is the one answer that cannot be
                // changed by editing the post later, and a mis-tap decides
                // whether strangers may report sightings on this animal.
                custody?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(
                            when (it) {
                                FoundCustody.WITH_FINDER ->
                                    R.string.create_ready_summary_custody_with_finder
                                FoundCustody.AT_LOCATION ->
                                    R.string.create_ready_summary_custody_at_location
                            }
                        ),
                        style = EponaTypography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}
