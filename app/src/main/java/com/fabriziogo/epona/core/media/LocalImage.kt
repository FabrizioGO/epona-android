package com.fabriziogo.epona.core.media

import androidx.compose.runtime.Immutable

/**
 * A picked image after processing: downscaled, EXIF-corrected, re-encoded as JPEG
 * and written into the app's own cache.
 *
 * Copying into our cache is what makes the rest of the flow safe. The Uri the
 * photo picker returns carries a one-shot read grant scoped to the activity that
 * asked for it, so it stops resolving once the process is recreated; a file we own
 * keeps working, and it is the same shape whether the photo came from the gallery
 * or the camera.
 *
 * [id] is generated per pick rather than derived from the path, so the same source
 * image picked twice yields two distinct list keys.
 */
@Immutable
data class LocalImage(
    val id: String,
    val uri: String,
    val fileName: String
)
