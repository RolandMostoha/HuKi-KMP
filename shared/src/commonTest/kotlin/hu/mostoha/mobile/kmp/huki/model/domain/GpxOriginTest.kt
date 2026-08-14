package hu.mostoha.mobile.kmp.huki.model.domain

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class GpxOriginTest {

    @Test
    fun `Given a sandbox path, When fromPath, Then the origin of the holding directory returns`() {
        testCases().forEach { (path, expected) ->
            GpxOrigin.fromPath(path) shouldBe expected
        }
    }

    companion object {

        data class TestCase(
            val path: String,
            val expected: GpxOrigin,
        )

        private fun testCases() =
            listOf(
                TestCase("/data/files/gpx/external/dera_szurdok.gpx", GpxOrigin.EXTERNAL),
                TestCase("/data/files/gpx/routeplanner/dobogoko.gpx", GpxOrigin.ROUTE_PLANNER),
                TestCase("gpx/routeplanner/dobogoko (2).gpx", GpxOrigin.ROUTE_PLANNER),
                TestCase("/data/files/gpx/unknown/dera_szurdok.gpx", GpxOrigin.EXTERNAL),
                TestCase("dera_szurdok.gpx", GpxOrigin.EXTERNAL),
            )
    }
}
