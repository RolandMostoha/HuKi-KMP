package hu.mostoha.mobile.kmp.huki.model.domain

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class BaseLayerTest {

    @Test
    fun `Given base layer and theme - When isDarkMap - Then only layers with a dark variant render dark`() {
        testCases().forEach { testCase ->
            val actual = testCase.baseLayer.isDarkMap(testCase.isDarkMode)

            actual shouldBe testCase.result
        }
    }

    companion object {
        fun testCases() =
            listOf(
                TestCase(BaseLayer.OUTDOORS, isDarkMode = true, result = true),
                TestCase(BaseLayer.CITY, isDarkMode = true, result = true),
                TestCase(BaseLayer.SATELLITE, isDarkMode = true, result = true),
                TestCase(BaseLayer.TUHU, isDarkMode = true, result = false),
                TestCase(BaseLayer.OUTDOORS, isDarkMode = false, result = false),
                TestCase(BaseLayer.CITY, isDarkMode = false, result = false),
                TestCase(BaseLayer.SATELLITE, isDarkMode = false, result = false),
                TestCase(BaseLayer.TUHU, isDarkMode = false, result = false),
            )
    }

    data class TestCase(
        val baseLayer: BaseLayer,
        val isDarkMode: Boolean,
        val result: Boolean,
    )
}
