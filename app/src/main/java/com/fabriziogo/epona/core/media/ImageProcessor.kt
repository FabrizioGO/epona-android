package com.fabriziogo.epona.core.media

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import coil3.ImageLoader
import coil3.request.CachePolicy
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.size.Precision
import coil3.size.Scale
import coil3.toBitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Turns a picked or captured image into a modest JPEG in the app's cache, ready to
 * upload.
 *
 * Decoding goes through Coil rather than BitmapFactory because Coil already applies
 * the EXIF orientation — its BitmapFactoryDecoder runs `ExifUtils.reverseTransformations`,
 * and on API 29+ ImageDecoder rotates on its own — which is what keeps a photo taken
 * in portrait from arriving sideways on the server. Hand-rolling it would mean an
 * extra dependency and a decode path that differs above and below API 28.
 */
@Singleton
class ImageProcessor @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val imageLoader: ImageLoader
) {
    /**
     * Where processed images live until the form that owns them is saved or
     * abandoned. Kept apart from the raw camera staging dir so the two can be
     * cleared independently, and deliberately absent from res/xml/file_paths.xml so
     * nothing outside the app can reach it.
     */
    private val workspace: File
        get() = File(context.cacheDir, WORKSPACE_DIR).apply { mkdirs() }

    /**
     * Decodes [source], scales its longest edge down to [MAX_EDGE_PX], re-encodes it
     * as JPEG and returns a handle to the result. Failure is returned rather than
     * thrown so one unreadable pick cannot take down a whole multi-select.
     */
    suspend fun process(source: Uri): Result<LocalImage> = withContext(Dispatchers.IO) {
        runCatching {
            val request = ImageRequest.Builder(context)
                .data(source)
                .size(MAX_EDGE_PX, MAX_EDGE_PX)
                .scale(Scale.FIT)
                // The default is EXACT, which would *upscale* a small pick to
                // 1600px and re-encode it larger than the original.
                .precision(Precision.INEXACT)
                // Coil defaults to a HARDWARE bitmap on API 26+, whose pixels live
                // in graphics memory and cannot be read back by compress().
                .allowHardware(false)
                // A 1600px ARGB_8888 bitmap is several MB; caching three of them
                // would evict every thumbnail on the home feed.
                .memoryCachePolicy(CachePolicy.DISABLED)
                .diskCachePolicy(CachePolicy.DISABLED)
                .build()

            val bitmap = when (val result = imageLoader.execute(request)) {
                is SuccessResult -> result.image.toBitmap()
                is ErrorResult -> throw result.throwable
            }

            val fileName = "${UUID.randomUUID()}.jpg"
            val file = File(workspace, fileName)
            file.outputStream().buffered().use { out ->
                check(bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)) {
                    "Could not encode the selected image"
                }
            }

            LocalImage(
                id = UUID.randomUUID().toString(),
                uri = Uri.fromFile(file).toString(),
                fileName = fileName
            )
        }.onFailure { e ->
            if (e is CancellationException) throw e
            Timber.e(e, "ImageProcessor: failed to process %s", source)
        }
    }

    /**
     * Reads a processed image back for upload.
     *
     * The cache directory is the OS's to reclaim, so the file can be gone by the
     * time the form is saved. That surfaces as a clear failure rather than an empty
     * upload.
     */
    fun readBytes(localUri: String): ByteArray {
        val file = localUri.toWorkspaceFile()
            ?: throw FileNotFoundException("Not a local photo: $localUri")
        if (!file.exists()) {
            throw FileNotFoundException("That photo is no longer available")
        }
        return file.readBytes()
    }

    /** The name the file should take in the bucket. */
    fun fileNameOf(localUri: String): String =
        localUri.toWorkspaceFile()?.name ?: "${UUID.randomUUID()}.jpg"

    /** Drops the cache files behind [localUris]; safe to call more than once. */
    fun delete(localUris: List<String>) {
        localUris.forEach { uri ->
            runCatching { uri.toWorkspaceFile()?.delete() }
                .onFailure { Timber.w(it, "ImageProcessor: could not delete %s", uri) }
        }
    }

    /**
     * Clears anything left behind by a form that was never finished — the process
     * can die between picking a photo and saving, and nothing else would collect it.
     */
    fun pruneWorkspace(olderThanMillis: Long = STALE_AFTER_MILLIS) {
        val cutoff = System.currentTimeMillis() - olderThanMillis
        runCatching {
            workspace.listFiles()?.forEach { file ->
                if (file.lastModified() < cutoff) file.delete()
            }
            clearCameraCaptures(context)
        }.onFailure { Timber.w(it, "ImageProcessor: cache prune failed") }
    }

    private fun String.toWorkspaceFile(): File? {
        val name = Uri.parse(this).lastPathSegment ?: return null
        return File(workspace, name)
    }

    private companion object {
        /**
         * Large enough that a pet stays recognisable opened full-screen, small
         * enough that three of them upload over a weak mobile link.
         */
        const val MAX_EDGE_PX = 1600
        const val JPEG_QUALITY = 85
        const val WORKSPACE_DIR = "picked_photos"
        const val STALE_AFTER_MILLIS = 24L * 60 * 60 * 1000
    }
}
