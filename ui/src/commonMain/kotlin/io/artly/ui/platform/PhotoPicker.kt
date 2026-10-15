package io.artly.ui.platform

import androidx.compose.runtime.Composable

/**
 * Takes a photo with the camera or picks one from the gallery, and stores it in the [io.artly.artworks.picture.PictureStorage].
 */
interface PhotoPicker {
    fun takePhoto()

    fun pickFromGallery()
}

/**
 * Remembers a [PhotoPicker], which calls [onPhotoTaken] or [onPhotoPicked] with the file name of the stored photo.
 * Nothing is called if the user cancels, or the photo can't be stored.
 */
@Composable
expect fun rememberPhotoPicker(
    onPhotoTaken: (fileName: String) -> Unit,
    onPhotoPicked: (fileName: String) -> Unit,
): PhotoPicker
