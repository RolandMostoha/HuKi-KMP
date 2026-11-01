package hu.mostoha.mobile.kmp.huki.util

import hu.mostoha.mobile.kmp.huki.model.domain.GpxWaypoint
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.WaypointType
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class GpxEdgeWaypointUtilsTest {

    private val track = listOf(
        Location(47.680, 18.900, 300.0),
        Location(47.690, 18.910, 450.0),
        Location(47.700, 18.920, 700.0),
    )
    private val start = GpxWaypoint(track.first(), WaypointType.START)
    private val end = GpxWaypoint(track.last(), WaypointType.END)

    @Test
    fun `Given named waypoints on the track edges - When withNamedEdges - Then they name the edges and are dropped`() {
        val waypoints = listOf(
            GpxWaypoint(Location(47.680, 18.900), WaypointType.INTERMEDIATE, name = "Dömös", description = "Rév"),
            GpxWaypoint(Location(47.690, 18.910), WaypointType.INTERMEDIATE, name = "Rám-szakadék"),
            GpxWaypoint(Location(47.700, 18.920), WaypointType.INTERMEDIATE, name = "Dobogókő"),
        )

        val actual = waypoints.withNamedEdges(listOf(start, end), track)

        actual shouldBe listOf(
            waypoints[1],
            start.copy(name = "Dömös", description = "Rév"),
            end.copy(name = "Dobogókő"),
        )
    }

    @Test
    fun `Given a named waypoint near but not on an edge - When withNamedEdges - Then the edge keeps no name`() {
        val stamp = GpxWaypoint(Location(47.6801, 18.9001), WaypointType.INTERMEDIATE, name = "Bélyegzőhely")

        val actual = listOf(stamp).withNamedEdges(listOf(start, end), track)

        actual shouldBe listOf(stamp, start, end)
    }

    @Test
    fun `Given an unnamed waypoint on an edge - When withNamedEdges - Then it is kept as an intermediate marker`() {
        val unnamed = GpxWaypoint(track.first(), WaypointType.INTERMEDIATE, name = " ", description = "Parkoló")

        val actual = listOf(unnamed).withNamedEdges(listOf(start, end), track)

        actual shouldBe listOf(unnamed, start, end)
    }

    @Test
    fun `Given a round trip with named waypoints on both ends - When withNamedEdges - Then both are merged into it`() {
        val roundTripTrack = track + Location(47.68005, 18.90005)
        val roundTrip = GpxWaypoint(roundTripTrack.first(), WaypointType.ROUND_TRIP)
        val waypoints = listOf(
            GpxWaypoint(roundTripTrack.first(), WaypointType.INTERMEDIATE, name = "Dobogókő"),
            GpxWaypoint(roundTripTrack.last(), WaypointType.INTERMEDIATE, name = "Dobogókő"),
        )

        val actual = waypoints.withNamedEdges(listOf(roundTrip), roundTripTrack)

        actual shouldBe listOf(roundTrip.copy(name = "Dobogókő"))
    }
}
