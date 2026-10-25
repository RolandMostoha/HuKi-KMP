package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.model.domain.RoutingQuotaState
import kotlin.time.Instant

class FakeRoutingQuotaStore(
    var reserveReachedUntil: Instant? = null,
    var blockedUntil: Instant? = null,
) : RoutingQuotaStore {
    override suspend fun getQuotaState(now: Instant): RoutingQuotaState =
        RoutingQuotaState(
            isReserveReached = reserveReachedUntil?.let { now < it } ?: false,
            isBlocked = blockedUntil?.let { now < it } ?: false,
        )

    override suspend fun setReserveReachedUntil(until: Instant) {
        reserveReachedUntil = until
    }

    override suspend fun setBlockedUntil(until: Instant) {
        blockedUntil = until
    }
}
