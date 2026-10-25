package hu.mostoha.mobile.kmp.huki.repository

import co.touchlab.kermit.Logger
import hu.mostoha.mobile.kmp.huki.Secrets
import hu.mostoha.mobile.kmp.huki.model.analytics.AnalyticsEvent
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlannerProfile
import hu.mostoha.mobile.kmp.huki.model.mapper.toGraphhopperRateLimit
import hu.mostoha.mobile.kmp.huki.model.mapper.toRouteRequest
import hu.mostoha.mobile.kmp.huki.model.network.GraphhopperRateLimit
import hu.mostoha.mobile.kmp.huki.model.network.GraphhopperRouteResponse
import hu.mostoha.mobile.kmp.huki.model.network.NetworkError
import hu.mostoha.mobile.kmp.huki.model.network.NetworkResult
import hu.mostoha.mobile.kmp.huki.network.handleNetworkCall
import hu.mostoha.mobile.kmp.huki.service.AnalyticsService
import hu.mostoha.mobile.kmp.huki.service.CrashlyticsService
import hu.mostoha.mobile.kmp.huki.util.RoutePlanConstants
import hu.mostoha.mobile.kmp.huki.util.nextUtcMidnight
import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlin.time.Clock
import kotlin.time.Instant

class GraphhopperRoutePlannerRepository(
    private val httpClient: HttpClient,
    private val crashlyticsService: CrashlyticsService,
    private val routingQuotaStore: RoutingQuotaStore,
    private val analyticsService: AnalyticsService,
    private val clock: Clock,
) : RoutePlannerRepository {
    companion object {
        private const val URL_ROUTE = "https://graphhopper.com/api/1/route"
    }

    override suspend fun getRoutePlan(
        profile: RoutePlannerProfile,
        waypoints: List<Location>,
    ): NetworkResult<GraphhopperRouteResponse> {
        var rateLimit: GraphhopperRateLimit? = null

        val result = handleNetworkCall<GraphhopperRouteResponse>(crashlyticsService) {
            httpClient.post(urlString = URL_ROUTE) {
                parameter("key", Secrets.GRAPHHOPPER_API_KEY)
                contentType(ContentType.Application.Json)
                setBody(profile.toRouteRequest(waypoints))
            }.also { response -> rateLimit = response.headers.toGraphhopperRateLimit() }
        }

        rateLimit?.let { updateQuota(result, it) }

        return result
    }

    private suspend fun updateQuota(result: NetworkResult<GraphhopperRouteResponse>, rateLimit: GraphhopperRateLimit) {
        when {
            result is NetworkResult.Success -> updateReserveReached(rateLimit)
            result is NetworkResult.Error && result.error == NetworkError.RATE_LIMITED -> updateBlocked(rateLimit)
        }
    }

    private suspend fun updateReserveReached(rateLimit: GraphhopperRateLimit) {
        val remaining = rateLimit.remaining ?: return
        if (remaining >= RoutePlanConstants.GRAPHHOPPER_CREDIT_RESERVE) return

        val now = clock.now()
        if (routingQuotaStore.getQuotaState(now).isReserveReached) return

        Logger.i { "Routing: GraphHopper credit reserve reached, $remaining credits remaining." }
        routingQuotaStore.setReserveReachedUntil(rateLimit.resetAt(now))
        analyticsService.logEvent(AnalyticsEvent.RoutingGraphhopperReserveReached)
    }

    private suspend fun updateBlocked(rateLimit: GraphhopperRateLimit) {
        Logger.w { "Routing: GraphHopper rate limited, ${rateLimit.remaining} credits remaining." }
        crashlyticsService.log("Routing: GraphHopper rate limited")
        analyticsService.logEvent(AnalyticsEvent.RoutingGraphhopperLimitHit)

        // A 429 with daily credits left is a short burst limit, so only the current request falls back
        val remaining = rateLimit.remaining
        if (remaining != null && remaining > 0) return

        routingQuotaStore.setBlockedUntil(rateLimit.resetAt(clock.now()))
    }

    private fun GraphhopperRateLimit.resetAt(now: Instant): Instant = resetIn?.let { now + it } ?: now.nextUtcMidnight()
}
