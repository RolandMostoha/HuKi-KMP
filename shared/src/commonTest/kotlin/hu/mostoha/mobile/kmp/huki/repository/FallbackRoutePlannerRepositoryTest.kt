package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.model.analytics.AnalyticsEvent
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlannerProfile
import hu.mostoha.mobile.kmp.huki.model.domain.RoutingMode
import hu.mostoha.mobile.kmp.huki.model.network.GraphhopperPath
import hu.mostoha.mobile.kmp.huki.model.network.GraphhopperPoints
import hu.mostoha.mobile.kmp.huki.model.network.GraphhopperRouteResponse
import hu.mostoha.mobile.kmp.huki.model.network.NetworkError
import hu.mostoha.mobile.kmp.huki.model.network.NetworkResult
import hu.mostoha.mobile.kmp.huki.service.FakeAnalyticsService
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

class FallbackRoutePlannerRepositoryTest {

    private val graphhopperRepository = FakeRoutePlannerRepository(GRAPHHOPPER_SUCCESS)
    private val hukiRoutingRepository = FakeRoutePlannerRepository(HUKI_ROUTING_SUCCESS)
    private val routingQuotaStore = FakeRoutingQuotaStore()
    private val analyticsService = FakeAnalyticsService()

    @Test
    fun `Given no quota limit - When getRoutePlan - Then GraphHopper serves`() {
        runTest {
            val actual = repository().getRoutePlan(PROFILE, HUNGARY_WAYPOINTS)

            actual shouldBe GRAPHHOPPER_SUCCESS
            hukiRoutingRepository.callCount shouldBe 0
            analyticsService.loggedEvents.shouldBeEmpty()
        }
    }

    @Test
    fun `Given GraphHopper rate limited in Hungary - When getRoutePlan - Then HuKi-Routing serves`() {
        runTest {
            graphhopperRepository.result = NetworkResult.Error(NetworkError.RATE_LIMITED)

            val actual = repository().getRoutePlan(PROFILE, HUNGARY_WAYPOINTS)

            actual shouldBe HUKI_ROUTING_SUCCESS
            analyticsService.loggedEvents shouldBe listOf(AnalyticsEvent.RoutingHukiServed)
        }
    }

    @Test
    fun `Given GraphHopper server error in Hungary - When getRoutePlan - Then HuKi-Routing serves`() {
        runTest {
            graphhopperRepository.result = NetworkResult.Error(NetworkError.INTERNAL_SERVER_ERROR)

            val actual = repository().getRoutePlan(PROFILE, HUNGARY_WAYPOINTS)

            actual shouldBe HUKI_ROUTING_SUCCESS
        }
    }

    @Test
    fun `Given GraphHopper bad request in Hungary - When getRoutePlan - Then GraphHopper error returns`() {
        runTest {
            graphhopperRepository.result = NetworkResult.Error(NetworkError.BAD_REQUEST)

            val actual = repository().getRoutePlan(PROFILE, HUNGARY_WAYPOINTS)

            actual shouldBe NetworkResult.Error(NetworkError.BAD_REQUEST)
            hukiRoutingRepository.callCount shouldBe 0
        }
    }

    @Test
    fun `Given GraphHopper rate limited abroad - When getRoutePlan - Then rate limited returns`() {
        runTest {
            graphhopperRepository.result = NetworkResult.Error(NetworkError.RATE_LIMITED)

            val actual = repository().getRoutePlan(PROFILE, ABROAD_WAYPOINTS)

            actual shouldBe NetworkResult.Error(NetworkError.RATE_LIMITED)
            hukiRoutingRepository.callCount shouldBe 0
        }
    }

    @Test
    fun `Given GraphHopper rate limited and HuKi-Routing fails - When getRoutePlan - Then rate limited returns`() {
        runTest {
            graphhopperRepository.result = NetworkResult.Error(NetworkError.RATE_LIMITED)
            hukiRoutingRepository.result = NetworkResult.Error(NetworkError.BAD_REQUEST)

            val actual = repository().getRoutePlan(PROFILE, HUNGARY_WAYPOINTS)

            actual shouldBe NetworkResult.Error(NetworkError.RATE_LIMITED)
            analyticsService.loggedEvents shouldBe listOf(AnalyticsEvent.RoutingHukiFailed)
        }
    }

    @Test
    fun `Given blocked in Hungary - When getRoutePlan - Then HuKi-Routing serves without GraphHopper`() {
        runTest {
            routingQuotaStore.blockedUntil = NOW + 1.hours

            val actual = repository().getRoutePlan(PROFILE, HUNGARY_WAYPOINTS)

            actual shouldBe HUKI_ROUTING_SUCCESS
            graphhopperRepository.callCount shouldBe 0
        }
    }

    @Test
    fun `Given blocked abroad - When getRoutePlan - Then rate limited returns without any request`() {
        runTest {
            routingQuotaStore.blockedUntil = NOW + 1.hours

            val actual = repository().getRoutePlan(PROFILE, ABROAD_WAYPOINTS)

            actual shouldBe NetworkResult.Error(NetworkError.RATE_LIMITED)
            graphhopperRepository.callCount shouldBe 0
            hukiRoutingRepository.callCount shouldBe 0
        }
    }

    @Test
    fun `Given blocked and HuKi-Routing fails - When getRoutePlan - Then rate limited returns`() {
        runTest {
            routingQuotaStore.blockedUntil = NOW + 1.hours
            hukiRoutingRepository.result = NetworkResult.Error(NetworkError.BAD_REQUEST)

            val actual = repository().getRoutePlan(PROFILE, HUNGARY_WAYPOINTS)

            actual shouldBe NetworkResult.Error(NetworkError.RATE_LIMITED)
            graphhopperRepository.callCount shouldBe 0
        }
    }

    @Test
    fun `Given blocked and no internet - When getRoutePlan - Then no internet returns without failure event`() {
        runTest {
            routingQuotaStore.blockedUntil = NOW + 1.hours
            hukiRoutingRepository.result = NetworkResult.Error(NetworkError.NO_INTERNET)

            val actual = repository().getRoutePlan(PROFILE, HUNGARY_WAYPOINTS)

            actual shouldBe NetworkResult.Error(NetworkError.NO_INTERNET)
            analyticsService.loggedEvents.shouldBeEmpty()
        }
    }

    @Test
    fun `Given block expired - When getRoutePlan - Then GraphHopper serves`() {
        runTest {
            routingQuotaStore.blockedUntil = NOW

            val actual = repository().getRoutePlan(PROFILE, HUNGARY_WAYPOINTS)

            actual shouldBe GRAPHHOPPER_SUCCESS
            hukiRoutingRepository.callCount shouldBe 0
        }
    }

    @Test
    fun `Given reserve reached in Hungary - When getRoutePlan - Then HuKi-Routing serves without GraphHopper`() {
        runTest {
            routingQuotaStore.reserveReachedUntil = NOW + 1.hours

            val actual = repository().getRoutePlan(PROFILE, HUNGARY_WAYPOINTS)

            actual shouldBe HUKI_ROUTING_SUCCESS
            graphhopperRepository.callCount shouldBe 0
        }
    }

    @Test
    fun `Given reserve reached and HuKi-Routing fails - When getRoutePlan - Then GraphHopper serves`() {
        runTest {
            routingQuotaStore.reserveReachedUntil = NOW + 1.hours
            hukiRoutingRepository.result = NetworkResult.Error(NetworkError.INTERNAL_SERVER_ERROR)

            val actual = repository().getRoutePlan(PROFILE, HUNGARY_WAYPOINTS)

            actual shouldBe GRAPHHOPPER_SUCCESS
            analyticsService.loggedEvents shouldBe listOf(
                AnalyticsEvent.RoutingHukiFailed,
                AnalyticsEvent.RoutingGraphhopperAfterHukiFailure,
            )
        }
    }

    @Test
    fun `Given reserve reached and no internet - When getRoutePlan - Then no internet returns without GraphHopper`() {
        runTest {
            routingQuotaStore.reserveReachedUntil = NOW + 1.hours
            hukiRoutingRepository.result = NetworkResult.Error(NetworkError.NO_INTERNET)

            val actual = repository().getRoutePlan(PROFILE, HUNGARY_WAYPOINTS)

            actual shouldBe NetworkResult.Error(NetworkError.NO_INTERNET)
            graphhopperRepository.callCount shouldBe 0
        }
    }

    @Test
    fun `Given reserve reached and HuKi-Routing timeout - When getRoutePlan - Then timeout returns without GraphHopper`() {
        runTest {
            routingQuotaStore.reserveReachedUntil = NOW + 1.hours
            hukiRoutingRepository.result = NetworkResult.Error(NetworkError.REQUEST_TIMEOUT)

            val actual = repository().getRoutePlan(PROFILE, HUNGARY_WAYPOINTS)

            actual shouldBe NetworkResult.Error(NetworkError.REQUEST_TIMEOUT)
            graphhopperRepository.callCount shouldBe 0
            analyticsService.loggedEvents.shouldBeEmpty()
        }
    }

    @Test
    fun `Given reserve reached abroad - When getRoutePlan - Then GraphHopper serves`() {
        runTest {
            routingQuotaStore.reserveReachedUntil = NOW + 1.hours

            val actual = repository().getRoutePlan(PROFILE, ABROAD_WAYPOINTS)

            actual shouldBe GRAPHHOPPER_SUCCESS
            hukiRoutingRepository.callCount shouldBe 0
        }
    }

    @Test
    fun `Given GraphHopper only mode and blocked - When getRoutePlan - Then GraphHopper serves`() {
        runTest {
            routingQuotaStore.blockedUntil = NOW + 1.hours

            val actual = repository(RoutingMode.GRAPHHOPPER_ONLY).getRoutePlan(PROFILE, HUNGARY_WAYPOINTS)

            actual shouldBe GRAPHHOPPER_SUCCESS
            hukiRoutingRepository.callCount shouldBe 0
        }
    }

    @Test
    fun `Given HuKi-Routing only mode - When getRoutePlan abroad - Then HuKi-Routing serves`() {
        runTest {
            val actual = repository(RoutingMode.HUKI_ROUTING_ONLY).getRoutePlan(PROFILE, ABROAD_WAYPOINTS)

            actual shouldBe HUKI_ROUTING_SUCCESS
            graphhopperRepository.callCount shouldBe 0
        }
    }

    private fun repository(routingMode: RoutingMode = RoutingMode.AUTO) =
        FallbackRoutePlannerRepository(
            graphhopperRepository = graphhopperRepository,
            hukiRoutingRepository = hukiRoutingRepository,
            routingQuotaStore = routingQuotaStore,
            analyticsService = analyticsService,
            clock = FIXED_CLOCK,
            routingMode = routingMode,
        )

    companion object {
        private val NOW = Instant.parse("2026-10-10T14:00:00Z")
        private val FIXED_CLOCK = object : Clock {
            override fun now(): Instant = NOW
        }
        private val PROFILE = RoutePlannerProfile.ON_TRAILS

        private val HUNGARY_WAYPOINTS = listOf(Location(47.4979, 19.0402), Location(47.52, 19.0))
        private val ABROAD_WAYPOINTS = listOf(Location(47.4979, 19.0402), Location(47.0707, 15.4395))

        private val GRAPHHOPPER_SUCCESS = NetworkResult.Success(GraphhopperRouteResponse(emptyList()))
        private val HUKI_ROUTING_SUCCESS = NetworkResult.Success(
            GraphhopperRouteResponse(
                listOf(
                    GraphhopperPath(
                        distance = 1.0,
                        time = 1,
                        points = GraphhopperPoints(emptyList()),
                        snappedWaypoints = GraphhopperPoints(emptyList()),
                    ),
                ),
            ),
        )
    }
}
