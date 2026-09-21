package com.fabriziogo.epona.core.ui.navigation

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.core.os.bundleOf
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.fabriziogo.epona.core.domain.model.Location

/** Key the picked location travels under, on the destination's saved state. */
private const val PICKED_LOCATION_KEY = "picked_location"
private const val PICKED_LAT = "lat"
private const val PICKED_LNG = "lng"
private const val PICKED_ADDRESS = "address"

/**
 * Pops back to the previous destination and hands it the location picked on the map.
 *
 * The Location is stored as a Bundle because SavedStateHandle.set only accepts
 * primitives / Parcelable / Serializable / Bundle, and Location is none of those.
 * One key keeps the read atomic — no torn state from separate emissions.
 */
fun NavController.popBackStackWithLocation(location: Location) {
    previousBackStackEntry?.savedStateHandle?.set(
        PICKED_LOCATION_KEY,
        bundleOf(
            PICKED_LAT to location.latitude,
            PICKED_LNG to location.longitude,
            PICKED_ADDRESS to location.address
        )
    )
    popBackStack()
}

/**
 * The location this destination was returned to with, or null. Read it in the
 * navigation layer — the back stack entry is what carries the result — and hand it to
 * the screen, which consumes it and then calls [clearPickedLocation].
 */
@Composable
fun NavBackStackEntry.pickedLocation(): Location? {
    val bundle by savedStateHandle
        .getStateFlow<Bundle?>(PICKED_LOCATION_KEY, null)
        .collectAsStateWithLifecycle()
    val b = bundle ?: return null
    if (!b.containsKey(PICKED_LAT) || !b.containsKey(PICKED_LNG)) return null
    return Location(
        latitude = b.getDouble(PICKED_LAT),
        longitude = b.getDouble(PICKED_LNG),
        address = b.getString(PICKED_ADDRESS)
    )
}

/** Drops the delivered location so returning to this destination does not replay it. */
fun NavBackStackEntry.clearPickedLocation() {
    savedStateHandle[PICKED_LOCATION_KEY] = null
}
