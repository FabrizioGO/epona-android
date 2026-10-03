package com.fabriziogo.epona.feature.profile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.domain.model.UserStats
import com.fabriziogo.epona.core.ui.theme.EponaTheme
import com.fabriziogo.epona.core.ui.theme.EponaTypography

@Composable
fun StatsRow(
    stats: UserStats,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            value = stats.totalPets.toString(),
            label = stringResource(R.string.profile_stat_pets),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            value = stats.activeAlerts.toString(),
            label = stringResource(R.string.profile_stat_alerts),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            value = stats.sightingsReported.toString(),
            label = stringResource(R.string.profile_stat_sightings),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        // Translucent tile: StatsRow lives on the always-teal hero, like AlertCategoryRow.
        color = Color.White.copy(alpha = 0.12f)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = EponaTypography.headlineSmall,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                text = label,
                style = EponaTypography.bodySmall,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F4F44)
@Composable
private fun StatsRowPreview() {
    EponaTheme(dynamicColor = false) {
        StatsRow(stats = UserStats(totalPets = 2, activeAlerts = 1, sightingsReported = 5))
    }
}
