package hu.mostoha.mobile.kmp.huki.util

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class NameNormalizerTest {

    @Test
    fun `Given value - When normalize - Then it is lowercased and Hungarian diacritics are folded`() {
        testCases().forEach { testCase ->
            val actual = NameNormalizer.normalize(testCase.input)

            actual shouldBe testCase.expected
        }
    }

    companion object {
        fun testCases() =
            listOf(
                TestCase("Dobogókő", "dobogoko"),
                TestCase("BUDAPEST", "budapest"),
                TestCase("Mátraszentimre", "matraszentimre"),
                TestCase("Visegrádi vár", "visegradi var"),
                TestCase("Pilis-tető", "pilis-teto"),
                TestCase("Bükk", "bukk"),
                TestCase("Győr", "gyor"),
                TestCase("already lower", "already lower"),
            )
    }

    data class TestCase(
        val input: String,
        val expected: String,
    )
}
