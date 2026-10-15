package io.artly.artworks.repository

import kotlinx.coroutines.flow.Flow
import io.artly.artworks.model.Artwork
import io.artly.location.model.Coordinates

interface ArtworkRepository {
    fun getArtworks(): Flow<List<Artwork>>
    fun getArtwork(id: Int): Flow<Artwork?>
    suspend fun saveArtwork(artwork: Artwork)
    suspend fun deleteArtwork(artwork: Artwork)

    /** Returns the artworks with a location whose place name hasn't been looked up yet. */
    suspend fun getArtworksWithoutPlaceName(): List<Artwork>

    /**
     * Sets the place name of the artwork with the given ID, or that there is none if null, so that it's not looked up
     * again. Does nothing if the location of the artwork changed meanwhile.
     */
    suspend fun setPlaceName(id: Int, location: Coordinates, placeName: String?)
}
