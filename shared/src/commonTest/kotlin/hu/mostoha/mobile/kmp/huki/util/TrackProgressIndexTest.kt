package hu.mostoha.mobile.kmp.huki.util

import hu.mostoha.mobile.kmp.huki.model.domain.Location
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import org.maplibre.spatialk.units.extensions.inMeters
import kotlin.test.Test
import kotlin.time.DurationUnit

class TrackProgressIndexTest {

    @Test
    fun `Given track positions - When progress - Then it matches the route progress along the track`() {
        val index = TrackProgressIndex(TRACK)

        testCases().forEach { testCase ->
            val actual = index.progress(
                from = index.positionOf(testCase.from),
                to = index.positionOf(testCase.to),
            )

            val expected = TRACK.routeProgressTo(from = testCase.from, to = testCase.to, isRoundTrip = false)
            actual.distance.inMeters shouldBe expected.distance.inMeters.plusOrMinus(TOLERANCE_METERS)
            actual.travelTime.toDouble(DurationUnit.MINUTES) shouldBe
                expected.travelTime.toDouble(DurationUnit.MINUTES).plusOrMinus(TOLERANCE_MINUTES)
        }
    }

    @Test
    fun `Given a location next to the track - When positionOf - Then the off track distance is measured to the track`() {
        val index = TrackProgressIndex(TRACK)
        val location = Location(47.5050, 19.0100)

        val actual = index.positionOf(location)

        actual.segmentIndex shouldBe 0
        actual.fraction shouldBe 0.5.plusOrMinus(0.01)
        actual.offTrackDistance.inMeters shouldBe 752.0.plusOrMinus(TOLERANCE_METERS)
    }

    @Test
    fun `Given uphill track - When progress in both directions - Then walking up takes longer than walking down`() {
        val index = TrackProgressIndex(TRACK)
        val start = index.positionOf(TRACK.first())
        val end = index.positionOf(TRACK.last())

        val uphill = index.progress(from = start, to = end)
        val downhill = index.progress(from = end, to = start)

        uphill.distance shouldBe downhill.distance
        (uphill.travelTime > downhill.travelTime) shouldBe true
    }

    private data class TestCase(
        val from: Location,
        val to: Location,
    )

    private companion object {
        // North-south line climbing northwards, ~1.1 km per 0.01° latitude
        val TRACK = listOf(
            Location(47.5000, 19.0000, 100.0),
            Location(47.5100, 19.0000, 180.0),
            Location(47.5200, 19.0000, 150.0),
            Location(47.5300, 19.0000, 300.0),
        )

        const val TOLERANCE_METERS = 2.0
        const val TOLERANCE_MINUTES = 0.5

        fun testCases() =
            listOf(
                TestCase(from = TRACK[0], to = TRACK[3]),
                TestCase(from = TRACK[3], to = TRACK[0]),
                TestCase(from = TRACK[1], to = TRACK[2]),
                TestCase(from = Location(47.5050, 19.0000), to = Location(47.5250, 19.0000)),
                TestCase(from = Location(47.5250, 19.0003), to = Location(47.5050, 18.9997)),
            )
    }
}
