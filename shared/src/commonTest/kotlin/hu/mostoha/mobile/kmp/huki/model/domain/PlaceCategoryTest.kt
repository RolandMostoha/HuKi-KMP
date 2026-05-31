package hu.mostoha.mobile.kmp.huki.model.domain

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class PlaceCategoryTest {

    @Test
    fun `Given OSM type, When fromString, Then expected PlaceCategory returns`() {
        testCases().forEach { testCase ->
            val actual = PlaceCategory.fromString(testCase.input)

            actual shouldBe testCase.result
        }
    }

    companion object {
        fun testCases() =
            listOf(
                TestCase(input = "peak", result = PlaceCategory.PEAK),
                TestCase(input = "village", result = PlaceCategory.SETTLEMENT),
                TestCase(input = "viewpoint", result = PlaceCategory.VIEWPOINT),
                TestCase(input = "alpine_hut", result = PlaceCategory.CAMP_SITE),
                TestCase(input = "national_park", result = PlaceCategory.NATIONAL_PARK),
                TestCase(input = "valley", result = PlaceCategory.VALLEY),
                TestCase(input = "gorge", result = PlaceCategory.VALLEY),
                TestCase(input = "plateau", result = PlaceCategory.PLATEAU),
                TestCase(input = "island", result = PlaceCategory.ISLAND),
                TestCase(input = "meadow", result = PlaceCategory.MEADOW),
                TestCase(input = "garden", result = PlaceCategory.GARDEN),
                TestCase(input = "arboretum", result = PlaceCategory.GARDEN),
                TestCase(input = "zoo", result = PlaceCategory.WILDLIFE_PARK),
                TestCase(input = "observatory", result = PlaceCategory.OBSERVATORY),
                TestCase(input = "suburb", result = PlaceCategory.SETTLEMENT),
                TestCase(input = "neighbourhood", result = PlaceCategory.SETTLEMENT),
                TestCase(input = "residential", result = PlaceCategory.SETTLEMENT),
                TestCase(input = "dam", result = PlaceCategory.LAKE),
                TestCase(input = "farm", result = PlaceCategory.FARM),
                TestCase(input = "farmyard", result = PlaceCategory.FARM),
                TestCase(input = null, result = null),
                TestCase(input = "unmapped_tag", result = null),
            )
    }

    data class TestCase(
        val input: String?,
        val result: PlaceCategory?,
    )
}
