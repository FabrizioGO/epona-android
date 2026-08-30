package com.fabriziogo.epona.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.domain.model.AlertStatus
import com.fabriziogo.epona.core.domain.model.AlertType
import com.fabriziogo.epona.core.ui.theme.EponaColors
import com.fabriziogo.epona.core.ui.theme.EponaTypography

@Composable
fun AlertTypeBadge(
    type: AlertType,
    modifier: Modifier = Modifier
) {
    val bg = when (type) {
        AlertType.LOST -> EponaColors.LostContainer
        AlertType.FOUND -> EponaColors.FoundContainer
    }
    val fg = when (type) {
        AlertType.LOST -> EponaColors.Lost
        AlertType.FOUND -> EponaColors.Found
    }
    val label = when (type) {
        AlertType.LOST -> stringResource(R.string.alert_type_lost)
        AlertType.FOUND -> stringResource(R.string.alert_type_found)
    }
    Text(
        text = label.uppercase(),
        style = EponaTypography.labelSmall.copy(
            fontWeight = FontWeight.Bold
        ),
        color = fg,
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 3.dp)
    )
}

@Composable
fun AlertStatusBadge(
    status: AlertStatus,
    modifier: Modifier = Modifier
) {
    val bg = when (status) {
        AlertStatus.ACTIVE -> MaterialTheme.colorScheme.primaryContainer
        AlertStatus.RESOLVED -> EponaColors.FoundContainer
        AlertStatus.EXPIRED -> MaterialTheme.colorScheme.surfaceVariant
    }
    val fg = when (status) {
        AlertStatus.ACTIVE -> MaterialTheme.colorScheme.primary
        AlertStatus.RESOLVED -> EponaColors.Found
        AlertStatus.EXPIRED -> MaterialTheme.colorScheme.outline
    }
    val label = when (status) {
        AlertStatus.ACTIVE -> stringResource(R.string.alert_status_active)
        AlertStatus.RESOLVED -> stringResource(R.string.alert_status_resolved)
        AlertStatus.EXPIRED -> stringResource(R.string.alert_status_expired)
    }
    Text(
        text = label.uppercase(),
        style = EponaTypography.labelSmall.copy(
            fontWeight = FontWeight.Bold
        ),
        color = fg,
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 3.dp)
    )
}

@Composable
fun RewardBadge(
    amount: Double,
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(R.string.alert_reward_amount, amount.toInt()),
        style = EponaTypography.labelLarge.copy(
            fontWeight = FontWeight.Bold
        ),
        color = EponaColors.Reward,
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(EponaColors.RewardContainer)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}
