package io.artly.ui.platform

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import io.artly.location.model.LocationStatus

@Composable
actual fun LocationPermissionEffect(status: LocationStatus) {
    // The location repository notices the granted permission itself.
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }
    LaunchedEffect(status) {
        if (status is LocationStatus.MissingPermission) {
            launcher.launch(
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            )
        }
    }
}
