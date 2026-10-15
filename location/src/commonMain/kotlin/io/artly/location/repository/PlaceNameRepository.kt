package io.artly.location.repository

import io.artly.location.model.Coordinates

/**
 * Looks up the names of places, e.g. "Hoog Soeren, Gelderland".
 */
interface PlaceNameRepository {
    suspend fun getPlaceName(coordinates: Coordinates): PlaceNameResult
}

sealed interface PlaceNameResult {
    data class Found(val name: String) : PlaceNameResult

    /** The place has no name, e.g. at sea. */
    data object NoName : PlaceNameResult

    /** The name couldn't be looked up, e.g. without a network connection, so try again later. */
    data object Unavailable : PlaceNameResult
}
