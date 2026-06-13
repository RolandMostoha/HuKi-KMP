package hu.mostoha.mobile.kmp.huki.util

import hu.mostoha.mobile.kmp.huki.util.formatter.DistanceFormatter
import io.kotest.matchers.shouldBe
import org.maplibre.spatialk.units.extensions.kilometers
import org.maplibre.spatialk.units.extensions.meters
import kotlin.test.Test

class DistanceFormatterTest {

    @Test
    fun `Given short distance, When formatting, Then meters are shown`() {
        val input = 850.meters

        val actual = DistanceFormatter.formatDistance(input)

        actual shouldBe "850 m"
    }

    @Test
    fun `Given long distance, When formatting, Then kilometers are shown`() {
        val input = 15.kilometers

        val actual = DistanceFormatter.formatDistance(input)

        actual shouldBe "15 km"
    }

    @Test
    fun `Given long distance with fraction, When formatting rounded, Then whole kilometers are shown`() {
        val input = 220.1.kilometers

        val actual = DistanceFormatter.formatRoundedDistance(input)

        actual shouldBe "220 km"
    }

    @Test
    fun `Given short distance, When formatting rounded, Then meters are shown`() {
        val input = 850.meters

        val actual = DistanceFormatter.formatRoundedDistance(input)

        actual shouldBe "850 m"
    }

    @Test
    fun `Given elevation value, When formatting, Then meters are shown`() {
        val input = 500

        val actual = DistanceFormatter.formatMeters(input)

        actual shouldBe "500 m"
    }
}
