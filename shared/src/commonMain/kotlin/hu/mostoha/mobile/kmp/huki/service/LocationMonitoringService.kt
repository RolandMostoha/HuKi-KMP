package hu.mostoha.mobile.kmp.huki.service

import hu.mostoha.mobile.kmp.huki.model.domain.Location
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

interface LocationMonitoringService {
    val locationUpdates: SharedFlow<Location>

    suspend fun lastKnownLocation(): Location?
}

/**
 * Emits the last known location first (if available) then continues with live [locationUpdates].
 */
fun LocationMonitoringService.locations(): Flow<Location> =
    flow {
        lastKnownLocation()?.let { emit(it) }
        emitAll(locationUpdates)
    }
