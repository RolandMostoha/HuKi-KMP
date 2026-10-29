package hu.mostoha.mobile.kmp.huki.model.mapper

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class LocationMapperTest {

    @Test
    fun `Given altitude and vertical accuracy - When accurateAltitudeOrNull - Then only accurate altitude is returned`() {
        testCases().forEach { testCase ->
            val actual = accurateAltitudeOrNull(testCase.altitude, testCase.verticalAccuracy)

            actual shouldBe testCase.result
        }
    }

    companion object {
        fun testCases() =
            listOf(
                TestCase(altitude = 412.6, verticalAccuracy = 3.0, result = 412.6),
                TestCase(altitude = -12.0, verticalAccuracy = 5.0, result = -12.0),
                TestCase(altitude = 412.6, verticalAccuracy = 0.0, result = null),
                TestCase(altitude = 412.6, verticalAccuracy = -1.0, result = null),
                TestCase(altitude = 412.6, verticalAccuracy = null, result = null),
                TestCase(altitude = null, verticalAccuracy = 3.0, result = null),
            )
    }

    data class TestCase(
        val altitude: Double?,
        val verticalAccuracy: Double?,
        val result: Double?,
    )
}
