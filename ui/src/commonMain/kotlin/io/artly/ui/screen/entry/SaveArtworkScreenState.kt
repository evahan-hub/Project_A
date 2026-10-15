package io.artly.ui.screen.entry

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import io.artly.artworks.picture.PhotoMetadata
import io.artly.location.model.Coordinates

/**
 * The form of an artwork, apart from its text, which [SaveArtworkViewModel] keeps as Compose state of its own, as text
 * fields lose keystrokes if their value comes back asynchronously.
 */
data class SaveArtworkScreenState(
    /** Path of the photo of the artwork, or null if it has none. */
    val imagePath: String? = null,
    /** Where the art was seen, or null if unknown, e.g. while the location of the device is unknown. */
    val location: Coordinates? = null,
    val locationSource: LocationSource = LocationSource.DEVICE,
    /** When the art was seen, or null for now. */
    val dateTime: LocalDateTime? = null,
    /** Time zone of [dateTime], or null if unknown. */
    val timeZone: TimeZone? = null,
    /** When and where the photo picked from the gallery was taken, offered to use for the artwork. */
    val photoMetadata: PhotoMetadata? = null,
    /** Whether a saved artwork is edited, until it's loaded. */
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
)

enum class LocationSource {
    /** The location of the device, which follows it while recording. */
    DEVICE,

    /** Where the photo of the artwork was taken. */
    PHOTO,

    /** As saved with the edited artwork. */
    SAVED,

    /** No location, e.g. removed by the user, or the shared photo of the artwork doesn't tell. */
    NONE,
}
