package hu.mostoha.mobile.kmp.huki.model.domain

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class OsmTypeTest {

    @Test
    fun `Given raw OSM type string - When fromString - Then expected OsmType returns`() {
        testCases().forEach { testCase ->
            val actual = OsmType.fromString(testCase.input)

            actual shouldBe testCase.result
        }
    }

    companion object {
        fun testCases() =
            listOf(
                TestCase(input = null, result = null),
                TestCase(input = "node", result = OsmType.NODE),
                TestCase(input = "N", result = OsmType.NODE),
                TestCase(input = "way", result = OsmType.WAY),
                TestCase(input = "W", result = OsmType.WAY),
                TestCase(input = "relation", result = OsmType.RELATION),
                TestCase(input = "R", result = OsmType.RELATION),
                TestCase(input = "garbage", result = null),
            )
    }

    data class TestCase(
        val input: String?,
        val result: OsmType?,
    )
}
