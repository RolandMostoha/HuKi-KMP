package hu.mostoha.mobile.kmp.huki.util

import hu.mostoha.mobile.kmp.huki.model.domain.CameraPosition
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class CameraTargetParserTest {

    @Test
    fun `Given camera target string, When parsed, Then it returns the matching camera position`() {
        testCases().forEach { testCase ->
            val actual = CameraTargetParser.parse(testCase.input)

            actual shouldBe testCase.result
        }
    }

    companion object {
        fun testCases() =
            listOf(
                TestCase(
                    input = "47.78403,18.93396,11.63",
                    result = CameraPosition(
                        location = Location(latitude = 47.78403, longitude = 18.93396),
                        zoom = 11.63,
                        bearing = 0.0,
                        pitch = 0.0,
                    ),
                ),
                TestCase(
                    input = " 47.78403 , 18.93396 , 11.63 ",
                    result = CameraPosition(
                        location = Location(latitude = 47.78403, longitude = 18.93396),
                        zoom = 11.63,
                        bearing = 0.0,
                        pitch = 0.0,
                    ),
                ),
                TestCase(input = "", result = null),
                TestCase(input = "47.78403,18.93396", result = null),
                TestCase(input = "47.78403,18.93396,11.63,0", result = null),
                TestCase(input = "abc,18.93396,11.63", result = null),
                TestCase(input = "47.78403,,11.63", result = null),
            )
    }

    data class TestCase(
        val input: String,
        val result: CameraPosition?,
    )
}
