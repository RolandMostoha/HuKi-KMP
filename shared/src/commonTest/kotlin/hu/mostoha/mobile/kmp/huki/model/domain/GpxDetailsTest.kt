package hu.mostoha.mobile.kmp.huki.model.domain

import hu.mostoha.mobile.kmp.huki.data.TEST_GPX_DETAILS
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class GpxDetailsTest {

    @Test
    fun `Given GPX waypoints - When mapsNavigationTarget - Then the edge or first last waypoint returns`() {
        testCases().forEach { (waypoints, type, expected) ->
            val gpxDetails = TEST_GPX_DETAILS.copy(waypoints = waypoints)

            val actual = gpxDetails.mapsNavigationTarget(type)

            actual shouldBe expected
        }
    }

    companion object {

        private val START = Location(47.0, 19.0)
        private val END = Location(48.0, 20.0)
        private val FIRST = Location(47.5, 19.5)
        private val LAST = Location(47.6, 19.6)

        private val trackWaypoints = listOf(
            GpxWaypoint(FIRST, WaypointType.INTERMEDIATE),
            GpxWaypoint(START, WaypointType.START),
            GpxWaypoint(END, WaypointType.END),
        )
        private val roundTripWaypoints = listOf(
            GpxWaypoint(FIRST, WaypointType.INTERMEDIATE),
            GpxWaypoint(START, WaypointType.ROUND_TRIP),
        )
        private val waypointOnlyWaypoints = listOf(
            GpxWaypoint(FIRST, WaypointType.INTERMEDIATE),
            GpxWaypoint(START, WaypointType.INTERMEDIATE),
            GpxWaypoint(LAST, WaypointType.INTERMEDIATE),
        )
        private val singleWaypoint = listOf(GpxWaypoint(FIRST, WaypointType.INTERMEDIATE))

        data class TestCase(
            val waypoints: List<GpxWaypoint>,
            val type: GpxMapsNavigationType,
            val expected: Location?,
        )

        private fun testCases() =
            listOf(
                TestCase(trackWaypoints, GpxMapsNavigationType.START, START),
                TestCase(trackWaypoints, GpxMapsNavigationType.END, END),
                TestCase(roundTripWaypoints, GpxMapsNavigationType.START, START),
                TestCase(roundTripWaypoints, GpxMapsNavigationType.END, null),
                TestCase(waypointOnlyWaypoints, GpxMapsNavigationType.START, FIRST),
                TestCase(waypointOnlyWaypoints, GpxMapsNavigationType.END, LAST),
                TestCase(singleWaypoint, GpxMapsNavigationType.START, FIRST),
                TestCase(singleWaypoint, GpxMapsNavigationType.END, null),
                TestCase(emptyList(), GpxMapsNavigationType.START, null),
                TestCase(emptyList(), GpxMapsNavigationType.END, null),
            )
    }
}
