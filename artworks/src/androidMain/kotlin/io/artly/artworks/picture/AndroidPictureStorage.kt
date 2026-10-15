package io.artly.artworks.picture

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.IOException
import java.io.InputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import io.artly.location.model.Coordinates

/**
 * Stores pictures in the app's files directory, shared with the camera through a `FileProvider`.
 */
class AndroidPictureStorage(
    private val context: Context,
) : PictureStorage {
    private val authority = "${context.packageName}.provider"

    private val pictureDir: File
        get() = File(context.filesDir, PICTURE_DIR).apply { mkdirs() }

    /**
     * Creates an empty picture for the camera to write to. Returns its file name, and the URI to pass to the camera.
     */
    suspend fun createCapture(): Pair<String, Uri> = withContext(Dispatchers.IO) {
        val file = createPictureFile("jpg")
        file.name to FileProvider.getUriForFile(context, authority, file)
    }

    /**
     * Copies a picture owned by another app, e.g. picked from the gallery, into the app's storage.
     * Access to such pictures is only granted temporarily, so they can't be referenced directly.
     *
     * Returns the file name of the copy, or null if the picture couldn't be read.
     */
    suspend fun importPicture(source: Uri): String? = withContext(Dispatchers.IO) {
        val extension = context.contentResolver.getType(source)
            ?.let { MimeTypeMap.getSingleton().getExtensionFromMimeType(it) }
            ?: "jpg"
        val file = createPictureFile(extension)
        try {
            val original = openOriginal(source)
            val input = original
                ?: context.contentResolver.openInputStream(source)
                ?: throw IOException("Can't open $source")
            input.use { inputStream ->
                file.outputStream().use { inputStream.copyTo(it) }
            }
            // Tells how a picture without a location came, as where it was taken is private, it isn't logged.
            Log.i(TAG, "Imported ${source.authority} as ${if (original != null) "original" else "given"} $extension")
            file.name
        } catch (e: IOException) {
            Log.w(TAG, "Failed to import picture $source", e)
            file.delete()
            null
        } catch (e: SecurityException) {
            Log.w(TAG, "No permission to import picture $source", e)
            file.delete()
            null
        }
    }

    /**
     * Opens the original of a picture from the gallery, with the location it was taken at, which Android removes from
     * pictures unless the app may access it. Returns null if the original isn't available.
     */
    private fun openOriginal(source: Uri): InputStream? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null
        val permission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_MEDIA_LOCATION)
        if (permission != PackageManager.PERMISSION_GRANTED) return null
        return try {
            val mediaUri = MediaStore.getMediaUri(context, source) ?: source
            context.contentResolver.openInputStream(MediaStore.setRequireOriginal(mediaUri))
        } catch (e: Exception) {
            // Not a picture of the media store, e.g. from a cloud storage.
            Log.i(TAG, "No original of $source", e)
            null
        }
    }

    override suspend fun readMetadata(fileName: String): PhotoMetadata? = withContext(Dispatchers.IO) {
        val file = file(fileName) ?: return@withContext null
        try {
            val exif = ExifInterface(file)
            val altitude = exif.getAttribute(ExifInterface.TAG_GPS_ALTITUDE)?.let { exif.getAltitude(0.0) }
            Log.i(
                TAG,
                "Metadata of $fileName: date ${exif.hasAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)}, " +
                    "GPS ${exif.hasAttribute(ExifInterface.TAG_GPS_LATITUDE)}, location ${exif.latLong != null}",
            )
            PhotoMetadata(
                dateTime = parseExifDateTime(exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)),
                timeZone = parseExifOffset(exif.getAttribute(ExifInterface.TAG_OFFSET_TIME_ORIGINAL)),
                location = exif.latLong?.let { (latitude, longitude) -> Coordinates(latitude, longitude, altitude) },
            ).takeUnless { it.isEmpty }
        } catch (e: IOException) {
            Log.w(TAG, "Failed to read the metadata of $fileName", e)
            null
        }
    }

    override fun path(fileName: String): String = File(pictureDir, fileName).path

    override suspend fun delete(fileName: String): Boolean = withContext(Dispatchers.IO) {
        file(fileName)?.delete() ?: false
    }

    private fun createPictureFile(extension: String): File {
        return File.createTempFile("artly_${System.currentTimeMillis()}_", ".$extension", pictureDir)
    }

    private fun file(fileName: String): File? = File(pictureDir, fileName).takeIf { isPlainFileName(fileName) }

    private companion object {
        const val TAG = "PictureStorage"

        // Must match the files-path entry in the app's res/xml/file_paths.xml.
        const val PICTURE_DIR = "Pictures"
    }
}
