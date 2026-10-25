package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.model.analytics.AnalyticsEvent
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlannerProfile
import hu.mostoha.mobile.kmp.huki.model.network.NetworkError
import hu.mostoha.mobile.kmp.huki.model.network.NetworkResult
import hu.mostoha.mobile.kmp.huki.network.createHttpClient
import hu.mostoha.mobile.kmp.huki.service.FakeAnalyticsService
import hu.mostoha.mobile.kmp.huki.service.FakeCrashlyticsService
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class GraphhopperRoutePlannerRepositoryTest {

    private val routingQuotaStore = FakeRoutingQuotaStore()
    private val analyticsService = FakeAnalyticsService()

    @Test
    fun `Given credits above reserve - When getRoutePlan - Then quota is not changed`() {
        runTest {
            val actual = repository(HttpStatusCode.OK, remaining = "2000").getRoutePlan(PROFILE, WAYPOINTS)

            actual.shouldBeInstanceOf<NetworkResult.Success<*>>()
            routingQuotaStore.reserveReachedUntil.shouldBeNull()
            analyticsService.loggedEvents.shouldBeEmpty()
        }
    }

    @Test
    fun `Given credits below reserve - When getRoutePlan - Then reserve is reached until reset`() {
        runTest {
            repository(HttpStatusCode.OK, remaining = "499").getRoutePlan(PROFILE, WAYPOINTS)

            routingQuotaStore.reserveReachedUntil shouldBe NOW + RESET_SECONDS.seconds
            analyticsService.loggedEvents shouldBe listOf(AnalyticsEvent.RoutingGraphhopperReserveReached)
        }
    }

    @Test
    fun `Given reserve already reached - When getRoutePlan - Then reserve is not logged again`() {
        runTest {
            val reserveReachedUntil = NOW + 1.hours
            routingQuotaStore.reserveReachedUntil = reserveReachedUntil

            repository(HttpStatusCode.OK, remaining = "400").getRoutePlan(PROFILE, WAYPOINTS)

            routingQuotaStore.reserveReachedUntil shouldBe reserveReachedUntil
            analyticsService.loggedEvents.shouldBeEmpty()
        }
    }

    @Test
    fun `Given no rate limit headers - When getRoutePlan - Then quota is not changed`() {
        runTest {
            repository(HttpStatusCode.OK, remaining = null, reset = null).getRoutePlan(PROFILE, WAYPOINTS)

            routingQuotaStore.reserveReachedUntil.shouldBeNull()
            routingQuotaStore.blockedUntil.shouldBeNull()
        }
    }

    @Test
    fun `Given daily limit reached - When getRoutePlan - Then blocked until reset`() {
        runTest {
            val actual = repository(HttpStatusCode.TooManyRequests, remaining = "0").getRoutePlan(PROFILE, WAYPOINTS)

            actual shouldBe NetworkResult.Error(NetworkError.RATE_LIMITED)
            routingQuotaStore.blockedUntil shouldBe NOW + RESET_SECONDS.seconds
            analyticsService.loggedEvents shouldBe listOf(AnalyticsEvent.RoutingGraphhopperLimitHit)
        }
    }

    @Test
    fun `Given daily limit reached without reset header - When getRoutePlan - Then blocked until UTC midnight`() {
        runTest {
            repository(HttpStatusCode.TooManyRequests, remaining = null, reset = null).getRoutePlan(PROFILE, WAYPOINTS)

            routingQuotaStore.blockedUntil shouldBe Instant.parse("2026-10-11T00:00:00Z")
        }
    }

    @Test
    fun `Given rate limited with credits left - When getRoutePlan - Then not blocked`() {
        runTest {
            repository(HttpStatusCode.TooManyRequests, remaining = "1200").getRoutePlan(PROFILE, WAYPOINTS)

            routingQuotaStore.blockedUntil.shouldBeNull()
            analyticsService.loggedEvents shouldBe listOf(AnalyticsEvent.RoutingGraphhopperLimitHit)
        }
    }

    private fun repository(
        status: HttpStatusCode,
        remaining: String?,
        reset: String? = RESET_SECONDS.toString(),
    ): GraphhopperRoutePlannerRepository {
        val headers = listOfNotNull(
            HttpHeaders.ContentType to listOf("application/json"),
            remaining?.let { "X-RateLimit-Remaining" to listOf(it) },
            reset?.let { "X-RateLimit-Reset" to listOf(it) },
        )
        val engine = MockEngine {
            respond(content = """{"paths":[]}""", status = status, headers = headersOf(*headers.toTypedArray()))
        }

        return GraphhopperRoutePlannerRepository(
            httpClient = createHttpClient(engine),
            crashlyticsService = FakeCrashlyticsService(),
            routingQuotaStore = routingQuotaStore,
            analyticsService = analyticsService,
            clock = FIXED_CLOCK,
        )
    }

    companion object {
        private val NOW = Instant.parse("2026-10-10T14:00:00Z")
        private val FIXED_CLOCK = object : Clock {
            override fun now(): Instant = NOW
        }
        private const val RESET_SECONDS = 36_000L
        private val PROFILE = RoutePlannerProfile.ON_TRAILS
        private val WAYPOINTS = listOf(Location(47.4979, 19.0402), Location(47.52, 19.0))
    }
}
