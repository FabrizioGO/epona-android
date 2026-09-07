package com.fabriziogo.epona.core.media

import androidx.compose.runtime.Immutable

/**
 * A photo shown in a picker row. Edit screens start with photos already on the
 * server while new picks live on disk, and the two are told apart by type rather
 * than by sniffing the string for "http" — [PhotoItem.Local] entries are the ones
 * that still need uploading at save time.
 */
@Immutable
sealed interface PhotoItem {

    /** Stable identity for list keys. */
    val key: String

    /** What Coil loads: a remote URL or a local file Uri. */
    val model: String

    /** Already uploaded; its URL is persisted server-side. */
    @Immutable
    data class Remote(val url: String) : PhotoItem {
        override val key: String get() = url
        override val model: String get() = url
    }

    /** Picked in this session; uploaded when the form is saved. */
    @Immutable
    data class Local(val image: LocalImage) : PhotoItem {
        override val key: String get() = image.id
        override val model: String get() = image.uri
    }
}

/**
 * The cache-file URIs behind the entries that still need uploading. Used to clear
 * them once a save has gone through.
 */
fun List<PhotoItem>.localUris(): List<String> =
    filterIsInstance<PhotoItem.Local>().map { it.image.uri }
