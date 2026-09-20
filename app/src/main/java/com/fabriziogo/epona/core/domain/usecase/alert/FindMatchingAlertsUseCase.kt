package com.fabriziogo.epona.core.domain.usecase.alert

import com.fabriziogo.epona.core.domain.model.AlertType
import com.fabriziogo.epona.core.domain.model.AlertWithDetails
import com.fabriziogo.epona.core.domain.model.Location
import com.fabriziogo.epona.core.domain.model.Species
import com.fabriziogo.epona.core.domain.repository.AlertRepository
import javax.inject.Inject

/**
 * Finds already-published alerts that might be the same animal the user is
 * looking at, so a finder can report a sighting on the owner's post instead of
 * creating a duplicate (and an owner can see a found post before publishing).
 *
 * Reuses AlertRepository.getNearbyAlerts — no new RPC; get_nearby_alerts
 * already returns species, breed, color and distance_meters. Filters to exact
 * species, scores breed/color, sorts by score then distance, takes 10.
 */
class FindMatchingAlertsUseCase @Inject constructor(
    private val alertRepo: AlertRepository
) {
    suspend operator fun invoke(
        species: Species,
        breed: String?,
        color: String?,
        location: Location,
        lookingFor: AlertType,
        radiusMeters: Int = 15_000
    ): Result<List<AlertWithDetails>> = runCatching {
        val nearby = alertRepo.getNearbyAlerts(
            latitude = location.latitude,
            longitude = location.longitude,
            radiusMeters = radiusMeters,
            type = lookingFor,
            limit = 50
        ).getOrThrow()

        nearby
            .filter { it.pet.species == species }
            .map { it to petMatchScore(breed, color, it.pet.breed, it.pet.color) }
            .sortedWith(
                compareByDescending<Pair<AlertWithDetails, Int>> { it.second }
                    .thenBy { it.first.distanceMeters ?: Double.MAX_VALUE }
            )
            .take(10)
            .map { it.first }
    }
}

/**
 * Pure breed/color score. Null or blank on either side is tolerated (scores
 * nothing rather than excluding); matching is case-insensitive `contains` in
 * either direction so "lab" matches "Labrador Mix" and vice versa.
 */
internal fun petMatchScore(
    wantedBreed: String?,
    wantedColor: String?,
    candidateBreed: String?,
    candidateColor: String?
): Int {
    var score = 0
    if (matchesLoosely(wantedBreed, candidateBreed)) score += 2
    if (matchesLoosely(wantedColor, candidateColor)) score += 1
    return score
}

private fun matchesLoosely(a: String?, b: String?): Boolean {
    if (a.isNullOrBlank() || b.isNullOrBlank()) return false
    return a.contains(b, ignoreCase = true) || b.contains(a, ignoreCase = true)
}
