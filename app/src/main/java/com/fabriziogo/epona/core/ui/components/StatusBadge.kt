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
import com.fabriziogo.epona.core.ui.theme.extendedColors

@Composable
fun AlertTypeBadge(
    type: AlertType,
    modifier: Modifier = Modifier
) {
    val extended = MaterialTheme.extendedColors
    val bg = when (type) {
        AlertType.LOST -> extended.lostContainer
        AlertType.FOUND -> extended.foundContainer
    }
    val fg = when (type) {
        AlertType.LOST -> extended.onLostContainer
        AlertType.FOUND -> extended.onFoundContainer
    }
    val label = when (type) {
        AlertType.LOST -> stringResource(R.string.alert_type_lost)
        AlertType.FOUND -> stringResource(R.string.alert_type_found)
    }
    Text(
        text = label.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(
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
    val extended = MaterialTheme.extendedColors
    val bg = when (status) {
        AlertStatus.ACTIVE -> MaterialTheme.colorScheme.primary
        AlertStatus.RESOLVED -> extended.foundContainer
        AlertStatus.EXPIRED -> MaterialTheme.colorScheme.surfaceVariant
    }
    val fg = when (status) {
        AlertStatus.ACTIVE -> MaterialTheme.colorScheme.onPrimary
        AlertStatus.RESOLVED -> extended.onFoundContainer
        AlertStatus.EXPIRED -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val label = when (status) {
        AlertStatus.ACTIVE -> stringResource(R.string.alert_status_active)
        AlertStatus.RESOLVED -> stringResource(R.string.alert_status_resolved)
        AlertStatus.EXPIRED -> stringResource(R.string.alert_status_expired)
    }
    Text(
        text = label.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(
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
        style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Bold
        ),
        color = MaterialTheme.extendedColors.onRewardContainer,
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.extendedColors.rewardContainer)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}

/**
 * Marks a found pet the finder took with them. Sits next to [AlertTypeBadge]:
 * "FOUND" alone does not say whether the animal is still out there, and that is
 * the difference between going to look for it and picking up the phone.
 */
@Composable
fun InSafeHandsBadge(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.detail_badge_in_safe_hands).uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 10.dp, vertical = 3.dp)
    )
}

/** Type badge for adoption posts; the counterpart of [AlertTypeBadge] for LOST / FOUND. */
@Composable
fun AdoptionBadge(modifier: Modifier = Modifier) {
    val extended = MaterialTheme.extendedColors
    Text(
        text = stringResource(R.string.adoption_badge).uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        color = extended.onAdoptionContainer,
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(extended.adoptionContainer)
            .padding(horizontal = 10.dp, vertical = 3.dp)
    )
}

/** Shown on a post its owner has marked as adopted. */
@Composable
fun AdoptedStatusBadge(modifier: Modifier = Modifier) {
    val extended = MaterialTheme.extendedColors
    Text(
        text = stringResource(R.string.adoption_status_adopted).uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        color = extended.onFoundContainer,
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(extended.foundContainer)
            .padding(horizontal = 10.dp, vertical = 3.dp)
    )
}

/** Adoption fee, or "Free" when the post carries no price (the home feed card hides the chip instead). */
@Composable
fun AdoptionPriceBadge(
    price: Double?,
    modifier: Modifier = Modifier
) {
    val text = if (price == null || price <= 0.0) {
        stringResource(R.string.adoption_price_free)
    } else {
        stringResource(R.string.adoption_price_amount, formatAdoptionPrice(price))
    }
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.extendedColors.onRewardContainer,
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.extendedColors.rewardContainer)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}

/** "25" for whole amounts, "25.50" otherwise. */
fun formatAdoptionPrice(price: Double): String =
    if (price % 1.0 == 0.0) price.toLong().toString() else String.format(java.util.Locale.getDefault(), "%.2f", price)
