package io.artly.location.data

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import io.artly.location.model.Coordinates
import java.io.IOException
import java.util.Locale
import kotlin.coroutines.resume

internal sealed interface GeocodeResult {
    data class Found(val address: Address) : GeocodeResult

    /** There is no address at the location, e.g. at sea. */
    data object NotFound : GeocodeResult

    /** The address couldn't be looked up, e.g. without a network connection. */
    data object Failed : GeocodeResult
}

private const val TAG = "Geocoding"

/**
 * Looks up the address at [coordinates], in the language of [locale].
 */
internal suspend fun geocode(context: Context, coordinates: Coordinates, locale: Locale): GeocodeResult {
    if (!Geocoder.isPresent()) return GeocodeResult.Failed
    val geocoder = Geocoder(context, locale)
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { continuation ->
                geocoder.getFromLocation(
                    coordinates.latitude,
                    coordinates.longitude,
                    1,
                    object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            continuation.resume(addresses.firstOrNull().toResult())
                        }

                        override fun onError(errorMessage: String?) {
                            Log.w(TAG, "Failed to geocode location: $errorMessage")
                            continuation.resume(GeocodeResult.Failed)
                        }
                    },
                )
            }
        } else {
            withContext(Dispatchers.IO) {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(coordinates.latitude, coordinates.longitude, 1)?.firstOrNull().toResult()
            }
        }
    } catch (e: IOException) {
        // Geocoding requires a network connection.
        Log.w(TAG, "Failed to geocode location", e)
        GeocodeResult.Failed
    }
}

private fun Address?.toResult() = if (this == null) GeocodeResult.NotFound else GeocodeResult.Found(this)
