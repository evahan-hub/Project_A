package io.artly.location.repository

import kotlinx.coroutines.flow.StateFlow

interface CountryRepository {
    /** ISO 3166-1 alpha-2 code of the country the device is in, e.g. `DE`, or null if unknown. */
    val currentCountry: StateFlow<String?>
}
