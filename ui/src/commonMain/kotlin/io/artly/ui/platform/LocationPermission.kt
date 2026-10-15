package io.artly.ui.platform

import androidx.compose.runtime.Composable
import io.artly.location.model.LocationStatus

/**
 * Asks for the permission to use the location while [status] tells it's missing, where the platform doesn't ask
 * itself when the location is first needed.
 */
@Composable
expect fun LocationPermissionEffect(status: LocationStatus)
