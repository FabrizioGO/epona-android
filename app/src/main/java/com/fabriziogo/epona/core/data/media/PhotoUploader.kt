package com.fabriziogo.epona.core.data.media

import com.fabriziogo.epona.core.media.ImageProcessor
import com.fabriziogo.epona.core.network.service.AuthService
import com.fabriziogo.epona.core.network.service.StorageService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Puts locally picked photos in a Storage bucket and hands back their public URLs.
 *
 * Shared by the pet and sighting repositories so the two cannot drift apart on
 * pathing or on what happens when an upload fails halfway through.
 */
@Singleton
class PhotoUploader @Inject constructor(
    private val authService: AuthService,
    private val imageProcessor: ImageProcessor,
    private val storageService: StorageService
) {
    /**
     * Uploads [localUris] to [bucket] under `{userId}/{fileName}` and returns their
     * public URLs in the order given.
     *
     * The path names the uploader rather than the pet or the sighting, which is what
     * lets this run before the row exists — a new pet has no id until its insert
     * comes back — and it is what the storage policy checks.
     *
     * Sequential on purpose: at most three images, one byte array alive at a time,
     * and an exact list of what to unwind if one of them fails.
     */
    suspend fun uploadAll(bucket: String, localUris: List<String>): List<String> {
        if (localUris.isEmpty()) return emptyList()

        val userId = authService.getCurrentUserId()
            ?: throw IllegalStateException("Not authenticated")

        val uploadedPaths = mutableListOf<String>()
        val urls = mutableListOf<String>()
        try {
            localUris.forEach { localUri ->
                val path = "$userId/${imageProcessor.fileNameOf(localUri)}"
                urls += storageService.upload(bucket, path, imageProcessor.readBytes(localUri))
                uploadedPaths += path
            }
            return urls
        } catch (e: Throwable) {
            if (e is CancellationException) throw e
            // Nothing references these yet, so they are pure litter. Best effort
            // only: a failed cleanup must not replace the error the caller needs.
            withContext(NonCancellable) {
                uploadedPaths.forEach { path ->
                    runCatching { storageService.delete(bucket, path) }
                        .onFailure { Timber.w(it, "Orphaned storage object %s/%s", bucket, path) }
                }
            }
            throw e
        }
    }
}
