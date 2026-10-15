package io.artly.ui.platform

import androidx.compose.runtime.Composable
import io.artly.location.model.LocationStatus

// Core Location asks itself, see CoreLocationRepository.
@Composable
actual fun LocationPermissionEffect(status: LocationStatus) {}
