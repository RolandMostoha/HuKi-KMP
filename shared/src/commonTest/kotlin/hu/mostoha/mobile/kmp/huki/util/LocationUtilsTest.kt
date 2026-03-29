package hu.mostoha.mobile.kmp.huki.util

import hu.mostoha.mobile.kmp.huki.model.domain.Location
import io.kotest.matchers.shouldBe
import org.maplibre.spatialk.units.extensions.meters
import kotlin.test.Test

class LocationUtilsTest {

    @Test
    fun `Given two locations, when distanceBetween, then distance returns`() {
        val from = Location(47.123, 19.234)
        val to = Location(46.567, 19.345)

        val distance = from.distanceBetween(to)

        distance.toString() shouldBe 62_398.16.meters.toString()
    }

    @Test
    fun `Given list of locations, when calculateDistance, then the total distance returns`() {
        val from = Location(47.123, 19.234)
        val to = Location(46.567, 19.345)
        val locations = listOf(from, to, from, to)

        val distance = locations.calculateTotalDistance()

        distance.toString() shouldBe (3 * 62_398.16).meters.toString()
    }

    @Test
    fun `Given list of locations, when calculateCenter, then the center location returns`() {
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
    fun `Given list of locations, when calculateIncline, then the total incline of locations returns`() {
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
    fun `Given list of locations, when calculateDecline, then the total decline of locations returns`() {
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
}
