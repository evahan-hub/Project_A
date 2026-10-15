package io.artly.artworks.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import io.artly.artworks.data.db.ArtworkDao
import io.artly.artworks.data.db.ArtworkEntity
import io.artly.artworks.model.Artwork
import io.artly.artworks.repository.ArtworkRepository
import io.artly.location.model.Coordinates

class RoomArtworkRepository(
    private val artworkDao: ArtworkDao,
) : ArtworkRepository {

    override fun getArtworks(): Flow<List<Artwork>> {
        return artworkDao.getAllArtworks().map { artworks -> artworks.map { it.toDomain() } }
    }

    override fun getArtwork(id: Int): Flow<Artwork?> {
        return artworkDao.getArtworkById(id).map { it?.toDomain() }
    }

    override suspend fun saveArtwork(artwork: Artwork) {
        artworkDao.insertArtwork(artwork.toEntity())
    }

    override suspend fun deleteArtwork(artwork: Artwork) {
        artworkDao.deleteArtwork(artwork.toEntity())
    }

    override suspend fun getArtworksWithoutPlaceName(): List<Artwork> {
        return artworkDao.getArtworksWithoutPlaceName().map { it.toDomain() }
    }

    override suspend fun setPlaceName(id: Int, location: Coordinates, placeName: String?) {
        // Empty marks that there is no name, so that it's not looked up again.
        artworkDao.setPlaceName(id, location.latitude, location.longitude, placeName.orEmpty())
    }
}

private fun ArtworkEntity.toDomain() = Artwork(
    id = id,
    title = title,
    artist = artist,
    location = if (latitude != null && longitude != null) Coordinates(latitude, longitude, altitude) else null,
    dateTime = LocalDateTime.parse(timestamp),
    timeZone = timeZone?.let { runCatching { TimeZone.of(it) }.getOrNull() },
    note = note,
    pictureFile = pictureFile,
    placeName = placeName?.takeIf { it.isNotEmpty() },
)

private fun Artwork.toEntity() = ArtworkEntity(
    id = id,
    title = title,
    artist = artist,
    latitude = location?.latitude,
    longitude = location?.longitude,
    altitude = location?.altitude,
    timestamp = dateTime.toString(),
    timeZone = timeZone?.id,
    note = note,
    pictureFile = pictureFile,
    placeName = placeName,
)
