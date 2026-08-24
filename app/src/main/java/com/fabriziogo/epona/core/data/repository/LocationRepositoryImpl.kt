package com.fabriziogo.epona.core.data.repository

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Looper
import com.fabriziogo.epona.core.domain.model.Location
import com.fabriziogo.epona.core.domain.repository.LocationRepository
import com.fabriziogo.epona.core.permission.hasLocationPermission
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import android.location.Location as AndroidLocation

/**
 * Location access on top of Play Services' fused provider.
 *
 * Every entry point that needs the runtime permission checks it first and fails with
 * a [SecurityException] rather than letting the platform throw from deeper down — the
 * screens ask for the permission (see `core/ui/permission`), the repository only ever
 * reports whether it is there.
 */
@Singleton
class LocationRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient
) : LocationRepository {

    override suspend fun getCurrentLocation(): Result<Location> = runCatching {
        requireLocationPermission()

        // A recent cached fix answers instantly. Only wake the radio when there is
        // nothing cached, or what is cached is too old to pin a pet sighting on.
        val cached = fusedLocationClient.lastLocation.awaitOrNull()
        val fix = if (cached == null || cached.isStale()) {
            requestSingleFix() ?: cached
        } else {
            cached
        }

        checkNotNull(fix) {
            "Location unavailable. Check that location services are on, then try again."
        }.toDomainLocation()
    }

    override fun observeLocation(): Flow<Location> = callbackFlow {
        requireLocationPermission()

        val request = LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            UPDATE_INTERVAL_MS
        )
            .setMinUpdateDistanceMeters(MIN_UPDATE_DISTANCE_METERS)
            .setWaitForAccurateLocation(false)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { trySend(it.toDomainLocation()) }
            }
        }

        fusedLocationClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
        awaitClose { fusedLocationClient.removeLocationUpdates(callback) }
    }

    override suspend fun reverseGeocode(
        latitude: Double,
        longitude: Double
    ): Result<String> = runCatching {
        check(Geocoder.isPresent()) { "This device has no geocoder available" }

        Geocoder(context).lookup(latitude, longitude).firstOrNull()?.toShortAddress()
            ?: error("No address found for $latitude, $longitude")
    }

    /**
     * Haversine formula — calculates distance in meters
     * between two lat/lng points on Earth's surface.
     */
    override fun calculateDistance(from: Location, to: Location): Double {
        val r = 6_371_000.0 // Earth radius in meters
        val dLat = Math.toRadians(to.latitude - from.latitude)
        val dLng = Math.toRadians(to.longitude - from.longitude)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(from.latitude)) *
                cos(Math.toRadians(to.latitude)) *
                sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    private fun requireLocationPermission() {
        if (!context.hasLocationPermission()) {
            throw SecurityException("Location permission has not been granted")
        }
    }

    /**
     * One-shot fix. High accuracy is worth the power here: it is always the answer to
     * a deliberate tap ("use my location"), and a lost pet is a street-level report.
     */
    private suspend fun requestSingleFix(): AndroidLocation? {
        val cancellation = CancellationTokenSource()
        return fusedLocationClient.getCurrentLocation(
            CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setDurationMillis(SINGLE_FIX_TIMEOUT_MS)
                .setMaxUpdateAgeMillis(MAX_FIX_AGE_MS)
                .build(),
            cancellation.token
        ).awaitOrNull(onCancel = cancellation::cancel)
    }

    private suspend fun Geocoder.lookup(
        latitude: Double,
        longitude: Double
    ): List<Address> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { continuation ->
                // Spelled out rather than SAM-converted: GeocodeListener.onError has a
                // no-op default, so a lambda would leave the coroutine suspended forever
                // whenever the lookup fails.
                getFromLocation(
                    latitude,
                    longitude,
                    1,
                    object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            continuation.resume(addresses)
                        }

                        override fun onError(errorMessage: String?) {
                            continuation.resumeWithException(
                                IllegalStateException(errorMessage ?: "Reverse geocoding failed")
                            )
                        }
                    }
                )
            }
        } else {
            // The pre-33 call blocks on the network, so keep it off the caller's thread.
            withContext(Dispatchers.IO) {
                @Suppress("DEPRECATION")
                getFromLocation(latitude, longitude, 1).orEmpty()
            }
        }

    private fun AndroidLocation.isStale(): Boolean =
        System.currentTimeMillis() - time > MAX_FIX_AGE_MS

    private fun AndroidLocation.toDomainLocation() = Location(
        latitude = latitude,
        longitude = longitude
    )

    /** Street and city, skipping the postcode and country that [Address] lines carry. */
    private fun Address.toShortAddress(): String {
        val street = listOfNotNull(subThoroughfare, thoroughfare)
            .joinToString(" ")
            .takeIf { it.isNotBlank() }
        val short = listOfNotNull(street, locality ?: subAdminArea, adminArea)
            .joinToString(", ")
            .takeIf { it.isNotBlank() }
        return short ?: getAddressLine(0) ?: error("Address has no printable form")
    }

    private suspend fun <T> Task<T>.awaitOrNull(onCancel: () -> Unit = {}): T? =
        suspendCancellableCoroutine { continuation ->
            addOnSuccessListener { continuation.resume(it) }
            addOnFailureListener { continuation.resumeWithException(it) }
            addOnCanceledListener { continuation.resume(null) }
            continuation.invokeOnCancellation { onCancel() }
        }

    private companion object {
        /** Older than this and a cached fix is refreshed instead of trusted. */
        const val MAX_FIX_AGE_MS = 2 * 60 * 1000L
        const val SINGLE_FIX_TIMEOUT_MS = 10_000L
        const val UPDATE_INTERVAL_MS = 15_000L
        const val MIN_UPDATE_DISTANCE_METERS = 25f
    }
}
