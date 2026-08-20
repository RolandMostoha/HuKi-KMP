package hu.mostoha.mobile.kmp.huki.util

import hu.mostoha.mobile.kmp.huki.model.domain.Location
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import org.maplibre.spatialk.units.extensions.inMeters
import kotlin.test.Test
import kotlin.time.Duration

class GpxRouteUtilsTest {

    private companion object {
        // North-south line at constant longitude, ~1.1 km per 0.01° latitude.
        val LINE_TRACK = listOf(
            Location(47.5000, 19.0000, 100.0),
            Location(47.5100, 19.0000, 120.0),
            Location(47.5200, 19.0000, 140.0),
            Location(47.5300, 19.0000, 160.0),
        )

        // Closed loop returning to the start point.
        val LOOP_TRACK = listOf(
            Location(47.5000, 19.0000, 100.0),
            Location(47.5100, 19.0000, 110.0),
            Location(47.5100, 19.0100, 120.0),
            Location(47.5000, 19.0100, 110.0),
            Location(47.5000, 19.0000, 100.0),
        )

        const val TOLERANCE_METERS = 2.0
    }

    @Test
    fun `Given full track - When from start to end - Then distance equals total track distance`() {
        val expected = LINE_TRACK.calculateTotalDistance().inMeters

        val progress = LINE_TRACK.routeProgressTo(
            from = LINE_TRACK.first(),
            to = LINE_TRACK.last(),
            isRoundTrip = false,
        )

        progress.distance.inMeters shouldBe expected.plusOrMinus(TOLERANCE_METERS)
        progress.travelTime shouldBeGreaterThan Duration.ZERO
    }

    @Test
    fun `Given full track - When from start to intermediate point - Then distance is partial`() {
        val expected = LINE_TRACK.subList(0, 3).calculateTotalDistance().inMeters

        val progress = LINE_TRACK.routeProgressTo(
            from = LINE_TRACK.first(),
            to = LINE_TRACK[2],
            isRoundTrip = false,
        )

        progress.distance.inMeters shouldBe expected.plusOrMinus(TOLERANCE_METERS)
    }

    @Test
    fun `Given full track - When target is behind on a linear route - Then distance is measured backward`() {
        val expected = LINE_TRACK.calculateTotalDistance().inMeters

        val progress = LINE_TRACK.routeProgressTo(
            from = LINE_TRACK.last(),
            to = LINE_TRACK.first(),
            isRoundTrip = false,
        )

        progress.distance.inMeters shouldBe expected.plusOrMinus(TOLERANCE_METERS)
        progress.travelTime shouldBeGreaterThan Duration.ZERO
    }

    @Test
    fun `Given round trip - When target is ahead - Then distance is measured forward without wrapping`() {
        val expected = LOOP_TRACK[0].distanceBetween(LOOP_TRACK[1]).inMeters

        val progress = LOOP_TRACK.routeProgressTo(
            from = LOOP_TRACK.first(),
            to = LOOP_TRACK[1],
            isRoundTrip = true,
        )

        progress.distance.inMeters shouldBe expected.plusOrMinus(TOLERANCE_METERS)
    }

    @Test
    fun `Given round trip - When target is behind - Then distance wraps forward around the loop`() {
        val total = LOOP_TRACK.calculateTotalDistance().inMeters
        val firstSegment = LOOP_TRACK[0].distanceBetween(LOOP_TRACK[1]).inMeters

        val progress = LOOP_TRACK.routeProgressTo(
            from = LOOP_TRACK[1],
            to = LOOP_TRACK.first(),
            isRoundTrip = true,
        )

        progress.distance.inMeters shouldBe (total - firstSegment).plusOrMinus(TOLERANCE_METERS)
        progress.distance.inMeters shouldBeGreaterThan firstSegment
    }

    @Test
    fun `Given location beside the track - When computing progress - Then it snaps onto the track`() {
        val besideStart = Location(47.5000, 19.0010, 100.0)
        val expected = LINE_TRACK.calculateTotalDistance().inMeters

        val progress = LINE_TRACK.routeProgressTo(
            from = besideStart,
            to = LINE_TRACK.last(),
            isRoundTrip = false,
        )

        progress.distance.inMeters shouldBe expected.plusOrMinus(TOLERANCE_METERS)
    }

    @Test
    fun `Given single point track - When computing progress - Then falls back to straight-line distance`() {
        val single = listOf(Location(47.5000, 19.0000, 100.0))
        val target = Location(47.5100, 19.0000, 120.0)

        val progress = single.routeProgressTo(from = single.first(), to = target, isRoundTrip = false)

        val straightLine = single.first().distanceBetween(target).inMeters
        progress.distance.inMeters shouldBe straightLine.plusOrMinus(TOLERANCE_METERS)
    }
}
