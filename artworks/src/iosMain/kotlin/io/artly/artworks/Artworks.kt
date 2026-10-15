package io.artly.artworks

import androidx.room.Room
import kotlinx.cinterop.ExperimentalForeignApi
import io.artly.artworks.data.RoomArtworkRepository
import io.artly.artworks.data.db.ArtworkDatabase
import io.artly.artworks.data.db.buildArtworkDatabase
import io.artly.artworks.repository.ArtworkRepository
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

/**
 * Creates the repository of the artworks stored in the app's database. Create it only once, as it opens the database.
 */
fun createArtworkRepository(): ArtworkRepository {
    val database = Room.databaseBuilder<ArtworkDatabase>(
        name = "${applicationSupportDirectory()}/${ArtworkDatabase.NAME}",
    ).buildArtworkDatabase()
    return RoomArtworkRepository(database.artworkDao())
}

/** The directory for the app's own data, which users don't see, unlike the documents directory. */
@OptIn(ExperimentalForeignApi::class)
internal fun applicationSupportDirectory(): String {
    val url = NSFileManager.defaultManager.URLForDirectory(
        directory = NSApplicationSupportDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null,
    )
    return requireNotNull(url?.path) { "No application support directory" }
}
