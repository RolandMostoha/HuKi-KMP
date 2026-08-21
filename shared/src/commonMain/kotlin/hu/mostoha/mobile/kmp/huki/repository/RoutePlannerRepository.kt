package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlannerProfile
import hu.mostoha.mobile.kmp.huki.model.network.GraphhopperRouteResponse
import hu.mostoha.mobile.kmp.huki.model.network.NetworkResult

interface RoutePlannerRepository {
    suspend fun getRoutePlan(
        profile: RoutePlannerProfile,
        waypoints: List<Location>,
    ): NetworkResult<GraphhopperRouteResponse>
}
