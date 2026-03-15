package hu.mostoha.mobile.kmp.huki.features.main

import app.cash.turbine.test
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.location.LOCATION
import dev.icerock.moko.permissions.test.createPermissionControllerMock
import hu.mostoha.mobile.kmp.huki.model.domain.BaseLayer
import hu.mostoha.mobile.kmp.huki.model.domain.MyLocationStatus
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Given not granted location permission, When allow, Then uiState has Granted Following`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = false, allowPermission = true)
            advanceUntilIdle()

            viewModel.uiState.test {
                with(awaitItem().myLocationState) {
                    permissionState shouldBe PermissionState.NotDetermined
                    myLocationStatus shouldBe MyLocationStatus.NotAvailable
                }

                viewModel.onEvent(MainUiEvents.MyLocationClicked)

                with(awaitItem().myLocationState) {
                    permissionState shouldBe PermissionState.Granted
                    myLocationStatus shouldBe MyLocationStatus.Following
                }
            }
        }
    }

    @Test
    fun `Given not granted location permission, When disallow, Then uiState has Denied`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = false, allowPermission = false)
            advanceUntilIdle()

            viewModel.uiState.test {
                with(awaitItem().myLocationState) {
                    permissionState shouldBe PermissionState.NotDetermined
                    myLocationStatus shouldBe MyLocationStatus.NotAvailable
                }

                viewModel.onEvent(MainUiEvents.MyLocationClicked)

                with(awaitItem().myLocationState) {
                    permissionState shouldBe PermissionState.Denied
                    myLocationStatus shouldBe MyLocationStatus.NotAvailable
                }
            }
        }
    }

    @Test
    fun `Given granted location permission, When init, Then uiState has Granted Following`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.uiState.test {
                with(awaitItem().myLocationState) {
                    permissionState shouldBe PermissionState.Granted
                    myLocationStatus shouldBe MyLocationStatus.Following
                }
            }
        }
    }

    @Test
    fun `Given Following my location, When MyLocationClicked, Then uiState has FollowingLiveCompass`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.uiState.test {
                awaitItem().myLocationState.myLocationStatus shouldBe MyLocationStatus.Following

                viewModel.onEvent(MainUiEvents.MyLocationClicked)

                awaitItem().myLocationState.myLocationStatus shouldBe MyLocationStatus.FollowingLiveCompass
            }
        }
    }

    @Test
    fun `Given FollowingLiveCompass my location, When MyLocationClicked, Then uiState has Following`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.uiState.test {
                awaitItem().myLocationState.myLocationStatus shouldBe MyLocationStatus.Following
                viewModel.onEvent(MainUiEvents.MyLocationClicked)
                awaitItem().myLocationState.myLocationStatus shouldBe MyLocationStatus.FollowingLiveCompass

                viewModel.onEvent(MainUiEvents.MyLocationClicked)

                awaitItem().myLocationState.myLocationStatus shouldBe MyLocationStatus.Following
            }
        }
    }

    @Test
    fun `Given Following my location, When MyLocationUpdated, Then uiState has Default MyLocationStatus`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.uiState.test {
                awaitItem().myLocationState.myLocationStatus shouldBe MyLocationStatus.Following

                viewModel.onEvent(MainUiEvents.FollowingDisabled)

                awaitItem().myLocationState.myLocationStatus shouldBe MyLocationStatus.Default
            }
        }
    }

    @Test
    fun `Given granted location permission, When init, Then uiEffect is ShowMyLocation`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.mapUiEffects.test {
                awaitItem() shouldBe MapUiEffects.ShowMyLocation(MyLocationStatus.Following, animated = false)
                ensureAllEventsConsumed()
            }
        }
    }

    @Test
    fun `Given granted location permission, When MyLocationClicked, Then uiEffect is animated ShowMyLocation`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.mapUiEffects.test {
                awaitItem() shouldBe MapUiEffects.ShowMyLocation(
                    MyLocationStatus.Following,
                    animated = false,
                )

                viewModel.onEvent(MainUiEvents.MyLocationClicked)

                awaitItem() shouldBe MapUiEffects.ShowMyLocation(
                    MyLocationStatus.FollowingLiveCompass,
                    animated = true,
                )
                ensureAllEventsConsumed()
            }
        }
    }

    @Test
    fun `When LayersClicked, Then uiEffect is ShowLayersBottomSheet`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.mainUiEffects.test {
                viewModel.onEvent(MainUiEvents.LayersClicked)

                awaitItem() shouldBe MainUiEffects.ShowLayersBottomSheet
                ensureAllEventsConsumed()
            }
        }
    }

    @Test
    fun `Given OUTDOORS, When BaseLayerSelected, Then uiState has SATELLITE`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.uiState.test {
                awaitItem().mapUiState.baseLayer shouldBe BaseLayer.OUTDOORS

                viewModel.onEvent(MainUiEvents.BaseLayerSelected(BaseLayer.SATELLITE))

                awaitItem().mapUiState.baseLayer shouldBe BaseLayer.SATELLITE
            }
        }
    }

    @Test
    fun `When HikingLayerSelected, Then uiState has switched hiking layer visibility`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.uiState.test {
                awaitItem().mapUiState.hikingLayerVisible shouldBe true

                viewModel.onEvent(MainUiEvents.HikingLayerSelected)

                awaitItem().mapUiState.hikingLayerVisible shouldBe false

                viewModel.onEvent(MainUiEvents.HikingLayerSelected)

                awaitItem().mapUiState.hikingLayerVisible shouldBe true
            }
        }
    }

    private fun createViewModel(grantedPermission: Boolean, allowPermission: Boolean = true) =
        MainViewModel(
            permissionsController = createPermissionControllerMock(
                allow = if (allowPermission) {
                    setOf(Permission.LOCATION)
                } else {
                    emptySet()
                },
                granted = if (grantedPermission) {
                    setOf(Permission.LOCATION)
                } else {
                    emptySet()
                },
            ),
        )
}
