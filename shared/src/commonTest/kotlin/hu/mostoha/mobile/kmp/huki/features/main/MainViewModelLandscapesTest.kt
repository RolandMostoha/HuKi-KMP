package hu.mostoha.mobile.kmp.huki.features.main

import app.cash.turbine.test
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.location.LOCATION
import dev.icerock.moko.permissions.test.createPermissionControllerMock
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.data.TEST_GPX_DETAILS
import hu.mostoha.mobile.kmp.huki.features.map.LandscapesGrouping
import hu.mostoha.mobile.kmp.huki.features.map.MapUiEffects
import hu.mostoha.mobile.kmp.huki.model.analytics.AnalyticsEvent
import hu.mostoha.mobile.kmp.huki.model.analytics.BlueTrail
import hu.mostoha.mobile.kmp.huki.model.analytics.PlaceDetailsSource
import hu.mostoha.mobile.kmp.huki.model.domain.CameraTarget
import hu.mostoha.mobile.kmp.huki.model.domain.ContentPadding
import hu.mostoha.mobile.kmp.huki.model.domain.Destination
import hu.mostoha.mobile.kmp.huki.model.domain.DestinationChallenge
import hu.mostoha.mobile.kmp.huki.model.domain.DestinationType
import hu.mostoha.mobile.kmp.huki.model.domain.Landscape
import hu.mostoha.mobile.kmp.huki.model.domain.LandscapeOutlines
import hu.mostoha.mobile.kmp.huki.model.domain.LandscapeType
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.OktType
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceDetails
import hu.mostoha.mobile.kmp.huki.model.domain.Sheet
import hu.mostoha.mobile.kmp.huki.model.domain.UserPreferences
import hu.mostoha.mobile.kmp.huki.model.domain.toLocations
import hu.mostoha.mobile.kmp.huki.model.mapper.toDestinationCategory
import hu.mostoha.mobile.kmp.huki.model.mapper.toPlace
import hu.mostoha.mobile.kmp.huki.model.network.LocationIqPlace
import hu.mostoha.mobile.kmp.huki.model.network.NetworkResult
import hu.mostoha.mobile.kmp.huki.repository.DefaultMapCameraStore
import hu.mostoha.mobile.kmp.huki.repository.DestinationRepository
import hu.mostoha.mobile.kmp.huki.repository.FakeLandscapeRepository
import hu.mostoha.mobile.kmp.huki.repository.FakeOktRepository
import hu.mostoha.mobile.kmp.huki.repository.GeocodingRepository
import hu.mostoha.mobile.kmp.huki.repository.GpxRepository
import hu.mostoha.mobile.kmp.huki.repository.LandscapeRepository
import hu.mostoha.mobile.kmp.huki.repository.PlaceHistoryRepository
import hu.mostoha.mobile.kmp.huki.repository.SettingsRepository
import hu.mostoha.mobile.kmp.huki.repository.WhatsNewRepository
import hu.mostoha.mobile.kmp.huki.service.FakeAnalyticsService
import hu.mostoha.mobile.kmp.huki.service.FakeCrashlyticsService
import hu.mostoha.mobile.kmp.huki.service.LocationMonitoringService
import hu.mostoha.mobile.kmp.huki.util.MapConstants
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelLandscapesTest {

    private companion object {
        private val TEST_DESTINATION_PEAK = Destination(
            id = "1",
            name = "Kékestető",
            town = "Mátraszentimre",
            type = DestinationType.PEAK,
            location = Location(47.8721, 20.0102),
            description = SharedRes.strings.destinations_type_peak,
            popularity = 10,
        )
        private val TEST_DESTINATION_RIVER = Destination(
            id = "2",
            name = "Fátyol-vízesés",
            town = "Szilvásvárad",
            type = DestinationType.RIVER,
            location = Location(48.0855, 20.3889),
            description = SharedRes.strings.destinations_type_river,
            popularity = 8,
        )
        private val TEST_TRAIL_GPX_FILE = SharedRes.files.edut_jagi_tanosveny_gpx
        private val TEST_DESTINATION_TRAIL = Destination(
            id = "3",
            name = "Jági tanösvény",
            town = "Pilisszentiván",
            type = DestinationType.EDUCATIONAL_TRAIL,
            location = Location(47.6120670, 18.8845000),
            description = SharedRes.strings.destinations_type_educational_trail,
            popularity = 4,
            routeGpxFile = TEST_TRAIL_GPX_FILE,
        )
        private val TEST_LANDSCAPE_MATRA = Landscape(
            id = "matra",
            nameRes = SharedRes.strings.landscape_matra,
            colorRes = SharedRes.colors.landscapeMatra,
            landscapeType = LandscapeType.MOUNTAIN_HIGH,
            center = Location(47.87, 19.95),
            destinations = listOf(TEST_DESTINATION_RIVER, TEST_DESTINATION_PEAK),
            areaTags = emptyMap(),
        )
        private val TEST_LANDSCAPE_BUKK = Landscape(
            id = "bukk",
            nameRes = SharedRes.strings.landscape_bukk,
            colorRes = SharedRes.colors.landscapeBukk,
            landscapeType = LandscapeType.MOUNTAIN_HIGH,
            center = Location(48.03, 20.52),
            destinations = listOf(TEST_DESTINATION_TRAIL),
            areaTags = emptyMap(),
        )
        private val TEST_LANDSCAPES = listOf(TEST_LANDSCAPE_MATRA, TEST_LANDSCAPE_BUKK)
        private val TEST_CATEGORY_PEAK = DestinationType.PEAK.toDestinationCategory(
            destinations = listOf(TEST_DESTINATION_PEAK),
        )
        private val TEST_CHALLENGE_HIGHEST_PEAKS = DestinationChallenge.HIGHEST_PEAKS.toDestinationCategory(
            destinations = listOf(TEST_DESTINATION_RIVER, TEST_DESTINATION_PEAK),
        )
        private val TEST_CATEGORIES = listOf(TEST_CHALLENGE_HIGHEST_PEAKS, TEST_CATEGORY_PEAK)
        private val TEST_OUTLINES = FakeLandscapeRepository.TEST_LANDSCAPE_OUTLINES
    }

    private val testDispatcher = StandardTestDispatcher()
    private val analyticsService = FakeAnalyticsService()
    private val destinationRepository = mock<DestinationRepository>(MockMode.autoUnit) {
        every { getLandscapes() } returns TEST_LANDSCAPES
        every { getDestinationCategories() } returns TEST_CATEGORIES
    }
    private val gpxRepository = mock<GpxRepository> {
        everySuspend { readBundledGpxFile(TEST_TRAIL_GPX_FILE) } returns TEST_GPX_DETAILS
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
    }

    private fun createViewModel(
        landscapeRepository: LandscapeRepository = FakeLandscapeRepository(),
    ): MainViewModel =
        MainViewModel(
            permissionsController = createPermissionControllerMock(
                allow = setOf(Permission.LOCATION),
                granted = setOf(Permission.LOCATION),
            ),
            gpxRepository = gpxRepository,
            placeHistoryRepository = mock<PlaceHistoryRepository>(MockMode.autoUnit),
            destinationRepository = destinationRepository,
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
                everySuspend { shouldShowWhatsNew() } returns false
            },
            oktRepository = FakeOktRepository(),
            landscapeRepository = landscapeRepository,
            analyticsService = analyticsService,
            crashlyticsService = FakeCrashlyticsService(),
            defaultDispatcher = testDispatcher,
        )

    private fun TestScope.createOpenedViewModel(selectedLandscapeId: String? = null): MainViewModel {
        val viewModel = createViewModel()
        viewModel.onEvent(LandscapesUiEvents.LandscapesShowClicked)
        advanceUntilIdle()
        selectedLandscapeId?.let {
            viewModel.onEvent(LandscapesUiEvents.LandscapeClicked(it))
            advanceUntilIdle()
        }
        analyticsService.loggedEvents.clear()
        return viewModel
    }

    @Test
    fun `Given Discover sheet - When LandscapesShowClicked - Then Landscapes sheet shows every landscape without destinations`() {
        runTest {
            val viewModel = createViewModel()
            viewModel.onEvent(MainUiEvents.DiscoverClicked)
            advanceUntilIdle()

            viewModel.onEvent(LandscapesUiEvents.LandscapesShowClicked)
            advanceUntilIdle()

            val uiState = viewModel.uiState.value
            uiState.sheet shouldBe Sheet.Landscapes
            uiState.isSearchBarVisible shouldBe false
            with(uiState.mapUiState.landscapes.shouldNotBeNull()) {
                landscapes shouldBe TEST_LANDSCAPES
                outlinesGeoJson shouldBe TEST_OUTLINES.geoJson
                labelsGeoJson shouldBe TEST_OUTLINES.labelsGeoJson
                hungaryMaskGeoJson shouldBe TEST_OUTLINES.hungaryMaskGeoJson
                selectedLandscapeId.shouldBeNull()
                destinations shouldBe emptyList()
            }
            analyticsService.loggedEvents.last() shouldBe AnalyticsEvent.LandscapesOpened
        }
    }

    @Test
    fun `When LandscapesShowClicked - Then hiking layer is hidden`() {
        runTest {
            val viewModel = createViewModel()

            viewModel.onEvent(LandscapesUiEvents.LandscapesShowClicked)
            advanceUntilIdle()

            viewModel.uiState.value.mapUiState.hikingLayerVisible shouldBe false
        }
    }

    @Test
    fun `Given Landscapes with hidden hiking layer - When HikingLayerSelected - Then the user can show it`() {
        runTest {
            val viewModel = createOpenedViewModel()

            viewModel.onEvent(MainUiEvents.HikingLayerSelected)

            viewModel.uiState.value.mapUiState.hikingLayerVisible shouldBe true
            viewModel.uiState.value.mapUiState.landscapes.shouldNotBeNull()
        }
    }

    @Test
    fun `Given Landscapes - When OktTypeClicked - Then hiking layer is shown again`() {
        runTest {
            val viewModel = createOpenedViewModel()

            viewModel.onEvent(OktUiEvents.OktTypeClicked(OktType.OKT))
            advanceUntilIdle()

            viewModel.uiState.value.mapUiState.hikingLayerVisible shouldBe true
        }
    }

    @Test
    fun `Given loading Landscapes - When OktTypeClicked - Then OKT is not overwritten by Landscapes`() {
        runTest {
            val slowLandscapeRepository = object : LandscapeRepository {
                override suspend fun getLandscapeOutlines(): LandscapeOutlines {
                    delay(1.seconds)
                    return TEST_OUTLINES
                }
            }
            val viewModel = createViewModel(landscapeRepository = slowLandscapeRepository)
            viewModel.onEvent(LandscapesUiEvents.LandscapesShowClicked)
            runCurrent()

            viewModel.onEvent(OktUiEvents.OktTypeClicked(OktType.OKT))
            advanceUntilIdle()

            val uiState = viewModel.uiState.value
            uiState.sheet shouldBe Sheet.Okt
            uiState.mapUiState.okt.shouldNotBeNull()
            uiState.mapUiState.landscapes.shouldBeNull()
        }
    }

    @Test
    fun `When LandscapesShowClicked - Then camera fits every landscape outline`() {
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.mapUiEffects.test {
                viewModel.onEvent(LandscapesUiEvents.LandscapesShowClicked)
                advanceUntilIdle()

                expectMostRecentItem() shouldBe MapUiEffects.UpdateCamera(
                    target = CameraTarget.Bounds(MapConstants.HUNGARY_BOUNDING_BOX.toLocations()),
                    contentPadding = ContentPadding.MAP_LANDSCAPES,
                )
            }
        }
    }

    @Test
    fun `Given outlines fail to load - When LandscapesShowClicked - Then Landscapes are not shown`() {
        runTest {
            val viewModel = createViewModel(
                landscapeRepository = FakeLandscapeRepository(exception = IllegalStateException("broken")),
            )

            viewModel.onEvent(LandscapesUiEvents.LandscapesShowClicked)
            advanceUntilIdle()

            viewModel.uiState.value.mapUiState.landscapes.shouldBeNull()
            viewModel.uiState.value.sheet.shouldBeNull()
        }
    }

    @Test
    fun `Given Okt - When LandscapesShowClicked - Then Okt is closed`() {
        runTest {
            val viewModel = createViewModel()
            viewModel.onEvent(OktUiEvents.OktTypeClicked(OktType.OKT))
            advanceUntilIdle()

            viewModel.onEvent(LandscapesUiEvents.LandscapesShowClicked)
            advanceUntilIdle()

            viewModel.uiState.value.mapUiState.okt.shouldBeNull()
            viewModel.uiState.value.mapUiState.landscapes.shouldNotBeNull()
        }
    }

    @Test
    fun `Given Landscapes - When OktTypeClicked - Then Landscapes are closed`() {
        runTest {
            val viewModel = createOpenedViewModel()

            viewModel.onEvent(OktUiEvents.OktTypeClicked(OktType.OKT))
            advanceUntilIdle()

            viewModel.uiState.value.mapUiState.landscapes.shouldBeNull()
            analyticsService.loggedEvents shouldBe listOf(AnalyticsEvent.OktOpened(BlueTrail.OKT))
        }
    }

    @Test
    fun `Given Landscapes - When LandscapeClicked - Then its destinations are revealed along a path from the south-west`() {
        runTest {
            val viewModel = createOpenedViewModel()

            viewModel.onEvent(LandscapesUiEvents.LandscapeClicked(TEST_LANDSCAPE_MATRA.id))
            advanceUntilIdle()

            with(viewModel.uiState.value.mapUiState.landscapes.shouldNotBeNull()) {
                selectedLandscape shouldBe TEST_LANDSCAPE_MATRA
                destinations shouldBe listOf(TEST_DESTINATION_PEAK, TEST_DESTINATION_RIVER)
            }
            analyticsService.loggedEvents shouldBe listOf(AnalyticsEvent.LandscapeSelected(TEST_LANDSCAPE_MATRA.id))
        }
    }

    @Test
    fun `Given Landscapes - When LandscapeClicked - Then camera fits its outline and destinations`() {
        runTest {
            val viewModel = createOpenedViewModel()

            viewModel.mapUiEffects.test {
                viewModel.onEvent(LandscapesUiEvents.LandscapeClicked(TEST_LANDSCAPE_MATRA.id))
                advanceUntilIdle()

                expectMostRecentItem() shouldBe MapUiEffects.UpdateCamera(
                    target = CameraTarget.Bounds(
                        TEST_OUTLINES.bounds.getValue(TEST_LANDSCAPE_MATRA.id).toLocations() +
                            listOf(TEST_DESTINATION_PEAK.location, TEST_DESTINATION_RIVER.location),
                    ),
                    contentPadding = ContentPadding.MAP_LANDSCAPES,
                )
            }
        }
    }

    @Test
    fun `Given selected landscape - When the same LandscapeClicked - Then selection is cleared`() {
        runTest {
            val viewModel = createOpenedViewModel(selectedLandscapeId = TEST_LANDSCAPE_MATRA.id)

            viewModel.onEvent(LandscapesUiEvents.LandscapeClicked(TEST_LANDSCAPE_MATRA.id))
            advanceUntilIdle()

            with(viewModel.uiState.value.mapUiState.landscapes.shouldNotBeNull()) {
                selectedLandscapeId.shouldBeNull()
                destinations shouldBe emptyList()
            }
            analyticsService.loggedEvents shouldBe emptyList()
        }
    }

    @Test
    fun `Given selected destination - When LandscapesShowAllClicked - Then selections are cleared and camera fits every landscape`() {
        runTest {
            val viewModel = createOpenedViewModel(selectedLandscapeId = TEST_LANDSCAPE_MATRA.id)
            viewModel.onEvent(LandscapesUiEvents.LandscapesDestinationClicked(TEST_DESTINATION_PEAK))
            advanceUntilIdle()

            viewModel.mapUiEffects.test {
                viewModel.onEvent(LandscapesUiEvents.LandscapesShowAllClicked)
                advanceUntilIdle()

                expectMostRecentItem() shouldBe MapUiEffects.UpdateCamera(
                    target = CameraTarget.Bounds(MapConstants.HUNGARY_BOUNDING_BOX.toLocations()),
                    contentPadding = ContentPadding.MAP_LANDSCAPES,
                )
            }
            with(viewModel.uiState.value.mapUiState.landscapes.shouldNotBeNull()) {
                selectedLandscapeId.shouldBeNull()
                selectedDestinationId.shouldBeNull()
            }
        }
    }

    @Test
    fun `Given selected destination - When another LandscapeClicked - Then destination selection is cleared`() {
        runTest {
            val viewModel = createOpenedViewModel(selectedLandscapeId = TEST_LANDSCAPE_MATRA.id)
            viewModel.onEvent(LandscapesUiEvents.LandscapesDestinationClicked(TEST_DESTINATION_PEAK))
            advanceUntilIdle()

            viewModel.onEvent(LandscapesUiEvents.LandscapeClicked(TEST_LANDSCAPE_BUKK.id))
            advanceUntilIdle()

            with(viewModel.uiState.value.mapUiState.landscapes.shouldNotBeNull()) {
                selectedLandscape shouldBe TEST_LANDSCAPE_BUKK
                selectedDestinationId.shouldBeNull()
            }
        }
    }

    @Test
    fun `Given selected landscape - When LandscapesDestinationClicked - Then destination is selected and camera centers on it`() {
        runTest {
            val viewModel = createOpenedViewModel(selectedLandscapeId = TEST_LANDSCAPE_MATRA.id)

            viewModel.mapUiEffects.test {
                viewModel.onEvent(LandscapesUiEvents.LandscapesDestinationClicked(TEST_DESTINATION_RIVER))
                advanceUntilIdle()

                expectMostRecentItem() shouldBe MapUiEffects.UpdateCamera(
                    target = CameraTarget.Center(TEST_DESTINATION_RIVER.location),
                    contentPadding = ContentPadding.MAP_LANDSCAPES_DESTINATION,
                )
            }
            viewModel.uiState.value.mapUiState.landscapes?.selectedDestination shouldBe TEST_DESTINATION_RIVER
            analyticsService.loggedEvents shouldBe listOf(
                AnalyticsEvent.LandscapesDestinationClicked(TEST_DESTINATION_RIVER.name),
            )
        }
    }

    @Test
    fun `Given selected destination - When LandscapesInfoWindowDismissed - Then selection is cleared`() {
        runTest {
            val viewModel = createOpenedViewModel(selectedLandscapeId = TEST_LANDSCAPE_MATRA.id)
            viewModel.onEvent(LandscapesUiEvents.LandscapesDestinationClicked(TEST_DESTINATION_PEAK))
            advanceUntilIdle()

            viewModel.onEvent(LandscapesUiEvents.LandscapesInfoWindowDismissed)

            with(viewModel.uiState.value.mapUiState.landscapes.shouldNotBeNull()) {
                selectedDestinationId.shouldBeNull()
                selectedLandscape shouldBe TEST_LANDSCAPE_MATRA
            }
        }
    }

    @Test
    fun `Given selected destination - When place details clicked - Then PlaceDetails shows the destination`() {
        runTest {
            val viewModel = createOpenedViewModel(selectedLandscapeId = TEST_LANDSCAPE_MATRA.id)
            viewModel.onEvent(LandscapesUiEvents.LandscapesDestinationClicked(TEST_DESTINATION_PEAK))
            advanceUntilIdle()
            analyticsService.loggedEvents.clear()

            viewModel.onEvent(LandscapesUiEvents.LandscapesInfoWindowPlaceDetailsClicked)
            advanceUntilIdle()

            val uiState = viewModel.uiState.value
            uiState.sheet shouldBe Sheet.PlaceDetails
            uiState.mapUiState.placeDetails
                .shouldBeInstanceOf<PlaceDetails.PlaceLoaded>()
                .place shouldBe TEST_DESTINATION_PEAK.toPlace()
            with(uiState.mapUiState.landscapes.shouldNotBeNull()) {
                selectedDestinationId.shouldBeNull()
            }
            analyticsService.loggedEvents shouldBe listOf(
                AnalyticsEvent.PlaceDetailsOpened(PlaceDetailsSource.LANDSCAPES),
            )
        }
    }

    @Test
    fun `Given selected trail destination - When route clicked - Then its bundled GPX opens and Landscapes closes`() {
        runTest {
            val viewModel = createOpenedViewModel(selectedLandscapeId = TEST_LANDSCAPE_BUKK.id)
            viewModel.onEvent(LandscapesUiEvents.LandscapesDestinationClicked(TEST_DESTINATION_TRAIL))
            advanceUntilIdle()
            analyticsService.loggedEvents.clear()

            viewModel.onEvent(LandscapesUiEvents.LandscapesInfoWindowRouteClicked)
            advanceUntilIdle()

            val uiState = viewModel.uiState.value
            uiState.sheet shouldBe Sheet.Gpx(TEST_GPX_DETAILS)
            uiState.mapUiState.gpxDetails shouldBe TEST_GPX_DETAILS
            uiState.mapUiState.landscapes.shouldBeNull()
            analyticsService.loggedEvents shouldBe listOf(
                AnalyticsEvent.DestinationEduTrailOpened(TEST_DESTINATION_TRAIL.name),
            )
        }
    }

    @Test
    fun `Given selected destination without route - When route clicked - Then nothing changes`() {
        runTest {
            val viewModel = createOpenedViewModel(selectedLandscapeId = TEST_LANDSCAPE_MATRA.id)
            viewModel.onEvent(LandscapesUiEvents.LandscapesDestinationClicked(TEST_DESTINATION_PEAK))
            advanceUntilIdle()
            val expected = viewModel.uiState.value
            analyticsService.loggedEvents.clear()

            viewModel.onEvent(LandscapesUiEvents.LandscapesInfoWindowRouteClicked)
            advanceUntilIdle()

            viewModel.uiState.value shouldBe expected
            analyticsService.loggedEvents shouldBe emptyList()
        }
    }

    @Test
    fun `Given swiped Landscapes sheet - When LandscapesResumeClicked - Then the sheet is shown again`() {
        runTest {
            val viewModel = createOpenedViewModel(selectedLandscapeId = TEST_LANDSCAPE_MATRA.id)
            viewModel.onEvent(MainUiEvents.SheetSwipeDismissed(Sheet.Landscapes))
            viewModel.uiState.value.sheet.shouldBeNull()

            viewModel.onEvent(LandscapesUiEvents.LandscapesResumeClicked)
            advanceUntilIdle()

            val uiState = viewModel.uiState.value
            uiState.sheet shouldBe Sheet.Landscapes
            uiState.mapUiState.landscapes?.selectedLandscape shouldBe TEST_LANDSCAPE_MATRA
        }
    }

    @Test
    fun `Given selected destination and swiped sheet - When LandscapesResumeClicked - Then camera centers on the destination`() {
        runTest {
            val viewModel = createOpenedViewModel(selectedLandscapeId = TEST_LANDSCAPE_MATRA.id)
            viewModel.onEvent(LandscapesUiEvents.LandscapesDestinationClicked(TEST_DESTINATION_PEAK))
            viewModel.onEvent(MainUiEvents.SheetSwipeDismissed(Sheet.Landscapes))
            advanceUntilIdle()

            viewModel.mapUiEffects.test {
                viewModel.onEvent(LandscapesUiEvents.LandscapesResumeClicked)
                advanceUntilIdle()

                expectMostRecentItem() shouldBe MapUiEffects.UpdateCamera(
                    target = CameraTarget.Center(TEST_DESTINATION_PEAK.location),
                    contentPadding = ContentPadding.MAP_LANDSCAPES_DESTINATION,
                )
            }
        }
    }

    @Test
    fun `Given Landscapes - When LandscapesCloseClicked - Then sheet and outlines are hidden and search bar is back`() {
        runTest {
            val viewModel = createOpenedViewModel(selectedLandscapeId = TEST_LANDSCAPE_MATRA.id)

            viewModel.onEvent(LandscapesUiEvents.LandscapesCloseClicked)

            val uiState = viewModel.uiState.value
            uiState.sheet.shouldBeNull()
            uiState.mapUiState.landscapes.shouldBeNull()
            uiState.mapUiState.hikingLayerVisible shouldBe true
            uiState.isSearchBarVisible shouldBe true
        }
    }

    private fun TestScope.createOpenedCategoriesViewModel(selectedCategoryId: String? = null): MainViewModel {
        val viewModel = createViewModel()
        viewModel.onEvent(LandscapesUiEvents.CategoriesShowClicked)
        advanceUntilIdle()
        selectedCategoryId?.let {
            viewModel.onEvent(LandscapesUiEvents.CategoryClicked(it))
            advanceUntilIdle()
        }
        analyticsService.loggedEvents.clear()
        return viewModel
    }

    @Test
    fun `When CategoriesShowClicked - Then the sheet lists every category without destinations`() {
        runTest {
            val viewModel = createViewModel()

            viewModel.onEvent(LandscapesUiEvents.CategoriesShowClicked)
            advanceUntilIdle()

            val uiState = viewModel.uiState.value
            uiState.sheet shouldBe Sheet.Landscapes
            uiState.mapUiState.hikingLayerVisible shouldBe false
            with(uiState.mapUiState.landscapes.shouldNotBeNull()) {
                grouping shouldBe LandscapesGrouping.CATEGORY
                categories shouldBe TEST_CATEGORIES
                selectedCategoryId.shouldBeNull()
                destinations shouldBe emptyList()
            }
            analyticsService.loggedEvents.last() shouldBe AnalyticsEvent.CategoriesOpened
        }
    }

    @Test
    fun `When CategoriesShowClicked - Then camera fits Hungary`() {
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.mapUiEffects.test {
                viewModel.onEvent(LandscapesUiEvents.CategoriesShowClicked)
                advanceUntilIdle()

                expectMostRecentItem() shouldBe MapUiEffects.UpdateCamera(
                    target = CameraTarget.Bounds(MapConstants.HUNGARY_BOUNDING_BOX.toLocations()),
                    contentPadding = ContentPadding.MAP_LANDSCAPES,
                )
            }
        }
    }

    @Test
    fun `Given Categories - When CategoryClicked - Then its destinations are revealed and camera fits them`() {
        runTest {
            val viewModel = createOpenedCategoriesViewModel()

            viewModel.mapUiEffects.test {
                viewModel.onEvent(LandscapesUiEvents.CategoryClicked(TEST_CHALLENGE_HIGHEST_PEAKS.id))
                advanceUntilIdle()

                expectMostRecentItem() shouldBe MapUiEffects.UpdateCamera(
                    target = CameraTarget.Bounds(listOf(TEST_DESTINATION_PEAK.location, TEST_DESTINATION_RIVER.location)),
                    contentPadding = ContentPadding.MAP_LANDSCAPES,
                )
            }
            with(viewModel.uiState.value.mapUiState.landscapes.shouldNotBeNull()) {
                selectedCategory shouldBe TEST_CHALLENGE_HIGHEST_PEAKS
                destinations shouldBe listOf(TEST_DESTINATION_PEAK, TEST_DESTINATION_RIVER)
            }
            analyticsService.loggedEvents shouldBe listOf(AnalyticsEvent.CategorySelected("highest_peaks"))
        }
    }

    @Test
    fun `Given Categories - When a single-destination CategoryClicked - Then camera centers on the destination`() {
        runTest {
            val viewModel = createOpenedCategoriesViewModel()

            viewModel.mapUiEffects.test {
                viewModel.onEvent(LandscapesUiEvents.CategoryClicked(TEST_CATEGORY_PEAK.id))
                advanceUntilIdle()

                expectMostRecentItem() shouldBe MapUiEffects.UpdateCamera(
                    target = CameraTarget.Center(TEST_DESTINATION_PEAK.location, zoom = 11.0),
                    contentPadding = ContentPadding.MAP_LANDSCAPES,
                )
            }
        }
    }

    @Test
    fun `Given selected category - When the same CategoryClicked - Then selection is cleared`() {
        runTest {
            val viewModel = createOpenedCategoriesViewModel(selectedCategoryId = TEST_CATEGORY_PEAK.id)

            viewModel.onEvent(LandscapesUiEvents.CategoryClicked(TEST_CATEGORY_PEAK.id))
            advanceUntilIdle()

            with(viewModel.uiState.value.mapUiState.landscapes.shouldNotBeNull()) {
                selectedCategoryId.shouldBeNull()
                destinations shouldBe emptyList()
            }
            analyticsService.loggedEvents shouldBe emptyList()
        }
    }

    @Test
    fun `Given selected category - When LandscapesShowAllClicked - Then category selection is cleared`() {
        runTest {
            val viewModel = createOpenedCategoriesViewModel(selectedCategoryId = TEST_CHALLENGE_HIGHEST_PEAKS.id)

            viewModel.onEvent(LandscapesUiEvents.LandscapesShowAllClicked)
            advanceUntilIdle()

            viewModel.uiState.value.mapUiState.landscapes.shouldNotBeNull().selectedCategoryId.shouldBeNull()
        }
    }
}
