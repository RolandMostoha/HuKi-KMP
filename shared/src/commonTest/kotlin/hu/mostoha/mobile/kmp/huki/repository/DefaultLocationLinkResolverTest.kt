package hu.mostoha.mobile.kmp.huki.repository

import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.not
import dev.mokkery.verifySuspend
import hu.mostoha.mobile.kmp.huki.model.domain.IncomingLink
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.mapper.toPlaceSearchResult
import hu.mostoha.mobile.kmp.huki.model.network.LocationIqPlace
import hu.mostoha.mobile.kmp.huki.model.network.NetworkError
import hu.mostoha.mobile.kmp.huki.model.network.NetworkResult
import hu.mostoha.mobile.kmp.huki.network.createHttpClient
import hu.mostoha.mobile.kmp.huki.service.FakeCrashlyticsService
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlin.test.Test

class DefaultLocationLinkResolverTest {

    private val geocodingRepository = mock<GeocodingRepository> {
        everySuspend { autocomplete(any()) } returns NetworkResult.Success(emptyList())
    }

    @Test
    fun `Given geo uri - When resolved - Then it returns its coordinates without network`() {
        runTest {
            val resolver = createResolver(MockEngine { error("No request expected") })

            val actual = resolver.resolve(IncomingLink.Text("geo:47.7136,18.4507"))

            actual shouldBe LocationLinkResult.Coordinates(SUTTO)
            verifySuspend(not) { geocodingRepository.autocomplete(any()) }
        }
    }

    @Test
    fun `Given geo uri with only a name - When resolved - Then the name is searched`() {
        runTest {
            everySuspend { geocodingRepository.autocomplete("Dobogókő") } returns NetworkResult.Success(listOf(DOBOGOKO))
            val resolver = createResolver(MockEngine { error("No request expected") })

            val actual = resolver.resolve(IncomingLink.Text("geo:0,0?q=Dobog%C3%B3k%C5%91"))

            actual shouldBe LocationLinkResult.PlaceFound(DOBOGOKO.toPlaceSearchResult())
        }
    }

    @Test
    fun `Given no internet - When a name is searched - Then it fails with the network error`() {
        runTest {
            everySuspend { geocodingRepository.autocomplete(any()) } returns NetworkResult.Error(NetworkError.NO_INTERNET)
            val resolver = createResolver(MockEngine { error("No request expected") })

            val actual = resolver.resolve(IncomingLink.Text("geo:0,0?q=Dobog%C3%B3k%C5%91"))

            actual shouldBe LocationLinkResult.Failed(NetworkError.NO_INTERNET)
        }
    }

    @Test
    fun `Given shared Google Maps short link - When resolved - Then it follows the redirect to the coordinates`() {
        runTest {
            val engine = MockEngine { request ->
                when (request.url.host) {
                    "maps.app.goo.gl" -> redirectTo("https://www.google.com/maps/search/47.7136,+18.4507?entry=tts")
                    else -> error("The redirect target must not be fetched")
                }
            }
            val resolver = createResolver(engine)

            val actual = resolver.resolve(IncomingLink.Text("Süttő\nhttps://maps.app.goo.gl/AbC"))

            actual shouldBe LocationLinkResult.Coordinates(SUTTO)
        }
    }

    @Test
    fun `Given short link redirecting to an Android intent - When resolved - Then it uses the browser fallback`() {
        runTest {
            val engine = MockEngine {
                redirectTo(
                    "intent://maps.app.goo.gl/AbC#Intent;scheme=https;" +
                        "S.browser_fallback_url=https%3A%2F%2Fmaps.google.com%2F%3Fq%3D47.7136%2C18.4507;end;",
                )
            }
            val resolver = createResolver(engine)

            val actual = resolver.resolve(IncomingLink.Text("https://maps.app.goo.gl/AbC"))

            actual shouldBe LocationLinkResult.Coordinates(SUTTO)
        }
    }

    @Test
    fun `Given short link to a named place without coordinates - When resolved - Then the name is searched`() {
        runTest {
            val place = LocationIqPlace(
                placeId = "1",
                osmId = "42",
                osmType = "node",
                licence = "",
                lat = 47.7181,
                lon = 18.8948,
                displayName = "Dobogókő",
            )
            everySuspend { geocodingRepository.autocomplete("Dobogókő") } returns NetworkResult.Success(listOf(place))
            val engine = MockEngine { redirectTo("https://maps.google.com/maps?q=Dobog%C3%B3k%C5%91&ftid=0x1:0x2") }
            val resolver = createResolver(engine)

            val actual = resolver.resolve(IncomingLink.Text("https://maps.app.goo.gl/AbC"))

            actual shouldBe LocationLinkResult.PlaceFound(place.toPlaceSearchResult())
        }
    }

    @Test
    fun `Given short link that does not redirect - When resolved - Then it is unsupported`() {
        runTest {
            val resolver = createResolver(MockEngine { respond(content = "", status = HttpStatusCode.OK) })

            val actual = resolver.resolve(IncomingLink.Text("https://maps.app.goo.gl/AbC"))

            actual shouldBe LocationLinkResult.Unsupported
        }
    }

    @Test
    fun `Given failing request - When short link is resolved - Then it fails with a network error`() {
        runTest {
            val resolver = createResolver(MockEngine { throw IOException("Connection reset") })

            val actual = resolver.resolve(IncomingLink.Text("https://maps.app.goo.gl/AbC"))

            actual shouldBe LocationLinkResult.Failed(NetworkError.UNKNOWN)
        }
    }

    @Test
    fun `Given text without a location link - When resolved - Then it is unsupported without network`() {
        runTest {
            val resolver = createResolver(MockEngine { error("No request expected") })

            val actual = resolver.resolve(IncomingLink.Text("See you at the trailhead!"))

            actual shouldBe LocationLinkResult.Unsupported
            verifySuspend(not) { geocodingRepository.autocomplete(any()) }
        }
    }

    private fun createResolver(engine: MockEngine) = DefaultLocationLinkResolver(createHttpClient(engine), geocodingRepository, FakeCrashlyticsService())

    private fun MockRequestHandleScope.redirectTo(url: String) = respond(content = "", status = HttpStatusCode.Found, headers = headersOf(HttpHeaders.Location, url))

    private companion object {
        val SUTTO = Location(47.7136, 18.4507)
        val DOBOGOKO = LocationIqPlace(
            placeId = "1",
            osmId = "42",
            osmType = "node",
            licence = "",
            lat = 47.7181,
            lon = 18.8948,
            displayName = "Dobogókő",
        )
    }
}
