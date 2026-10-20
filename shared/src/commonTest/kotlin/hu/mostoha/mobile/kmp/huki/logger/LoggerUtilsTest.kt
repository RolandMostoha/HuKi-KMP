package hu.mostoha.mobile.kmp.huki.logger

import hu.mostoha.mobile.kmp.huki.features.destinations.DestinationsUiState
import hu.mostoha.mobile.kmp.huki.features.destinations.NearbyState
import hu.mostoha.mobile.kmp.huki.features.main.MainUiState
import hu.mostoha.mobile.kmp.huki.features.map.LandscapesUiState
import hu.mostoha.mobile.kmp.huki.features.map.MapUiState
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import kotlin.test.Test

class LoggerUtilsTest {

    @Test
    fun `Given a list within limit - When trimming - Then it remains unchanged`() {
        val input = "Data(items=[1, 2, 3])"

        val actual = input.trimLongLists(limit = 5)

        actual shouldBe "Data(items=[1, 2, 3])"
    }

    @Test
    fun `Given a list exactly at limit - When trimming - Then it remains unchanged`() {
        val input = "Data(items=[1, 2, 3])"

        val actual = input.trimLongLists(limit = 3)

        actual shouldBe "Data(items=[1, 2, 3])"
    }

    @Test
    fun `Given a list above limit - When trimming - Then it is truncated`() {
        val input = "Data(items=[1, 2, 3, 4])"

        val actual = input.trimLongLists(limit = 2)

        actual shouldBe "Data(items=[1, 2, [...trimmed...] (total=4)])"
    }

    @Test
    fun `Given multiple lists - When trimming - Then all are processed`() {
        val input = "Data(a=[1, 2, 3], b=[4, 5, 6, 7])"

        val actual = input.trimLongLists(limit = 2)

        actual shouldBe "Data(a=[1, 2, [...trimmed...] (total=3)], b=[4, 5, [...trimmed...] (total=4)])"
    }

    @Test
    fun `Given no lists - When trimming - Then it remains unchanged`() {
        val input = "Data(a=1, b=2)"

        val actual = input.trimLongLists(limit = 5)

        actual shouldBe "Data(a=1, b=2)"
    }

    @Test
    fun `Given empty list - When trimming - Then it remains unchanged`() {
        val input = "Data(items=[])"

        val actual = input.trimLongLists(limit = 5)

        actual shouldBe "Data(items=[])"
    }

    @Test
    fun `Given open landscapes - When logging the main state - Then the GeoJSON is summarised`() {
        val geoJson = "{\"type\":\"FeatureCollection\"}"
        val landscapes = LandscapesUiState(
            landscapes = emptyList(),
            outlinesGeoJson = geoJson,
            labelsGeoJson = geoJson,
            hungaryMaskGeoJson = geoJson,
            selectedLandscapeId = "bukk",
        )
        val state = MainUiState().copy(mapUiState = MapUiState().copy(landscapes = landscapes))

        val actual = state.toLogString()

        actual shouldNotContain "FeatureCollection"
        actual shouldContain "selectedLandscapeId=bukk"
        actual shouldContain "outlinesGeoJson=${geoJson.length} chars"
    }

    @Test
    fun `Given no landscapes - When logging the main state - Then it matches the trimmed string`() {
        val state = MainUiState()

        val actual = state.toLogString()

        actual shouldBe state.toString().trimLongLists()
    }

    @Test
    fun `Given loaded nearby items - When logging the destinations state - Then only counts are logged`() {
        val state = DestinationsUiState(nearbyState = NearbyState.Loaded(emptyList()))

        val actual = state.toLogString()

        actual shouldContain "nearbyState=Loaded(items=0)"
        actual shouldContain "popularItems=0"
    }
}
