package com.fabriziogo.epona.feature.map.navigation

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.core.os.bundleOf
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController

/** Key a map focus request travels under, on the Map destination's saved state. */
private const val MAP_FOCUS_KEY = "map_focus"
private const val MAP_FOCUS_LAT = "lat"
private const val MAP_FOCUS_LNG = "lng"

/**
 * Points the Map tab at a specific coordinate — e.g. "View on map" from an alert
 * detail. Call it after navigating to the Map tab, when its back stack entry is
 * current.
 *
 * The coordinate is stored as a Bundle because SavedStateHandle.set only accepts
 * primitives / Parcelable / Serializable / Bundle, and a raw pair is none of those.
 */
fun NavController.focusMapOn(latitude: Double, longitude: Double) {
    getBackStackEntry(MAP_ROUTE).savedStateHandle[MAP_FOCUS_KEY] =
        bundleOf(MAP_FOCUS_LAT to latitude, MAP_FOCUS_LNG to longitude)
}

/**
 * The coordinate the Map tab was opened at, or null. Read it in the navigation layer
 * — the back stack entry is what carries the request — and hand it to the screen,
 * which applies it and then calls [clearMapFocus].
 *
 * Reading the entry's own [NavBackStackEntry.savedStateHandle] (rather than a
 * ViewModel-injected one) is what guarantees the write from [focusMapOn] is seen even
 * when the Map ViewModel was already alive from an earlier visit.
 */
@Composable
fun NavBackStackEntry.mapFocus(): Pair<Double, Double>? {
    val bundle by savedStateHandle
        .getStateFlow<Bundle?>(MAP_FOCUS_KEY, null)
        .collectAsStateWithLifecycle()
    return bundle?.toLatLng()
}

/** Drops the delivered focus so returning to the Map tab does not replay it. */
fun NavBackStackEntry.clearMapFocus() {
    savedStateHandle[MAP_FOCUS_KEY] = null
}

private fun Bundle.toLatLng(): Pair<Double, Double>? {
    val lat = getDouble(MAP_FOCUS_LAT, Double.NaN)
    val lng = getDouble(MAP_FOCUS_LNG, Double.NaN)
    if (lat.isNaN() || lng.isNaN()) return null
    return lat to lng
}
