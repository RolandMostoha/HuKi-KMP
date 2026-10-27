package hu.mostoha.mobile.kmp.huki.util

import hu.mostoha.mobile.kmp.huki.model.domain.Location
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import org.maplibre.spatialk.units.extensions.inMeters
import kotlin.math.abs
import kotlin.test.Test

class SlopeUtilsTest {

    @Test
    fun `Given flat track - When calculateSlopeSegments - Then a single segment covers the whole track`() {
        val locations = track(pointCount = 20) { 300.0 }

        val actual = locations.calculateSlopeSegments()

        actual shouldHaveSize 1
        actual.single().slopePercent shouldBe 0.0
        actual.single().locations shouldBe locations
    }

    @Test
    fun `Given constant 10 percent climb - When calculateSlopeSegments - Then every slope is 10 percent`() {
        val locations = track(pointCount = 20) { distance -> 300.0 + distance * 0.1 }

        val actual = locations.calculateSlopeSegments()

        actual.forEach { it.slopePercent shouldBe (10.0 plusOrMinus 0.01) }
    }

    @Test
    fun `Given climb - When reversed track is calculated - Then slopes are negated`() {
        val locations = track(pointCount = 20) { distance -> 300.0 + distance * 0.1 }

        val actual = locations.reversed().calculateSlopeSegments()

        actual.forEach { it.slopePercent shouldBe (-10.0 plusOrMinus 0.01) }
    }

    @Test
    fun `Given climb - When calculateSlopeSegments - Then segments chain from start to end`() {
        val locations = track(pointCount = 20) { distance -> 300.0 + distance * 0.1 }

        val actual = locations.calculateSlopeSegments()

        actual.first().locations.first() shouldBe locations.first()
        actual.last().locations.last() shouldBe locations.last()
        actual.zipWithNext().forEach { (previous, next) -> previous.locations.last() shouldBe next.locations.first() }
    }

    @Test
    fun `Given climb - When calculateSlopeSegments - Then every track point is kept in order`() {
        val locations = track(pointCount = 20) { distance -> 300.0 + distance * 0.1 }

        val actual = locations.calculateSlopeSegments().flatMap { it.locations }

        actual.filter { it in locations } shouldBe locations
    }

    @Test
    fun `Given track without altitudes - When calculateSlopeSegments - Then segments are empty`() {
        val locations = track(pointCount = 20) { null }

        val actual = locations.calculateSlopeSegments()

        actual shouldBe emptyList()
    }

    @Test
    fun `Given single location - When calculateSlopeSegments - Then segments are empty`() {
        val locations = listOf(Location(47.0, 19.0, 300.0))

        val actual = locations.calculateSlopeSegments()

        actual shouldBe emptyList()
    }

    @Test
    fun `Given flat dense track with altitude noise - When calculateSlopeSegments - Then slopes stay flat`() {
        val locations = track(pointCount = 200, latitudeStep = 0.00002) { distance ->
            if ((distance / 2).toInt() % 2 == 0) 301.0 else 299.0
        }

        val actual = locations.calculateSlopeSegments()

        actual.forEach { abs(it.slopePercent) shouldBeLessThan SlopeConstants.FLAT_SLOPE_PERCENT }
    }

    @Test
    fun `Given long climbing track - When calculateSlopeSegments - Then segment count is capped`() {
        val locations = track(pointCount = 2000, latitudeStep = 0.001) { distance -> 300.0 + distance * 0.1 }

        val actual = locations.calculateSlopeSegments()

        actual.size shouldBeLessThanOrEqual SlopeConstants.MAX_SEGMENTS
    }

    @Test
    fun `Given long track with climbs between flat segment ends - When calculateSlopeSegments - Then climbs are kept`() {
        val points = List(9000) { index -> Location(latitude = 47.0 + index * 0.0001, longitude = 19.0) }
        val distances = points.runningDistances()
        val segmentLength = distances.last() / SlopeConstants.MAX_SEGMENTS
        // Each segment climbs 20 m in its middle half and is flat around both of its ends
        val locations = points.mapIndexed { index, location ->
            val position = distances[index] / segmentLength
            val withinSegment = (position - position.toInt()).coerceIn(0.25, 0.75)
            location.copy(altitude = 300.0 + 20.0 * (position.toInt() + (withinSegment - 0.25) * 2))
        }

        val actual = locations.calculateSlopeSegments()

        actual.forEach { it.slopePercent shouldBe (20.0 / segmentLength * 100 plusOrMinus 0.5) }
    }

    private fun track(
        pointCount: Int,
        latitudeStep: Double = 0.0005,
        altitude: (distance: Double) -> Double?,
    ): List<Location> {
        val points = List(pointCount) { index -> Location(latitude = 47.0 + index * latitudeStep, longitude = 19.0) }
        val distances = points.runningDistances()
        return points.mapIndexed { index, location -> location.copy(altitude = altitude(distances[index])) }
    }

    private fun List<Location>.runningDistances(): List<Double> =
        indices.runningFold(0.0) { distance, index ->
            if (index == 0) distance else distance + this[index - 1].distanceBetween(this[index]).inMeters
        }.drop(1)
}
