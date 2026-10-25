package hu.mostoha.mobile.kmp.huki.model.domain

import hu.mostoha.mobile.kmp.huki.util.RoutePlanConstants
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class BoundingBoxTest {

    @Test
    fun `Given location - When contains on HuKi-Routing bounding box - Then expected result returns`() {
        testCases().forEach { testCase ->
            val actual = RoutePlanConstants.HUKI_ROUTING_BOUNDING_BOX.contains(testCase.input)

            actual shouldBe testCase.result
        }
    }

    private data class TestCase(
        val input: Location,
        val result: Boolean,
    )

    companion object {
        private fun testCases() =
            listOf(
                // Budapest
                TestCase(Location(47.4979, 19.0402), true),
                // Štúrovo, just across the border
                TestCase(Location(47.7985, 18.7183), true),
                // South west corner
                TestCase(Location(45.71, 16.09), true),
                // Graz
                TestCase(Location(47.0707, 15.4395), false),
                // Kraków
                TestCase(Location(50.0647, 19.945), false),
                // Cluj-Napoca
                TestCase(Location(46.7712, 23.6236), false),
            )
    }
}
