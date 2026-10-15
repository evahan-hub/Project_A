package io.artly.location.data

import android.content.Context
import android.telephony.TelephonyManager
import java.util.Locale
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
import io.artly.location.model.Coordinates
import io.artly.location.model.LocationStatus
import io.artly.location.model.distanceTo
import io.artly.location.repository.CountryRepository
import io.artly.location.repository.LocationRepository

/**
 * Determines the country from the device location. Falls back to the country of the mobile network,
 * which also works offline, and to the region of the device locale.
 */
class GeocoderCountryRepository(
    private val context: Context,
    locationRepository: LocationRepository,
) : CountryRepository {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override val currentCountry: StateFlow<String?> = locationRepository.locationStatus
        .map { (it as? LocationStatus.Available)?.coordinates }
        .filterNotNull()
        // The country only needs to be resolved again after moving a considerable distance.
        .distinctUntilChanged { old, new -> old.distanceTo(new) < RESOLVE_DISTANCE_METERS }
        .map { location -> geocodeCountry(location) ?: fallbackCountry() }
        .onStart { emit(fallbackCountry()) }
        .distinctUntilChanged()
        .stateIn(
            scope = repositoryScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = fallbackCountry(),
        )

    private suspend fun geocodeCountry(location: Coordinates): String? {
        val result = geocode(context, location, Locale.ROOT)
        return (result as? GeocodeResult.Found)?.address?.countryCode?.normalized()
    }

    private fun fallbackCountry(): String? {
        val telephonyManager = context.getSystemService(TelephonyManager::class.java)
        return telephonyManager?.networkCountryIso?.normalized()
            ?: Locale.getDefault().country.normalized()
    }

    private fun String.normalized(): String? = uppercase(Locale.ROOT).takeIf { it.length == 2 }

    private companion object {
        const val RESOLVE_DISTANCE_METERS = 10_000f
    }
}
