package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlanRequest
import hu.mostoha.mobile.kmp.huki.model.network.GraphhopperRouteResponse
import hu.mostoha.mobile.kmp.huki.model.network.NetworkResult

class FakeRoutePlannerRepository(
    var result: NetworkResult<GraphhopperRouteResponse> = NetworkResult.Success(GraphhopperRouteResponse(emptyList())),
) : RoutePlannerRepository {
    var callCount = 0
        private set

    var lastRequest: RoutePlanRequest? = null
        private set

    override suspend fun getRoutePlan(request: RoutePlanRequest): NetworkResult<GraphhopperRouteResponse> {
        callCount++
        lastRequest = request
        return result
    }
}
