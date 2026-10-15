package io.artly.artworks.picture

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import io.artly.location.model.Coordinates

/**
 * When and where a photo was taken, as far as its EXIF data tells. Null values are unknown.
 */
data class PhotoMetadata(
    val dateTime: LocalDateTime?,
    val timeZone: TimeZone?,
    val location: Coordinates?,
) {
    val isEmpty: Boolean
        get() = dateTime == null && location == null
}

private val exifDateTime = Regex("""(\d{4}):(\d{2}):(\d{2}) (\d{2}):(\d{2}):(\d{2})""")
private val exifOffset = Regex("""[+-]\d{2}:\d{2}""")

/**
 * Parses an EXIF date and time, e.g. `2026:10:04 14:32:07`, or returns null if it's missing or invalid.
 */
internal fun parseExifDateTime(value: String?): LocalDateTime? {
    val match = value?.trim()?.let { exifDateTime.matchEntire(it) } ?: return null
    val (year, month, day, hour, minute, second) = match.destructured
    return runCatching {
        LocalDateTime(year.toInt(), month.toInt(), day.toInt(), hour.toInt(), minute.toInt(), second.toInt())
    }.getOrNull()
}

/**
 * Parses an EXIF time offset, e.g. `+02:00`, into a time zone of that fixed offset, or returns null.
 */
internal fun parseExifOffset(value: String?): TimeZone? {
    val offset = value?.trim()?.takeIf { exifOffset.matches(it) } ?: return null
    return runCatching { TimeZone.of(offset) }.getOrNull()
}
