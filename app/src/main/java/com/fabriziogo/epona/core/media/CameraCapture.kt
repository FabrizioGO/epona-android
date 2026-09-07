package com.fabriziogo.epona.core.media

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID

/** Matches the authority declared for FileProvider in AndroidManifest.xml. */
private const val FILE_PROVIDER_SUFFIX = ".fileprovider"

/** Matches the `camera_photos` cache-path in res/xml/file_paths.xml. */
private const val CAMERA_DIR = "camera"

/**
 * Creates the destination the system camera app writes the full-size photo into.
 *
 * ACTION_IMAGE_CAPTURE only returns a thumbnail unless it is handed somewhere to
 * write, and it cannot be handed a raw `file://` path — since API 24 that trips
 * FileUriExposedException — so the file is wrapped by FileProvider.
 * `TakePicture` attaches the write grant for us.
 */
fun createCameraCaptureUri(context: Context): Uri {
    val dir = File(context.cacheDir, CAMERA_DIR).apply { mkdirs() }
    val file = File(dir, "capture_${UUID.randomUUID()}.jpg")
    return FileProvider.getUriForFile(
        context,
        context.packageName + FILE_PROVIDER_SUFFIX,
        file
    )
}

/**
 * Drops a single capture. Used when the user backs out of the camera app, which
 * otherwise leaves behind the empty file that was created for it.
 */
fun deleteCameraCapture(context: Context, uri: Uri) {
    val name = uri.lastPathSegment ?: return
    runCatching { File(File(context.cacheDir, CAMERA_DIR), name).delete() }
}

/**
 * Clears the raw captures. They are only a staging area — [ImageProcessor]
 * re-encodes what it needs into its own workspace — and a full-size capture runs to
 * several megabytes, so they go as soon as processing is done.
 */
fun clearCameraCaptures(context: Context) {
    File(context.cacheDir, CAMERA_DIR).listFiles()?.forEach { it.delete() }
}
