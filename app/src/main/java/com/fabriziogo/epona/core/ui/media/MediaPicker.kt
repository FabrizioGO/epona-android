package com.fabriziogo.epona.core.ui.media

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.fabriziogo.epona.core.media.createCameraCaptureUri
import com.fabriziogo.epona.core.media.deleteCameraCapture
import com.fabriziogo.epona.core.permission.hasCameraApp
import com.fabriziogo.epona.core.ui.permission.CameraPermissionState
import com.fabriziogo.epona.core.ui.permission.rememberCameraPermissionState
import timber.log.Timber

/**
 * Drives the "add a photo" flow for one screen: the source sheet, the gallery
 * picker, the camera intent and the camera permission behind it.
 *
 * Create it with [rememberMediaPickerState].
 */
@Stable
class MediaPickerState internal constructor(
    /** False on devices with no camera app, which hides the camera row. */
    val isCameraAvailable: Boolean,
    val cameraPermission: CameraPermissionState
) {
    var isSheetVisible: Boolean by mutableStateOf(false)
        private set

    internal var launchGallery: () -> Unit = {}
    internal var launchCamera: () -> Unit = {}

    /** Opens the source chooser. */
    fun open() {
        isSheetVisible = true
    }

    fun dismiss() {
        isSheetVisible = false
    }

    fun pickFromGallery() {
        isSheetVisible = false
        launchGallery()
    }

    fun takePhoto() {
        isSheetVisible = false
        launchCamera()
    }
}

/**
 * Wires up the gallery and camera launchers for a screen that can hold
 * [remainingSlots] more photos.
 *
 * Hands back raw [Uri]s rather than decoded images: turning a Uri into an
 * uploadable JPEG is disk work that belongs on the ViewModel's scope, not on one
 * that dies with the composition.
 *
 * [onCameraDenied] fires when the camera permission request comes back refused,
 * so the screen can explain itself and offer the settings route.
 */
@Composable
fun rememberMediaPickerState(
    remainingSlots: Int,
    onUrisPicked: (List<Uri>) -> Unit,
    onCameraDenied: () -> Unit = {}
): MediaPickerState {
    val context = LocalContext.current
    val currentOnUrisPicked by rememberUpdatedState(onUrisPicked)
    val currentOnCameraDenied by rememberUpdatedState(onCameraDenied)
    val currentRemaining by rememberUpdatedState(remainingSlots)

    // Set while the camera intent is pending. TakePicture reports only a boolean —
    // it never hands the output Uri back — so losing this to process death while the
    // camera app is in front would lose the photo with it.
    var pendingCameraUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    // Distinguishes "granted because the user just asked to take a photo" from a
    // grant noticed on resume, which must not open the camera on its own.
    var isCameraRequestPending by rememberSaveable { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { saved ->
        val uri = pendingCameraUri
        pendingCameraUri = null
        when {
            saved && uri != null -> currentOnUrisPicked(listOf(uri))
            // Backing out of the camera app leaves the empty file we created for it.
            uri != null -> deleteCameraCapture(context, uri)
        }
    }

    val takePhoto: () -> Unit = {
        runCatching { createCameraCaptureUri(context) }
            .onSuccess { uri ->
                pendingCameraUri = uri
                cameraLauncher.launch(uri)
            }
            .onFailure { Timber.e(it, "MediaPicker: could not create a capture file") }
    }
    val currentTakePhoto by rememberUpdatedState(takePhoto)

    val cameraPermission = rememberCameraPermissionState(
        onGranted = {
            if (isCameraRequestPending) {
                isCameraRequestPending = false
                currentTakePhoto()
            }
        },
        onDenied = {
            isCameraRequestPending = false
            currentOnCameraDenied()
        }
    )

    // maxItems is fixed when the contract is built and must be at least 2
    // (PickMultipleVisualMedia rejects 1 in its constructor), so the contract is
    // rebuilt whenever the free-slot count changes and the single-select contract
    // covers the last remaining slot.
    val multiContract = remember(remainingSlots) {
        ActivityResultContracts.PickMultipleVisualMedia(remainingSlots.coerceAtLeast(2))
    }
    val multiGalleryLauncher = rememberLauncherForActivityResult(multiContract) { uris ->
        // Below API 30 there is no photo picker and the contract falls back to
        // ACTION_OPEN_DOCUMENT with EXTRA_ALLOW_MULTIPLE, which enforces no limit at
        // all — without this clamp an Android 8 user could return twenty images.
        val picked = uris.take(currentRemaining)
        if (picked.isNotEmpty()) currentOnUrisPicked(picked)
    }
    val singleGalleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) currentOnUrisPicked(listOf(uri))
    }

    val state = remember(cameraPermission) {
        MediaPickerState(
            isCameraAvailable = context.hasCameraApp(),
            cameraPermission = cameraPermission
        )
    }

    SideEffect {
        state.launchGallery = {
            // ImageOnly keeps videos out of the picker entirely, which is cheaper
            // than filtering them afterwards.
            val request = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            if (currentRemaining >= 2) {
                multiGalleryLauncher.launch(request)
            } else {
                singleGalleryLauncher.launch(request)
            }
        }
        state.launchCamera = {
            if (cameraPermission.isGranted) {
                currentTakePhoto()
            } else {
                isCameraRequestPending = true
                cameraPermission.requestOrOpenAppSettings()
            }
        }
    }

    return state
}
