package hu.mostoha.mobile.kmp.huki.util

import hu.mostoha.mobile.kmp.huki.model.domain.RoundTripOptions
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlin.random.Random
import kotlin.test.Test

class RoundTripRandomTest {

    @Test
    fun `Given a distance - When nextRoundTripOptions - Then the seed and heading are in range and the distance is kept`() {
        val random = Random(42)

        repeat(200) {
            val actual = random.nextRoundTripOptions(distanceKm = 12)

            actual.distanceKm shouldBe 12
            (actual.seed in RoutePlanConstants.ROUND_TRIP_SEED_RANGE) shouldBe true
            (actual.heading in RoutePlanConstants.ROUND_TRIP_HEADING_RANGE) shouldBe true
        }
    }

    @Test
    fun `Given previous options - When nextRoundTripOptions - Then the seed always differs`() {
        val random = Random(7)
        var previous = RoundTripOptions(distanceKm = 5, seed = 1, heading = 0)

        repeat(500) {
            val actual = random.nextRoundTripOptions(distanceKm = 5, previous = previous)

            actual.seed shouldNotBe previous.seed
            previous = actual
        }
    }
}
