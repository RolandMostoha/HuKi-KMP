package hu.mostoha.mobile.kmp.huki.model.mapper

import dev.icerock.moko.resources.desc.RawStringDesc
import hu.mostoha.mobile.kmp.huki.data.TEST_GPX_DETAILS
import hu.mostoha.mobile.kmp.huki.model.domain.GpxWaypoint
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlannerWaypoint
import hu.mostoha.mobile.kmp.huki.model.domain.WaypointType
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlin.test.Test

class ReplanStopsMapperTest {

    @Test
    fun `Given HuKi GPX waypoints - When toReplanStops - Then start, stops and end are rebuilt in order`() {
        testCases().forEach { testCase ->
            val gpxDetails = TEST_GPX_DETAILS.copy(waypoints = testCase.input)

            val actual = gpxDetails.toReplanStops().map { it.toStop() }

            actual shouldBe testCase.result
        }
    }

    @Test
    fun `Given a round trip GPX - When toReplanStops - Then the end is a separate stop at the start`() {
        val gpxDetails = TEST_GPX_DETAILS.copy(
            waypoints = listOf(
                GpxWaypoint(KEKES, WaypointType.INTERMEDIATE, name = "Kékes"),
                GpxWaypoint(DOBOGOKO, WaypointType.ROUND_TRIP, name = "Dobogókő"),
            ),
        )

        val actual = gpxDetails.toReplanStops()

        actual.map { it.toStop() } shouldBe listOf(
            Stop("Dobogókő", DOBOGOKO),
            Stop("Kékes", KEKES),
            Stop("Dobogókő", DOBOGOKO),
        )
        actual.first().id shouldNotBe actual.last().id
    }

    @Test
    fun `Given an unnamed stop - When toReplanStops - Then it keeps the coordinates label`() {
        val gpxDetails = TEST_GPX_DETAILS.copy(
            waypoints = listOf(
                GpxWaypoint(KEKES, WaypointType.INTERMEDIATE),
                GpxWaypoint(DOBOGOKO, WaypointType.START),
                GpxWaypoint(SZENTENDRE, WaypointType.END, name = "Szentendre"),
            ),
        )

        val actual = gpxDetails.toReplanStops()[1]
        val expected = KEKES.toRoutePlannerWaypoint()

        (actual.name as RawStringDesc).string shouldBe (expected.name as RawStringDesc).string
        actual.placeName shouldBe null
        actual.location shouldBe KEKES
    }

    private data class Stop(
        val placeName: String?,
        val location: Location?,
    )

    private data class TestCase(
        val input: List<GpxWaypoint>,
        val result: List<Stop>,
    )

    private fun RoutePlannerWaypoint.toStop() = Stop(placeName, location)

    companion object {
        private val DOBOGOKO = Location(47.7194, 18.8970)
        private val KEKES = Location(47.8722, 20.0094)
        private val SZENTENDRE = Location(47.6694, 19.0756)

        private fun testCases() =
            listOf(
                TestCase(
                    input = listOf(
                        GpxWaypoint(KEKES, WaypointType.INTERMEDIATE, name = "Kékes"),
                        GpxWaypoint(DOBOGOKO, WaypointType.START, name = "Dobogókő"),
                        GpxWaypoint(SZENTENDRE, WaypointType.END, name = "Szentendre"),
                    ),
                    result = listOf(
                        Stop("Dobogókő", DOBOGOKO),
                        Stop("Kékes", KEKES),
                        Stop("Szentendre", SZENTENDRE),
                    ),
                ),
                TestCase(
                    input = listOf(
                        GpxWaypoint(DOBOGOKO, WaypointType.START),
                        GpxWaypoint(SZENTENDRE, WaypointType.END, name = " "),
                    ),
                    result = listOf(
                        Stop(null, DOBOGOKO),
                        Stop(null, SZENTENDRE),
                    ),
                ),
                TestCase(
                    input = List(12) { index ->
                        GpxWaypoint(Location(47.0 + index / 100.0, 19.0), WaypointType.INTERMEDIATE, name = "$index")
                    } + listOf(
                        GpxWaypoint(DOBOGOKO, WaypointType.START, name = "Dobogókő"),
                        GpxWaypoint(SZENTENDRE, WaypointType.END, name = "Szentendre"),
                    ),
                    result = listOf(Stop("Dobogókő", DOBOGOKO)) +
                        List(8) { index -> Stop("$index", Location(47.0 + index / 100.0, 19.0)) } +
                        listOf(Stop("Szentendre", SZENTENDRE)),
                ),
                TestCase(
                    input = emptyList(),
                    result = emptyList(),
                ),
            )
    }
}
