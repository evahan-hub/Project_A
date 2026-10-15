package io.artly.location.model

sealed interface LocationStatus {
    data object MissingPermission : LocationStatus
    data object LocationDisabled : LocationStatus
    data object Loading : LocationStatus
    data class Available(val coordinates: Coordinates) : LocationStatus
}
