package com.fabriziogo.epona.feature.home.components

import com.fabriziogo.epona.core.ui.preview.EponaPreviews
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.ui.components.EponaAvatar
import com.fabriziogo.epona.core.ui.components.EponaSearchBar
import com.fabriziogo.epona.core.ui.components.HeroIconButton
import com.fabriziogo.epona.core.ui.theme.EponaTheme
import com.fabriziogo.epona.feature.home.AlertFilter

/**
 * The teal hero at the top of Home: avatar + greeting, notification/map shortcuts, headline,
 * inline search and the scrollable category chips. Content colors come from
 * `onPrimaryContainer`, so it stays legible in light, dark and Material You themes.
 */
@Composable
fun HomeHero(
    userName: String,
    userAvatar: String?,
    unreadCount: Int,
    searchQuery: String,
    selectedFilter: AlertFilter,
    onSearchQueryChanged: (String) -> Unit,
    onFilterSelected: (AlertFilter) -> Unit,
    onNotificationsClick: () -> Unit,
    onMapClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentInset: Dp = 0.dp
) {
    val contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    // The category chips scroll edge to edge, so the inset is applied per row, not on the Column.
    val horizontalPadding = 20.dp + contentInset

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .statusBarsPadding()
            .padding(top = 8.dp, bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = horizontalPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                EponaAvatar(
                    url = userAvatar,
                    name = userName,
                    size = 40.dp,
                    containerColor = contentColor.copy(alpha = 0.16f),
                    contentColor = contentColor
                )
                Spacer(Modifier.size(10.dp))
                Text(
                    text = if (userName.isNotBlank())
                        stringResource(R.string.home_hero_greeting, userName)
                    else greetingText(),
                    style = MaterialTheme.typography.titleMedium,
                    color = contentColor
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HeroIconButton(
                    icon = Icons.Outlined.Notifications,
                    contentDescription = stringResource(R.string.cd_notifications),
                    badgeCount = unreadCount,
                    onClick = onNotificationsClick
                )
                HeroIconButton(
                    icon = Icons.Outlined.Map,
                    contentDescription = stringResource(R.string.cd_view_map),
                    onClick = onMapClick
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = stringResource(R.string.home_hero_headline),
            style = MaterialTheme.typography.headlineMedium,
            color = contentColor,
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .padding(start = horizontalPadding)
        )

        Spacer(Modifier.height(20.dp))

        EponaSearchBar(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            modifier = Modifier.padding(horizontal = horizontalPadding)
        )

        Spacer(Modifier.height(20.dp))

        AlertCategoryRow(
            selectedFilter = selectedFilter,
            onFilterSelected = onFilterSelected,
            contentPadding = PaddingValues(horizontal = horizontalPadding)
        )
    }
}

@Composable
private fun greetingText(): String {
    val hour = java.time.LocalTime.now().hour
    return when {
        hour < 12 -> stringResource(R.string.home_greeting_morning)
        hour < 17 -> stringResource(R.string.home_greeting_afternoon)
        else -> stringResource(R.string.home_greeting_evening)
    }
}

@EponaPreviews
@Composable
private fun HomeHeroPreview() {
    EponaTheme(dynamicColor = false) {
        HomeHero(
            userName = "Fabrizio",
            userAvatar = null,
            unreadCount = 3,
            searchQuery = "",
            selectedFilter = AlertFilter.ALL,
            onSearchQueryChanged = {},
            onFilterSelected = {},
            onNotificationsClick = {},
            onMapClick = {}
        )
    }
}
