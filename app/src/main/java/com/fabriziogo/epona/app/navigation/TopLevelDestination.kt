package com.fabriziogo.epona.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.fabriziogo.epona.feature.home.navigation.HOME_ROUTE
import com.fabriziogo.epona.feature.map.navigation.MAP_ROUTE
import com.fabriziogo.epona.feature.notifications.navigation.NOTIFICATIONS_ROUTE
import com.fabriziogo.epona.feature.profile.navigation.PROFILE_GRAPH_ROUTE

enum class TopLevelDestination(
    val route: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
    val label: String
) {
    HOME(
        route = HOME_ROUTE,
        icon = Icons.Outlined.Home,
        selectedIcon = Icons.Filled.Home,
        label = "Home"
    ),
    MAP(
        route = MAP_ROUTE,
        icon = Icons.Outlined.Map,
        selectedIcon = Icons.Filled.Map,
        label = "Map"
    ),
    ALERTS(
        route = NOTIFICATIONS_ROUTE,
        icon = Icons.Outlined.Notifications,
        selectedIcon = Icons.Filled.Notifications,
        label = "Alerts"
    ),
    PROFILE(
        route = PROFILE_GRAPH_ROUTE,
        icon = Icons.Outlined.Person,
        selectedIcon = Icons.Filled.Person,
        label = "Profile"
    )
}