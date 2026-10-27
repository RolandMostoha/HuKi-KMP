package hu.mostoha.mobile.kmp.huki.util

import hu.mostoha.mobile.kmp.huki.data.TEST_GPX_DETAILS
import hu.mostoha.mobile.kmp.huki.model.domain.GpxWaypoint
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.WaypointType
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlin.test.Test

class GpxDirectionUtilsTest {

    private val uphillTrack = listOf(
        Location(47.680, 18.900, 300.0),
        Location(47.690, 18.910, 450.0),
        Location(47.700, 18.920, 700.0),
    )
    private val intermediate = GpxWaypoint(Location(47.690, 18.910), WaypointType.INTERMEDIATE, name = "Kilátó")
    private val gpxDetails = TEST_GPX_DETAILS.copy(
        locations = uphillTrack,
        waypoints = listOf(
            intermediate,
            GpxWaypoint(uphillTrack.first(), WaypointType.START),
            GpxWaypoint(uphillTrack.last(), WaypointType.END),
        ),
        travelTime = uphillTrack.calculateTravelTime(),
        incline = uphillTrack.calculateIncline(),
        decline = uphillTrack.calculateDecline(),
        slopeSegments = uphillTrack.calculateSlopeSegments(),
    )

    @Test
    fun `Given GPX - When reversed - Then locations are in reverse order`() {
        val actual = gpxDetails.reversed()

        actual.locations shouldBe uphillTrack.reversed()
    }

    @Test
    fun `Given GPX with start and end - When reversed - Then start and end swap and others stay`() {
        val actual = gpxDetails.reversed()

        actual.waypoints shouldBe listOf(
            intermediate,
            GpxWaypoint(uphillTrack.first(), WaypointType.END),
            GpxWaypoint(uphillTrack.last(), WaypointType.START),
        )
    }

    @Test
    fun `Given round trip GPX - When reversed - Then round trip waypoint is kept`() {
        val roundTrip = GpxWaypoint(uphillTrack.first(), WaypointType.ROUND_TRIP)

        val actual = gpxDetails.copy(waypoints = listOf(roundTrip)).reversed()

        actual.waypoints shouldBe listOf(roundTrip)
    }

    @Test
    fun `Given uphill GPX - When reversed - Then incline and decline swap`() {
        val actual = gpxDetails.reversed()

        actual.incline shouldBe gpxDetails.decline
        actual.decline shouldBe gpxDetails.incline
    }

    @Test
    fun `Given uphill GPX - When reversed - Then slopes turn into descents`() {
        val actual = gpxDetails.reversed()

        actual.slopeSegments.first().slopePercent shouldBeLessThan 0.0
        actual.slopeSegments shouldBe uphillTrack.reversed().calculateSlopeSegments()
    }

    @Test
    fun `Given uphill GPX - When reversed - Then downhill travel time is shorter`() {
        val actual = gpxDetails.reversed()

        actual.travelTime shouldBe uphillTrack.reversed().calculateTravelTime()
        actual.travelTime shouldBeLessThan gpxDetails.travelTime
    }

    @Test
    fun `Given GPX - When reversed - Then a new layer id is assigned`() {
        val actual = gpxDetails.reversed()

        actual.layerId shouldNotBe gpxDetails.layerId
    }

    @Test
    fun `Given GPX - When reversed twice - Then the original route returns`() {
        val actual = gpxDetails.reversed().reversed()

        actual.copy(layerId = gpxDetails.layerId) shouldBe gpxDetails
    }
}
