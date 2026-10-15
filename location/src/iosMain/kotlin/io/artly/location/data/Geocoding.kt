package io.artly.location.data

import kotlinx.coroutines.suspendCancellableCoroutine
import io.artly.location.model.Coordinates
import platform.CoreLocation.CLGeocoder
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLPlacemark
import platform.CoreLocation.kCLErrorDomain
import platform.CoreLocation.kCLErrorGeocodeFoundNoResult
import platform.Foundation.NSLocale
import kotlin.coroutines.resume

internal sealed interface GeocodeResult {
    data class Found(val placemark: CLPlacemark) : GeocodeResult

    /** There is no address at the location, e.g. at sea. */
    data object NotFound : GeocodeResult

    /** The address couldn't be looked up, e.g. without a network connection. */
    data object Failed : GeocodeResult
}

/**
 * Looks up the address at [coordinates] with Apple's geocoder, in the language of [locale].
 */
internal suspend fun geocode(coordinates: Coordinates, locale: NSLocale): GeocodeResult {
    val geocoder = CLGeocoder()
    return suspendCancellableCoroutine { continuation ->
        continuation.invokeOnCancellation { geocoder.cancelGeocode() }
        geocoder.reverseGeocodeLocation(
            CLLocation(latitude = coordinates.latitude, longitude = coordinates.longitude),
            preferredLocale = locale,
        ) { placemarks, error ->
            val result = when {
                error != null && error.domain == kCLErrorDomain && error.code == kCLErrorGeocodeFoundNoResult ->
                    GeocodeResult.NotFound
                error != null -> GeocodeResult.Failed
                else -> (placemarks?.firstOrNull() as? CLPlacemark)?.let { GeocodeResult.Found(it) }
                    ?: GeocodeResult.NotFound
            }
            continuation.resume(result)
        }
    }
}
