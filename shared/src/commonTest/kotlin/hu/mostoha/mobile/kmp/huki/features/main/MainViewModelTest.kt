package hu.mostoha.mobile.kmp.huki.features.main

import app.cash.turbine.test
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.location.LOCATION
import dev.icerock.moko.permissions.test.createPermissionControllerMock
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.data.TEST_GPX_DETAILS
import hu.mostoha.mobile.kmp.huki.features.map.MapUiEffects
import hu.mostoha.mobile.kmp.huki.model.domain.BaseLayer
import hu.mostoha.mobile.kmp.huki.model.domain.MyLocationStatus
import hu.mostoha.mobile.kmp.huki.model.domain.NonGpxFileException
import hu.mostoha.mobile.kmp.huki.model.domain.Sheet
import hu.mostoha.mobile.kmp.huki.repository.GpxRepository
import hu.mostoha.mobile.kmp.huki.theme.SharedDimens
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
    private val gpxRepository = mock<GpxRepository>()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(grantedPermission: Boolean, allowPermission: Boolean = true): MainViewModel {
        val allow = if (allowPermission) setOf(Permission.LOCATION) else emptySet()
        val granted = if (grantedPermission) setOf(Permission.LOCATION) else emptySet()
        return MainViewModel(
            permissionsController = createPermissionControllerMock(
                allow = allow,
                granted = granted,
            ),
            gpxRepository = gpxRepository,
        )
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
    fun `Given granted permission and no fix, When init, Then isMyLocationLoading is true`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.uiState.test {
                awaitItem().isMyLocationLoading shouldBe true
            }
        }
    }

    @Test
    fun `Given searching my location, When MyLocationReceived, Then isMyLocationLoading is false`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.uiState.test {
                awaitItem().isMyLocationLoading shouldBe true

                viewModel.onEvent(MainUiEvents.MyLocationReceived)

                awaitItem().isMyLocationLoading shouldBe false
            }
        }
    }

    @Test
    fun `Given location fix received, When MyLocationClicked, Then isMyLocationLoading stays false`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.uiState.test {
                awaitItem().isMyLocationLoading shouldBe true

                viewModel.onEvent(MainUiEvents.MyLocationReceived)
                awaitItem().isMyLocationLoading shouldBe false

                viewModel.onEvent(MainUiEvents.MyLocationClicked)

                with(awaitItem()) {
                    myLocationState.myLocationStatus shouldBe MyLocationStatus.FollowingLiveCompass
                    isMyLocationLoading shouldBe false
                }
            }
        }
    }

    @Test
    fun `Given granted permission, When GpxStartNavigationClicked, Then uiState has FollowingLiveCompass`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.uiState.test {
                awaitItem().myLocationState.myLocationStatus shouldBe MyLocationStatus.Following

                viewModel.onEvent(MainUiEvents.GpxStartNavigationClicked)

                awaitItem().myLocationState.myLocationStatus shouldBe MyLocationStatus.FollowingLiveCompass
            }
        }
    }

    @Test
    fun `Given not granted permission, When GpxStartNavigationClicked and allow, Then FollowingLiveCompass`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = false, allowPermission = true)
            advanceUntilIdle()

            viewModel.uiState.test {
                with(awaitItem().myLocationState) {
                    permissionState shouldBe PermissionState.NotDetermined
                    myLocationStatus shouldBe MyLocationStatus.NotAvailable
                }

                viewModel.onEvent(MainUiEvents.GpxStartNavigationClicked)

                with(awaitItem().myLocationState) {
                    permissionState shouldBe PermissionState.Granted
                    myLocationStatus shouldBe MyLocationStatus.FollowingLiveCompass
                }
            }
        }
    }

    @Test
    fun `Given not granted permission, When GpxStartNavigationClicked and disallow, Then stays NotAvailable Denied`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = false, allowPermission = false)
            advanceUntilIdle()

            viewModel.uiState.test {
                with(awaitItem().myLocationState) {
                    permissionState shouldBe PermissionState.NotDetermined
                    myLocationStatus shouldBe MyLocationStatus.NotAvailable
                }

                viewModel.onEvent(MainUiEvents.GpxStartNavigationClicked)

                with(awaitItem().myLocationState) {
                    permissionState shouldBe PermissionState.Denied
                    myLocationStatus shouldBe MyLocationStatus.NotAvailable
                }
            }
        }
    }

    @Test
    fun `When LayersClicked, Then uiState sheet is Layers`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.uiState.test {
                awaitItem().sheet shouldBe null

                viewModel.onEvent(MainUiEvents.LayersClicked)

                awaitItem().sheet shouldBe Sheet.Layers
            }
        }
    }

    @Test
    fun `Given Layers sheet, When ModalSheetDismissed, Then uiState sheet is SearchBar`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.uiState.test {
                awaitItem().sheet shouldBe null

                viewModel.onEvent(MainUiEvents.LayersClicked)
                awaitItem().sheet shouldBe Sheet.Layers

                viewModel.onEvent(MainUiEvents.SheetDismissed)
                awaitItem().sheet shouldBe null
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

    @Test
    fun `When GpxLayerSelected and no GPX imported, Then sheet is hidden and mainUiEffect is ShowGpxFilePicker`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.uiState.test {
                awaitItem().sheet shouldBe null

                viewModel.onEvent(MainUiEvents.GpxLayerSelected)

                expectNoEvents()
            }

            viewModel.mainUiEffects.test {
                awaitItem() shouldBe MainUiEffects.ShowGpxFilePicker
                ensureAllEventsConsumed()
            }
        }
    }

    @Test
    fun `When GpxFileSelected, Then uiState has updated loading state and GPX details`() {
        runTest {
            everySuspend { gpxRepository.readGpxFile(any()) } returns TEST_GPX_DETAILS
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.uiState.test {
                with(awaitItem()) {
                    isGpxLoading shouldBe false
                    mapUiState.gpxDetails shouldBe null
                }

                viewModel.onEvent(MainUiEvents.GpxFileSelected("uri"))

                with(awaitItem()) {
                    isGpxLoading shouldBe true
                    mapUiState.gpxDetails shouldBe null
                }

                with(awaitItem()) {
                    isGpxLoading shouldBe false
                    mapUiState.gpxDetails shouldBe TEST_GPX_DETAILS
                    mapUiState.gpxLayerVisible shouldBe true
                }
            }
        }
    }

    @Test
    fun `When GpxLayerSelected and GPX already imported, Then uiState toggles GPX layer visibility`() {
        runTest {
            everySuspend { gpxRepository.readGpxFile(any()) } returns TEST_GPX_DETAILS
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.uiState.test {
                awaitItem().mapUiState.gpxLayerVisible shouldBe false

                viewModel.onEvent(MainUiEvents.GpxFileSelected("uri"))

                awaitItem().mapUiState.gpxLayerVisible shouldBe false
                awaitItem().mapUiState.gpxLayerVisible shouldBe true

                viewModel.onEvent(MainUiEvents.GpxLayerSelected)

                awaitItem().mapUiState.gpxLayerVisible shouldBe false

                viewModel.onEvent(MainUiEvents.GpxLayerSelected)

                awaitItem().mapUiState.gpxLayerVisible shouldBe true
            }
        }
    }

    @Test
    fun `When GpxFileSelected, Then mapUiEffect is UpdateCamera`() {
        runTest {
            everySuspend { gpxRepository.readGpxFile(any()) } returns TEST_GPX_DETAILS
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.mapUiEffects.test {
                viewModel.onEvent(MainUiEvents.GpxFileSelected("uri"))

                skipItems(1)

                awaitItem() shouldBe MapUiEffects.UpdateCamera(
                    bounds = TEST_GPX_DETAILS.locations + TEST_GPX_DETAILS.waypoints.map { it.location },
                    contentPadding = SharedDimens.GPX_CONTENT_PADDING,
                )
                ensureAllEventsConsumed()
            }
        }
    }

    @Test
    fun `When GpxFileSelected, Then uiState sheet is Gpx`() {
        runTest {
            everySuspend { gpxRepository.readGpxFile(any()) } returns TEST_GPX_DETAILS
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.uiState.test {
                awaitItem().sheet shouldBe null

                viewModel.onEvent(MainUiEvents.GpxFileSelected("uri"))

                awaitItem().sheet shouldBe null
                awaitItem().sheet shouldBe Sheet.Gpx(TEST_GPX_DETAILS)
            }
        }
    }

    @Test
    fun `Given standard sheet dismissed after GPX import, When GpxRouteClicked, Then uiState sheet is Gpx`() {
        runTest {
            everySuspend { gpxRepository.readGpxFile(any()) } returns TEST_GPX_DETAILS
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.uiState.test {
                awaitItem().sheet shouldBe null

                viewModel.onEvent(MainUiEvents.GpxFileSelected("uri"))
                awaitItem() // Loading
                awaitItem().sheet shouldBe Sheet.Gpx(TEST_GPX_DETAILS)

                viewModel.onEvent(MainUiEvents.SheetDismissed)
                awaitItem().sheet shouldBe null

                viewModel.onEvent(MainUiEvents.GpxRouteClicked(TEST_GPX_DETAILS))

                awaitItem().sheet shouldBe Sheet.Gpx(TEST_GPX_DETAILS)
            }
        }
    }

    @Test
    fun `When GpxCloseClicked, Then gpx details is null and sheet is SearchBar`() {
        runTest {
            everySuspend { gpxRepository.readGpxFile(any()) } returns TEST_GPX_DETAILS
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.uiState.test {
                awaitItem()

                viewModel.onEvent(MainUiEvents.GpxFileSelected("uri"))

                awaitItem()
                awaitItem()

                viewModel.onEvent(MainUiEvents.GpxCloseClicked)

                with(awaitItem()) {
                    mapUiState.gpxDetails shouldBe null
                    mapUiState.gpxLayerVisible shouldBe false
                    sheet shouldBe null
                }
            }
        }
    }

    @Test
    fun `Given GPX import error, When GpxImportErrorDismissed, Then import error is cleared`() {
        runTest {
            everySuspend { gpxRepository.readGpxFile(any()) } throws NonGpxFileException()
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.uiState.test {
                awaitItem().alert shouldBe null

                viewModel.onEvent(MainUiEvents.GpxFileSelected("uri"))

                // Loading state
                awaitItem().alert shouldBe null

                with(awaitItem()) {
                    alert!!.title shouldBe SharedRes.strings.gpx_import_error_title
                    alert.message shouldBe SharedRes.strings.gpx_import_error_non_gpx_message
                }

                viewModel.onEvent(MainUiEvents.AlertDismissed)

                awaitItem().alert shouldBe null
            }
        }
    }

    @Test
    fun `Given granted permission, When init, Then isSearchBarVisible is true`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.uiState.test {
                awaitItem().isSearchBarVisible shouldBe true
            }
        }
    }

    @Test
    fun `Given Following, When my location becomes FollowingLiveCompass and back, Then SearchBar toggles`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.uiState.test {
                awaitItem().isSearchBarVisible shouldBe true

                viewModel.onEvent(MainUiEvents.MyLocationClicked)

                with(awaitItem()) {
                    myLocationState.myLocationStatus shouldBe MyLocationStatus.FollowingLiveCompass
                    isSearchBarVisible shouldBe false
                }

                viewModel.onEvent(MainUiEvents.MyLocationClicked)

                with(awaitItem()) {
                    myLocationState.myLocationStatus shouldBe MyLocationStatus.Following
                    isSearchBarVisible shouldBe true
                }
            }
        }
    }

    @Test
    fun `When GpxStartNavigationClicked, Then isSearchBarVisible is false`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.uiState.test {
                awaitItem().isSearchBarVisible shouldBe true

                viewModel.onEvent(MainUiEvents.GpxStartNavigationClicked)

                awaitItem().isSearchBarVisible shouldBe false
            }
        }
    }

    @Test
    fun `When GPX becomes visible, Then isSearchBarVisible is false, and true again when closed`() {
        runTest {
            everySuspend { gpxRepository.readGpxFile(any()) } returns TEST_GPX_DETAILS
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.uiState.test {
                awaitItem().isSearchBarVisible shouldBe true

                viewModel.onEvent(MainUiEvents.GpxFileSelected("uri"))

                awaitItem().isSearchBarVisible shouldBe true // Loading
                with(awaitItem()) {
                    mapUiState.gpxLayerVisible shouldBe true
                    isSearchBarVisible shouldBe false
                }

                viewModel.onEvent(MainUiEvents.GpxCloseClicked)

                with(awaitItem()) {
                    mapUiState.gpxLayerVisible shouldBe false
                    isSearchBarVisible shouldBe true
                }
            }
        }
    }
}
