package hu.mostoha.mobile.kmp.huki.repository

import hu.mostoha.mobile.kmp.huki.model.domain.Location
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class DefaultLandscapeLocatorTest {

    private val locator = DefaultLandscapeLocator(
        FakeLandscapeRepository(
            outlines = FakeLandscapeRepository.TEST_LANDSCAPE_OUTLINES.copy(geoJson = SQUARE_OUTLINES_GEOJSON),
        ),
    )

    @Test
    fun `Given track locations - When findLandscapeId - Then the landscape holding the largest share is returned`() =
        runTest {
            testCases().forEach { testCase ->
                val actual = locator.findLandscapeId(testCase.input)

                actual shouldBe testCase.result
            }
        }

    companion object {
        private val MATRA = Location(latitude = 47.85, longitude = 19.9)
        private val BUKK = Location(latitude = 48.0, longitude = 20.5)
        private val OUTSIDE = Location(latitude = 47.5, longitude = 19.0)

        private val SQUARE_OUTLINES_GEOJSON = """
            {"type":"FeatureCollection","features":[
            {"type":"Feature","properties":{"id":"matra"},"geometry":{"type":"Polygon",
            "coordinates":[[[19.7,47.8],[20.2,47.8],[20.2,47.95],[19.7,47.95],[19.7,47.8]]]}},
            {"type":"Feature","properties":{"id":"bukk"},"geometry":{"type":"Polygon",
            "coordinates":[[[20.2,47.9],[20.8,47.9],[20.8,48.2],[20.2,48.2],[20.2,47.9]]]}}
            ]}
        """.trimIndent()

        fun testCases() =
            listOf(
                TestCase(input = emptyList(), result = null),
                TestCase(input = listOf(MATRA, MATRA), result = "matra"),
                TestCase(input = listOf(BUKK, MATRA, BUKK), result = "bukk"),
                TestCase(input = listOf(MATRA, OUTSIDE, OUTSIDE), result = "matra"),
                TestCase(input = listOf(MATRA, OUTSIDE, OUTSIDE, OUTSIDE), result = null),
                TestCase(input = listOf(OUTSIDE), result = null),
            )
    }

    data class TestCase(
        val input: List<Location>,
        val result: String?,
    )
}
