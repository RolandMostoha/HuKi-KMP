package hu.mostoha.mobile.kmp.huki.util

import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.LocationLink
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class LocationLinkParserTest {

    @Test
    fun `Given shared link text - When parsed - Then it returns the location link it describes`() {
        testCases().forEach { testCase ->
            val actual = LocationLinkParser.parse(testCase.input)

            actual shouldBe testCase.result
        }
    }

    data class TestCase(
        val input: String,
        val result: LocationLink?,
    )

    companion object {
        private val SUTTO = Location(47.7136, 18.4507)

        fun testCases() =
            listOf(
                // geo: URIs
                TestCase("geo:47.7136,18.4507", LocationLink.Coordinates(SUTTO)),
                TestCase("geo:47.7136,18.4507?z=15", LocationLink.Coordinates(SUTTO)),
                TestCase("geo:47.7136,18.4507,120;u=35", LocationLink.Coordinates(SUTTO)),
                TestCase("geo:0,0?q=47.7136,18.4507(S%C3%BCtt%C5%91)", LocationLink.Coordinates(SUTTO)),
                TestCase("geo:47.7136,18.4507?q=47.7136%2C18.4507", LocationLink.Coordinates(SUTTO)),
                TestCase("geo:0,0?q=Dobog%C3%B3k%C5%91", LocationLink.PlaceName("Dobogókő")),
                TestCase("geo:0,0?q=R%C3%A9v+utca+1%2C+Tihany", LocationLink.PlaceName("Rév utca 1, Tihany")),
                TestCase("geo:0,0", null),
                TestCase("geo:91,18.4507", null),
                TestCase("geo:47.7136,181", null),
                TestCase("geo:abc", null),
                // HuKi links
                TestCase("https://huki.hu/place?lat=47.7136&lon=18.4507", LocationLink.Coordinates(SUTTO)),
                TestCase("https://www.huki.hu/place/?lat=47.7136&lon=18.4507", LocationLink.Coordinates(SUTTO)),
                TestCase("https://huki.hu/landscape?id=pilis", null),
                TestCase("https://huki.hu/place?lat=47.7136", null),
                // Malformed or unusual encodings from other apps
                TestCase("geo:0,0?q=50%", null),
                TestCase("geo:0,0?q=Rév utca 1, Tihany", LocationLink.PlaceName("Rév utca 1, Tihany")),
                // Unrelated text
                TestCase("See you at the trailhead!", null),
                TestCase("https://example.com/maps/@47.7136,18.4507,15z", null),
            )
    }
}
