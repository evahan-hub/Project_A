package io.artly.location.data

import io.artly.location.model.Coordinates
import io.artly.location.model.placeName
import io.artly.location.repository.PlaceNameRepository
import io.artly.location.repository.PlaceNameResult
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale

/**
 * Looks up place names with Apple's geocoder, which sends the coordinates to Apple.
 */
class GeocoderPlaceNameRepository : PlaceNameRepository {
    override suspend fun getPlaceName(coordinates: Coordinates): PlaceNameResult {
        return when (val result = geocode(coordinates, NSLocale.currentLocale)) {
            is GeocodeResult.Found -> {
                val placemark = result.placemark
                placeName(
                    locality = placemark.locality,
                    municipality = placemark.subAdministrativeArea,
                    region = placemark.administrativeArea,
                )?.let { PlaceNameResult.Found(it) } ?: PlaceNameResult.NoName
            }
            GeocodeResult.NotFound -> PlaceNameResult.NoName
            GeocodeResult.Failed -> PlaceNameResult.Unavailable
        }
    }
}
