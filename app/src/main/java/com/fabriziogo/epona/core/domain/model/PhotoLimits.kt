package com.fabriziogo.epona.core.domain.model

/**
 * How many photos a pet or a sighting may carry. Shared so the picker, the
 * ViewModels and the use cases cannot drift apart — they did before, when the
 * sighting card stopped at three and the alert ViewModel allowed five.
 */
const val MAX_PHOTOS_PER_ENTITY = 3

/** Photos already in a bucket are https URLs; anything else is still on the device. */
fun String.isRemotePhotoUrl(): Boolean =
    startsWith("http://", ignoreCase = true) || startsWith("https://", ignoreCase = true)

fun String.isLocalPhotoUri(): Boolean = isNotBlank() && !isRemotePhotoUrl()

/**
 * Replaces each local URI with the next of [uploaded], leaving already-remote URLs
 * where they are.
 *
 * Substituting in place rather than appending is what preserves the order the user
 * arranged: editing a pet that had three photos, dropping the middle one and adding
 * a new one should leave the newcomer where it was put, not at the end.
 *
 * [uploaded] must be the upload result for this list's local entries, in order.
 */
fun List<String>.withUploadedPhotos(uploaded: List<String>): List<String> {
    val next = uploaded.iterator()
    return map { if (it.isLocalPhotoUri()) next.next() else it }
}
