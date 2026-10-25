package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.model.domain.RoutingQuotaState
import kotlin.time.Instant

interface RoutingQuotaStore {
    suspend fun getQuotaState(now: Instant): RoutingQuotaState

    suspend fun setReserveReachedUntil(until: Instant)

    suspend fun setBlockedUntil(until: Instant)
}
