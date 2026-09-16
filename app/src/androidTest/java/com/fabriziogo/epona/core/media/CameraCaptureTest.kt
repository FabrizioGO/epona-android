package com.fabriziogo.epona.core.media

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import coil3.SingletonImageLoader
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Hermetic checks for the camera half of the photo flow, without involving the
 * system camera app: the Uri [MediaPicker] hands to TakePicture must be servable
 * by our FileProvider, the exact capture intent must resolve, and a JPEG written
 * through the provider (what a cooperative camera app does) must survive
 * [ImageProcessor].
 */
@RunWith(AndroidJUnit4::class)
class CameraCaptureTest {

    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun captureUri_usesDeclaredFileProviderAuthority() {
        val uri = createCameraCaptureUri(context)
        assertEquals("${context.packageName}.fileprovider", uri.authority)
    }

    @Test
    fun takePictureIntent_resolvesToACameraApp() {
        val uri = createCameraCaptureUri(context)
        // Byte-for-byte what TakePicture.createIntent builds, minus nothing.
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            .putExtra(MediaStore.EXTRA_OUTPUT, uri)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        assertNotNull(
            "No app handles ACTION_IMAGE_CAPTURE — check the <queries> entry",
            intent.resolveActivity(context.packageManager)
        )
    }

    @Test
    fun cameraWriteThroughProvider_survivesImageProcessor() {
        val context = context
        val captureUri = createCameraCaptureUri(context)

        // Stand in for the camera app: open the Uri for writing and store a JPEG.
        val bitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888).apply {
            eraseColor(0xFFFF0000.toInt())
        }
        val jpeg = ByteArrayOutputStream().use { out ->
            check(bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out))
            out.toByteArray()
        }
        context.contentResolver.openOutputStream(captureUri)?.use { it.write(jpeg) }
            ?: error("FileProvider refused the capture Uri for writing")

        val written = querySize(captureUri)
        assertTrue("Capture file is empty after write", written > 0)

        // What every ViewModel does with the Uri the TakePicture callback hands back.
        val processor = ImageProcessor(context, SingletonImageLoader.get(context))
        val local = runBlocking { processor.process(captureUri).getOrThrow() }
        assertTrue(
            "Processed avatar/pet photo is empty",
            processor.readBytes(local.uri).isNotEmpty()
        )

        processor.delete(listOf(local.uri))
        deleteCameraCapture(context, captureUri)
    }

    private fun querySize(uri: Uri): Long {
        context.contentResolver.openInputStream(uri)?.use { stream ->
            return stream.readBytes().size.toLong()
        }
        return -1
    }
}
