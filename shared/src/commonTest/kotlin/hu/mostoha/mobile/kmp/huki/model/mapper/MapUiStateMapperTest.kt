package hu.mostoha.mobile.kmp.huki.model.mapper

import dev.icerock.moko.resources.desc.Raw
import dev.icerock.moko.resources.desc.StringDesc
import hu.mostoha.mobile.kmp.huki.data.TEST_GPX_DETAILS
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
                    location = Location(47.0, 19.0),
                    distance = "1 km",
                    travelTime = StringDesc.Raw("15 min"),
                ),
            ),
        )

        val actual = state.withoutGpx()

        actual shouldBe MapUiState(
            baseLayer = BaseLayer.SATELLITE,
            hikingLayerVisible = false,
        )
    }
}
