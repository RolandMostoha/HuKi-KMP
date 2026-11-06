package hu.mostoha.mobile.kmp.huki.theme

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SlopeLegendTest {

    @Test
    fun `Given slope legend - When rows are read - Then labels follow the slope thresholds`() {
        val actual = SlopeLegend.rows.map { it.slopePercent to it.label }

        actual shouldBe listOf(
            -25.0 to "-25%",
            -10.0 to "-10%",
            0.0 to "±3%",
            10.0 to "+10%",
            25.0 to "+25%",
        )
    }

    @Test
    fun `Given slope legend - When range labels are read - Then they cover the steepest slopes`() {
        val actual = SlopeLegend.minLabel to SlopeLegend.maxLabel

        actual shouldBe ("-25%" to "+25%")
    }
}
