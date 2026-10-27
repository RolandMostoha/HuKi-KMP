package hu.mostoha.mobile.kmp.huki.theme

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SlopeColorScaleTest {

    @Test
    fun `Given slope percent - When colorHex - Then it returns the blended scale color`() {
        testCases().forEach { testCase ->
            val actual = SlopeColorScale(flatColor = 0x136313).colorHex(testCase.input)

            actual shouldBe testCase.result
        }
    }

    @Test
    fun `Given ARGB flat color - When colorHex on flat slope - Then alpha is dropped`() {
        val scale = SlopeColorScale(flatColor = 0xFF136313.toInt())

        val actual = scale.colorHex(0.0)

        actual shouldBe "#136313"
    }

    companion object {
        private const val FLAT_COLOR = "#136313"

        fun testCases() =
            listOf(
                TestCase(input = -40.0, result = "#063D68"),
                TestCase(input = -25.0, result = "#063D68"),
                TestCase(input = -10.0, result = "#2096F3"),
                TestCase(input = -6.5, result = "#1A7D83"),
                TestCase(input = -3.0, result = FLAT_COLOR),
                TestCase(input = 0.0, result = FLAT_COLOR),
                TestCase(input = 3.0, result = FLAT_COLOR),
                TestCase(input = 6.5, result = "#828E0A"),
                TestCase(input = 10.0, result = "#F0B800"),
                TestCase(input = 25.0, result = "#D50001"),
                TestCase(input = 40.0, result = "#D50001"),
                TestCase(input = 1.0, result = FLAT_COLOR),
            )
    }

    data class TestCase(
        val input: Double,
        val result: String,
    )
}
