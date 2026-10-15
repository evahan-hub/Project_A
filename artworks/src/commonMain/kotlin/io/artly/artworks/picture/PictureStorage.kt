package io.artly.artworks.picture

/**
 * The pictures of artworks, stored in the app's private files by file name: pictures taken with the camera, and copies of
 * pictures picked from the gallery. Pictures of other apps are never touched.
 */
interface PictureStorage {
    /**
     * Returns the absolute path of the picture with the given file name, to show it.
     */
    fun path(fileName: String): String

    /**
     * Reads when and where the picture with the given file name was taken. Returns null if it doesn't tell.
     */
    suspend fun readMetadata(fileName: String): PhotoMetadata?

    /**
     * Deletes the picture with the given file name. Returns true if a file was deleted.
     */
    suspend fun delete(fileName: String): Boolean
}

/** Whether [fileName] names a file directly in the picture directory, rather than a path leading out of it. */
internal fun isPlainFileName(fileName: String): Boolean {
    return fileName.isNotEmpty() && fileName != "." && fileName != ".." && '/' !in fileName && '\\' !in fileName
}
