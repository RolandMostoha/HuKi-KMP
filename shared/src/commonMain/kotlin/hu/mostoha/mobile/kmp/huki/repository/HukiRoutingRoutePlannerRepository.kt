package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.Secrets
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlannerProfile
import hu.mostoha.mobile.kmp.huki.model.mapper.toHukiRoutingRouteRequest
import hu.mostoha.mobile.kmp.huki.model.network.GraphhopperRouteResponse
import hu.mostoha.mobile.kmp.huki.model.network.NetworkResult
import hu.mostoha.mobile.kmp.huki.network.NetworkHeaders
import hu.mostoha.mobile.kmp.huki.network.handleNetworkCall
import hu.mostoha.mobile.kmp.huki.service.CrashlyticsService
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * Self-hosted GraphHopper covering Hungary only, with the same API as the hosted GraphHopper.
 */
class HukiRoutingRoutePlannerRepository(
    private val httpClient: HttpClient,
    private val crashlyticsService: CrashlyticsService,
) : RoutePlannerRepository {
    companion object {
        private const val URL_ROUTE = "https://huki-routing-550684341915.europe-west1.run.app/route"
    }

    override suspend fun getRoutePlan(
        profile: RoutePlannerProfile,
        waypoints: List<Location>,
    ): NetworkResult<GraphhopperRouteResponse> =
        handleNetworkCall(crashlyticsService) {
            httpClient.post(urlString = URL_ROUTE) {
                header(NetworkHeaders.HUKI_ROUTING_API_KEY, Secrets.HUKI_ROUTING_API_KEY)
                contentType(ContentType.Application.Json)
                setBody(profile.toHukiRoutingRouteRequest(waypoints))
            }
        }
}
