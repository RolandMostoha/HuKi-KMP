package hu.mostoha.mobile.kmp.huki.features.main

import app.cash.turbine.test
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.location.LOCATION
import dev.icerock.moko.permissions.test.createPermissionControllerMock
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.WhatsNewContent
import hu.mostoha.mobile.kmp.huki.data.TEST_GPX_DETAILS
import hu.mostoha.mobile.kmp.huki.data.TEST_GPX_WAY_CLOSED
import hu.mostoha.mobile.kmp.huki.features.map.MapUiEffects
import hu.mostoha.mobile.kmp.huki.model.analytics.AnalyticsEvent
import hu.mostoha.mobile.kmp.huki.model.analytics.GpxSource
import hu.mostoha.mobile.kmp.huki.model.analytics.Layer
import hu.mostoha.mobile.kmp.huki.model.analytics.MyLocationMode
import hu.mostoha.mobile.kmp.huki.model.analytics.PlaceDetailsSource
import hu.mostoha.mobile.kmp.huki.model.analytics.Screen
import hu.mostoha.mobile.kmp.huki.model.domain.BaseLayer
import hu.mostoha.mobile.kmp.huki.model.domain.BoundingBox
import hu.mostoha.mobile.kmp.huki.model.domain.CameraTarget
import hu.mostoha.mobile.kmp.huki.model.domain.ContentPadding
import hu.mostoha.mobile.kmp.huki.model.domain.Destination
import hu.mostoha.mobile.kmp.huki.model.domain.DestinationType
import hu.mostoha.mobile.kmp.huki.model.domain.GpxMapsNavigationType
import hu.mostoha.mobile.kmp.huki.model.domain.GpxWaypoint
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.MyLocationStatus
import hu.mostoha.mobile.kmp.huki.model.domain.NonGpxFileException
import hu.mostoha.mobile.kmp.huki.model.domain.OsmType
import hu.mostoha.mobile.kmp.huki.model.domain.Place
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceCategory
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceDetails
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceSource
import hu.mostoha.mobile.kmp.huki.model.domain.Sheet
import hu.mostoha.mobile.kmp.huki.model.domain.UserPreferences
import hu.mostoha.mobile.kmp.huki.model.domain.WaypointType
import hu.mostoha.mobile.kmp.huki.model.domain.toLocations
import hu.mostoha.mobile.kmp.huki.model.mapper.toCurrentWhatsNew
import hu.mostoha.mobile.kmp.huki.model.network.LocationIqPlace
import hu.mostoha.mobile.kmp.huki.model.network.NetworkError
import hu.mostoha.mobile.kmp.huki.model.network.NetworkResult
import hu.mostoha.mobile.kmp.huki.repository.DestinationRepository
import hu.mostoha.mobile.kmp.huki.repository.GeocodingRepository
import hu.mostoha.mobile.kmp.huki.repository.GpxRepository
import hu.mostoha.mobile.kmp.huki.repository.PlaceHistoryRepository
import hu.mostoha.mobile.kmp.huki.repository.SettingsRepository
import hu.mostoha.mobile.kmp.huki.repository.WhatsNewRepository
import hu.mostoha.mobile.kmp.huki.service.FakeAnalyticsService
import hu.mostoha.mobile.kmp.huki.service.FakeCrashlyticsService
import hu.mostoha.mobile.kmp.huki.service.LocationMonitoringService
import hu.mostoha.mobile.kmp.huki.util.formatter.DistanceFormatter
import hu.mostoha.mobile.kmp.huki.util.formatter.TravelTimeFormatter
import hu.mostoha.mobile.kmp.huki.util.routeProgressTo
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.flowOf
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

    private companion object {
        private val TEST_DESTINATION = Destination(
            osmId = "1",
            name = "Kékestető",
            town = "Mátraszentimre",
            type = DestinationType.HIGHEST_PEAK,
            location = Location(47.8721, 20.0102),
            description = SharedRes.strings.destinations_type_peak,
            popularity = 10,
        )
        private val TEST_PLACE = Place(
            osmId = "1",
            location = Location(47.4979, 19.0402),
            name = "Budapest",
            placeSource = PlaceSource.SEARCH_AUTOCOMPLETE,
        )
        private val TEST_LONG_TAP_LOCATION = Location(47.7181, 18.8948)
        private val TEST_LOCATION_IQ_PLACE = LocationIqPlace(
            placeId = "1",
            osmId = "2",
            osmType = "node",
            licence = "LocationIQ",
            lat = 47.7185,
            lon = 18.8951,
            displayName = "Dobogókő, Pilisszentkereszt, Pest, Hungary",
            displayPlace = "Dobogókő",
            displayAddress = "Pilisszentkereszt, Pest, Hungary",
            type = "peak",
        )
        private val TEST_PLACE_WITH_BOUNDING_BOX = TEST_PLACE.copy(
            boundingBox = BoundingBox(
                north = 47.6131,
                east = 19.3343,
                south = 47.3496,
                west = 18.9261,
            ),
        )
    }

    private val testDispatcher = StandardTestDispatcher()
    private val gpxRepository = mock<GpxRepository>()
    private val placeHistoryRepository = mock<PlaceHistoryRepository>(MockMode.autoUnit)
    private val destinationRepository = mock<DestinationRepository>(MockMode.autoUnit)
    private val settingsRepository = mock<SettingsRepository>(MockMode.autoUnit) {
        every { settings } returns flowOf(UserPreferences.DEFAULTS)
    }
    private val whatsNewRepository = mock<WhatsNewRepository>(MockMode.autoUnit) {
        everySuspend { shouldShowWhatsNew() } returns false
    }
    private val analyticsService = FakeAnalyticsService()
    private val crashlyticsService = FakeCrashlyticsService()
    private var reverseGeocodeResult: NetworkResult<LocationIqPlace?> = NetworkResult.Success(TEST_LOCATION_IQ_PLACE)
    private val geocodingRepository = object : GeocodingRepository {
        override suspend fun autocomplete(searchText: String) = NetworkResult.Success(emptyList<LocationIqPlace>())

        override suspend fun reverseGeocode(location: Location): NetworkResult<LocationIqPlace?> = reverseGeocodeResult
    }

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        grantedPermission: Boolean,
        allowPermission: Boolean = true,
        locationMonitoringService: LocationMonitoringService = locationMonitoringService(lastKnownLocation = null),
    ): MainViewModel {
        val allow = if (allowPermission) setOf(Permission.LOCATION) else emptySet()
        val granted = if (grantedPermission) setOf(Permission.LOCATION) else emptySet()
        return MainViewModel(
            permissionsController = createPermissionControllerMock(
                allow = allow,
                granted = granted,
            ),
            gpxRepository = gpxRepository,
            placeHistoryRepository = placeHistoryRepository,
            destinationRepository = destinationRepository,
            geocodingRepository = geocodingRepository,
            locationMonitoringService = locationMonitoringService,
            settingsRepository = settingsRepository,
            whatsNewRepository = whatsNewRepository,
            analyticsService = analyticsService,
            crashlyticsService = crashlyticsService,
            defaultDispatcher = testDispatcher,
        )
    }

    private fun locationMonitoringService(
        lastKnownLocation: Location?,
        updates: SharedFlow<Location> = MutableSharedFlow(),
    ) = object : LocationMonitoringService {
        override val locationUpdates: SharedFlow<Location> = updates
        override suspend fun lastKnownLocation(): Location? = lastKnownLocation
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

                analyticsService.loggedEvents shouldBe listOf(
                    AnalyticsEvent.LocationPermissionDenied(deniedAlways = false),
                )
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
    fun `Given FollowingLiveCompass, When CompassClicked, Then uiState has Following`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.uiState.test {
                awaitItem().myLocationState.myLocationStatus shouldBe MyLocationStatus.Following
                viewModel.onEvent(MainUiEvents.MyLocationClicked)
                with(awaitItem()) {
                    myLocationState.myLocationStatus shouldBe MyLocationStatus.FollowingLiveCompass
                    isSearchBarVisible shouldBe false
                }

                viewModel.onEvent(MainUiEvents.CompassClicked)

                with(awaitItem()) {
                    myLocationState.myLocationStatus shouldBe MyLocationStatus.Following
                    isSearchBarVisible shouldBe true
                }
            }
        }
    }

    @Test
    fun `Given FollowingLiveCompass, When CompassClicked, Then uiEffect is animated ShowMyLocation Following`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.mapUiEffects.test {
                awaitItem() shouldBe MapUiEffects.ShowMyLocation(MyLocationStatus.Following, animated = false)
                viewModel.onEvent(MainUiEvents.MyLocationClicked)
                awaitItem() shouldBe MapUiEffects.ShowMyLocation(MyLocationStatus.FollowingLiveCompass, animated = true)

                viewModel.onEvent(MainUiEvents.CompassClicked)

                awaitItem() shouldBe MapUiEffects.ShowMyLocation(MyLocationStatus.Following, animated = true)
                ensureAllEventsConsumed()
            }
        }
    }

    @Test
    fun `Given Default, When CompassClicked, Then uiState still has Default`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.uiState.test {
                awaitItem().myLocationState.myLocationStatus shouldBe MyLocationStatus.Following
                viewModel.onEvent(MainUiEvents.FollowingDisabled)
                awaitItem().myLocationState.myLocationStatus shouldBe MyLocationStatus.Default

                viewModel.onEvent(MainUiEvents.CompassClicked)
                advanceUntilIdle()

                expectNoEvents()
            }
        }
    }

    @Test
    fun `Given Default, When CompassClicked, Then uiEffect is ResetBearing`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.mapUiEffects.test {
                awaitItem() shouldBe MapUiEffects.ShowMyLocation(MyLocationStatus.Following, animated = false)
                viewModel.onEvent(MainUiEvents.FollowingDisabled)

                viewModel.onEvent(MainUiEvents.CompassClicked)

                awaitItem() shouldBe MapUiEffects.ResetBearing
                ensureAllEventsConsumed()
            }
        }
    }

    @Test
    fun `Given Following, When ZoomInClicked, Then uiEffect is Zoom zoomIn`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.mapUiEffects.test {
                awaitItem() shouldBe MapUiEffects.ShowMyLocation(MyLocationStatus.Following, animated = false)

                viewModel.onEvent(MainUiEvents.ZoomInClicked)

                awaitItem() shouldBe MapUiEffects.Zoom(zoomIn = true)
                ensureAllEventsConsumed()
            }
        }
    }

    @Test
    fun `Given FollowingLiveCompass, When ZoomOutClicked, Then uiEffect is Zoom zoomOut`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.mapUiEffects.test {
                awaitItem() shouldBe MapUiEffects.ShowMyLocation(MyLocationStatus.Following, animated = false)
                viewModel.onEvent(MainUiEvents.MyLocationClicked)
                awaitItem() shouldBe MapUiEffects.ShowMyLocation(MyLocationStatus.FollowingLiveCompass, animated = true)

                viewModel.onEvent(MainUiEvents.ZoomOutClicked)

                awaitItem() shouldBe MapUiEffects.Zoom(zoomIn = false)
                ensureAllEventsConsumed()
            }
        }
    }

    @Test
    fun `Given Default my location, When ZoomInClicked, Then uiEffect is Zoom zoomIn`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.mapUiEffects.test {
                awaitItem() shouldBe MapUiEffects.ShowMyLocation(MyLocationStatus.Following, animated = false)
                viewModel.onEvent(MainUiEvents.FollowingDisabled)

                viewModel.onEvent(MainUiEvents.ZoomInClicked)

                awaitItem() shouldBe MapUiEffects.Zoom(zoomIn = true)
                ensureAllEventsConsumed()
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
    fun `Given imported GPX and known location, When GpxWaypointClicked, Then distanceInfoWindow is set`() {
        runTest {
            everySuspend { gpxRepository.readGpxFile(any()) } returns TEST_GPX_DETAILS
            val service = locationMonitoringService(lastKnownLocation = TEST_GPX_WAY_CLOSED.first())
            val viewModel = createViewModel(grantedPermission = true, locationMonitoringService = service)
            advanceUntilIdle()
            viewModel.onEvent(MainUiEvents.GpxFileSelected("uri"))
            advanceUntilIdle()

            val waypoint = GpxWaypoint(TEST_GPX_WAY_CLOSED[2], WaypointType.INTERMEDIATE)
            viewModel.onEvent(MainUiEvents.GpxWaypointClicked(waypoint))
            advanceUntilIdle()

            val info = viewModel.uiState.value.mapUiState.distanceInfoWindows.singleOrNull()
            info.shouldNotBeNull()
            info.location shouldBe waypoint.location

            viewModel.onEvent(MainUiEvents.DistanceInfoWindowDismissed)
            advanceUntilIdle()

            viewModel.uiState.value.mapUiState.distanceInfoWindows shouldBe emptyList()
        }
    }

    @Test
    fun `Given round trip GPX and intermediate waypoint behind current location, When GpxWaypointClicked, Then distance wraps forward`() {
        runTest {
            val waypoint = GpxWaypoint(TEST_GPX_WAY_CLOSED[1], WaypointType.INTERMEDIATE)
            val gpxDetails = TEST_GPX_DETAILS.copy(
                waypoints = listOf(
                    waypoint,
                    GpxWaypoint(TEST_GPX_WAY_CLOSED.first(), WaypointType.ROUND_TRIP),
                ),
            )
            everySuspend { gpxRepository.readGpxFile(any()) } returns gpxDetails
            val currentLocation = TEST_GPX_WAY_CLOSED[2]
            val service = locationMonitoringService(lastKnownLocation = currentLocation)
            val viewModel = createViewModel(grantedPermission = true, locationMonitoringService = service)
            advanceUntilIdle()
            viewModel.onEvent(MainUiEvents.GpxFileSelected("uri"))
            advanceUntilIdle()

            viewModel.onEvent(MainUiEvents.GpxWaypointClicked(waypoint))
            advanceUntilIdle()

            val info = viewModel.uiState.value.mapUiState.distanceInfoWindows.singleOrNull()
            info.shouldNotBeNull()

            val expected = TEST_GPX_WAY_CLOSED.routeProgressTo(
                from = currentLocation,
                to = waypoint.location,
                isRoundTrip = true,
            )
            info.distance shouldBe DistanceFormatter.formatDistance(expected.distance)
            info.travelTime shouldBe TravelTimeFormatter.formatTravelTime(expected.travelTime)
        }
    }

    @Test
    fun `Given imported GPX, When GpxDistancesVisibilityToggled, Then all waypoints get distance windows`() {
        runTest {
            val waypoints = listOf(
                GpxWaypoint(TEST_GPX_WAY_CLOSED.first(), WaypointType.START),
                GpxWaypoint(TEST_GPX_WAY_CLOSED[2], WaypointType.INTERMEDIATE),
                GpxWaypoint(TEST_GPX_WAY_CLOSED.last(), WaypointType.END),
            )
            val gpxDetails = TEST_GPX_DETAILS.copy(waypoints = waypoints)
            everySuspend { gpxRepository.readGpxFile(any()) } returns gpxDetails
            val service = locationMonitoringService(lastKnownLocation = TEST_GPX_WAY_CLOSED.first())
            val viewModel = createViewModel(grantedPermission = true, locationMonitoringService = service)
            advanceUntilIdle()
            viewModel.onEvent(MainUiEvents.GpxFileSelected("uri"))
            advanceUntilIdle()

            viewModel.uiState.value.mapUiState.allDistancesVisible shouldBe false
            viewModel.uiState.value.mapUiState.distanceInfoWindows shouldBe emptyList()

            viewModel.onEvent(MainUiEvents.GpxDistancesVisibilityToggled)
            advanceUntilIdle()

            with(viewModel.uiState.value.mapUiState) {
                allDistancesVisible shouldBe true
                distanceInfoWindows.map { it.location } shouldBe waypoints.map { it.location }
            }

            viewModel.onEvent(MainUiEvents.GpxDistancesVisibilityToggled)
            advanceUntilIdle()

            with(viewModel.uiState.value.mapUiState) {
                allDistancesVisible shouldBe false
                distanceInfoWindows shouldBe emptyList()
            }
        }
    }

    @Test
    fun `Given selected waypoint, When GpxDistancesVisibilityToggled on and off, Then windows are fully cleared`() {
        runTest {
            val waypoints = listOf(
                GpxWaypoint(TEST_GPX_WAY_CLOSED.first(), WaypointType.START),
                GpxWaypoint(TEST_GPX_WAY_CLOSED[2], WaypointType.INTERMEDIATE),
                GpxWaypoint(TEST_GPX_WAY_CLOSED.last(), WaypointType.END),
            )
            val gpxDetails = TEST_GPX_DETAILS.copy(waypoints = waypoints)
            everySuspend { gpxRepository.readGpxFile(any()) } returns gpxDetails
            val service = locationMonitoringService(lastKnownLocation = TEST_GPX_WAY_CLOSED.first())
            val viewModel = createViewModel(grantedPermission = true, locationMonitoringService = service)
            advanceUntilIdle()
            viewModel.onEvent(MainUiEvents.GpxFileSelected("uri"))
            advanceUntilIdle()

            viewModel.onEvent(MainUiEvents.GpxWaypointClicked(waypoints[1]))
            advanceUntilIdle()
            viewModel.uiState.value.mapUiState.distanceInfoWindows.map { it.location } shouldBe
                listOf(waypoints[1].location)

            viewModel.onEvent(MainUiEvents.GpxDistancesVisibilityToggled)
            advanceUntilIdle()

            with(viewModel.uiState.value.mapUiState) {
                allDistancesVisible shouldBe true
                distanceInfoWindows.map { it.location } shouldBe waypoints.map { it.location }
            }

            viewModel.onEvent(MainUiEvents.GpxDistancesVisibilityToggled)
            advanceUntilIdle()

            with(viewModel.uiState.value.mapUiState) {
                allDistancesVisible shouldBe false
                distanceInfoWindows shouldBe emptyList()
            }
        }
    }

    @Test
    fun `Given all distances visible, When DistanceInfoWindowDismissed, Then windows cleared and toggle off`() {
        runTest {
            val waypoints = listOf(
                GpxWaypoint(TEST_GPX_WAY_CLOSED.first(), WaypointType.START),
                GpxWaypoint(TEST_GPX_WAY_CLOSED.last(), WaypointType.END),
            )
            val gpxDetails = TEST_GPX_DETAILS.copy(waypoints = waypoints)
            everySuspend { gpxRepository.readGpxFile(any()) } returns gpxDetails
            val service = locationMonitoringService(lastKnownLocation = TEST_GPX_WAY_CLOSED.first())
            val viewModel = createViewModel(grantedPermission = true, locationMonitoringService = service)
            advanceUntilIdle()
            viewModel.onEvent(MainUiEvents.GpxFileSelected("uri"))
            advanceUntilIdle()
            viewModel.onEvent(MainUiEvents.GpxDistancesVisibilityToggled)
            advanceUntilIdle()
            viewModel.uiState.value.mapUiState.distanceInfoWindows.shouldNotBeEmpty()

            viewModel.onEvent(MainUiEvents.DistanceInfoWindowDismissed)
            advanceUntilIdle()

            with(viewModel.uiState.value.mapUiState) {
                allDistancesVisible shouldBe false
                distanceInfoWindows shouldBe emptyList()
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
                    target = CameraTarget.Bounds(
                        TEST_GPX_DETAILS.locations + TEST_GPX_DETAILS.waypoints.map { it.location },
                    ),
                    contentPadding = ContentPadding.MAP_GPX,
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
    fun `Given loaded GPX, When GpxMapsNavigationClicked START, Then effect is OpenMapsNavigation to start`() {
        runTest {
            val startLocation = Location(47.0, 19.0, 300.0)
            val endLocation = Location(48.0, 20.0, 500.0)
            everySuspend { gpxRepository.readGpxFile(any()) } returns TEST_GPX_DETAILS.copy(
                waypoints = listOf(
                    GpxWaypoint(startLocation, WaypointType.START),
                    GpxWaypoint(endLocation, WaypointType.END),
                ),
            )
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.onEvent(MainUiEvents.GpxFileSelected("uri"))
            advanceUntilIdle()

            viewModel.mainUiEffects.test {
                viewModel.onEvent(MainUiEvents.GpxMapsNavigationClicked(GpxMapsNavigationType.START))

                awaitItem() shouldBe MainUiEffects.OpenMapsNavigation(startLocation)
                ensureAllEventsConsumed()
            }
        }
    }

    @Test
    fun `Given loaded GPX, When GpxMapsNavigationClicked END, Then effect is OpenMapsNavigation to end`() {
        runTest {
            val startLocation = Location(47.0, 19.0, 300.0)
            val endLocation = Location(48.0, 20.0, 500.0)
            everySuspend { gpxRepository.readGpxFile(any()) } returns TEST_GPX_DETAILS.copy(
                waypoints = listOf(
                    GpxWaypoint(startLocation, WaypointType.START),
                    GpxWaypoint(endLocation, WaypointType.END),
                ),
            )
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.onEvent(MainUiEvents.GpxFileSelected("uri"))
            advanceUntilIdle()

            viewModel.mainUiEffects.test {
                viewModel.onEvent(MainUiEvents.GpxMapsNavigationClicked(GpxMapsNavigationType.END))

                awaitItem() shouldBe MainUiEffects.OpenMapsNavigation(endLocation)
                ensureAllEventsConsumed()
            }
        }
    }

    @Test
    fun `Given round trip GPX, When GpxMapsNavigationClicked START, Then effect is OpenMapsNavigation to round trip`() {
        runTest {
            val roundTripLocation = Location(47.0, 19.0, 300.0)
            everySuspend { gpxRepository.readGpxFile(any()) } returns TEST_GPX_DETAILS.copy(
                waypoints = listOf(
                    GpxWaypoint(roundTripLocation, WaypointType.ROUND_TRIP),
                ),
            )
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.onEvent(MainUiEvents.GpxFileSelected("uri"))
            advanceUntilIdle()

            viewModel.mainUiEffects.test {
                viewModel.onEvent(MainUiEvents.GpxMapsNavigationClicked(GpxMapsNavigationType.START))

                awaitItem() shouldBe MainUiEffects.OpenMapsNavigation(roundTripLocation)
                ensureAllEventsConsumed()
            }
        }
    }

    @Test
    fun `Given no loaded GPX, When GpxMapsNavigationClicked, Then no effect is emitted`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.mainUiEffects.test {
                viewModel.onEvent(MainUiEvents.GpxMapsNavigationClicked(GpxMapsNavigationType.START))
                advanceUntilIdle()

                expectNoEvents()
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
    fun `When SearchDestinationSelected, Then sheet is hidden`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.uiState.test {
                awaitItem().sheet shouldBe null

                viewModel.onEvent(MainUiEvents.LayersClicked)
                awaitItem().sheet shouldBe Sheet.Layers

                viewModel.onEvent(MainUiEvents.SearchDestinationSelected(TEST_DESTINATION))

                awaitItem().sheet shouldBe null
            }
        }
    }

    @Test
    fun `When SearchDestinationSelected, Then mapUiEffect is UpdateCamera to destination location`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.mapUiEffects.test {
                awaitItem() shouldBe MapUiEffects.ShowMyLocation(MyLocationStatus.Following, animated = false)

                viewModel.onEvent(MainUiEvents.SearchDestinationSelected(TEST_DESTINATION))

                awaitItem() shouldBe MapUiEffects.UpdateCamera(
                    target = CameraTarget.Center(TEST_DESTINATION.location, zoom = 16.0),
                )
                ensureAllEventsConsumed()
            }
        }
    }

    @Test
    fun `Given known osmId, When DestinationSelected, Then UpdateCamera to destination location and visit recorded`() {
        runTest {
            every { destinationRepository.requireDestination(TEST_DESTINATION.osmId) } returns TEST_DESTINATION
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.mapUiEffects.test {
                awaitItem() shouldBe MapUiEffects.ShowMyLocation(MyLocationStatus.Following, animated = false)

                viewModel.onEvent(MainUiEvents.DestinationSelected(TEST_DESTINATION.osmId))

                awaitItem() shouldBe MapUiEffects.UpdateCamera(
                    target = CameraTarget.Center(TEST_DESTINATION.location, zoom = 16.0),
                )
                ensureAllEventsConsumed()
            }
            advanceUntilIdle()
            verifySuspend { placeHistoryRepository.recordVisit(TEST_DESTINATION) }
        }
    }

    @Test
    fun `Given place without bounding box, When SearchPlaceSelected, Then UpdateCamera centers on location`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.mapUiEffects.test {
                awaitItem() shouldBe MapUiEffects.ShowMyLocation(MyLocationStatus.Following, animated = false)

                viewModel.onEvent(MainUiEvents.SearchPlaceSelected(TEST_PLACE))

                awaitItem() shouldBe MapUiEffects.UpdateCamera(
                    target = CameraTarget.Center(TEST_PLACE.location, zoom = 16.0),
                )
                ensureAllEventsConsumed()
            }
        }
    }

    @Test
    fun `Given place with bounding box, When SearchPlaceSelected, Then UpdateCamera fits the bounding box`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.mapUiEffects.test {
                awaitItem() shouldBe MapUiEffects.ShowMyLocation(MyLocationStatus.Following, animated = false)

                viewModel.onEvent(MainUiEvents.SearchPlaceSelected(TEST_PLACE_WITH_BOUNDING_BOX))

                awaitItem() shouldBe MapUiEffects.UpdateCamera(
                    target = CameraTarget.Bounds(
                        locations = TEST_PLACE_WITH_BOUNDING_BOX.boundingBox!!.toLocations(),
                        maxZoom = 16.0,
                    ),
                )
                ensureAllEventsConsumed()
            }
        }
    }

    @Test
    fun `Given place from search, When SearchPlaceSelected, Then visit is recorded`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.onEvent(MainUiEvents.SearchPlaceSelected(TEST_PLACE))
            advanceUntilIdle()

            verifySuspend { placeHistoryRepository.recordVisit(TEST_PLACE) }
        }
    }

    @Test
    fun `Given history place, When HistoryPlaceSelected, Then visit is re-recorded keeping its original source`() {
        runTest {
            val historyPlace = TEST_PLACE.copy(placeSource = PlaceSource.DESTINATIONS)
            everySuspend { placeHistoryRepository.getPlace(any(), any()) } returns historyPlace
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.onEvent(MainUiEvents.HistoryPlaceSelected(OsmType.NODE, historyPlace.osmId))
            advanceUntilIdle()

            verifySuspend { placeHistoryRepository.recordVisit(historyPlace) }
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

    @Test
    fun `Given following state, When my location clicked, Then live compass analytics event is logged`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.onEvent(MainUiEvents.MyLocationClicked)
            advanceUntilIdle()

            analyticsService.loggedEvents shouldBe listOf(
                AnalyticsEvent.MyLocationFollowed(MyLocationMode.LIVE_COMPASS),
            )
        }
    }

    @Test
    fun `Given live compass state, When my location clicked, Then following analytics event is logged`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.onEvent(MainUiEvents.MyLocationClicked)
            advanceUntilIdle()
            viewModel.onEvent(MainUiEvents.MyLocationClicked)
            advanceUntilIdle()

            analyticsService.loggedEvents shouldBe listOf(
                AnalyticsEvent.MyLocationFollowed(MyLocationMode.LIVE_COMPASS),
                AnalyticsEvent.MyLocationFollowed(MyLocationMode.FOLLOWING),
            )
        }
    }

    @Test
    fun `Given base layer selection, When onEvent, Then layer analytics event is logged`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.onEvent(MainUiEvents.BaseLayerSelected(BaseLayer.SATELLITE))

            analyticsService.loggedEvents shouldBe listOf(AnalyticsEvent.LayerSelected(Layer.SATELLITE))
        }
    }

    @Test
    fun `Given place selection from search, When onEvent, Then place analytics event is logged`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.onEvent(MainUiEvents.SearchPlaceSelected(TEST_PLACE))

            analyticsService.loggedEvents shouldBe listOf(AnalyticsEvent.SearchPlaceSelected)
        }
    }

    @Test
    fun `Given recent place selection from search, When onEvent, Then history place analytics event is logged`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.onEvent(MainUiEvents.SearchRecentPlaceSelected(TEST_PLACE))

            analyticsService.loggedEvents shouldBe listOf(AnalyticsEvent.HistoryPlaceSelected)
        }
    }

    @Test
    fun `Given destination selection from search, When onEvent, Then destination analytics event is logged`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.onEvent(MainUiEvents.SearchDestinationSelected(TEST_DESTINATION))

            analyticsService.loggedEvents shouldBe listOf(AnalyticsEvent.DestinationSelected(TEST_DESTINATION.name))
        }
    }

    @Test
    fun `Given place history match from search results, When onEvent, Then search place history event is logged`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.onEvent(MainUiEvents.SearchResultPlaceHistorySelected(TEST_PLACE))

            analyticsService.loggedEvents shouldBe listOf(AnalyticsEvent.SearchPlaceHistorySelected)
        }
    }

    @Test
    fun `Given destination match from search results, When onEvent, Then search destination event is logged`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.onEvent(MainUiEvents.SearchResultDestinationSelected(TEST_DESTINATION))

            analyticsService.loggedEvents shouldBe listOf(
                AnalyticsEvent.SearchDestinationSelected(TEST_DESTINATION.name),
            )
        }
    }

    @Test
    fun `Given successful GPX import, When onEvent, Then gpx imported analytics event is logged`() {
        runTest {
            everySuspend { gpxRepository.readGpxFile(any()) } returns TEST_GPX_DETAILS
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.onEvent(MainUiEvents.GpxFileSelected("uri"))
            advanceUntilIdle()

            analyticsService.loggedEvents shouldBe listOf(AnalyticsEvent.GpxImported(GpxSource.FILES))
            analyticsService.screenViews shouldBe listOf(AnalyticsEvent.ScreenView(Screen.GPX_DETAILS))
        }
    }

    @Test
    fun `Given GPX imported from the Layers sheet, When onEvent, Then gpx imported layers event is logged`() {
        runTest {
            everySuspend { gpxRepository.readGpxFile(any()) } returns TEST_GPX_DETAILS
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.onEvent(MainUiEvents.GpxFileSelected("uri", GpxSource.LAYERS))
            advanceUntilIdle()

            analyticsService.loggedEvents shouldBe listOf(AnalyticsEvent.GpxImported(GpxSource.LAYERS))
        }
    }

    @Test
    fun `Given saved GPX reopened, When onEvent, Then history gpx selected analytics event is logged`() {
        runTest {
            everySuspend { gpxRepository.readGpxFile(any()) } returns TEST_GPX_DETAILS
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.onEvent(MainUiEvents.GpxFileReopened("uri"))
            advanceUntilIdle()

            analyticsService.loggedEvents shouldBe listOf(AnalyticsEvent.HistoryGpxSelected)
        }
    }

    @Test
    fun `Given GPX details, When GpxStartNavigationClicked, Then gpx navigation started event is logged`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.onEvent(MainUiEvents.GpxStartNavigationClicked)
            advanceUntilIdle()

            analyticsService.loggedEvents shouldBe listOf(AnalyticsEvent.GpxNavigationStarted)
        }
    }

    @Test
    fun `Given GPX details, When GpxMapsNavigationClicked, Then gpx maps navigation opened event is logged`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.onEvent(MainUiEvents.GpxMapsNavigationClicked(GpxMapsNavigationType.START))
            advanceUntilIdle()

            analyticsService.loggedEvents shouldBe listOf(AnalyticsEvent.GpxMapsNavigationOpened)
        }
    }

    @Test
    fun `Given failing GPX import, When onEvent, Then gpx import failed event is logged`() {
        runTest {
            everySuspend { gpxRepository.readGpxFile(any()) } throws NonGpxFileException()
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.onEvent(MainUiEvents.GpxFileSelected("uri"))
            advanceUntilIdle()

            analyticsService.loggedEvents shouldBe listOf(AnalyticsEvent.GpxImportFailed)
        }
    }

    @Test
    fun `Given GPX details, When GpxCloseClicked, Then gpx closed event is logged`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.onEvent(MainUiEvents.GpxCloseClicked)
            advanceUntilIdle()

            analyticsService.loggedEvents shouldBe listOf(AnalyticsEvent.GpxClosed)
        }
    }

    @Test
    fun `Given GPX details, When GpxRouteVisibilityToggled, Then gpx route visibility toggled event is logged`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.onEvent(MainUiEvents.GpxRouteVisibilityToggled)
            advanceUntilIdle()

            analyticsService.loggedEvents shouldBe listOf(AnalyticsEvent.GpxRouteVisibilityToggled)
        }
    }

    @Test
    fun `Given GPX details, When GpxDistancesVisibilityToggled, Then gpx distances toggled event is logged`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.onEvent(MainUiEvents.GpxDistancesVisibilityToggled)
            advanceUntilIdle()

            analyticsService.loggedEvents shouldBe listOf(AnalyticsEvent.GpxDistancesToggled)
        }
    }

    @Test
    fun `Given GPX details, When GpxOverviewClicked, Then gpx overview clicked event is logged`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.onEvent(MainUiEvents.GpxOverviewClicked)
            advanceUntilIdle()

            analyticsService.loggedEvents shouldBe listOf(AnalyticsEvent.GpxOverviewClicked)
        }
    }

    @Test
    fun `Given whats new to show, When init, Then whats new screen view is logged`() {
        runTest {
            everySuspend { whatsNewRepository.shouldShowWhatsNew() } returns true
            every { whatsNewRepository.currentWhatsNew } returns WhatsNewContent.toCurrentWhatsNew()

            createViewModel(grantedPermission = true)
            advanceUntilIdle()

            analyticsService.screenViews shouldBe listOf(AnalyticsEvent.ScreenView(Screen.WHATS_NEW))
        }
    }

    @Test
    fun `Given search sheet opened, When onEvent, Then search screen view is logged`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.onEvent(MainUiEvents.SearchClicked)
            advanceUntilIdle()

            analyticsService.screenViews shouldBe listOf(AnalyticsEvent.ScreenView(Screen.SEARCH))
        }
    }

    @Test
    fun `Given layers sheet opened, When onEvent, Then layers screen view is logged`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.onEvent(MainUiEvents.LayersClicked)
            advanceUntilIdle()

            analyticsService.screenViews shouldBe listOf(AnalyticsEvent.ScreenView(Screen.LAYERS))
        }
    }

    @Test
    fun `Given search sheet dismissed and reopened, When onEvent, Then two search screen views are logged`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.onEvent(MainUiEvents.SearchClicked)
            advanceUntilIdle()
            viewModel.onEvent(MainUiEvents.SheetDismissed)
            advanceUntilIdle()
            viewModel.onEvent(MainUiEvents.SearchClicked)
            advanceUntilIdle()

            analyticsService.screenViews shouldBe listOf(
                AnalyticsEvent.ScreenView(Screen.SEARCH),
                AnalyticsEvent.ScreenView(Screen.SEARCH),
            )
        }
    }

    @Test
    fun `When MapLongClicked, Then uiState has loading PlaceDetails, then the reverse geocoded place`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.uiState.test {
                awaitItem().sheet shouldBe null

                viewModel.onEvent(MainUiEvents.MapLongClicked(TEST_LONG_TAP_LOCATION))

                with(awaitItem()) {
                    sheet shouldBe Sheet.PlaceDetails
                    mapUiState.placeDetails shouldBe PlaceDetails.Loading(TEST_LONG_TAP_LOCATION)
                }

                advanceUntilIdle()

                awaitItem().mapUiState.placeDetails shouldBe PlaceDetails.PlaceLoaded(
                    Place(
                        osmId = "2",
                        location = TEST_LONG_TAP_LOCATION,
                        name = "Dobogókő",
                        placeSource = PlaceSource.LONG_TAP_ON_MAP,
                        address = "Pilisszentkereszt, Pest, Hungary",
                        placeCategory = PlaceCategory.PEAK,
                        osmType = OsmType.NODE,
                    ),
                )
            }
        }
    }

    @Test
    fun `Given last known location, When MapLongClicked, Then PlaceDetails has the straight line distance`() {
        runTest {
            val userLocation = Location(TEST_LONG_TAP_LOCATION.latitude - 0.1, TEST_LONG_TAP_LOCATION.longitude)
            val viewModel = createViewModel(
                grantedPermission = true,
                locationMonitoringService = locationMonitoringService(lastKnownLocation = userLocation),
            )
            advanceUntilIdle()

            viewModel.onEvent(MainUiEvents.MapLongClicked(TEST_LONG_TAP_LOCATION))
            advanceUntilIdle()

            viewModel.uiState.test {
                val placeDetails = awaitItem().mapUiState.placeDetails

                placeDetails.shouldBeInstanceOf<PlaceDetails.PlaceLoaded>().place.distance shouldBe "11.1 km"
            }
        }
    }

    @Test
    fun `Given successful reverse geocode, When MapLongClicked, Then the long tapped place visit is recorded`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.onEvent(MainUiEvents.MapLongClicked(TEST_LONG_TAP_LOCATION))
            advanceUntilIdle()

            verifySuspend {
                placeHistoryRepository.recordVisit(
                    Place(
                        osmId = "2",
                        location = TEST_LONG_TAP_LOCATION,
                        name = "Dobogókő",
                        placeSource = PlaceSource.LONG_TAP_ON_MAP,
                        address = "Pilisszentkereszt, Pest, Hungary",
                        placeCategory = PlaceCategory.PEAK,
                        osmType = OsmType.NODE,
                    ),
                )
            }
        }
    }

    @Test
    fun `Given failing reverse geocode, When MapLongClicked, Then no place visit is recorded`() {
        runTest {
            reverseGeocodeResult = NetworkResult.Error(NetworkError.NO_INTERNET)
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.onEvent(MainUiEvents.MapLongClicked(TEST_LONG_TAP_LOCATION))
            advanceUntilIdle()

            verifySuspend(VerifyMode.not) { placeHistoryRepository.recordVisit(any<Place>()) }
        }
    }

    @Test
    fun `Given failing reverse geocode, When MapLongClicked, Then PlaceDetails is Unresolved with the location`() {
        runTest {
            reverseGeocodeResult = NetworkResult.Error(NetworkError.NO_INTERNET)
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.onEvent(MainUiEvents.MapLongClicked(TEST_LONG_TAP_LOCATION))
            advanceUntilIdle()

            viewModel.uiState.test {
                with(awaitItem()) {
                    sheet shouldBe Sheet.PlaceDetails
                    mapUiState.placeDetails shouldBe PlaceDetails.Unresolved(TEST_LONG_TAP_LOCATION)
                }
            }
        }
    }

    @Test
    fun `Given failing reverse geocode and last known location, When MapLongClicked, Then distance is shown`() {
        runTest {
            reverseGeocodeResult = NetworkResult.Error(NetworkError.NO_INTERNET)
            val userLocation = Location(TEST_LONG_TAP_LOCATION.latitude - 0.1, TEST_LONG_TAP_LOCATION.longitude)
            val viewModel = createViewModel(
                grantedPermission = true,
                locationMonitoringService = locationMonitoringService(lastKnownLocation = userLocation),
            )
            advanceUntilIdle()

            viewModel.onEvent(MainUiEvents.MapLongClicked(TEST_LONG_TAP_LOCATION))
            advanceUntilIdle()

            viewModel.uiState.test {
                val placeDetails = awaitItem().mapUiState.placeDetails

                placeDetails shouldBe PlaceDetails.Unresolved(TEST_LONG_TAP_LOCATION, distance = "11.1 km")
            }
        }
    }

    @Test
    fun `Given Unresolved PlaceDetails, When PlaceDetailsMapsNavigationClicked, Then the tapped location opens`() {
        runTest {
            reverseGeocodeResult = NetworkResult.Error(NetworkError.NO_INTERNET)
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()
            viewModel.onEvent(MainUiEvents.MapLongClicked(TEST_LONG_TAP_LOCATION))
            advanceUntilIdle()

            viewModel.mainUiEffects.test {
                viewModel.onEvent(MainUiEvents.PlaceDetailsMapsNavigationClicked)

                awaitItem() shouldBe MainUiEffects.OpenMapsNavigation(TEST_LONG_TAP_LOCATION)
                ensureAllEventsConsumed()
            }
        }
    }

    @Test
    fun `Given shown PlaceDetails, When PlaceDetailsCloseClicked, Then sheet and marker are cleared`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()
            viewModel.onEvent(MainUiEvents.MapLongClicked(TEST_LONG_TAP_LOCATION))
            advanceUntilIdle()

            viewModel.onEvent(MainUiEvents.PlaceDetailsCloseClicked)
            advanceUntilIdle()

            viewModel.uiState.test {
                with(awaitItem()) {
                    sheet shouldBe null
                    mapUiState.placeDetails shouldBe null
                }
            }
        }
    }

    @Test
    fun `Given shown PlaceDetails, When SearchClicked, Then the place marker is cleared`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()
            viewModel.onEvent(MainUiEvents.MapLongClicked(TEST_LONG_TAP_LOCATION))
            advanceUntilIdle()

            viewModel.onEvent(MainUiEvents.SearchClicked)
            advanceUntilIdle()

            viewModel.uiState.test {
                with(awaitItem()) {
                    sheet shouldBe Sheet.Search
                    mapUiState.placeDetails shouldBe null
                }
            }
        }
    }

    @Test
    fun `Given shown PlaceDetails, When PlaceDetailsMapsNavigationClicked, Then effect is OpenMapsNavigation`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()
            viewModel.onEvent(MainUiEvents.MapLongClicked(TEST_LONG_TAP_LOCATION))
            advanceUntilIdle()

            viewModel.mainUiEffects.test {
                viewModel.onEvent(MainUiEvents.PlaceDetailsMapsNavigationClicked)

                awaitItem() shouldBe MainUiEffects.OpenMapsNavigation(TEST_LONG_TAP_LOCATION)
                ensureAllEventsConsumed()
            }
        }
    }

    @Test
    fun `When MapLongClicked, Then place details analytics events are logged`() {
        runTest {
            val viewModel = createViewModel(grantedPermission = true)
            advanceUntilIdle()

            viewModel.onEvent(MainUiEvents.MapLongClicked(TEST_LONG_TAP_LOCATION))
            advanceUntilIdle()
            viewModel.onEvent(MainUiEvents.PlaceDetailsRoutePlanClicked)
            viewModel.onEvent(MainUiEvents.PlaceDetailsCloseClicked)
            advanceUntilIdle()

            analyticsService.loggedEvents shouldBe listOf(
                AnalyticsEvent.PlaceDetailsOpened(PlaceDetailsSource.LONG_TAP),
                AnalyticsEvent.PlaceDetailsRoutePlanClicked,
                AnalyticsEvent.PlaceDetailsClosed,
            )
        }
    }
}
