package hu.mostoha.mobile.kmp.huki.features.main

import app.cash.turbine.test
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.location.LOCATION
import dev.icerock.moko.permissions.test.createPermissionControllerMock
import hu.mostoha.mobile.kmp.huki.model.domain.MyLocationStatus
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class MainViewModelTest {

    @Test
    fun `Given not granted location permission, When allow, Then uiState is Granted Following`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = false, allowPermission = true)

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
    fun `Given not granted location permission, When disallow, Then uiState is Denied`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = false, allowPermission = false)

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
    fun `Given granted location permission, When init, Then uiState is Granted Following`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)

            viewModel.uiState.test {
                with(awaitItem().myLocationState) {
                    permissionState shouldBe PermissionState.Granted
                    myLocationStatus shouldBe MyLocationStatus.Following
                }
            }
        }
    }

    @Test
    fun `Given Following my location, When MyLocationClicked, Then uiState is FollowingLiveCompass`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)

            viewModel.uiState.test {
                awaitItem().myLocationState.myLocationStatus shouldBe MyLocationStatus.Following

                viewModel.onEvent(MainUiEvents.MyLocationClicked)

                awaitItem().myLocationState.myLocationStatus shouldBe MyLocationStatus.FollowingLiveCompass
            }
        }
    }

    @Test
    fun `Given FollowingLiveCompass my location, When MyLocationClicked, Then uiState is Following`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)

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
    fun `Given Following my location, When MyLocationUpdated, Then uiState is Default`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)

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

            viewModel.uiEffect.test {
                awaitItem() shouldBe MainUiEffects.ShowMyLocation(MyLocationStatus.Following, animated = false)
                ensureAllEventsConsumed()
            }
        }
    }

    @Test
    fun `Given granted location permission, When MyLocationClicked, Then uiEffect is animated ShowMyLocation`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)

            viewModel.uiEffect.test {
                awaitItem() shouldBe MainUiEffects.ShowMyLocation(
                    MyLocationStatus.Following,
                    animated = false,
                )

                viewModel.onEvent(MainUiEvents.MyLocationClicked)

                awaitItem() shouldBe MainUiEffects.ShowMyLocation(
                    MyLocationStatus.FollowingLiveCompass,
                    animated = true,
                )
                ensureAllEventsConsumed()
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
