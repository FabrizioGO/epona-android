package com.fabriziogo.epona.core.domain.model

import com.fabriziogo.epona.R

/**
 * Where a found pet is now. Decides whether the post accepts sightings at all:
 * an animal someone took home cannot be spotted by anybody else, so its post
 * asks readers to contact the finder instead.
 *
 * Only a FOUND alert has one; a lost alert's is null.
 */
enum class FoundCustody(val value: String, val label: Int, val description: Int) {
    WITH_FINDER(
        value = "with_finder",
        label = R.string.custody_with_finder,
        description = R.string.custody_with_finder_desc
    ),
    AT_LOCATION(
        value = "at_location",
        label = R.string.custody_at_location,
        description = R.string.custody_at_location_desc
    );

    companion object {
        /**
         * Null rather than a default, unlike the other enums here. A lost alert
         * legitimately has no custody, and quietly defaulting an unrecognised
         * value to [AT_LOCATION] would re-open sightings on a pet asleep in
         * somebody's flat — the one mistake this type exists to prevent.
         */
        fun fromValue(v: String?): FoundCustody? = entries.firstOrNull { it.value == v }
    }
}
