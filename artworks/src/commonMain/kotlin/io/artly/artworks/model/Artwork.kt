package io.artly.artworks.model

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import io.artly.location.model.Coordinates

/** A piece of art the user saw and recorded. */
data class Artwork(
    val id: Int = 0,
    val title: String?,
    val artist: String?,
    /** Where the art was seen, or null if unknown, e.g. indoors without a location fix. */
    val location: Coordinates?,
    /** Local date and time at the place the art was seen. */
    val dateTime: LocalDateTime,
    /** Time zone of [dateTime], or null if unknown. */
    val timeZone: TimeZone?,
    val note: String?,
    /** File name of the picture in the picture storage of the app. */
    val pictureFile: String? = null,
    /**
     * Name of the place, e.g. "Amsterdam, North Holland", or null if unknown: not looked up yet, or there is none.
     * Must be null when the location changes, so that it's looked up again.
     */
    val placeName: String? = null,
)
