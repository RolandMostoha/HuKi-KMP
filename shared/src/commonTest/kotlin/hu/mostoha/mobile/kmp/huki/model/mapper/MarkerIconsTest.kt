package hu.mostoha.mobile.kmp.huki.model.mapper

import dev.icerock.moko.resources.ImageResource
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.BaseLayer
import hu.mostoha.mobile.kmp.huki.model.domain.WaypointType
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class MarkerIconsTest {

    @Test
    fun `Given appearance and base layer, when waypointSymbol, then pre-graded icon is only used on dark Outdoors`() {
        waypointTestCases().forEach { testCase ->
            val actual = MarkerIcons.waypointSymbol(testCase.type, testCase.isDarkMode, testCase.baseLayer)

            actual shouldBe testCase.result
        }
    }

    @Test
    fun `Given appearance and base layer, when placeSymbol, then pre-graded icon is only used on dark Outdoors`() {
        placeTestCases().forEach { testCase ->
            val actual = MarkerIcons.placeSymbol(testCase.isDarkMode, testCase.baseLayer)

            actual shouldBe testCase.result
        }
    }

    companion object {

        private data class WaypointTestCase(
            val type: WaypointType,
            val isDarkMode: Boolean,
            val baseLayer: BaseLayer,
            val result: ImageResource,
        )

        private data class PlaceTestCase(
            val isDarkMode: Boolean,
            val baseLayer: BaseLayer,
            val result: ImageResource,
        )

        private fun waypointTestCases() = WaypointType.entries.flatMap { type ->
            listOf(
                WaypointTestCase(type, isDarkMode = true, BaseLayer.OUTDOORS, type.outdoorsDarkIcon),
                WaypointTestCase(type, isDarkMode = true, BaseLayer.CITY, type.icon),
                WaypointTestCase(type, isDarkMode = true, BaseLayer.SATELLITE, type.icon),
                WaypointTestCase(type, isDarkMode = false, BaseLayer.OUTDOORS, type.icon),
                WaypointTestCase(type, isDarkMode = false, BaseLayer.CITY, type.icon),
                WaypointTestCase(type, isDarkMode = false, BaseLayer.SATELLITE, type.icon),
            )
        }

        private fun placeTestCases() = listOf(
            PlaceTestCase(true, BaseLayer.OUTDOORS, SharedRes.images.ic_marker_picker_outdoors_dark),
            PlaceTestCase(true, BaseLayer.CITY, SharedRes.images.ic_marker_picker),
            PlaceTestCase(true, BaseLayer.SATELLITE, SharedRes.images.ic_marker_picker),
            PlaceTestCase(false, BaseLayer.OUTDOORS, SharedRes.images.ic_marker_picker),
            PlaceTestCase(false, BaseLayer.CITY, SharedRes.images.ic_marker_picker),
            PlaceTestCase(false, BaseLayer.SATELLITE, SharedRes.images.ic_marker_picker),
        )
    }
}
