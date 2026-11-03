package hu.mostoha.mobile.kmp.huki.features.main

import hu.mostoha.mobile.kmp.huki.data.TEST_GPX_DETAILS
import hu.mostoha.mobile.kmp.huki.features.map.MapUiState
import hu.mostoha.mobile.kmp.huki.model.domain.MyLocationState
import hu.mostoha.mobile.kmp.huki.model.domain.MyLocationStatus
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class MainUiStateTest {

    @Test
    fun `Given main ui state - When isZoomControlsVisible is read - Then it reflects setting gpx and live compass`() {
        testCases().forEach { testCase ->
            val actual = testCase.input.isZoomControlsVisible

            actual shouldBe testCase.result
        }
    }

    companion object {
        private fun state(
            alwaysVisible: Boolean = false,
            gpxLayerVisible: Boolean = false,
            gpxLoaded: Boolean = gpxLayerVisible,
            myLocationStatus: MyLocationStatus = MyLocationStatus.Default,
        ) = MainUiState(
            mapUiState = MapUiState(
                gpxLayerVisible = gpxLayerVisible,
                gpxDetails = TEST_GPX_DETAILS.takeIf { gpxLoaded },
            ),
            myLocationState = MyLocationState(myLocationStatus = myLocationStatus),
            mapZoomControlsAlwaysVisible = alwaysVisible,
        )

        fun testCases() =
            listOf(
                TestCase(input = state(), result = false),
                TestCase(input = state(myLocationStatus = MyLocationStatus.Following), result = false),
                TestCase(input = state(alwaysVisible = true), result = true),
                TestCase(input = state(gpxLayerVisible = true), result = true),
                TestCase(input = state(gpxLayerVisible = true, gpxLoaded = false), result = false),
                TestCase(input = state(gpxLayerVisible = false, gpxLoaded = true), result = false),
                TestCase(
                    input = state(gpxLayerVisible = true, myLocationStatus = MyLocationStatus.NotAvailable),
                    result = true,
                ),
                TestCase(input = state(myLocationStatus = MyLocationStatus.FollowingLiveCompass), result = true),
            )
    }

    data class TestCase(
        val input: MainUiState,
        val result: Boolean,
    )
}
