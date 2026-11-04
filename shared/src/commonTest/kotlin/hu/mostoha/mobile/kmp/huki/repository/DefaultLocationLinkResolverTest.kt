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
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class DefaultLocationLinkResolverTest {

    private val geocodingRepository = mock<GeocodingRepository> {
        everySuspend { autocomplete(any()) } returns NetworkResult.Success(emptyList())
    }

    @Test
    fun `Given geo uri - When resolved - Then it returns its coordinates without network`() {
        runTest {
            val resolver = createResolver()

            val actual = resolver.resolve(IncomingLink("geo:47.7136,18.4507"))

            actual shouldBe LocationLinkResult.Coordinates(SUTTO)
            verifySuspend(not) { geocodingRepository.autocomplete(any()) }
        }
    }

    @Test
    fun `Given geo uri with only a name - When resolved - Then the name is searched`() {
        runTest {
            everySuspend { geocodingRepository.autocomplete("Dobogókő") } returns NetworkResult.Success(listOf(DOBOGOKO))
            val resolver = createResolver()

            val actual = resolver.resolve(IncomingLink("geo:0,0?q=Dobog%C3%B3k%C5%91"))

            actual shouldBe LocationLinkResult.PlaceFound(DOBOGOKO.toPlaceSearchResult())
        }
    }

    @Test
    fun `Given no internet - When a name is searched - Then it fails with the network error`() {
        runTest {
            everySuspend { geocodingRepository.autocomplete(any()) } returns NetworkResult.Error(NetworkError.NO_INTERNET)
            val resolver = createResolver()

            val actual = resolver.resolve(IncomingLink("geo:0,0?q=Dobog%C3%B3k%C5%91"))

            actual shouldBe LocationLinkResult.Failed(NetworkError.NO_INTERNET)
        }
    }

    @Test
    fun `Given text without a location link - When resolved - Then it is unsupported without network`() {
        runTest {
            val resolver = createResolver()

            val actual = resolver.resolve(IncomingLink("See you at the trailhead!"))

            actual shouldBe LocationLinkResult.Unsupported
            verifySuspend(not) { geocodingRepository.autocomplete(any()) }
        }
    }

    private fun createResolver() = DefaultLocationLinkResolver(geocodingRepository)

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
