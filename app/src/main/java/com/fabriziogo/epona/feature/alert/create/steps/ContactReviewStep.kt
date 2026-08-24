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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fabriziogo.epona.core.domain.model.AlertType
import com.fabriziogo.epona.core.ui.components.EponaTextField
import com.fabriziogo.epona.core.ui.theme.EponaTypography

@Composable
fun ContactReviewStep(
    phone: String,
    reward: String,
    alertType: AlertType,
    petName: String,
    address: String,
    onPhoneChanged: (String) -> Unit,
    onRewardChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(top = 24.dp)) {
        Text(
            "Contact & Review",
            style = EponaTypography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "How can people reach you?",
            style = EponaTypography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))

        EponaTextField(
            value = phone,
            onValueChange = onPhoneChanged,
            label = "Contact Phone",
            placeholder = "+1 (555) 000-0000",
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Next
        )

        if (alertType == AlertType.LOST) {
            Spacer(Modifier.height(12.dp))
            EponaTextField(
                value = reward,
                onValueChange = onRewardChanged,
                label = "Reward (optional)",
                placeholder = "e.g. 50",
                supportingText = "Offering a reward can increase response",
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
                        "Ready to publish!",
                        style = EponaTypography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = buildString {
                        val label = if (alertType == AlertType.LOST) "Lost" else "Found"
                        append("$label alert for $petName")
                        if (address.isNotBlank()) append(" near $address")
                        append(". Your community will be notified instantly.")
                    },
                    style = EponaTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                )
            }
        }
    }
}
