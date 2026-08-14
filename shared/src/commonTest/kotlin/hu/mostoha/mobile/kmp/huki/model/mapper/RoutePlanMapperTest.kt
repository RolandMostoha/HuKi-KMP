package hu.mostoha.mobile.kmp.huki.model.mapper

import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.GpxWaypoint
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlan
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlannerProfile
import hu.mostoha.mobile.kmp.huki.model.domain.RouteStats
import hu.mostoha.mobile.kmp.huki.model.domain.WaypointType
import hu.mostoha.mobile.kmp.huki.model.network.GraphhopperPath
import hu.mostoha.mobile.kmp.huki.model.network.GraphhopperPoints
import hu.mostoha.mobile.kmp.huki.model.network.GraphhopperProfile
import hu.mostoha.mobile.kmp.huki.model.network.GraphhopperRouteResponse
import hu.mostoha.mobile.kmp.huki.model.network.NetworkError
import hu.mostoha.mobile.kmp.huki.util.calculateTravelTime
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.maplibre.spatialk.units.extensions.inMeters
import org.maplibre.spatialk.units.extensions.meters
import kotlin.test.Test
import kotlin.time.Duration.Companion.hours

class RoutePlanMapperTest {

    @Test
    fun `Given on trails profile, When toRouteRequest, Then hike profile with the trail custom model is used`() {
        val actual = RoutePlannerProfile.ON_TRAILS.toRouteRequest(WAYPOINTS)

        actual.profile shouldBe GraphhopperProfile.HIKE
        actual.chDisabled shouldBe true
        actual.customModel?.priority?.single()?.ifCondition shouldBe "foot_network == MISSING"
        actual.elevation shouldBe true
        actual.pointsEncoded shouldBe false
        actual.instructions shouldBe false
    }

    @Test
    fun `Given shortest route profile, When toRouteRequest, Then hike profile without a custom model is used`() {
        val actual = RoutePlannerProfile.SHORTEST_ROUTE.toRouteRequest(WAYPOINTS)

        actual.profile shouldBe GraphhopperProfile.HIKE
        actual.customModel.shouldBeNull()
        actual.chDisabled.shouldBeNull()
    }

    @Test
    fun `Given bike profile, When toRouteRequest, Then bike profile is used`() {
        val actual = RoutePlannerProfile.BIKE.toRouteRequest(WAYPOINTS)

        actual.profile shouldBe GraphhopperProfile.BIKE
        actual.customModel.shouldBeNull()
    }

    @Test
    fun `Given waypoints, When toRouteRequest, Then points are sent as longitude latitude pairs`() {
        val actual = RoutePlannerProfile.ON_TRAILS.toRouteRequest(WAYPOINTS)

        actual.points shouldBe listOf(listOf(18.9, 47.7), listOf(19.0, 47.8))
    }

    @Test
    fun `Given a route response, When toRoutePlan, Then locations and stats are mapped`() {
        val response = routeResponse()

        val actual = response.toRoutePlan()!!

        actual.locations shouldBe listOf(
            Location(latitude = 47.7, longitude = 18.9, altitude = 300.0),
            Location(latitude = 47.75, longitude = 18.95, altitude = 500.0),
            Location(latitude = 47.8, longitude = 19.0, altitude = 400.0),
        )
        actual.waypoints shouldBe listOf(
            Location(latitude = 47.7, longitude = 18.9),
            Location(latitude = 47.8, longitude = 19.0),
        )
        actual.routeStats.distance.inMeters shouldBe 8500.0
        actual.routeStats.incline shouldBe 240
        actual.routeStats.decline shouldBe 120
        actual.routeStats.travelTime shouldBe actual.locations.calculateTravelTime()
    }

    @Test
    fun `Given a response without paths, When toRoutePlan, Then null is returned`() {
        GraphhopperRouteResponse().toRoutePlan().shouldBeNull()
    }

    @Test
    fun `Given a path without points, When toRoutePlan, Then null is returned`() {
        val response = routeResponse(points = emptyList())

        response.toRoutePlan().shouldBeNull()
    }

    @Test
    fun `Given a route plan with distinct edges, When toGpxWaypoints, Then start and end waypoints are mapped`() {
        val routePlan = routePlan(
            waypoints = listOf(
                Location(latitude = 47.7, longitude = 18.9),
                Location(latitude = 47.75, longitude = 18.95),
                Location(latitude = 47.8, longitude = 19.0),
            ),
        )

        val actual = routePlan.toGpxWaypoints()

        actual shouldBe listOf(
            GpxWaypoint(Location(latitude = 47.7, longitude = 18.9), WaypointType.START),
            GpxWaypoint(Location(latitude = 47.75, longitude = 18.95), WaypointType.INTERMEDIATE),
            GpxWaypoint(Location(latitude = 47.8, longitude = 19.0), WaypointType.END),
        )
    }

    @Test
    fun `Given a round trip route plan, When toGpxWaypoints, Then a single round trip waypoint is mapped`() {
        val routePlan = routePlan(
            waypoints = listOf(
                Location(latitude = 47.7, longitude = 18.9),
                Location(latitude = 47.8, longitude = 19.0),
                Location(latitude = 47.7, longitude = 18.9),
            ),
        )

        val actual = routePlan.toGpxWaypoints()

        actual shouldBe listOf(
            GpxWaypoint(Location(latitude = 47.7, longitude = 18.9), WaypointType.ROUND_TRIP),
            GpxWaypoint(Location(latitude = 47.8, longitude = 19.0), WaypointType.INTERMEDIATE),
        )
    }

    private fun routePlan(waypoints: List<Location>) =
        RoutePlan(
            waypoints = waypoints,
            locations = waypoints,
            routeStats = RouteStats(
                travelTime = 1.hours,
                distance = 8500.meters,
                incline = 240,
                decline = 120,
            ),
        )

    private fun routeResponse(
        points: List<List<Double>> = listOf(
            listOf(18.9, 47.7, 300.0),
            listOf(18.95, 47.75, 500.0),
            listOf(19.0, 47.8, 400.0),
        ),
    ) = GraphhopperRouteResponse(
        paths = listOf(
            GraphhopperPath(
                distance = 8500.0,
                time = 3_600_000,
                ascend = 239.6,
                descend = 120.4,
                points = GraphhopperPoints(coordinates = points),
                snappedWaypoints = GraphhopperPoints(
                    coordinates = listOf(listOf(18.9, 47.7), listOf(19.0, 47.8)),
                ),
            ),
        ),
    )

    @Test
    fun `Given rate limited error, When toRoutePlanInfoViewData, Then the daily limit info is returned`() {
        val actual = NetworkError.RATE_LIMITED.toRoutePlanInfoViewData()

        actual.title shouldBe SharedRes.strings.route_planner_daily_limit_error_title
        actual.message shouldBe SharedRes.strings.route_planner_daily_limit_error_message
    }

    @Test
    fun `Given any other error, When toRoutePlanInfoViewData, Then the generic network info is returned`() {
        NetworkError.entries
            .filterNot { error -> error == NetworkError.RATE_LIMITED }
            .forEach { error ->
                error.toRoutePlanInfoViewData() shouldBe error.toInfoViewData()
            }
    }

    companion object {
        private val WAYPOINTS = listOf(
            Location(latitude = 47.7, longitude = 18.9),
            Location(latitude = 47.8, longitude = 19.0),
        )
    }
}
