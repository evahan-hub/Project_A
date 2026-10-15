package io.artly.artworks

import android.content.Context
import androidx.room.Room
import io.artly.artworks.data.RoomArtworkRepository
import io.artly.artworks.data.db.ArtworkDatabase
import io.artly.artworks.data.db.buildArtworkDatabase
import io.artly.artworks.repository.ArtworkRepository

/**
 * Creates the repository of the artworks stored in the app's database. Create it only once, as it opens the database.
 */
fun createArtworkRepository(context: Context): ArtworkRepository {
    val database = Room.databaseBuilder<ArtworkDatabase>(
        context = context.applicationContext,
        name = context.getDatabasePath(ArtworkDatabase.NAME).absolutePath,
    ).buildArtworkDatabase()
    return RoomArtworkRepository(database.artworkDao())
}
