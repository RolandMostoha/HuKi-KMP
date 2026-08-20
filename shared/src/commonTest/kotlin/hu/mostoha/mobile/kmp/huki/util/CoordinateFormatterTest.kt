package hu.mostoha.mobile.kmp.huki.util

import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.util.formatter.CoordinateFormatter
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class CoordinateFormatterTest {

    @Test
    fun `Given location - When formatting coordinates - Then lat lon is shown in parentheses`() {
        testCases().forEach { testCase ->
            val actual = CoordinateFormatter.formatCoordinates(testCase.input)

            actual shouldBe testCase.result
        }
    }

    companion object {
        fun testCases() =
            listOf(
                TestCase(
                    input = Location(47.71810, 18.89480),
                    result = "(47.71810, 18.89480)",
                ),
                TestCase(
                    input = Location(47.7181123456, 18.8948987654),
                    result = "(47.71811, 18.89490)",
                ),
                TestCase(
                    input = Location(47.1, 18.0),
                    result = "(47.10000, 18.00000)",
                ),
                TestCase(
                    input = Location(-33.86880, -151.20930),
                    result = "(-33.86880, -151.20930)",
                ),
                TestCase(
                    input = Location(0.0, 0.0),
                    result = "(0.00000, 0.00000)",
                ),
                TestCase(
                    input = Location(-0.0000004, -0.0000004),
                    result = "(0.00000, 0.00000)",
                ),
            )
    }

    data class TestCase(
        val input: Location,
        val result: String,
    )
}
