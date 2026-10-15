package io.artly.artworks.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "artworks")
data class ArtworkEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String?,
    val artist: String?,
    /** Null together with [longitude] if the location is unknown. */
    val latitude: Double?,
    val longitude: Double?,
    val altitude: Double?,
    /** Local date and time as ISO 8601, e.g. `2026-05-14T11:49:03.275895`. */
    val timestamp: String,
    /** IANA time zone ID of [timestamp], e.g. `Europe/Berlin`, or null if unknown. */
    val timeZone: String? = null,
    val note: String?,
    /** File name of the picture in the picture storage of the app. */
    val pictureFile: String? = null,
    /** Name of the place, empty if there is none, or null if not looked up yet. */
    val placeName: String? = null,
)
