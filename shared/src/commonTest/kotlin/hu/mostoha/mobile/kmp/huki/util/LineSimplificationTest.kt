package hu.mostoha.mobile.kmp.huki.util

import hu.mostoha.mobile.kmp.huki.model.domain.Location
import io.kotest.matchers.shouldBe
import org.maplibre.spatialk.units.extensions.meters
import kotlin.test.Test

class LineSimplificationTest {

    @Test
    fun `Given nearly straight line - When simplified - Then only the ends are kept`() {
        val locations = List(1000) { index -> Location(47.0 + index * 0.0001, 19.0 + (index % 2) * 0.000001) }

        val actual = locations.simplify(10.meters)

        actual shouldBe listOf(locations.first(), locations.last())
    }

    @Test
    fun `Given a corner above tolerance - When simplified - Then the corner is kept`() {
        val corner = Location(47.01, 19.0)
        val locations = listOf(
            Location(47.0, 19.0),
            Location(47.005, 19.0),
            corner,
            Location(47.01, 19.005),
            Location(47.01, 19.01),
        )

        val actual = locations.simplify(10.meters)

        actual shouldBe listOf(locations.first(), corner, locations.last())
    }

    @Test
    fun `Given less than three locations - When simplified - Then they are returned as is`() {
        val locations = listOf(Location(47.0, 19.0), Location(47.1, 19.1))

        val actual = locations.simplify(10.meters)

        actual shouldBe locations
    }
}
