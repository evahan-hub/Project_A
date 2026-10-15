package io.artly.location.data

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import io.artly.location.model.Coordinates
import io.artly.location.model.LocationStatus
import io.artly.location.repository.LocationRepository
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLLocationAccuracyNearestTenMeters
import platform.Foundation.NSError
import platform.darwin.NSObject

/**
 * The location of the device from Core Location. Asks for the permission to use it while the app is in use, the first
 * time the location is needed.
 */
class CoreLocationRepository : LocationRepository {
    // Core Location calls back on the thread its manager was created on, which must have a run loop.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override val locationStatus: StateFlow<LocationStatus> = callbackFlow {
        val manager = CLLocationManager()
        manager.desiredAccuracy = kCLLocationAccuracyNearestTenMeters

        fun updateStatus() {
            if (isAuthorized(manager)) {
                trySend(LocationStatus.Loading)
                manager.startUpdatingLocation()
            } else {
                launch {
                    // Apple warns that this check can block the main thread.
                    val enabled = withContext(Dispatchers.Default) { CLLocationManager.locationServicesEnabled() }
                    // Unless the user allowed the location meanwhile.
                    if (!isAuthorized(manager)) {
                        send(if (enabled) LocationStatus.MissingPermission else LocationStatus.LocationDisabled)
                    }
                }
            }
        }

        val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
            override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
                updateStatus()
            }

            override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
                (didUpdateLocations.lastOrNull() as? CLLocation)?.let { trySend(LocationStatus.Available(it.toCoordinates())) }
            }

            override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
                // E.g. no location yet, which Core Location keeps trying to get.
            }
        }
        manager.delegate = delegate
        if (manager.authorizationStatus == kCLAuthorizationStatusNotDetermined) {
            manager.requestWhenInUseAuthorization()
        }
        updateStatus()

        awaitClose {
            manager.stopUpdatingLocation()
            manager.delegate = null
        }
    }
        .distinctUntilChanged()
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), LocationStatus.Loading)

    // Also false if location services are turned off, as the status is then denied for all apps.
    private fun isAuthorized(manager: CLLocationManager): Boolean {
        return manager.authorizationStatus == kCLAuthorizationStatusAuthorizedWhenInUse ||
            manager.authorizationStatus == kCLAuthorizationStatusAuthorizedAlways
    }
}

@OptIn(ExperimentalForeignApi::class)
internal fun CLLocation.toCoordinates(): Coordinates {
    val (latitude, longitude) = coordinate.useContents { latitude to longitude }
    // A negative vertical accuracy means the altitude is unknown.
    return Coordinates(latitude, longitude, altitude.takeIf { verticalAccuracy >= 0 })
}
