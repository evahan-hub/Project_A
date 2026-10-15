package io.artly.data

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Notices shown once, until the user dismisses them. Kept in [settings] of their own.
 */
class Notices(private val settings: Settings) {
    private val _placeNamesDismissed = MutableStateFlow(settings.getBoolean(KEY_PLACE_NAMES, false))

    /** Whether the user dismissed the notice that place names are looked up online, e.g. with Google. */
    val placeNamesDismissed: StateFlow<Boolean> = _placeNamesDismissed.asStateFlow()

    fun dismissPlaceNames() {
        settings.putBoolean(KEY_PLACE_NAMES, true)
        _placeNamesDismissed.value = true
    }

    private companion object {
        const val KEY_PLACE_NAMES = "place_names"
    }
}
