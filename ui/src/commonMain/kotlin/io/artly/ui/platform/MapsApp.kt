package io.artly.ui.platform

import androidx.compose.runtime.Composable
import io.artly.location.model.Coordinates

/**
 * Remembers a function that opens a location in a maps app, labelled with the given label, e.g. to navigate there.
 */
@Composable
expect fun rememberMapsAppOpener(): (location: Coordinates, label: String) -> Unit
