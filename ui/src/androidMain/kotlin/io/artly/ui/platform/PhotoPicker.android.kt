package io.artly.ui.platform

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import io.artly.artworks.picture.AndroidPictureStorage
import org.koin.compose.koinInject

@Composable
actual fun rememberPhotoPicker(
    onPhotoTaken: (fileName: String) -> Unit,
    onPhotoPicked: (fileName: String) -> Unit,
): PhotoPicker {
    val storage = koinInject<AndroidPictureStorage>()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // The file the camera writes to, saved in case the app is stopped while the camera is open.
    var captureFile by rememberSaveable { mutableStateOf<String?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val fileName = captureFile ?: return@rememberLauncherForActivityResult
        captureFile = null
        if (success) {
            onPhotoTaken(fileName)
        } else {
            coroutineScope.launch { storage.delete(fileName) }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            coroutineScope.launch { storage.importPicture(uri)?.let(onPhotoPicked) }
        }
    }
    // Where a photo was taken can only be read with this permission. The gallery opens either way.
    val mediaLocationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        galleryLauncher.launch("image/*")
    }

    return remember(storage, context) {
        object : PhotoPicker {
            override fun takePhoto() {
                coroutineScope.launch {
                    val (fileName, uri) = storage.createCapture()
                    captureFile = fileName
                    cameraLauncher.launch(uri)
                }
            }

            override fun pickFromGallery() {
                val permission = Manifest.permission.ACCESS_MEDIA_LOCATION
                if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                    galleryLauncher.launch("image/*")
                } else {
                    mediaLocationLauncher.launch(permission)
                }
            }
        }
    }
}
