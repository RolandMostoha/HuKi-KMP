package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlannerProfile
import hu.mostoha.mobile.kmp.huki.network.NetworkHeaders
import hu.mostoha.mobile.kmp.huki.network.createHttpClient
import hu.mostoha.mobile.kmp.huki.service.FakeCrashlyticsService
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class HukiRoutingRoutePlannerRepositoryTest {

    @Test
    fun `Given route request - When getRoutePlan - Then API key is sent in the header only`() {
        runTest {
            val request = captureRequest(RoutePlannerProfile.ON_TRAILS)

            request.headers[NetworkHeaders.HUKI_ROUTING_API_KEY].shouldNotBeNull()
            request.url.parameters.isEmpty() shouldBe true
        }
    }

    @Test
    fun `Given on trails profile - When getRoutePlan - Then hike_huki profile is sent without custom model`() {
        runTest {
            val body = (captureRequest(RoutePlannerProfile.ON_TRAILS).body as TextContent).text

            body shouldContain "\"profile\":\"hike_huki\""
            body shouldNotContain "custom_model"
        }
    }

    private suspend fun captureRequest(profile: RoutePlannerProfile): HttpRequestData {
        var request: HttpRequestData? = null
        val engine = MockEngine { requestData ->
            request = requestData
            respond(
                content = """{"paths":[]}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val repository = HukiRoutingRoutePlannerRepository(createHttpClient(engine), FakeCrashlyticsService())

        repository.getRoutePlan(profile, listOf(Location(47.4979, 19.0402), Location(47.52, 19.0)))

        return request.shouldNotBeNull()
    }
}
