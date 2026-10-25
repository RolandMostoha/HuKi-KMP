package hu.mostoha.mobile.kmp.huki.model.mapper

import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.StringDesc
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.GpxWaypoint
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.RouteProgress
import hu.mostoha.mobile.kmp.huki.model.domain.WaypointType
import hu.mostoha.mobile.kmp.huki.util.formatter.DistanceFormatter
import hu.mostoha.mobile.kmp.huki.util.formatter.TravelTimeFormatter
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.maplibre.spatialk.units.extensions.meters
import kotlin.test.Test
import kotlin.time.Duration.Companion.minutes

class DistanceInfoWindowMapperTest {

    @Test
    fun `Given waypoint types - When toDistanceInfoWindow - Then label and priority follow the type`() {
        testCases().forEach { testCase ->
            val waypoint = GpxWaypoint(LOCATION, testCase.type)

            val actual = waypoint.toDistanceInfoWindow(id = 0, progress = null)

            actual.label shouldBe testCase.label
            actual.priority shouldBe testCase.priority
        }
    }

    @Test
    fun `Given progress - When toDistanceInfoWindow - Then distance and travel time are formatted`() {
        val progress = RouteProgress(distance = 850.meters, travelTime = 15.minutes)

        val actual = GpxWaypoint(LOCATION, WaypointType.INTERMEDIATE).toDistanceInfoWindow(id = 2, progress = progress)

        actual.id shouldBe 2
        actual.distance shouldBe DistanceFormatter.formatDistance(progress.distance)
        actual.travelTime shouldBe TravelTimeFormatter.formatTravelTime(progress.travelTime)
    }

    @Test
    fun `Given no progress - When toDistanceInfoWindow - Then there are no metrics`() {
        val actual = GpxWaypoint(LOCATION, WaypointType.START).toDistanceInfoWindow(id = 0, progress = null)

        actual.distance.shouldBeNull()
        actual.travelTime.shouldBeNull()
    }

    @Test
    fun `Given waypoint names - When toDistanceInfoWindow - Then names are trimmed and blanks dropped`() {
        val names = listOf(" Dobogókő ", "  ", null)

        val actual = names.map { name ->
            GpxWaypoint(LOCATION, WaypointType.INTERMEDIATE, name = name).toDistanceInfoWindow(id = 0, progress = null)
        }

        actual.map { it.name } shouldBe listOf("Dobogókő", null, null)
    }

    private data class TestCase(
        val type: WaypointType,
        val label: StringDesc?,
        val priority: Int,
    )

    private companion object {
        val LOCATION = Location(47.5, 19.0)

        fun testCases() =
            listOf(
                TestCase(
                    type = WaypointType.END,
                    label = StringDesc.Resource(SharedRes.strings.gpx_distance_info_window_end),
                    priority = 3,
                ),
                TestCase(type = WaypointType.ROUND_TRIP, label = null, priority = 3),
                TestCase(
                    type = WaypointType.START,
                    label = StringDesc.Resource(SharedRes.strings.gpx_distance_info_window_start),
                    priority = 2,
                ),
                TestCase(type = WaypointType.INTERMEDIATE, label = null, priority = 1),
            )
    }
}
