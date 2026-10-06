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
import hu.mostoha.mobile.kmp.huki.data.TEST_OKT_FULL_TRAIL
import hu.mostoha.mobile.kmp.huki.data.TEST_OKT_LOCATIONS
import hu.mostoha.mobile.kmp.huki.data.TEST_OKT_SECTION_01
import hu.mostoha.mobile.kmp.huki.data.TEST_OKT_SECTION_02
import hu.mostoha.mobile.kmp.huki.data.TEST_OKT_STAMP
import hu.mostoha.mobile.kmp.huki.data.TEST_OKT_TRAIL
import hu.mostoha.mobile.kmp.huki.features.map.MapUiEffects
import hu.mostoha.mobile.kmp.huki.model.analytics.AnalyticsEvent
import hu.mostoha.mobile.kmp.huki.model.analytics.BlueTrail
import hu.mostoha.mobile.kmp.huki.model.analytics.PlaceDetailsSource
import hu.mostoha.mobile.kmp.huki.model.domain.Alert
import hu.mostoha.mobile.kmp.huki.model.domain.CameraTarget
import hu.mostoha.mobile.kmp.huki.model.domain.ContentPadding
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.OktMarkerType
import hu.mostoha.mobile.kmp.huki.model.domain.OktType
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceDetails
import hu.mostoha.mobile.kmp.huki.model.domain.Sheet
import hu.mostoha.mobile.kmp.huki.model.domain.UserPreferences
import hu.mostoha.mobile.kmp.huki.model.mapper.toOktMarker
import hu.mostoha.mobile.kmp.huki.model.network.LocationIqPlace
import hu.mostoha.mobile.kmp.huki.model.network.NetworkResult
import hu.mostoha.mobile.kmp.huki.repository.DefaultMapCameraStore
import hu.mostoha.mobile.kmp.huki.repository.DestinationRepository
import hu.mostoha.mobile.kmp.huki.repository.FakeOktRepository
import hu.mostoha.mobile.kmp.huki.repository.GeocodingRepository
import hu.mostoha.mobile.kmp.huki.repository.GpxRepository
import hu.mostoha.mobile.kmp.huki.repository.OktRepository
import hu.mostoha.mobile.kmp.huki.repository.PlaceHistoryRepository
import hu.mostoha.mobile.kmp.huki.repository.SettingsRepository
import hu.mostoha.mobile.kmp.huki.repository.WhatsNewRepository
import hu.mostoha.mobile.kmp.huki.service.FakeAnalyticsService
import hu.mostoha.mobile.kmp.huki.service.FakeCrashlyticsService
import hu.mostoha.mobile.kmp.huki.service.LocationMonitoringService
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
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
class MainViewModelOktTest {

    private val testDispatcher = StandardTestDispatcher()
    private val analyticsService = FakeAnalyticsService()
    private val crashlyticsService = FakeCrashlyticsService()
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
        oktRepository: OktRepository = FakeOktRepository(),
        lastKnownLocation: Location? = null,
        locationUpdates: MutableSharedFlow<Location> = MutableSharedFlow(),
    ): MainViewModel =
        MainViewModel(
            permissionsController = createPermissionControllerMock(
                allow = setOf(Permission.LOCATION),
                granted = setOf(Permission.LOCATION),
            ),
            gpxRepository = mock<GpxRepository>(),
            placeHistoryRepository = mock<PlaceHistoryRepository>(MockMode.autoUnit),
            destinationRepository = mock<DestinationRepository>(MockMode.autoUnit),
            geocodingRepository = geocodingRepository,
            locationMonitoringService = object : LocationMonitoringService {
                override val locationUpdates: SharedFlow<Location> = locationUpdates
                override suspend fun lastKnownLocation(): Location? = lastKnownLocation
            },
            settingsRepository = mock<SettingsRepository>(MockMode.autoUnit) {
                every { settings } returns flowOf(UserPreferences.DEFAULTS)
            },
            mapCameraStore = DefaultMapCameraStore(),
            whatsNewRepository = mock<WhatsNewRepository>(MockMode.autoUnit) {
                everySuspend { shouldShowWhatsNew() } returns false
            },
            oktRepository = oktRepository,
            analyticsService = analyticsService,
            crashlyticsService = crashlyticsService,
            defaultDispatcher = testDispatcher,
        )

    private fun TestScope.createOpenedViewModel(
        lastKnownLocation: Location? = null,
        locationUpdates: MutableSharedFlow<Location> = MutableSharedFlow(),
    ): MainViewModel {
        val viewModel = createViewModel(lastKnownLocation = lastKnownLocation, locationUpdates = locationUpdates)
        viewModel.onEvent(OktUiEvents.OktTypeClicked(OktType.OKT))
        advanceUntilIdle()
        analyticsService.loggedEvents.clear()
        return viewModel
    }

    @Test
    fun `When OktTypeClicked - Then Okt sheet shows the full trail selected`() {
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEvent(OktUiEvents.OktTypeClicked(OktType.OKT))
            advanceUntilIdle()

            val uiState = viewModel.uiState.value
            uiState.sheet shouldBe Sheet.Okt
            uiState.isOktLoading shouldBe false
            with(uiState.mapUiState.okt.shouldNotBeNull()) {
                type shouldBe OktType.OKT
                sections.map { it.id } shouldBe listOf(TEST_OKT_FULL_TRAIL.id, "OKT-01", "OKT-02")
                selectedSectionId shouldBe TEST_OKT_FULL_TRAIL.id
                selectedLine shouldBe baseLine
                markers.map { it.type } shouldBe listOf(OktMarkerType.START, OktMarkerType.END)
            }
            analyticsService.loggedEvents shouldBe listOf(AnalyticsEvent.OktOpened(BlueTrail.OKT))
        }
    }

    @Test
    fun `When OktTypeClicked - Then camera fits the trail`() {
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.mapUiEffects.test {
                viewModel.onEvent(OktUiEvents.OktTypeClicked(OktType.OKT))
                advanceUntilIdle()

                val effect = expectMostRecentItem().shouldBeInstanceOf<MapUiEffects.UpdateCamera>()
                effect.contentPadding shouldBe ContentPadding.MAP_OKT
                effect.target shouldBe CameraTarget.Bounds(viewModel.uiState.value.mapUiState.okt!!.baseLine.bounds)
            }
        }
    }

    @Test
    fun `Given failing trail load - When OktTypeClicked - Then alert is shown and the failure is recorded`() {
        runTest {
            val exception = IllegalStateException("Broken GPX")
            val viewModel = createViewModel(oktRepository = FakeOktRepository(exception = exception))
            advanceUntilIdle()

            viewModel.onEvent(OktUiEvents.OktTypeClicked(OktType.OKT))
            advanceUntilIdle()

            val uiState = viewModel.uiState.value
            uiState.alert shouldBe Alert(SharedRes.strings.okt_header_title, SharedRes.strings.okt_loading_error)
            uiState.isOktLoading shouldBe false
            uiState.mapUiState.okt.shouldBeNull()
            analyticsService.loggedEvents shouldBe listOf(
                AnalyticsEvent.OktOpened(BlueTrail.OKT),
                AnalyticsEvent.OktLoadFailed,
            )
            crashlyticsService.recordedExceptions shouldBe listOf(exception)
        }
    }

    @Test
    fun `Given Okt - When OktSectionClicked - Then the section is selected with its markers`() {
        runTest {
            val viewModel = createOpenedViewModel()

            viewModel.onEvent(OktUiEvents.OktSectionClicked(TEST_OKT_SECTION_01.id))
            advanceUntilIdle()

            with(viewModel.uiState.value.mapUiState.okt.shouldNotBeNull()) {
                selectedSectionId shouldBe TEST_OKT_SECTION_01.id
                selectedLine.id shouldBe "okt_section_OKT-01"
                markers.map { it.type } shouldBe listOf(
                    OktMarkerType.START,
                    OktMarkerType.STAMP,
                    OktMarkerType.END,
                )
                selectedSection.stamps.map { it.tag } shouldBe listOf(TEST_OKT_STAMP.tag)
            }
            analyticsService.loggedEvents shouldBe listOf(AnalyticsEvent.OktSectionSelected(TEST_OKT_SECTION_01.id))
        }
    }

    @Test
    fun `Given Okt - When OktSectionStartClicked - Then sheet is hidden and camera uses the started padding`() {
        runTest {
            val viewModel = createOpenedViewModel()

            viewModel.mapUiEffects.test {
                viewModel.onEvent(OktUiEvents.OktSectionStartClicked(TEST_OKT_SECTION_01.id))
                advanceUntilIdle()

                val effect = expectMostRecentItem().shouldBeInstanceOf<MapUiEffects.UpdateCamera>()
                effect.contentPadding shouldBe ContentPadding.MAP_OKT_STARTED
            }
            viewModel.uiState.value.sheet shouldBe null
            viewModel.uiState.value.mapUiState.okt?.selectedSectionId shouldBe TEST_OKT_SECTION_01.id
            analyticsService.loggedEvents shouldBe listOf(AnalyticsEvent.OktSectionStarted(TEST_OKT_SECTION_01.id))
        }
    }

    @Test
    fun `Given started section - When OktResumeClicked - Then Okt sheet is shown again`() {
        runTest {
            val viewModel = createOpenedViewModel()
            viewModel.onEvent(OktUiEvents.OktSectionStartClicked(TEST_OKT_SECTION_01.id))
            advanceUntilIdle()

            viewModel.onEvent(OktUiEvents.OktResumeClicked)
            advanceUntilIdle()

            viewModel.uiState.value.sheet shouldBe Sheet.Okt
            viewModel.uiState.value.mapUiState.okt?.selectedSectionId shouldBe TEST_OKT_SECTION_01.id
        }
    }

    @Test
    fun `Given Okt sheet - When SheetSwipeDismissed - Then camera refits with the started padding`() {
        runTest {
            val viewModel = createOpenedViewModel()

            viewModel.mapUiEffects.test {
                viewModel.onEvent(MainUiEvents.SheetSwipeDismissed(Sheet.Okt))
                advanceUntilIdle()

                val effect = expectMostRecentItem().shouldBeInstanceOf<MapUiEffects.UpdateCamera>()
                effect.contentPadding shouldBe ContentPadding.MAP_OKT_STARTED
                effect.target shouldBe CameraTarget.Bounds(viewModel.uiState.value.mapUiState.okt!!.selectedLine.bounds)
            }
            viewModel.uiState.value.sheet shouldBe null
        }
    }

    @Test
    fun `Given Okt - When OktMarkerClicked - Then camera centers the marker without changing the zoom`() {
        runTest {
            val viewModel = createOpenedViewModel()
            val marker = TEST_OKT_STAMP.toOktMarker()

            viewModel.mapUiEffects.test {
                viewModel.onEvent(OktUiEvents.OktMarkerClicked(marker))
                advanceUntilIdle()

                val effect = expectMostRecentItem().shouldBeInstanceOf<MapUiEffects.UpdateCamera>()
                effect.target shouldBe CameraTarget.Center(marker.location)
                effect.contentPadding shouldBe ContentPadding.MAP_OKT
            }
        }
    }

    @Test
    fun `Given Okt - When OktLineClicked next to a section - Then the nearest section is selected`() {
        runTest {
            val viewModel = createOpenedViewModel()

            viewModel.onEvent(OktUiEvents.OktLineClicked(TEST_OKT_LOCATIONS[3]))
            advanceUntilIdle()

            viewModel.uiState.value.mapUiState.okt?.selectedSectionId shouldBe TEST_OKT_SECTION_02.id
            analyticsService.loggedEvents shouldBe listOf(AnalyticsEvent.OktLineClicked)
        }
    }

    @Test
    fun `Given user on the trail - When OktMarkerClicked - Then info window has the distance from the user`() {
        runTest {
            val viewModel = createOpenedViewModel(lastKnownLocation = TEST_OKT_LOCATIONS[0])
            val marker = TEST_OKT_STAMP.toOktMarker()

            viewModel.onEvent(OktUiEvents.OktMarkerClicked(marker))
            advanceUntilIdle()

            with(viewModel.uiState.value.mapUiState.okt.shouldNotBeNull()) {
                selectedMarkerId shouldBe marker.id
                with(infoWindow.shouldNotBeNull()) {
                    this.marker shouldBe marker
                    distance.shouldNotBeNull()
                    travelTime.shouldNotBeNull()
                }
            }
            analyticsService.loggedEvents shouldBe listOf(AnalyticsEvent.OktStampClicked)
        }
    }

    @Test
    fun `Given open info window - When the user walks along the trail - Then its distance is refreshed`() {
        runTest {
            val locationUpdates = MutableSharedFlow<Location>()
            val viewModel = createOpenedViewModel(
                lastKnownLocation = TEST_OKT_LOCATIONS[0],
                locationUpdates = locationUpdates,
            )
            viewModel.onEvent(OktUiEvents.OktMarkerClicked(TEST_OKT_STAMP.toOktMarker()))
            advanceUntilIdle()
            val initialDistance = viewModel.uiState.value.mapUiState.okt?.infoWindow?.distance.shouldNotBeNull()

            val halfwayToStamp = Location(
                latitude = (TEST_OKT_LOCATIONS[0].latitude + TEST_OKT_STAMP.location.latitude) / 2,
                longitude = (TEST_OKT_LOCATIONS[0].longitude + TEST_OKT_STAMP.location.longitude) / 2,
            )
            locationUpdates.emit(halfwayToStamp)
            advanceUntilIdle()

            val actualDistance = viewModel.uiState.value.mapUiState.okt?.infoWindow?.distance.shouldNotBeNull()
            actualDistance shouldNotBe initialDistance
        }
    }

    @Test
    fun `Given user far from the trail - When OktMarkerClicked - Then info window has no distance`() {
        runTest {
            val viewModel = createOpenedViewModel(lastKnownLocation = Location(47.4979, 19.0402))

            viewModel.onEvent(OktUiEvents.OktMarkerClicked(TEST_OKT_STAMP.toOktMarker()))
            advanceUntilIdle()

            with(viewModel.uiState.value.mapUiState.okt?.infoWindow.shouldNotBeNull()) {
                distance.shouldBeNull()
                travelTime.shouldBeNull()
            }
        }
    }

    @Test
    fun `Given info window - When OktInfoWindowDismissed - Then info window is removed`() {
        runTest {
            val viewModel = createOpenedViewModel()
            viewModel.onEvent(OktUiEvents.OktMarkerClicked(TEST_OKT_STAMP.toOktMarker()))
            advanceUntilIdle()

            viewModel.onEvent(OktUiEvents.OktInfoWindowDismissed)
            advanceUntilIdle()

            with(viewModel.uiState.value.mapUiState.okt.shouldNotBeNull()) {
                selectedMarkerId.shouldBeNull()
                infoWindow.shouldBeNull()
            }
        }
    }

    @Test
    fun `Given info window - When OktInfoWindowPlaceDetailsClicked - Then Place Details of the stamp is shown`() {
        runTest {
            val viewModel = createOpenedViewModel()
            viewModel.onEvent(OktUiEvents.OktMarkerClicked(TEST_OKT_STAMP.toOktMarker()))
            advanceUntilIdle()
            analyticsService.loggedEvents.clear()

            viewModel.onEvent(OktUiEvents.OktInfoWindowPlaceDetailsClicked)
            advanceUntilIdle()

            val uiState = viewModel.uiState.value
            uiState.sheet shouldBe Sheet.PlaceDetails
            uiState.mapUiState.placeDetails?.location shouldBe TEST_OKT_STAMP.location
            uiState.mapUiState.okt?.infoWindow.shouldBeNull()
            analyticsService.loggedEvents.first() shouldBe AnalyticsEvent.PlaceDetailsOpened(PlaceDetailsSource.OKT_STAMP)
        }
    }

    @Test
    fun `Given Okt - When OktSectionReverseClicked - Then the section is selected and reversed`() {
        runTest {
            val viewModel = createOpenedViewModel()

            viewModel.onEvent(OktUiEvents.OktSectionReverseClicked(TEST_OKT_SECTION_01.id))
            advanceUntilIdle()

            with(viewModel.uiState.value.mapUiState.okt.shouldNotBeNull()) {
                selectedSectionId shouldBe TEST_OKT_SECTION_01.id
                isReversed(TEST_OKT_SECTION_01.id) shouldBe true
            }
            analyticsService.loggedEvents shouldBe listOf(AnalyticsEvent.OktSectionReversed(TEST_OKT_SECTION_01.id))
        }
    }

    @Test
    fun `Given reversed section - When OktSectionReverseClicked again - Then the direction is restored`() {
        runTest {
            val viewModel = createOpenedViewModel()
            viewModel.onEvent(OktUiEvents.OktSectionReverseClicked(TEST_OKT_SECTION_01.id))
            advanceUntilIdle()

            viewModel.onEvent(OktUiEvents.OktSectionReverseClicked(TEST_OKT_SECTION_01.id))
            advanceUntilIdle()

            viewModel.uiState.value.mapUiState.okt?.isReversed(TEST_OKT_SECTION_01.id) shouldBe false
        }
    }

    @Test
    fun `Given Okt - When OktSectionWebsiteClicked - Then the section website is opened`() {
        runTest {
            val viewModel = createOpenedViewModel()

            viewModel.mainUiEffects.test {
                viewModel.onEvent(OktUiEvents.OktSectionWebsiteClicked(TEST_OKT_SECTION_01.id))

                awaitItem() shouldBe MainUiEffects.OpenUrl("https://www.kektura.hu/okt-szakasz/okt-01")
            }
            analyticsService.loggedEvents shouldBe listOf(AnalyticsEvent.OktSectionLinkClicked(TEST_OKT_SECTION_01.id))
        }
    }

    @Test
    fun `Given Okt - When OktCloseClicked - Then Okt is removed from the map`() {
        runTest {
            val viewModel = createOpenedViewModel()

            viewModel.onEvent(OktUiEvents.OktCloseClicked)
            advanceUntilIdle()

            viewModel.uiState.value.sheet shouldBe null
            viewModel.uiState.value.mapUiState.okt.shouldBeNull()
            analyticsService.loggedEvents shouldBe listOf(AnalyticsEvent.OktClosed)
        }
    }

    @Test
    fun `Given Okt - When another sheet replaces it - Then Okt stays on the map`() {
        runTest {
            val viewModel = createOpenedViewModel()

            viewModel.onEvent(MainUiEvents.MapLongClicked(TEST_OKT_TRAIL.locations[1]))
            advanceUntilIdle()

            viewModel.uiState.value.sheet shouldBe Sheet.PlaceDetails
            viewModel.uiState.value.mapUiState.placeDetails.shouldBeInstanceOf<PlaceDetails.Unresolved>()
            viewModel.uiState.value.mapUiState.okt.shouldNotBeNull()
        }
    }
}
