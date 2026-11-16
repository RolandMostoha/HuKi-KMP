package hu.mostoha.mobile.kmp.huki.model.domain

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class TuHuBaseMapTest {

    @Test
    fun `Given scale factors - When tileSize - Then normal tiles are used below 150 percent and doubled tiles from it`() {
        testCases().forEach { testCase ->
            val actual = TuHuBaseMap.tileSize(testCase.input)

            actual shouldBe testCase.result
        }
    }

    private data class TestCase(val input: Float, val result: Long)

    companion object {
        private fun testCases() =
            listOf(
                TestCase(input = 1.0f, result = 256),
                TestCase(input = 1.4f, result = 256),
                TestCase(input = 1.5f, result = 512),
                TestCase(input = 2.0f, result = 512),
            )
    }
}
