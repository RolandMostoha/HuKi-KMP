package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlanRequest
import hu.mostoha.mobile.kmp.huki.model.network.GraphhopperRouteResponse
import hu.mostoha.mobile.kmp.huki.model.network.NetworkResult

interface RoutePlannerRepository {
    suspend fun getRoutePlan(request: RoutePlanRequest): NetworkResult<GraphhopperRouteResponse>
}
