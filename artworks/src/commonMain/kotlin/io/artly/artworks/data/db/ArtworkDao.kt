package io.artly.artworks.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtworkDao {
    @Query("SELECT * FROM artworks ORDER BY timestamp DESC")
    fun getAllArtworks(): Flow<List<ArtworkEntity>>

    @Query("SELECT * FROM artworks WHERE id = :id")
    fun getArtworkById(id: Int): Flow<ArtworkEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtwork(artwork: ArtworkEntity)

    @Delete
    suspend fun deleteArtwork(artwork: ArtworkEntity)

    /** Artworks with a location whose place name hasn't been looked up yet. */
    @Query("SELECT * FROM artworks WHERE placeName IS NULL AND latitude IS NOT NULL AND longitude IS NOT NULL")
    suspend fun getArtworksWithoutPlaceName(): List<ArtworkEntity>

    @Query(
        "UPDATE artworks SET placeName = :placeName " +
            "WHERE id = :id AND latitude = :latitude AND longitude = :longitude"
    )
    suspend fun setPlaceName(id: Int, latitude: Double, longitude: Double, placeName: String)
}
