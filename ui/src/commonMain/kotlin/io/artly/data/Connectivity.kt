package io.artly.data

import kotlinx.coroutines.flow.Flow

/**
 * The network connection of the device.
 */
interface Connectivity {
    /** Emits whenever the device gets a network connection, and right away if it has one. */
    val networkAvailable: Flow<Unit>
}
