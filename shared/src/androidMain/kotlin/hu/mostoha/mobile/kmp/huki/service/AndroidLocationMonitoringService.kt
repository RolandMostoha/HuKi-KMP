package hu.mostoha.mobile.kmp.huki.service

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import co.touchlab.kermit.Logger
import com.mapbox.common.location.AccuracyLevel
import com.mapbox.common.location.DeviceLocationProvider
import com.mapbox.common.location.IntervalSettings
import com.mapbox.common.location.LocationObserver
import com.mapbox.common.location.LocationProviderRequest
import com.mapbox.common.location.LocationServiceFactory
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@OptIn(ExperimentalCoroutinesApi::class)
class AndroidLocationMonitoringService(
    private val context: Context,
    private val crashlyticsService: CrashlyticsService,
    scope: CoroutineScope,
) : LocationMonitoringService {

    // Re-check on every foreground resume and (re)start
    override val locationUpdates: SharedFlow<Location> = foregroundEvents()
        .map { hasLocationPermission() }
        .distinctUntilChanged()
        .flatMapLatest { granted -> if (granted) locationFlow() else emptyFlow() }
        .shareIn(scope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), replay = 1)

    private fun locationFlow(): Flow<Location> =
        callbackFlow {
            val locationService = LocationServiceFactory.getOrCreate()
            val request = LocationProviderRequest.Builder()
                .interval(
                    IntervalSettings.Builder()
                        .interval(UPDATE_INTERVAL_MS)
                        .minimumInterval(MIN_UPDATE_INTERVAL_MS)
                        .build(),
                )
                .accuracy(AccuracyLevel.HIGH)
                .build()

            var deviceLocationProvider: DeviceLocationProvider? = null

            val observer = LocationObserver { locations ->
                locations.lastOrNull()?.let { mapboxLocation ->
                    trySend(
                        Location(
                            latitude = mapboxLocation.latitude,
                            longitude = mapboxLocation.longitude,
                            altitude = mapboxLocation.altitude,
                        ),
                    )
                }
            }

            locationService.getDeviceLocationProvider(request).fold(
                { error ->
                    Logger.e { "Failed to get location provider: $error" }
                    crashlyticsService.recordException(IllegalStateException("Failed to get location provider: $error"))
                },
                { provider ->
                    deviceLocationProvider = provider
                    provider.addLocationObserver(observer)
                },
            )

            awaitClose {
                deviceLocationProvider?.removeLocationObserver(observer)
            }
        }

    override suspend fun lastKnownLocation(): Location? {
        if (!hasLocationPermission()) {
            return null
        }

        val provider = LocationServiceFactory.getOrCreate()
            .getDeviceLocationProvider(LocationProviderRequest.Builder().build())
            .value ?: return null

        return suspendCancellableCoroutine { continuation ->
            val cancelable = provider.getLastLocation { location ->
                continuation.resume(
                    location?.let { Location(it.latitude, it.longitude, it.altitude) },
                )
            }
            continuation.invokeOnCancellation { cancelable.cancel() }
        }
    }

    private fun foregroundEvents(): Flow<Unit> =
        callbackFlow {
            val application = context.applicationContext as Application
            val callbacks = object : Application.ActivityLifecycleCallbacks {
                override fun onActivityResumed(activity: Activity) {
                    trySend(Unit)
                }

                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
                override fun onActivityStarted(activity: Activity) = Unit
                override fun onActivityPaused(activity: Activity) = Unit
                override fun onActivityStopped(activity: Activity) = Unit
                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
                override fun onActivityDestroyed(activity: Activity) = Unit
            }

            trySend(Unit)
            application.registerActivityLifecycleCallbacks(callbacks)

            awaitClose {
                application.unregisterActivityLifecycleCallbacks(callbacks)
            }
        }

    private fun hasLocationPermission(): Boolean {
        val fine = context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        return fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED
    }

    private companion object {
        const val UPDATE_INTERVAL_MS = 1_000L
        const val MIN_UPDATE_INTERVAL_MS = 500L
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
