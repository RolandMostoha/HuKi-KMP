package hu.mostoha.mobile.kmp.huki.features.map

import hu.mostoha.mobile.kmp.huki.model.domain.OverlayLayer
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class MapUiStateTest {

    @Test
    fun `Given overlay flags - When visibleOverlays - Then overlays come in drawing order`() {
        testCases().forEach { testCase ->
            val actual = testCase.input.visibleOverlays

            actual shouldBe testCase.result
        }
    }

    data class TestCase(
        val input: MapUiState,
        val result: List<OverlayLayer>,
    )

    companion object {
        private val LANDSCAPES = LandscapesUiState(
            landscapes = emptyList(),
            outlinesGeoJson = "{}",
            labelsGeoJson = "{}",
            hungaryMaskGeoJson = "{}",
        )

        fun testCases() =
            listOf(
                TestCase(
                    input = MapUiState(hikingLayerVisible = false, bikeLayerVisible = false),
                    result = emptyList(),
                ),
                TestCase(
                    input = MapUiState(hikingLayerVisible = true, bikeLayerVisible = false),
                    result = listOf(OverlayLayer.TURISTAUTAK),
                ),
                TestCase(
                    input = MapUiState(hikingLayerVisible = true, bikeLayerVisible = true),
                    result = listOf(OverlayLayer.TURISTAUTAK, OverlayLayer.WAYMARKED_CYCLING),
                ),
                TestCase(
                    input = MapUiState(hikingLayerVisible = false, bikeLayerVisible = true),
                    result = listOf(OverlayLayer.WAYMARKED_CYCLING),
                ),
                TestCase(
                    input = MapUiState(hikingLayerVisible = false, bikeLayerVisible = true, landscapes = LANDSCAPES),
                    result = emptyList(),
                ),
            )
    }
}
