package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlannerProfile
import hu.mostoha.mobile.kmp.huki.model.network.GraphhopperRouteResponse
import hu.mostoha.mobile.kmp.huki.model.network.NetworkResult

class FakeRoutePlannerRepository(
    var result: NetworkResult<GraphhopperRouteResponse> = NetworkResult.Success(GraphhopperRouteResponse(emptyList())),
) : RoutePlannerRepository {
    var callCount = 0
        private set

    override suspend fun getRoutePlan(
        profile: RoutePlannerProfile,
        waypoints: List<Location>,
    ): NetworkResult<GraphhopperRouteResponse> {
        callCount++
        return result
    }
}
