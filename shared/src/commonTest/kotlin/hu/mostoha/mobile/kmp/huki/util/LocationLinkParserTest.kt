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
                // Google Maps short links, resolved later through their redirect
                TestCase(
                    "Süttő\nhttps://maps.app.goo.gl/AbCdEf123",
                    LocationLink.ShortUrl("https://maps.app.goo.gl/AbCdEf123"),
                ),
                TestCase("https://goo.gl/maps/AbCdEf123", LocationLink.ShortUrl("https://goo.gl/maps/AbCdEf123")),
                // Google Maps long links
                TestCase(
                    "https://www.google.com/maps/place/S%C3%BCtt%C5%91/@47.70,18.40,13z/data=!3m1!4b1!4m6!3m5" +
                        "!1s0x476a0b:0x400c4290c1e1180!8m2!3d47.7136!4d18.4507!16s",
                    LocationLink.Coordinates(SUTTO),
                ),
                TestCase("https://maps.google.com/?q=47.7136,18.4507", LocationLink.Coordinates(SUTTO)),
                TestCase(
                    "https://www.google.com/maps/search/?api=1&query=47.7136%2C18.4507",
                    LocationLink.Coordinates(SUTTO),
                ),
                TestCase(
                    "https://www.google.com/maps/search/47.7136,+18.4507?entry=tts",
                    LocationLink.Coordinates(SUTTO),
                ),
                TestCase("https://www.google.hu/maps/@47.7136,18.4507,15z", LocationLink.Coordinates(SUTTO)),
                TestCase(
                    "https://www.google.com/maps/place/Dobog%C3%B3k%C5%91+kil%C3%A1t%C3%B3/data=!4m2!3m1!1s0x0",
                    LocationLink.PlaceName("Dobogókő kilátó"),
                ),
                TestCase(
                    "https://maps.google.com/maps?q=Dobog%C3%B3k%C5%91&ftid=0x476a:0x400c",
                    LocationLink.PlaceName("Dobogókő"),
                ),
                TestCase("https://www.google.com/search?q=47.7136,18.4507", null),
                // Android intent redirects carry the web link as their browser fallback
                TestCase(
                    "intent://maps.app.goo.gl/AbC#Intent;package=com.google.android.gms;scheme=https;" +
                        "S.browser_fallback_url=https%3A%2F%2Fmaps.google.com%2F%3Fq%3D47.7136%2C18.4507;end;",
                    LocationLink.Coordinates(SUTTO),
                ),
                // HuKi links
                TestCase("https://huki.hu/place?lat=47.7136&lon=18.4507", LocationLink.Coordinates(SUTTO)),
                TestCase("https://www.huki.hu/place/?lat=47.7136&lon=18.4507", LocationLink.Coordinates(SUTTO)),
                TestCase("https://huki.hu/landscape?id=pilis", null),
                TestCase("https://huki.hu/place?lat=47.7136", null),
                // Malformed or unusual encodings from other apps
                TestCase("geo:0,0?q=50%", null),
                TestCase("https://www.google.com/maps/search/abc%ZZ", null),
                TestCase("https://maps.google.com/?q=Kil%C3%A1t%", null),
                TestCase("geo:0,0?q=Rév utca 1, Tihany", LocationLink.PlaceName("Rév utca 1, Tihany")),
                TestCase(
                    "https://www.google.com/maps/place/C%2B%2B+Caf%C3%A9/data=!4m2",
                    LocationLink.PlaceName("C++ Café"),
                ),
                TestCase(
                    "Look: https://www.google.com/maps/place/Kil%C3%A1t%C3%B3+(Pilis)/data=!4m2.",
                    LocationLink.PlaceName("Kilátó (Pilis)"),
                ),
                // Unrelated text
                TestCase("See you at the trailhead!", null),
                TestCase("https://example.com/maps/@47.7136,18.4507,15z", null),
            )
    }
}
