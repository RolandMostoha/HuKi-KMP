package hu.mostoha.mobile.kmp.huki.service

import hu.mostoha.mobile.kmp.huki.model.domain.Location
import kotlinx.coroutines.flow.SharedFlow

interface LocationMonitoringService {
    val locationUpdates: SharedFlow<Location>

    suspend fun lastKnownLocation(): Location?
}
