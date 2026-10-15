package io.artly.location.data

import android.content.Context
import io.artly.location.model.Coordinates
import io.artly.location.model.placeName
import io.artly.location.repository.PlaceNameRepository
import io.artly.location.repository.PlaceNameResult
import java.util.Locale

/**
 * Looks up place names with the Geocoder of Android, which sends the coordinates to Google.
 */
class GeocoderPlaceNameRepository(private val context: Context) : PlaceNameRepository {
    override suspend fun getPlaceName(coordinates: Coordinates): PlaceNameResult {
        return when (val result = geocode(context, coordinates, Locale.getDefault())) {
            is GeocodeResult.Found -> {
                val address = result.address
                placeName(locality = address.locality, municipality = address.subAdminArea, region = address.adminArea)
                    ?.let { PlaceNameResult.Found(it) }
                    ?: PlaceNameResult.NoName
            }
            GeocodeResult.NotFound -> PlaceNameResult.NoName
            GeocodeResult.Failed -> PlaceNameResult.Unavailable
        }
    }
}
