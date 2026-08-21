package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.Secrets
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlannerProfile
import hu.mostoha.mobile.kmp.huki.model.mapper.toRouteRequest
import hu.mostoha.mobile.kmp.huki.model.network.GraphhopperRouteResponse
import hu.mostoha.mobile.kmp.huki.model.network.NetworkResult
import hu.mostoha.mobile.kmp.huki.network.handleNetworkCall
import hu.mostoha.mobile.kmp.huki.service.CrashlyticsService
import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class GraphhopperRoutePlannerRepository(
    private val httpClient: HttpClient,
    private val crashlyticsService: CrashlyticsService,
) : RoutePlannerRepository {
    companion object {
        private const val URL_ROUTE = "https://graphhopper.com/api/1/route"
    }

    override suspend fun getRoutePlan(
        profile: RoutePlannerProfile,
        waypoints: List<Location>,
    ): NetworkResult<GraphhopperRouteResponse> =
        handleNetworkCall(crashlyticsService) {
            httpClient.post(urlString = URL_ROUTE) {
                parameter("key", Secrets.GRAPHHOPPER_API_KEY)
                contentType(ContentType.Application.Json)
                setBody(profile.toRouteRequest(waypoints))
            }
        }
}
