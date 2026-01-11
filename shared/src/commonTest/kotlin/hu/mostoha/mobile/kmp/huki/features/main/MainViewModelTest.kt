package hu.mostoha.mobile.kmp.huki.features.main

import app.cash.turbine.test
import hu.mostoha.mobile.kmp.huki.model.domain.CameraPosition
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class MainViewModelTest {
    private val viewModel = MainViewModel()

    @Test
    fun `When MyLocationClicked, then uiState is updated with new camera position`() =
        runTest {
            val cameraPosition = CameraPosition(
                location = Location(
                    latitude = 47.716808,
                    longitude = 18.895073,
                ),
                zoom = 13.0,
            )

            viewModel.uiState.test {
                awaitItem() shouldBe MainUiState.Default

                viewModel.onEvent(MainUiEvents.MyLocationClicked)

                awaitItem().mapUiState.cameraPosition shouldBe cameraPosition
            }
        }

    @Test
    fun `When MyLocationClicked, then uiEffect is sent with MoveCamera`() =
        runTest {
            val expectedCameraPosition = CameraPosition(
                location = Location(
                    latitude = 47.716808,
                    longitude = 18.895073,
                ),
                zoom = 13.0,
            )

            viewModel.uiEffect.test {
                viewModel.onEvent(MainUiEvents.MyLocationClicked)

                awaitItem() shouldBe MainUiEffects.MoveCamera(expectedCameraPosition)
            }
        }
}
