package com.fabriziogo.epona.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.ui.components.EponaAvatar
import com.fabriziogo.epona.core.ui.components.EponaSearchBar
import com.fabriziogo.epona.core.ui.theme.EponaColors
import com.fabriziogo.epona.core.ui.theme.EponaTealDark
import com.fabriziogo.epona.core.ui.theme.EponaTheme
import com.fabriziogo.epona.core.ui.theme.EponaTypography
import com.fabriziogo.epona.feature.home.AlertFilter

/**
 * The teal hero at the top of Home: avatar + greeting, notification/map shortcuts, headline,
 * inline search and the All/Lost/Found category row. Deliberately always-dark — see
 * [EponaTealDark] usage — so it reads correctly regardless of the app theme or Material You.
 */
@Composable
fun HomeHero(
    userName: String,
    userAvatar: String?,
    unreadCount: Int,
    searchQuery: String,
    selectedFilter: AlertFilter,
    totalCount: Int,
    lostCount: Int,
    foundCount: Int,
    onSearchQueryChanged: (String) -> Unit,
    onFilterSelected: (AlertFilter) -> Unit,
    onNotificationsClick: () -> Unit,
    onMapClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(EponaTealDark)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(top = 8.dp, bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                EponaAvatar(
                    url = userAvatar,
                    name = userName,
                    size = 40.dp,
                    containerColor = Color.White.copy(alpha = 0.16f),
                    contentColor = Color.White
                )
                Spacer(Modifier.size(10.dp))
                Text(
                    text = if (userName.isNotBlank())
                        stringResource(R.string.home_hero_greeting, userName)
                    else greetingText(),
                    style = EponaTypography.titleMedium,
                    color = Color.White
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
            style = EponaTypography.headlineMedium,
            color = Color.White,
            modifier = Modifier.fillMaxWidth(0.75f)
        )

        Spacer(Modifier.height(20.dp))

        EponaSearchBar(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            containerColor = Color.White,
            contentColor = EponaTealDark
        )

        Spacer(Modifier.height(20.dp))

        AlertCategoryRow(
            selectedFilter = selectedFilter,
            totalCount = totalCount,
            lostCount = lostCount,
            foundCount = foundCount,
            onFilterSelected = onFilterSelected
        )
    }
}

@Composable
private fun HeroIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    badgeCount: Int = 0
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(44.dp),
        colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White)
    ) {
        BadgedBox(
            badge = {
                if (badgeCount > 0) {
                    Badge(containerColor = EponaColors.Lost) {
                        Text(badgeCount.toString())
                    }
                }
            }
        ) {
            Icon(icon, contentDescription = contentDescription, tint = EponaTealDark)
        }
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

@Preview(showBackground = true, backgroundColor = 0xFF0F4F44)
@Composable
private fun HomeHeroPreview() {
    EponaTheme(dynamicColor = false) {
        HomeHero(
            userName = "Fabrizio",
            userAvatar = null,
            unreadCount = 3,
            searchQuery = "",
            selectedFilter = AlertFilter.ALL,
            totalCount = 12,
            lostCount = 8,
            foundCount = 4,
            onSearchQueryChanged = {},
            onFilterSelected = {},
            onNotificationsClick = {},
            onMapClick = {}
        )
    }
}
