package io.artly.location.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import io.artly.location.model.LocationStatus
import io.artly.location.model.distanceTo
import io.artly.location.repository.CountryRepository
import io.artly.location.repository.LocationRepository
import platform.Foundation.NSLocale
import platform.Foundation.NSLocaleCountryCode
import platform.Foundation.currentLocale

/**
 * Determines the country from the device location, falling back to the region of the device.
 */
class GeocoderCountryRepository(locationRepository: LocationRepository) : CountryRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override val currentCountry: StateFlow<String?> = locationRepository.locationStatus
        .map { (it as? LocationStatus.Available)?.coordinates }
        .filterNotNull()
        // The country only needs to be resolved again after moving a considerable distance.
        .distinctUntilChanged { old, new -> old.distanceTo(new) < RESOLVE_DISTANCE_METERS }
        .map { location ->
            val result = geocode(location, NSLocale.currentLocale)
            (result as? GeocodeResult.Found)?.placemark?.ISOcountryCode?.normalized() ?: fallbackCountry()
        }
        .onStart { emit(fallbackCountry()) }
        .distinctUntilChanged()
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), fallbackCountry())

    private fun fallbackCountry(): String? {
        return (NSLocale.currentLocale.objectForKey(NSLocaleCountryCode) as? String)?.normalized()
    }

    private fun String.normalized(): String? = uppercase().takeIf { it.length == 2 }

    private companion object {
        const val RESOLVE_DISTANCE_METERS = 10_000.0
    }
}
