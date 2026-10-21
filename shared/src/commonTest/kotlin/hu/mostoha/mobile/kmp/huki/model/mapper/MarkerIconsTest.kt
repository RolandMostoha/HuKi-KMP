package hu.mostoha.mobile.kmp.huki.model.mapper

import dev.icerock.moko.resources.ImageResource
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.BaseLayer
import hu.mostoha.mobile.kmp.huki.model.domain.JelSymbol
import hu.mostoha.mobile.kmp.huki.model.domain.WaypointType
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class MarkerIconsTest {

    @Test
    fun `Given appearance and base layer - When waypointSymbol - Then pre-graded icon is only used on dark Outdoors`() {
        waypointTestCases().forEach { testCase ->
            val actual = MarkerIcons.waypointSymbol(testCase.type, testCase.isDarkMode, testCase.baseLayer)

            actual shouldBe testCase.result
        }
    }

    @Test
    fun `Given appearance and base layer - When placeSymbol - Then pre-graded icon is only used on dark Outdoors`() {
        placeTestCases().forEach { testCase ->
            val actual = MarkerIcons.placeSymbol(testCase.isDarkMode, testCase.baseLayer)

            actual shouldBe testCase.result
        }
    }

    @Test
    fun `Given waypoint type and GPX jel symbol - When waypointJelSymbol - Then only edge markers carry it`() {
        jelTestCases().forEach { testCase ->
            val actual = MarkerIcons.waypointJelSymbol(testCase.type, testCase.jelSymbol)

            actual shouldBe testCase.result
        }
    }

    @Test
    fun `Given appearance and base layer - When jelMarkerFrame - Then pre-graded icon is only used on dark Outdoors`() {
        jelBackgroundTestCases().forEach { testCase ->
            val actual = MarkerIcons.jelMarkerFrame(testCase.isDarkMode, testCase.baseLayer)

            actual shouldBe testCase.result
        }
    }

    companion object {

        private data class JelTestCase(
            val type: WaypointType,
            val jelSymbol: JelSymbol?,
            val result: JelSymbol?,
        )

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

        private fun waypointTestCases() =
            WaypointType.entries.flatMap { type ->
                listOf(
                    WaypointTestCase(type, isDarkMode = true, BaseLayer.OUTDOORS, type.outdoorsDarkIcon),
                    WaypointTestCase(type, isDarkMode = true, BaseLayer.CITY, type.icon),
                    WaypointTestCase(type, isDarkMode = true, BaseLayer.SATELLITE, type.icon),
                    WaypointTestCase(type, isDarkMode = false, BaseLayer.OUTDOORS, type.icon),
                    WaypointTestCase(type, isDarkMode = false, BaseLayer.CITY, type.icon),
                    WaypointTestCase(type, isDarkMode = false, BaseLayer.SATELLITE, type.icon),
                )
            }

        private fun jelTestCases() =
            listOf(
                JelTestCase(WaypointType.START, JelSymbol.ZT, JelSymbol.ZT),
                JelTestCase(WaypointType.END, JelSymbol.ZT, JelSymbol.ZT),
                JelTestCase(WaypointType.ROUND_TRIP, JelSymbol.ZT, JelSymbol.ZT),
                JelTestCase(WaypointType.INTERMEDIATE, JelSymbol.ZT, null),
                JelTestCase(WaypointType.START, null, null),
            )

        private fun jelBackgroundTestCases() =
            listOf(
                PlaceTestCase(true, BaseLayer.OUTDOORS, SharedRes.images.ic_gpx_jel_marker_frame_outdoors_dark),
                PlaceTestCase(true, BaseLayer.CITY, SharedRes.images.ic_gpx_jel_marker_frame),
                PlaceTestCase(true, BaseLayer.SATELLITE, SharedRes.images.ic_gpx_jel_marker_frame),
                PlaceTestCase(false, BaseLayer.OUTDOORS, SharedRes.images.ic_gpx_jel_marker_frame),
            )

        private fun placeTestCases() =
            listOf(
                PlaceTestCase(true, BaseLayer.OUTDOORS, SharedRes.images.ic_marker_picker_outdoors_dark),
                PlaceTestCase(true, BaseLayer.CITY, SharedRes.images.ic_marker_picker),
                PlaceTestCase(true, BaseLayer.SATELLITE, SharedRes.images.ic_marker_picker),
                PlaceTestCase(false, BaseLayer.OUTDOORS, SharedRes.images.ic_marker_picker),
                PlaceTestCase(false, BaseLayer.CITY, SharedRes.images.ic_marker_picker),
                PlaceTestCase(false, BaseLayer.SATELLITE, SharedRes.images.ic_marker_picker),
            )
    }
}
