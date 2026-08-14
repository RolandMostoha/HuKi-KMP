package hu.mostoha.mobile.kmp.huki.service

import hu.mostoha.mobile.kmp.huki.model.domain.Location
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLLocationAccuracyBest
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
class IosLocationMonitoringService(scope: CoroutineScope) : LocationMonitoringService {

    private val locationManager = CLLocationManager()

    private var locationDelegate: CLLocationManagerDelegateProtocol? = null

    override suspend fun lastKnownLocation(): Location? {
        if (!locationManager.isAuthorized()) {
            return null
        }
        val clLocation = locationManager.location ?: return locationUpdates.replayCache.lastOrNull()
        return Location(
            latitude = clLocation.coordinate.useContents { latitude },
            longitude = clLocation.coordinate.useContents { longitude },
            altitude = clLocation.altitude,
        )
    }

    private fun CLLocationManager.isAuthorized(): Boolean =
        when (authorizationStatus) {
            kCLAuthorizationStatusAuthorizedWhenInUse, kCLAuthorizationStatusAuthorizedAlways -> {
                true
            }
            else -> {
                false
            }
        }

    override val locationUpdates: SharedFlow<Location> = callbackFlow {
        val manager = CLLocationManager()
        manager.desiredAccuracy = kCLLocationAccuracyBest
        manager.distanceFilter = DISTANCE_FILTER_METERS

        fun startIfAuthorized() {
            when (manager.authorizationStatus) {
                kCLAuthorizationStatusAuthorizedWhenInUse, kCLAuthorizationStatusAuthorizedAlways -> {
                    manager.startUpdatingLocation()
                }
                else -> {
                    manager.stopUpdatingLocation()
                }
            }
        }

        val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
            override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
                val clLocation = didUpdateLocations.lastOrNull() as? CLLocation ?: return
                trySend(
                    Location(
                        latitude = clLocation.coordinate.useContents { latitude },
                        longitude = clLocation.coordinate.useContents { longitude },
                        altitude = clLocation.altitude,
                    ),
                )
            }

            override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
                startIfAuthorized()
            }
        }

        // CLLocationManager holds `delegate` weakly; retain it strongly so Kotlin/Native does not garbage-collect
        locationDelegate = delegate
        manager.delegate = delegate
        startIfAuthorized()

        awaitClose {
            manager.stopUpdatingLocation()
            manager.delegate = null
            locationDelegate = null
        }
    }.shareIn(scope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), replay = 1)

    private companion object {
        const val DISTANCE_FILTER_METERS = 5.0
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
