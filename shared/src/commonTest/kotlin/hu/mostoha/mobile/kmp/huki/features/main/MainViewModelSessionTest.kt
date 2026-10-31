package hu.mostoha.mobile.kmp.huki.features.main

import app.cash.turbine.Event
import app.cash.turbine.test
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.location.LOCATION
import dev.icerock.moko.permissions.test.createPermissionControllerMock
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import hu.mostoha.mobile.kmp.huki.data.TEST_GPX_DETAILS
import hu.mostoha.mobile.kmp.huki.features.map.LandscapesGrouping
import hu.mostoha.mobile.kmp.huki.features.map.MapUiEffects
import hu.mostoha.mobile.kmp.huki.model.analytics.AnalyticsEvent
import hu.mostoha.mobile.kmp.huki.model.analytics.RestoredFeature
import hu.mostoha.mobile.kmp.huki.model.domain.CameraPosition
import hu.mostoha.mobile.kmp.huki.model.domain.CameraTarget
import hu.mostoha.mobile.kmp.huki.model.domain.ContentPadding
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.MyLocationStatus
import hu.mostoha.mobile.kmp.huki.model.domain.OktType
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlannerProfile
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlannerSession
import hu.mostoha.mobile.kmp.huki.model.domain.Sheet
import hu.mostoha.mobile.kmp.huki.model.domain.UserPreferences
import hu.mostoha.mobile.kmp.huki.model.mapper.toRoutePlannerWaypoint
import hu.mostoha.mobile.kmp.huki.model.network.LocationIqPlace
import hu.mostoha.mobile.kmp.huki.model.network.NetworkResult
import hu.mostoha.mobile.kmp.huki.repository.DefaultMapCameraStore
import hu.mostoha.mobile.kmp.huki.repository.DestinationRepository
import hu.mostoha.mobile.kmp.huki.repository.FakeLandscapeRepository
import hu.mostoha.mobile.kmp.huki.repository.FakeOktRepository
import hu.mostoha.mobile.kmp.huki.repository.GeocodingRepository
import hu.mostoha.mobile.kmp.huki.repository.GpxRepository
import hu.mostoha.mobile.kmp.huki.repository.PlaceHistoryRepository
import hu.mostoha.mobile.kmp.huki.repository.SettingsRepository
import hu.mostoha.mobile.kmp.huki.repository.WhatsNewRepository
import hu.mostoha.mobile.kmp.huki.service.FakeAnalyticsService
import hu.mostoha.mobile.kmp.huki.service.FakeCrashlyticsService
import hu.mostoha.mobile.kmp.huki.service.LocationMonitoringService
import hu.mostoha.mobile.kmp.huki.util.AppLaunchConfig
import hu.mostoha.mobile.kmp.huki.util.mainSessionSavedStateHandle
import hu.mostoha.mobile.kmp.huki.util.reversed
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelSessionTest {

    private val testDispatcher = StandardTestDispatcher()
    private val analyticsService = FakeAnalyticsService()
    private val crashlyticsService = FakeCrashlyticsService()
    private val gpxRepository = mock<GpxRepository> {
        everySuspend { readGpxFile(TEST_GPX_DETAILS.fileUri) } returns TEST_GPX_DETAILS
    }
    private val geocodingRepository = object : GeocodingRepository {
        override suspend fun autocomplete(searchText: String) = NetworkResult.Success(emptyList<LocationIqPlace>())

        override suspend fun reverseGeocode(location: Location): NetworkResult<LocationIqPlace?> = NetworkResult.Success(null)
    }

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        AppLaunchConfig.importGpxPath = null
    }

    private fun createViewModel(
        session: String? = null,
        granted: Boolean = true,
        gpxRepository: GpxRepository = this.gpxRepository,
        showWhatsNew: Boolean = false,
    ): MainViewModel =
        MainViewModel(
            permissionsController = createPermissionControllerMock(
                allow = setOf(Permission.LOCATION),
                granted = if (granted) setOf(Permission.LOCATION) else emptySet(),
            ),
            gpxRepository = gpxRepository,
            placeHistoryRepository = mock<PlaceHistoryRepository>(MockMode.autoUnit),
            destinationRepository = mock<DestinationRepository>(MockMode.autoUnit) {
                every { getLandscapes() } returns emptyList()
            },
            geocodingRepository = geocodingRepository,
            locationMonitoringService = object : LocationMonitoringService {
                override val locationUpdates: SharedFlow<Location> = MutableSharedFlow()
                override suspend fun lastKnownLocation(): Location? = null
            },
            settingsRepository = mock<SettingsRepository>(MockMode.autoUnit) {
                every { settings } returns flowOf(UserPreferences.DEFAULTS)
            },
            mapCameraStore = DefaultMapCameraStore(),
            whatsNewRepository = mock<WhatsNewRepository>(MockMode.autoUnit) {
                everySuspend { shouldShowWhatsNew() } returns showWhatsNew
            },
            oktRepository = FakeOktRepository(),
            landscapeRepository = FakeLandscapeRepository(),
            analyticsService = analyticsService,
            crashlyticsService = crashlyticsService,
            defaultDispatcher = testDispatcher,
            savedStateHandle = mainSessionSavedStateHandle(session),
        )

    private fun TestScope.restoreViewModel(
        session: String,
        granted: Boolean = true,
        gpxRepository: GpxRepository = this@MainViewModelSessionTest.gpxRepository,
        showWhatsNew: Boolean = false,
    ): MainViewModel {
        analyticsService.loggedEvents.clear()
        val viewModel = createViewModel(session, granted, gpxRepository, showWhatsNew)
        advanceUntilIdle()
        return viewModel
    }

    private suspend fun MainViewModel.emittedMapEffects(): List<MapUiEffects> {
        var effects = emptyList<MapUiEffects>()
        mapUiEffects.test {
            effects = cancelAndConsumeRemainingEvents().mapNotNull { (it as? Event.Item)?.value }
        }
        return effects
    }

    @Test
    fun `Given a reversed GPX with slope colors toggled - When the session is restored - Then the same GPX state comes back without re-opening it`() {
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.onEvent(GpxUiEvents.GpxFileReopened(TEST_GPX_DETAILS.fileUri))
            advanceUntilIdle()
            viewModel.onEvent(GpxUiEvents.GpxDirectionReversed)
            viewModel.onEvent(GpxUiEvents.GpxSlopeColorsToggled)
            advanceUntilIdle()

            val restored = restoreViewModel(viewModel.sessionSnapshot())

            val uiState = restored.uiState.value
            with(uiState.mapUiState.gpxDetails.shouldNotBeNull()) {
                isReversed shouldBe true
                locations shouldBe TEST_GPX_DETAILS.reversed().locations
            }
            uiState.mapUiState.gpxSlopeColorsVisible shouldBe !UserPreferences.DEFAULTS.gpxSlopeColorsVisible
            uiState.mapUiState.gpxLayerVisible shouldBe true
            uiState.sheet.shouldBeInstanceOf<Sheet.Gpx>()
            analyticsService.loggedEvents shouldContain AnalyticsEvent.SessionRestored(RestoredFeature.GPX)
            analyticsService.loggedEvents shouldNotContain AnalyticsEvent.HistoryGpxSelected
        }
    }

    @Test
    fun `Given the restored GPX file is gone - When the session is restored - Then the plain map shows without an alert`() {
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.onEvent(GpxUiEvents.GpxFileReopened(TEST_GPX_DETAILS.fileUri))
            advanceUntilIdle()
            val missingGpxRepository = mock<GpxRepository> {
                everySuspend { readGpxFile(any()) } throws IllegalStateException("deleted")
            }

            val restored = restoreViewModel(viewModel.sessionSnapshot(), gpxRepository = missingGpxRepository)

            val uiState = restored.uiState.value
            uiState.mapUiState.gpxDetails.shouldBeNull()
            uiState.alert.shouldBeNull()
            uiState.isGpxLoading shouldBe false
            uiState.sheet.shouldBeNull()
            crashlyticsService.recordedExceptions shouldBe emptyList()
            analyticsService.loggedEvents.filterIsInstance<AnalyticsEvent.SessionRestored>() shouldBe emptyList()
        }
    }

    @Test
    fun `Given a started and reversed OKT section - When the session is restored - Then the section comes back started`() {
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.onEvent(OktUiEvents.OktTypeClicked(OktType.OKT))
            advanceUntilIdle()
            viewModel.onEvent(OktUiEvents.OktSectionStartClicked("OKT-01"))
            viewModel.onEvent(OktUiEvents.OktSectionReverseClicked("OKT-01"))
            advanceUntilIdle()

            val restored = restoreViewModel(viewModel.sessionSnapshot())

            val uiState = restored.uiState.value
            with(uiState.mapUiState.okt.shouldNotBeNull()) {
                type shouldBe OktType.OKT
                selectedSectionId shouldBe "OKT-01"
                reversedSectionIds shouldBe setOf("OKT-01")
                selectedLine shouldBe viewModel.uiState.value.mapUiState.okt?.selectedLine
            }
            uiState.sheet.shouldBeNull()
            analyticsService.loggedEvents shouldBe listOf(AnalyticsEvent.SessionRestored(RestoredFeature.OKT))
        }
    }

    @Test
    fun `Given a selected landscape - When the session is restored - Then the landscapes come back with the selection`() {
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.onEvent(LandscapesUiEvents.LandscapesShowClicked)
            advanceUntilIdle()
            viewModel.onEvent(LandscapesUiEvents.LandscapeClicked("matra"))
            advanceUntilIdle()

            val restored = restoreViewModel(viewModel.sessionSnapshot())

            val uiState = restored.uiState.value
            with(uiState.mapUiState.landscapes.shouldNotBeNull()) {
                grouping shouldBe LandscapesGrouping.LANDSCAPE
                selectedLandscapeId shouldBe "matra"
            }
            uiState.mapUiState.hikingLayerVisible shouldBe false
            uiState.sheet shouldBe Sheet.Landscapes
            analyticsService.loggedEvents shouldBe listOf(AnalyticsEvent.SessionRestored(RestoredFeature.LANDSCAPES))
        }
    }

    @Test
    fun `Given an open Route Planner - When the session is restored - Then the planner reopens with its stops`() {
        runTest {
            val routePlannerSession = RoutePlannerSession(
                routeProfile = RoutePlannerProfile.BIKE,
                waypoints = listOf(
                    Location(47.76, 18.96).toRoutePlannerWaypoint(),
                    Location(47.70, 18.80).toRoutePlannerWaypoint(),
                ),
            )
            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.onEvent(MainUiEvents.RoutePlannerClicked)
            viewModel.onEvent(MainUiEvents.RoutePlannerStateChanged(routePlannerSession))

            val restored = restoreViewModel(viewModel.sessionSnapshot())

            restored.uiState.value.sheet shouldBe Sheet.RoutePlanner(restored = routePlannerSession)
        }
    }

    @Test
    fun `Given live compass following - When the session is restored with permission - Then following resumes`() {
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.onEvent(MainUiEvents.MyLocationLongClicked)
            advanceUntilIdle()

            val restored = restoreViewModel(viewModel.sessionSnapshot())

            restored.uiState.value.myLocationState.myLocationStatus shouldBe MyLocationStatus.FollowingLiveCompass
            restored.emittedMapEffects() shouldBe listOf(
                MapUiEffects.ShowMyLocation(MyLocationStatus.FollowingLiveCompass, animated = false),
            )
        }
    }

    @Test
    fun `Given a panned camera - When the session is restored without permission - Then the camera is put back unanimated`() {
        runTest {
            val camera = CameraPosition(location = Location(47.76, 18.96), zoom = 14.0, bearing = 30.0, pitch = 10.0)
            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.onEvent(MainUiEvents.FollowingDisabled)
            viewModel.onEvent(MainUiEvents.MapCameraChanged(camera))

            val restored = restoreViewModel(viewModel.sessionSnapshot(), granted = false)

            restored.uiState.value.myLocationState.myLocationStatus shouldBe MyLocationStatus.NotAvailable
            restored.emittedMapEffects() shouldBe listOf(
                MapUiEffects.UpdateCamera(
                    target = CameraTarget.Center(camera.location, camera.zoom),
                    bearing = camera.bearing,
                    pitch = camera.pitch,
                    animated = false,
                ),
            )
        }
    }

    @Test
    fun `Given a started OKT section - When the session is restored without permission - Then the camera keeps its padding`() {
        runTest {
            val camera = CameraPosition(location = Location(47.76, 18.96), zoom = 12.0, bearing = 0.0, pitch = 0.0)
            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.onEvent(OktUiEvents.OktTypeClicked(OktType.OKT))
            advanceUntilIdle()
            viewModel.onEvent(OktUiEvents.OktSectionStartClicked("OKT-01"))
            advanceUntilIdle()
            viewModel.onEvent(MainUiEvents.MapCameraChanged(camera))

            val restored = restoreViewModel(viewModel.sessionSnapshot(), granted = false)

            restored.emittedMapEffects() shouldContain MapUiEffects.UpdateCamera(
                target = CameraTarget.Center(camera.location, camera.zoom),
                bearing = camera.bearing,
                pitch = camera.pitch,
                contentPadding = ContentPadding.MAP_OKT_STARTED,
                animated = false,
            )
        }
    }

    @Test
    fun `Given a new version - When a session with a feature is restored - Then WhatsNew does not replace the sheet`() {
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.onEvent(LandscapesUiEvents.LandscapesShowClicked)
            advanceUntilIdle()

            val restored = restoreViewModel(viewModel.sessionSnapshot(), showWhatsNew = true)

            restored.uiState.value.sheet shouldBe Sheet.Landscapes
        }
    }

    @Test
    fun `Given no saved session - When the view model starts - Then nothing is restored`() {
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.uiState.value.myLocationState.myLocationStatus shouldBe MyLocationStatus.Following
            analyticsService.loggedEvents.filterIsInstance<AnalyticsEvent.SessionRestored>() shouldBe emptyList()
            viewModel.emittedMapEffects() shouldBe listOf(
                MapUiEffects.ShowMyLocation(MyLocationStatus.Following, animated = false),
            )
        }
    }

    @Test
    fun `Given a launch GPX - When a saved session exists - Then the launch GPX wins`() {
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.onEvent(OktUiEvents.OktTypeClicked(OktType.OKT))
            advanceUntilIdle()
            AppLaunchConfig.importGpxPath = TEST_GPX_DETAILS.fileUri

            val restored = restoreViewModel(viewModel.sessionSnapshot())

            restored.uiState.value.mapUiState.okt.shouldBeNull()
            restored.uiState.value.mapUiState.gpxDetails shouldBe TEST_GPX_DETAILS
        }
    }
}
