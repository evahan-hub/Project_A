package io.artly.artworks.data.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(entities = [ArtworkEntity::class], version = 1)
@ConstructedBy(ArtworkDatabaseConstructor::class)
abstract class ArtworkDatabase : RoomDatabase() {
    abstract fun artworkDao(): ArtworkDao

    companion object {
        const val NAME = "artworks.db"
    }
}

// Implemented by Room for each platform.
@Suppress("KotlinNoActualForExpect")
expect object ArtworkDatabaseConstructor : RoomDatabaseConstructor<ArtworkDatabase> {
    override fun initialize(): ArtworkDatabase
}

/**
 * Builds the database with the configuration of all platforms. It holds the user's artworks, so it must never be wiped
 * on a schema change: bump the version together with a migration instead.
 */
internal fun RoomDatabase.Builder<ArtworkDatabase>.buildArtworkDatabase(): ArtworkDatabase {
    return setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}
