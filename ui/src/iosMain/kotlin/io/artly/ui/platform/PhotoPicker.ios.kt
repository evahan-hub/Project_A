package io.artly.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.getValue
import androidx.compose.ui.uikit.LocalUIViewController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import io.artly.artworks.picture.IosPictureStorage
import org.koin.compose.koinInject
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.darwin.NSObject

@Composable
actual fun rememberPhotoPicker(
    onPhotoTaken: (fileName: String) -> Unit,
    onPhotoPicked: (fileName: String) -> Unit,
): PhotoPicker {
    val storage = koinInject<IosPictureStorage>()
    val viewController = LocalUIViewController.current
    val coroutineScope = rememberCoroutineScope()
    val currentOnPhotoTaken by rememberUpdatedState(onPhotoTaken)
    val currentOnPhotoPicked by rememberUpdatedState(onPhotoPicked)

    return remember(storage, viewController) {
        fun import(data: NSData?, onImported: (String) -> Unit) {
            data ?: return
            coroutineScope.launch(Dispatchers.Main) { storage.importPicture(data)?.let(onImported) }
        }

        // The pickers only keep weak references to their delegates.
        val cameraDelegate = object : NSObject(),
            UIImagePickerControllerDelegateProtocol,
            UINavigationControllerDelegateProtocol {
            override fun imagePickerController(
                picker: UIImagePickerController,
                didFinishPickingMediaWithInfo: Map<Any?, *>,
            ) {
                picker.dismissViewControllerAnimated(true, completion = null)
                val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
                import(image?.let { UIImageJPEGRepresentation(it, 1.0) }) { currentOnPhotoTaken(it) }
            }

            override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
                picker.dismissViewControllerAnimated(true, completion = null)
            }
        }

        val galleryDelegate = object : NSObject(), PHPickerViewControllerDelegateProtocol {
            override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
                picker.dismissViewControllerAnimated(true, completion = null)
                val provider = (didFinishPicking.firstOrNull() as? PHPickerResult)?.itemProvider ?: return
                // The original file, with when and where the photo was taken, which is deleted after the handler.
                provider.loadFileRepresentationForTypeIdentifier("public.image") { url: NSURL?, _ ->
                    import(url?.let { NSData.dataWithContentsOfURL(it) }) { currentOnPhotoPicked(it) }
                }
            }
        }

        object : PhotoPicker {
            override fun takePhoto() {
                val camera = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
                // E.g. in the simulator.
                if (!UIImagePickerController.isSourceTypeAvailable(camera)) return
                val picker = UIImagePickerController()
                picker.sourceType = camera
                picker.delegate = cameraDelegate
                viewController.presentViewController(picker, animated = true, completion = null)
            }

            override fun pickFromGallery() {
                val configuration = PHPickerConfiguration()
                configuration.filter = PHPickerFilter.imagesFilter
                configuration.selectionLimit = 1
                val picker = PHPickerViewController(configuration)
                picker.delegate = galleryDelegate
                viewController.presentViewController(picker, animated = true, completion = null)
            }
        }
    }
}
