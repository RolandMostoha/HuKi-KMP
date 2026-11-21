package hu.mostoha.mobile.kmp.huki.repository

import co.touchlab.kermit.Logger
import hu.mostoha.mobile.kmp.huki.model.analytics.AnalyticsEvent
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlanRequest
import hu.mostoha.mobile.kmp.huki.model.domain.RoutingMode
import hu.mostoha.mobile.kmp.huki.model.domain.contains
import hu.mostoha.mobile.kmp.huki.model.network.GraphhopperRouteResponse
import hu.mostoha.mobile.kmp.huki.model.network.NetworkError
import hu.mostoha.mobile.kmp.huki.model.network.NetworkResult
import hu.mostoha.mobile.kmp.huki.service.AnalyticsService
import hu.mostoha.mobile.kmp.huki.util.RoutePlanConstants
import kotlin.time.Clock

/**
 * Circuit breaker: GraphHopper stays primary, routes inside Hungary move to HuKi-Routing once its credits run low.
 */
class FallbackRoutePlannerRepository(
    private val graphhopperRepository: RoutePlannerRepository,
    private val hukiRoutingRepository: RoutePlannerRepository,
    private val routingQuotaStore: RoutingQuotaStore,
    private val analyticsService: AnalyticsService,
    private val clock: Clock,
    private val routingMode: RoutingMode,
) : RoutePlannerRepository {
    companion object {
        private val GRAPHHOPPER_FALLBACK_ERRORS = setOf(NetworkError.RATE_LIMITED, NetworkError.INTERNAL_SERVER_ERROR)

        /**
         * HuKi-Routing errors which are not caused by HuKi-Routing itself, so they are shown as they are.
         */
        private val HUKI_ROUTING_CLIENT_ERRORS = setOf(NetworkError.NO_INTERNET, NetworkError.REQUEST_TIMEOUT)
    }

    override suspend fun getRoutePlan(request: RoutePlanRequest): NetworkResult<GraphhopperRouteResponse> =
        when (routingMode) {
            RoutingMode.AUTO -> getRoutePlanWithFallback(request)
            RoutingMode.GRAPHHOPPER_ONLY -> graphhopperRepository.getRoutePlan(request)
            RoutingMode.HUKI_ROUTING_ONLY -> hukiRoutingRepository.getRoutePlan(request)
        }

    private suspend fun getRoutePlanWithFallback(request: RoutePlanRequest): NetworkResult<GraphhopperRouteResponse> {
        val quotaState = routingQuotaStore.getQuotaState(clock.now())
        val isInHukiRoutingArea = request.waypoints.all { RoutePlanConstants.HUKI_ROUTING_BOUNDING_BOX.contains(it) }

        return when {
            quotaState.isBlocked && isInHukiRoutingArea -> getHukiRoutingRoutePlan(request).orLimitReached()
            quotaState.isBlocked -> NetworkResult.Error(NetworkError.RATE_LIMITED)
            quotaState.isReserveReached && isInHukiRoutingArea -> getReserveRoutePlan(request)
            else -> getGraphhopperRoutePlan(request, isInHukiRoutingArea)
        }
    }

    private suspend fun getReserveRoutePlan(request: RoutePlanRequest): NetworkResult<GraphhopperRouteResponse> {
        val hukiRoutingResult = getHukiRoutingRoutePlan(request)
        if (hukiRoutingResult is NetworkResult.Success || hukiRoutingResult.isClientError()) {
            return hukiRoutingResult
        }

        analyticsService.logEvent(AnalyticsEvent.RoutingGraphhopperAfterHukiFailure)

        return graphhopperRepository.getRoutePlan(request)
    }

    private suspend fun getGraphhopperRoutePlan(
        request: RoutePlanRequest,
        isInHukiRoutingArea: Boolean,
    ): NetworkResult<GraphhopperRouteResponse> {
        val graphhopperResult = graphhopperRepository.getRoutePlan(request)
        if (graphhopperResult !is NetworkResult.Error) return graphhopperResult
        if (graphhopperResult.error !in GRAPHHOPPER_FALLBACK_ERRORS || !isInHukiRoutingArea) return graphhopperResult

        val hukiRoutingResult = getHukiRoutingRoutePlan(request)

        return if (hukiRoutingResult is NetworkResult.Success) hukiRoutingResult else graphhopperResult
    }

    private suspend fun getHukiRoutingRoutePlan(request: RoutePlanRequest): NetworkResult<GraphhopperRouteResponse> {
        val result = hukiRoutingRepository.getRoutePlan(request)

        when (result) {
            is NetworkResult.Success -> analyticsService.logEvent(AnalyticsEvent.RoutingHukiServed)
            is NetworkResult.Error -> {
                Logger.w { "Routing: HuKi-Routing failed with ${result.error}." }
                if (result.error !in HUKI_ROUTING_CLIENT_ERRORS) {
                    analyticsService.logEvent(AnalyticsEvent.RoutingHukiFailed)
                }
            }
        }

        return result
    }

    private fun NetworkResult<GraphhopperRouteResponse>.orLimitReached(): NetworkResult<GraphhopperRouteResponse> =
        if (this is NetworkResult.Error && !isClientError()) {
            NetworkResult.Error(NetworkError.RATE_LIMITED)
        } else {
            this
        }

    private fun NetworkResult<*>.isClientError(): Boolean =
        this is NetworkResult.Error && error in HUKI_ROUTING_CLIENT_ERRORS
}
