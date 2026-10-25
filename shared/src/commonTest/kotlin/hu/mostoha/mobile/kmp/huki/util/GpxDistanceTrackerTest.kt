package hu.mostoha.mobile.kmp.huki.util

import hu.mostoha.mobile.kmp.huki.model.domain.GpxWaypoint
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.RouteProgress
import hu.mostoha.mobile.kmp.huki.model.domain.WaypointType
import hu.mostoha.mobile.kmp.huki.util.formatter.DistanceFormatter
import hu.mostoha.mobile.kmp.huki.util.formatter.TravelTimeFormatter
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class GpxDistanceTrackerTest {

    @Test
    fun `Given unknown user location - When distanceInfoWindows - Then all windows are measured from the start`() {
        val tracker = GpxDistanceTracker(LINE_TRACK, WAYPOINTS)

        val windows = tracker.distanceInfoWindows(userLocation = null, showAll = true, selected = null)

        windows.map { it.location } shouldBe WAYPOINTS.map { it.location }
        windows.last().distance shouldBe expectedDistance(fromIndex = 0, toIndex = 3)
    }

    @Test
    fun `Given user off the route - When distanceInfoWindows - Then all windows are measured from the start`() {
        val tracker = GpxDistanceTracker(LINE_TRACK, WAYPOINTS)

        val windows = tracker.distanceInfoWindows(OFF_ROUTE_LOCATION, showAll = true, selected = null)

        windows.map { it.location } shouldBe WAYPOINTS.map { it.location }
        windows[1].distance shouldBe expectedDistance(fromIndex = 0, toIndex = 1)
    }

    @Test
    fun `Given start waypoint - When distanceInfoWindows - Then it has no metrics`() {
        val tracker = GpxDistanceTracker(LINE_TRACK, WAYPOINTS)

        val start = tracker.distanceInfoWindows(userLocation = null, showAll = true, selected = null).first()

        start.distance.shouldBeNull()
        start.travelTime.shouldBeNull()
    }

    @Test
    fun `Given waypoint ahead - When distanceInfoWindows - Then it shows the distance and travel time ahead`() {
        val tracker = GpxDistanceTracker(LINE_TRACK, WAYPOINTS)

        val first = tracker.distanceInfoWindows(LINE_TRACK[0], showAll = true, selected = null)[1]

        val progress = progressBetween(fromIndex = 0, toIndex = 1)
        first.distance shouldBe DistanceFormatter.formatDistance(progress.distance)
        first.travelTime shouldBe TravelTimeFormatter.formatTravelTime(progress.travelTime)
    }

    @Test
    fun `Given user at the start - When distanceInfoWindows - Then the start window stays visible`() {
        val tracker = GpxDistanceTracker(LINE_TRACK, WAYPOINTS)

        val windows = tracker.distanceInfoWindows(LINE_TRACK[0], showAll = true, selected = null)

        windows.map { it.location } shouldBe WAYPOINTS.map { it.location }
    }

    @Test
    fun `Given user on the route - When distanceInfoWindows - Then passed and nearby waypoints are hidden`() {
        val tracker = GpxDistanceTracker(LINE_TRACK, WAYPOINTS)

        val windows = tracker.distanceInfoWindows(LINE_TRACK[1], showAll = true, selected = null)

        windows.map { it.location } shouldBe listOf(SECOND.location, END.location)
    }

    @Test
    fun `Given user just before a waypoint - When distanceInfoWindows - Then that waypoint is hidden`() {
        val tracker = GpxDistanceTracker(LINE_TRACK, WAYPOINTS)
        val justBeforeSecond = Location(47.5197, 19.0000, 140.0)

        val windows = tracker.distanceInfoWindows(justBeforeSecond, showAll = true, selected = null)

        windows.map { it.location } shouldBe listOf(END.location)
    }

    @Test
    fun `Given selected passed waypoint - When distanceInfoWindows - Then it is shown with the distance back`() {
        val tracker = GpxDistanceTracker(LINE_TRACK, WAYPOINTS)

        val windows = tracker.distanceInfoWindows(LINE_TRACK[2], showAll = false, selected = FIRST)

        windows.single().distance shouldBe expectedDistance(fromIndex = 2, toIndex = 1)
    }

    @Test
    fun `Given all distances visible - When a passed waypoint is selected - Then it is shown next to the others`() {
        val tracker = GpxDistanceTracker(LINE_TRACK, WAYPOINTS)

        val windows = tracker.distanceInfoWindows(LINE_TRACK[2], showAll = true, selected = FIRST)

        windows.map { it.location } shouldBe listOf(FIRST.location, END.location)
    }

    @Test
    fun `Given distances hidden and no selection - When distanceInfoWindows - Then there are no windows`() {
        val tracker = GpxDistanceTracker(LINE_TRACK, WAYPOINTS)

        val windows = tracker.distanceInfoWindows(LINE_TRACK[0], showAll = false, selected = null)

        windows.shouldBeEmpty()
    }

    @Test
    fun `Given waypoints - When distanceInfoWindows - Then ids are the waypoint indexes`() {
        val tracker = GpxDistanceTracker(LINE_TRACK, WAYPOINTS)

        val windows = tracker.distanceInfoWindows(userLocation = null, showAll = true, selected = null)

        windows.map { it.id } shouldBe listOf(0, 1, 2, 3)
    }

    @Test
    fun `Given round trip and user at the start - When distanceInfoWindows - Then it shows the whole loop`() {
        val tracker = GpxDistanceTracker(LOOP_TRACK, listOf(ROUND_TRIP))

        val window = tracker.distanceInfoWindows(LOOP_TRACK.first(), showAll = true, selected = null).single()

        window.distance shouldBe DistanceFormatter.formatDistance(LOOP_TRACK.calculateTotalDistance())
    }

    @Test
    fun `Given round trip and user mid-loop - When distanceInfoWindows - Then it shows the rest of the loop`() {
        val tracker = GpxDistanceTracker(LOOP_TRACK, listOf(ROUND_TRIP))

        val window = tracker.distanceInfoWindows(LOOP_TRACK[2], showAll = true, selected = null).single()

        window.distance shouldBe DistanceFormatter.formatDistance(LOOP_TRACK.subList(2, 5).calculateTotalDistance())
    }

    @Test
    fun `Given round trip walked to the end - When back at the start - Then the loop counts as finished`() {
        val intermediate = GpxWaypoint(LOOP_TRACK[1], WaypointType.INTERMEDIATE)
        val tracker = GpxDistanceTracker(LOOP_TRACK, listOf(ROUND_TRIP, intermediate))
        tracker.distanceInfoWindows(LOOP_TRACK[3], showAll = true, selected = null)

        val windows = tracker.distanceInfoWindows(LOOP_TRACK.first(), showAll = true, selected = null)

        windows.shouldBeEmpty()
    }

    private fun progressBetween(fromIndex: Int, toIndex: Int): RouteProgress {
        val index = TrackProgressIndex(LINE_TRACK)
        return index.progress(
            from = index.positionOf(LINE_TRACK[fromIndex]),
            to = index.positionOf(LINE_TRACK[toIndex]),
        )
    }

    private fun expectedDistance(fromIndex: Int, toIndex: Int): String {
        val progress = progressBetween(fromIndex, toIndex)
        return DistanceFormatter.formatDistance(progress.distance)
    }

    private companion object {
        // North-south line at constant longitude, ~1.1 km per 0.01° latitude
        val LINE_TRACK = listOf(
            Location(47.5000, 19.0000, 100.0),
            Location(47.5100, 19.0000, 120.0),
            Location(47.5200, 19.0000, 140.0),
            Location(47.5300, 19.0000, 160.0),
        )

        val LOOP_TRACK = listOf(
            Location(47.5000, 19.0000, 100.0),
            Location(47.5100, 19.0000, 110.0),
            Location(47.5100, 19.0100, 120.0),
            Location(47.5000, 19.0100, 110.0),
            Location(47.5000, 19.0000, 100.0),
        )

        val START = GpxWaypoint(LINE_TRACK[0], WaypointType.START)
        val FIRST = GpxWaypoint(LINE_TRACK[1], WaypointType.INTERMEDIATE)
        val SECOND = GpxWaypoint(LINE_TRACK[2], WaypointType.INTERMEDIATE)
        val END = GpxWaypoint(LINE_TRACK[3], WaypointType.END)
        val WAYPOINTS = listOf(START, FIRST, SECOND, END)

        val ROUND_TRIP = GpxWaypoint(LOOP_TRACK.first(), WaypointType.ROUND_TRIP)

        // ~1.5 km east of the line track
        val OFF_ROUTE_LOCATION = Location(47.5100, 19.0200, 120.0)
    }
}
