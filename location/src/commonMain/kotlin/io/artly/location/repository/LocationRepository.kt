package io.artly.location.repository

import kotlinx.coroutines.flow.StateFlow
import io.artly.location.model.LocationStatus

interface LocationRepository {
    val locationStatus: StateFlow<LocationStatus>
}
