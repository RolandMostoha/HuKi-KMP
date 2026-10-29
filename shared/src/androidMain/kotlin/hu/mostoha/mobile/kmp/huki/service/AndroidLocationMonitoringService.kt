package hu.mostoha.mobile.kmp.huki.service

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.location.LocationCompat
import androidx.core.location.altitude.AltitudeConverterCompat
import co.touchlab.kermit.Logger
import com.mapbox.common.location.AccuracyLevel
import com.mapbox.common.location.DeviceLocationProvider
import com.mapbox.common.location.IntervalSettings
import com.mapbox.common.location.LocationObserver
import com.mapbox.common.location.LocationProviderRequest
import com.mapbox.common.location.LocationServiceFactory
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.mapper.toLocation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import android.location.Location as AndroidLocation

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
        .map { it.withMslAltitude() }
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
                locations.lastOrNull()?.let { trySend(it.toLocation()) }
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

        val location = suspendCancellableCoroutine { continuation ->
            val cancelable = provider.getLastLocation { location ->
                continuation.resume(location?.toLocation())
            }
            continuation.invokeOnCancellation { cancelable.cancel() }
        }
        return location?.withMslAltitude()
    }

    /**
     * Android reports altitude above the WGS84 ellipsoid, which differs from the sea level by ~40 m in Hungary.
     */
    private suspend fun Location.withMslAltitude(): Location {
        val ellipsoidAltitude = altitude ?: return this
        val platformLocation = AndroidLocation(MSL_CONVERSION_PROVIDER).also {
            it.latitude = latitude
            it.longitude = longitude
            it.altitude = ellipsoidAltitude
        }
        withContext(Dispatchers.IO) {
            runCatching { AltitudeConverterCompat.addMslAltitudeToLocation(context, platformLocation) }
                .onFailure { Logger.w(it) { "Location: Failed to convert altitude to MSL" } }
        }
        val mslAltitude = if (LocationCompat.hasMslAltitude(platformLocation)) {
            LocationCompat.getMslAltitudeMeters(platformLocation)
        } else {
            null
        }
        return copy(altitude = mslAltitude)
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
        const val MSL_CONVERSION_PROVIDER = "msl_conversion"
    }
}
