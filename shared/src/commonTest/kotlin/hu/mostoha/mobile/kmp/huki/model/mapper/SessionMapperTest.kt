package hu.mostoha.mobile.kmp.huki.model.mapper

import dev.icerock.moko.resources.desc.Raw
import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.StringDesc
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.features.map.LandscapesGrouping
import hu.mostoha.mobile.kmp.huki.features.routeplanner.RoutePlannerUiState
import hu.mostoha.mobile.kmp.huki.model.domain.CameraPosition
import hu.mostoha.mobile.kmp.huki.model.domain.ContentPadding
import hu.mostoha.mobile.kmp.huki.model.domain.FeatureSession
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.MainSession
import hu.mostoha.mobile.kmp.huki.model.domain.MyLocationStatus
import hu.mostoha.mobile.kmp.huki.model.domain.OktType
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlannerProfile
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlannerSession
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlannerWaypoint
import hu.mostoha.mobile.kmp.huki.util.decodeSession
import hu.mostoha.mobile.kmp.huki.util.encodeSession
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SessionMapperTest {

    @Test
    fun `Given a map session - When encoded and decoded - Then the same session comes back`() {
        testCases().forEach { testCase ->
            val actual = decodeSession(testCase.input.encodeSession())

            actual shouldBe testCase.result
        }
    }

    @Test
    fun `Given a session of another version - When decoded - Then it is dropped`() {
        val json = SESSION.encodeSession().replace("\"version\":1", "\"version\":0")

        val actual = decodeSession(json)

        actual.shouldBeNull()
    }

    @Test
    fun `Given a session with an unknown trail - When decoded - Then it is dropped`() {
        val json = SESSION.copy(feature = OKT_FEATURE).encodeSession().replace("\"OKT\"", "\"UNKNOWN\"")

        val actual = decodeSession(json)

        actual.shouldBeNull()
    }

    @Test
    fun `Given malformed JSON - When decoded - Then it is dropped`() {
        val json = "{not json"

        val actual = decodeSession(json)

        actual.shouldBeNull()
    }

    data class TestCase(
        val input: MainSession,
        val result: MainSession,
    )

    companion object {
        private val SESSION = MainSession(
            camera = CameraPosition(location = Location(47.76, 18.96), zoom = 13.5, bearing = 45.0, pitch = 20.0),
            cameraPadding = ContentPadding.MAP_OKT_STARTED,
            myLocationStatus = MyLocationStatus.Default,
            hikingLayerVisible = true,
            feature = null,
        )
        private val OKT_FEATURE = FeatureSession.Okt(
            type = OktType.OKT,
            selectedSectionId = "OKT-01",
            reversedSectionIds = setOf("OKT-01"),
            isSheetVisible = false,
        )
        private val WAYPOINTS = listOf(
            RoutePlannerWaypoint(
                id = RoutePlannerUiState.MY_LOCATION_WAYPOINT_ID,
                name = StringDesc.Resource(SharedRes.strings.route_planner_my_location),
                placeName = "Pilisszentkereszt",
                location = Location(47.69, 18.90),
            ),
            RoutePlannerWaypoint(
                id = "place",
                name = StringDesc.Raw("Dobogókő"),
                placeName = "Dobogókő",
                location = Location(47.72, 18.89),
            ),
            Location(47.70, 18.80).toRoutePlannerWaypoint().copy(id = "coordinates"),
            RoutePlannerWaypoint(id = "empty"),
        )

        fun testCases() =
            listOf(
                TestCase(input = SESSION, result = SESSION),
                TestCase(
                    input = SESSION.copy(myLocationStatus = MyLocationStatus.FollowingLiveCompass),
                    result = SESSION.copy(myLocationStatus = MyLocationStatus.FollowingLiveCompass),
                ),
                FeatureSession.Gpx(
                    fileUri = "/sandbox/external/dera_szurdok.gpx",
                    isReversed = true,
                    isLayerVisible = true,
                    isRouteVisible = false,
                    isAllDistancesVisible = true,
                    isSlopeColorsVisible = true,
                    isSheetVisible = false,
                ).toTestCase(),
                OKT_FEATURE.toTestCase(),
                FeatureSession.Landscapes(
                    grouping = LandscapesGrouping.CATEGORY,
                    selectedLandscapeId = null,
                    selectedCategoryId = "peak",
                    selectedDestinationId = "dobogoko",
                    isSheetVisible = true,
                ).toTestCase(),
                FeatureSession.RoutePlanner(
                    RoutePlannerSession(routeProfile = RoutePlannerProfile.BIKE, waypoints = WAYPOINTS),
                ).toTestCase(),
            )

        private fun FeatureSession.toTestCase() =
            TestCase(
                input = SESSION.copy(feature = this, hikingLayerVisible = false),
                result = SESSION.copy(feature = this, hikingLayerVisible = false),
            )
    }
}
