package io.artly.artworks.data

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.IO
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import io.artly.artworks.data.db.ArtworkDatabase
import io.artly.artworks.model.Artwork
import io.artly.location.model.Coordinates
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RoomArtworkRepositoryTest {

    private val database = Room.inMemoryDatabaseBuilder<ArtworkDatabase>()
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
    private val repository = RoomArtworkRepository(database.artworkDao())

    private val here = Coordinates(52.3600, 4.8852)
    private fun artwork(id: Int, location: Coordinates? = here) = Artwork(
        id = id,
        title = "The Milkmaid",
        artist = "Johannes Vermeer",
        location = location,
        dateTime = LocalDateTime(2026, 10, 4, 14, 32),
        timeZone = null,
        note = null,
    )

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun savedArtworksAreReadBack() = runTest {
        repository.saveArtwork(artwork(1))

        assertEquals(artwork(1), repository.getArtwork(1).first())
    }

    @Test
    fun artworksWithoutLocationAreKeptAndNotLookedUp() = runTest {
        repository.saveArtwork(artwork(1, location = null))

        assertEquals(artwork(1, location = null), repository.getArtwork(1).first())
        assertEquals(emptyList(), repository.getArtworksWithoutPlaceName())
    }

    @Test
    fun deletedArtworksAreGone() = runTest {
        repository.saveArtwork(artwork(1))
        repository.deleteArtwork(artwork(1))

        assertNull(repository.getArtwork(1).first())
        assertEquals(emptyList(), repository.getArtworks().first())
    }

    @Test
    fun placeNamesAreLookedUpOnce_alsoIfThereIsNone() = runTest {
        repository.saveArtwork(artwork(1))
        repository.saveArtwork(artwork(2))
        assertEquals(listOf(1, 2), repository.getArtworksWithoutPlaceName().map { it.id })

        repository.setPlaceName(1, here, "Amsterdam, North Holland")
        repository.setPlaceName(2, here, null)

        assertEquals(emptyList(), repository.getArtworksWithoutPlaceName())
        assertEquals("Amsterdam, North Holland", repository.getArtwork(1).first()?.placeName)
        assertNull(repository.getArtwork(2).first()?.placeName)
    }

    @Test
    fun placeNamesOfAnotherLocationAreIgnored() = runTest {
        // The location changed while the name of the old one was looked up.
        repository.saveArtwork(artwork(1, location = Coordinates(52.0, 5.0)))

        repository.setPlaceName(1, here, "Amsterdam, North Holland")

        assertNull(repository.getArtwork(1).first()?.placeName)
        assertEquals(listOf(1), repository.getArtworksWithoutPlaceName().map { it.id })
    }
}
