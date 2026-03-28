package hu.mostoha.mobile.kmp.huki.util

import hu.mostoha.mobile.kmp.huki.model.domain.Location
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class TravelTimeUtilsTest {
    @Test
    fun `Given 5 km distance without incline, when naismith, then 1 hour travel time returns`() {
        val travelTime = naismith(5.0, 0.0)

        travelTime shouldBe 1.25
    }

    @Test
    fun `Given list of locations, when calculateTravelTime, then estimated travel time returns`() {
        val locations = listOf(
            Location(47.123, 19.234, 90.0),
            Location(47.123, 19.235, 100.0),
            Location(47.123, 19.236, 100.0),
            Location(47.123, 19.237, 90.0),
        )

        val travelTime = locations.calculateTravelTime()

        travelTime shouldBe 4.minutes.plus(24.282289825.seconds)
    }
}
