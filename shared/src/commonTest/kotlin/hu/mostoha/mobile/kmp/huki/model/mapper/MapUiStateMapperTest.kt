package hu.mostoha.mobile.kmp.huki.model.mapper

import dev.icerock.moko.resources.desc.Raw
import dev.icerock.moko.resources.desc.StringDesc
import hu.mostoha.mobile.kmp.huki.data.TEST_GPX_DETAILS
import hu.mostoha.mobile.kmp.huki.features.map.LandscapesUiState
import hu.mostoha.mobile.kmp.huki.features.map.MapUiState
import hu.mostoha.mobile.kmp.huki.model.domain.BaseLayer
import hu.mostoha.mobile.kmp.huki.model.domain.DistanceInfoWindowData
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class MapUiStateMapperTest {

    @Test
    fun `Given state with GPX - When withoutGpx - Then GPX fields are reset and others are kept`() {
        val state = MapUiState(
            baseLayer = BaseLayer.SATELLITE,
            hikingLayerVisible = false,
            gpxDetails = TEST_GPX_DETAILS,
            gpxLayerVisible = true,
            gpxRouteVisible = false,
            allDistancesVisible = true,
            distanceInfoWindows = listOf(
                DistanceInfoWindowData(
                    id = 0,
                    location = Location(47.0, 19.0),
                    name = null,
                    label = null,
                    distance = "1 km",
                    travelTime = StringDesc.Raw("15 min"),
                    priority = 1,
                ),
            ),
        )

        val actual = state.withoutGpx()

        actual shouldBe MapUiState(
            baseLayer = BaseLayer.SATELLITE,
            hikingLayerVisible = false,
        )
    }

    @Test
    fun `Given open Landscapes with hidden hiking layer - When withoutLandscapes - Then landscapes are removed and hiking layer is shown`() {
        val state = MapUiState(
            hikingLayerVisible = false,
            landscapes = LandscapesUiState(
                landscapes = emptyList(),
                outlinesGeoJson = "{}",
                labelsGeoJson = "{}",
                hungaryMaskGeoJson = "{}",
            ),
        )

        val actual = state.withoutLandscapes()

        actual shouldBe MapUiState(hikingLayerVisible = true)
    }

    @Test
    fun `Given no Landscapes and hidden hiking layer - When withoutLandscapes - Then state is unchanged`() {
        val state = MapUiState(hikingLayerVisible = false)

        val actual = state.withoutLandscapes()

        actual shouldBe state
    }

    @Test
    fun `Given open Landscapes on TuHu - When withoutLandscapes - Then hiking layer stays hidden`() {
        val state = MapUiState(
            baseLayer = BaseLayer.TUHU,
            hikingLayerVisible = false,
            landscapes = LandscapesUiState(
                landscapes = emptyList(),
                outlinesGeoJson = "{}",
                labelsGeoJson = "{}",
                hungaryMaskGeoJson = "{}",
            ),
        )

        val actual = state.withoutLandscapes()

        actual shouldBe MapUiState(baseLayer = BaseLayer.TUHU, hikingLayerVisible = false)
    }

    @Test
    fun `Given base layer transitions - When withBaseLayer - Then hiking layer follows the TuHu rule`() {
        withBaseLayerTestCases().forEach { testCase ->
            val state = MapUiState(baseLayer = testCase.from, hikingLayerVisible = testCase.hikingLayerVisible)

            val actual = state.withBaseLayer(testCase.to)

            actual shouldBe MapUiState(baseLayer = testCase.to, hikingLayerVisible = testCase.expected)
        }
    }

    @Test
    fun `Given open Landscapes - When withBaseLayer leaves TuHu - Then hiking layer stays hidden`() {
        val landscapes = LandscapesUiState(
            landscapes = emptyList(),
            outlinesGeoJson = "{}",
            labelsGeoJson = "{}",
            hungaryMaskGeoJson = "{}",
        )
        val state = MapUiState(baseLayer = BaseLayer.TUHU, hikingLayerVisible = false, landscapes = landscapes)

        val actual = state.withBaseLayer(BaseLayer.OUTDOORS)

        actual shouldBe state.copy(baseLayer = BaseLayer.OUTDOORS)
    }

    private data class WithBaseLayerTestCase(
        val from: BaseLayer,
        val to: BaseLayer,
        val hikingLayerVisible: Boolean,
        val expected: Boolean,
    )

    companion object {
        private fun withBaseLayerTestCases() =
            listOf(
                WithBaseLayerTestCase(BaseLayer.OUTDOORS, BaseLayer.TUHU, hikingLayerVisible = true, expected = false),
                WithBaseLayerTestCase(BaseLayer.SATELLITE, BaseLayer.TUHU, hikingLayerVisible = false, expected = false),
                WithBaseLayerTestCase(BaseLayer.TUHU, BaseLayer.OUTDOORS, hikingLayerVisible = false, expected = true),
                WithBaseLayerTestCase(BaseLayer.TUHU, BaseLayer.CITY, hikingLayerVisible = true, expected = true),
                WithBaseLayerTestCase(BaseLayer.TUHU, BaseLayer.TUHU, hikingLayerVisible = true, expected = true),
                WithBaseLayerTestCase(BaseLayer.TUHU, BaseLayer.MAPNIK, hikingLayerVisible = false, expected = true),
                WithBaseLayerTestCase(BaseLayer.MAPNIK, BaseLayer.TUHU, hikingLayerVisible = true, expected = false),
                WithBaseLayerTestCase(BaseLayer.OUTDOORS, BaseLayer.CITY, hikingLayerVisible = false, expected = false),
                WithBaseLayerTestCase(BaseLayer.CITY, BaseLayer.SATELLITE, hikingLayerVisible = true, expected = true),
            )
    }
}
