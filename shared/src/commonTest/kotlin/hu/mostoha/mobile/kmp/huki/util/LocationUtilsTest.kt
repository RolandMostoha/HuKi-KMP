package hu.mostoha.mobile.kmp.huki.util

import hu.mostoha.mobile.kmp.huki.model.domain.Location
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import org.maplibre.spatialk.units.extensions.inMeters
import org.maplibre.spatialk.units.extensions.meters
import kotlin.test.Test

class LocationUtilsTest {

    @Test
    fun `Given two locations - when distanceBetween - then distance returns`() {
        val from = Location(47.123, 19.234)
        val to = Location(46.567, 19.345)

        val distance = from.distanceBetween(to)

        distance.toString() shouldBe 62_398.16.meters.toString()
    }

    @Test
    fun `Given list of locations - when calculateDistance - then the total distance returns`() {
        val from = Location(47.123, 19.234)
        val to = Location(46.567, 19.345)
        val locations = listOf(from, to, from, to)

        val distance = locations.calculateTotalDistance()

        distance.toString() shouldBe (3 * 62_398.16).meters.toString()
    }

    @Test
    fun `Given list of locations - when calculateCenter - then the center location returns`() {
        val location1 = Location(47.123, 19.234)
        val location2 = Location(46.567, 19.345)
        val locations = listOf(location1, location2)

        val center = locations.calculateCenter()

        center shouldBe Location(
            latitude = (location1.latitude + location2.latitude) / 2,
            longitude = (location1.longitude + location2.longitude) / 2,
        )
    }

    @Test
    fun `Given list of locations - when calculateIncline - then the total incline of locations returns`() {
        val locations = listOf(
            Location(47.123, 19.234, 90.0),
            Location(47.123, 19.234, 100.0),
            Location(46.567, 19.345, 95.0),
            Location(46.567, 19.345, 110.0),
            Location(46.567, 19.345, 120.0),
            Location(46.567, 19.345, 115.0),
        )

        val incline = locations.calculateIncline()

        incline shouldBe 10 + 15 + 10
    }

    @Test
    fun `Given list of locations - when calculateDecline - then the total decline of locations returns`() {
        val locations = listOf(
            Location(47.123, 19.234, 90.0),
            Location(47.123, 19.234, 100.0),
            Location(46.567, 19.345, 95.0),
            Location(46.567, 19.345, 110.0),
            Location(46.567, 19.345, 120.0),
            Location(46.567, 19.345, 115.0),
        )

        val incline = locations.calculateDecline()

        incline shouldBe 5 + 5
    }

    @Test
    fun `Given track - When nearestIndexTo - Then the index of the closest location returns`() {
        val track = listOf(Location(47.0, 19.0), Location(47.1, 19.1), Location(47.2, 19.2))

        val index = track.nearestIndexTo(Location(47.11, 19.09))

        index shouldBe 1
    }

    @Test
    fun `Given track and points on it - When legDistancesTo - Then distances between consecutive points return`() {
        val track = listOf(Location(47.0, 19.0), Location(47.01, 19.0), Location(47.02, 19.0), Location(47.03, 19.0))
        val points = listOf(track[1], track[3], track[2])

        val legDistances = track.legDistancesTo(points)

        legDistances[0] shouldBe track[0].distanceBetween(track[1])
        legDistances[1].inMeters shouldBe (track[1].distanceBetween(track[2]) + track[2].distanceBetween(track[3]))
            .inMeters.plusOrMinus(0.001)
        legDistances[2] shouldBe 0.meters
    }
}
