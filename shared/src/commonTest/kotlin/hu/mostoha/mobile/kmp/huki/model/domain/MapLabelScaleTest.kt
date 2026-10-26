package hu.mostoha.mobile.kmp.huki.model.domain

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class MapLabelScaleTest {

    @Test
    fun `Given scale - When coerceFactor - Then the clamped stepped factor is returned`() {
        testCases().forEach { testCase ->
            val actual = MapLabelScale.coerceFactor(testCase.input)

            actual shouldBe testCase.expected
        }
    }

    @Test
    fun `Given following system - When effectivePercent - Then the rounded system scale is returned`() {
        val labelScale = MapLabelScale(followSystem = true, factor = 1.8f)

        val actual = labelScale.effectivePercent(systemScale = 1.15f)

        actual shouldBe 120
    }

    @Test
    fun `Given custom factor - When effectivePercent - Then the custom factor is returned`() {
        val labelScale = MapLabelScale(followSystem = false, factor = 1.8f)

        val actual = labelScale.effectivePercent(systemScale = 1.15f)

        actual shouldBe 180
    }

    companion object {
        fun testCases() =
            listOf(
                TestCase(input = 0.85f, expected = 1.0f),
                TestCase(input = 1.0f, expected = 1.0f),
                TestCase(input = 1.15f, expected = 1.2f),
                TestCase(input = 1.3f, expected = 1.3f),
                TestCase(input = 1.34f, expected = 1.3f),
                TestCase(input = 2.0f, expected = 2.0f),
                TestCase(input = 2.5f, expected = 2.0f),
            )
    }

    data class TestCase(
        val input: Float,
        val expected: Float,
    )
}
