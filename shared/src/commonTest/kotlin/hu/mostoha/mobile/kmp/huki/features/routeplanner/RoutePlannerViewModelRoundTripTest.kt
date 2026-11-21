package hu.mostoha.mobile.kmp.huki.features.routeplanner

import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import hu.mostoha.mobile.kmp.huki.model.analytics.AnalyticsEvent
import hu.mostoha.mobile.kmp.huki.model.analytics.RouteProfile
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.OsmType
import hu.mostoha.mobile.kmp.huki.model.domain.Place
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceCategory
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceSource
import hu.mostoha.mobile.kmp.huki.model.domain.RoundTripOptions
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlannerMode
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlannerProfile
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlannerSession
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlannerWaypoint
import hu.mostoha.mobile.kmp.huki.model.domain.WaypointType
import hu.mostoha.mobile.kmp.huki.model.mapper.toRoutePlannerWaypoint
import hu.mostoha.mobile.kmp.huki.model.network.GraphhopperPath
import hu.mostoha.mobile.kmp.huki.model.network.GraphhopperPoints
import hu.mostoha.mobile.kmp.huki.model.network.GraphhopperRouteResponse
import hu.mostoha.mobile.kmp.huki.model.network.NetworkError
import hu.mostoha.mobile.kmp.huki.model.network.NetworkResult
import hu.mostoha.mobile.kmp.huki.repository.FakeRoutePlannerRepository
import hu.mostoha.mobile.kmp.huki.repository.GeocodingRepository
import hu.mostoha.mobile.kmp.huki.repository.GpxRepository
import hu.mostoha.mobile.kmp.huki.repository.PlaceHistoryRepository
import hu.mostoha.mobile.kmp.huki.service.FakeAnalyticsService
import hu.mostoha.mobile.kmp.huki.service.FakeCrashlyticsService
import hu.mostoha.mobile.kmp.huki.service.LocationMonitoringService
import hu.mostoha.mobile.kmp.huki.util.RoutePlanConstants
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RoutePlannerViewModelRoundTripTest {

    private val testDispatcher = StandardTestDispatcher()
    private val locationMonitoringService = mock<LocationMonitoringService>()
    private val routePlannerRepository = FakeRoutePlannerRepository(NetworkResult.Success(ROUND_TRIP_RESPONSE))
    private val geocodingRepository = mock<GeocodingRepository>()
    private val placeHistoryRepository = mock<PlaceHistoryRepository>()
    private val gpxRepository = mock<GpxRepository>()
    private val analyticsService = FakeAnalyticsService()
    private val crashlyticsService = FakeCrashlyticsService()

    private val locationUpdates = MutableSharedFlow<Location>(extraBufferCapacity = 1)

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { locationMonitoringService.locationUpdates } returns locationUpdates
        everySuspend { locationMonitoringService.lastKnownLocation() } returns MY_LOCATION
        everySuspend { geocodingRepository.reverseGeocode(any()) } returns NetworkResult.Success(null)
        everySuspend { placeHistoryRepository.recordVisit(any()) } returns Unit
        everySuspend { gpxRepository.saveRoutePlan(any(), any(), any(), any()) } returns SAVED_GPX_URI
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(restoredSession: RoutePlannerSession? = null): RoutePlannerViewModel =
        RoutePlannerViewModel(
            locationMonitoringService,
            routePlannerRepository,
            geocodingRepository,
            placeHistoryRepository,
            gpxRepository,
            analyticsService,
            crashlyticsService,
            restoredSession = restoredSession,
            mode = RoutePlannerMode.ROUND_TRIP_WIZARD,
            random = Random(RANDOM_SEED),
        )

    @Test
    fun `Given the wizard - When my location arrives - Then a round trip of the default length is planned from it`() {
        runTest {
            val viewModel = createViewModel()

            testDispatcher.scheduler.advanceUntilIdle()

            val request = routePlannerRepository.lastRequest
            request?.waypoints shouldBe listOf(MY_LOCATION)
            request?.roundTrip?.distanceKm shouldBe RoutePlanConstants.ROUND_TRIP_DEFAULT_DISTANCE_KM
            val uiState = viewModel.uiState.value
            uiState.stops.size shouldBe 1
            uiState.routePlan?.waypoints shouldBe listOf(MY_LOCATION, MY_LOCATION)
            uiState.routePlanMarkers.map { it.type } shouldBe listOf(WaypointType.ROUND_TRIP)
            analyticsService.loggedEvents shouldContain AnalyticsEvent.RoundTripCreated(RouteProfile.ON_TRAILS)
        }
    }

    @Test
    fun `Given no location fix - When the wizard opens - Then no round trip is requested`() {
        runTest {
            everySuspend { locationMonitoringService.lastKnownLocation() } returns null
            val viewModel = createViewModel()

            testDispatcher.scheduler.advanceUntilIdle()

            routePlannerRepository.callCount shouldBe 0
            viewModel.uiState.value.isRoutePlanExpected shouldBe false
            viewModel.uiState.value.isRegenerateEnabled shouldBe false
        }
    }

    @Test
    fun `Given a planned round trip - When the distance changes - Then it is replanned with the same seed`() {
        runTest {
            val viewModel = createViewModel()
            testDispatcher.scheduler.advanceUntilIdle()
            val previous = viewModel.uiState.value.roundTrip

            viewModel.onEvent(RoutePlannerUiEvents.RoundTripDistanceChanged(15))
            testDispatcher.scheduler.advanceUntilIdle()

            val roundTrip = routePlannerRepository.lastRequest?.roundTrip
            roundTrip?.distanceKm shouldBe 15
            roundTrip?.seed shouldBe previous?.seed
            roundTrip?.heading shouldBe previous?.heading
            analyticsService.loggedEvents shouldContain AnalyticsEvent.RoundTripDistanceChanged(15)
        }
    }

    @Test
    fun `Given a distance out of range - When it changes - Then it is clamped`() {
        runTest {
            val viewModel = createViewModel()

            viewModel.onEvent(RoutePlannerUiEvents.RoundTripDistanceChanged(40))

            viewModel.uiState.value.roundTrip?.distanceKm shouldBe RoutePlanConstants.ROUND_TRIP_MAX_DISTANCE_KM
        }
    }

    @Test
    fun `Given a planned round trip - When the same distance is chosen - Then no new request is sent`() {
        runTest {
            val viewModel = createViewModel()
            testDispatcher.scheduler.advanceUntilIdle()
            val callCount = routePlannerRepository.callCount

            viewModel.onEvent(RoutePlannerUiEvents.RoundTripDistanceChanged(RoutePlanConstants.ROUND_TRIP_DEFAULT_DISTANCE_KM))
            testDispatcher.scheduler.advanceUntilIdle()

            routePlannerRepository.callCount shouldBe callCount
        }
    }

    @Test
    fun `Given a planned round trip - When regenerate is clicked - Then another loop of the same length is planned`() {
        runTest {
            val viewModel = createViewModel()
            testDispatcher.scheduler.advanceUntilIdle()
            val previous = viewModel.uiState.value.roundTrip

            viewModel.onEvent(RoutePlannerUiEvents.RoundTripRegenerateClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            val roundTrip = routePlannerRepository.lastRequest?.roundTrip
            roundTrip?.seed shouldNotBe previous?.seed
            roundTrip?.distanceKm shouldBe previous?.distanceKm
            routePlannerRepository.callCount shouldBe 2
            analyticsService.loggedEvents shouldContain AnalyticsEvent.RoundTripRegenerated
        }
    }

    @Test
    fun `Given a round trip loading - When regenerate is clicked again - Then no further loop is requested`() {
        runTest {
            val viewModel = createViewModel()
            testDispatcher.scheduler.advanceUntilIdle()
            viewModel.onEvent(RoutePlannerUiEvents.RoundTripRegenerateClicked)
            testDispatcher.scheduler.runCurrent()
            val loadingRoundTrip = viewModel.uiState.value.roundTrip

            viewModel.onEvent(RoutePlannerUiEvents.RoundTripRegenerateClicked)
            viewModel.onEvent(RoutePlannerUiEvents.RoundTripRegenerateClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.uiState.value.roundTrip shouldBe loadingRoundTrip
            routePlannerRepository.callCount shouldBe 2
        }
    }

    @Test
    fun `Given a planned round trip - When the profile changes - Then the seed is kept`() {
        runTest {
            val viewModel = createViewModel()
            testDispatcher.scheduler.advanceUntilIdle()
            val previous = viewModel.uiState.value.roundTrip

            viewModel.onEvent(RoutePlannerUiEvents.ProfileSelected(RoutePlannerProfile.BIKE))
            testDispatcher.scheduler.advanceUntilIdle()

            val request = routePlannerRepository.lastRequest
            request?.routeProfile shouldBe RoutePlannerProfile.BIKE
            request?.roundTrip shouldBe previous
        }
    }

    @Test
    fun `Given the wizard - When a place is picked from search - Then it replaces the start`() {
        runTest {
            val viewModel = createViewModel()
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.onEvent(RoutePlannerUiEvents.SearchPlaceAdded(place("Dobogókő")))
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.uiState.value.stops.map { it.location } shouldBe listOf(PLACE_LOCATION)
            routePlannerRepository.lastRequest?.waypoints shouldBe listOf(PLACE_LOCATION)
        }
    }

    @Test
    fun `Given the wizard - When the map is long tapped - Then the start moves there`() {
        runTest {
            val viewModel = createViewModel()
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.onEvent(RoutePlannerUiEvents.LocationAdded(PLACE_LOCATION))

            viewModel.uiState.value.stops.map { it.location } shouldBe listOf(PLACE_LOCATION)
        }
    }

    @Test
    fun `Given the wizard - When the start is removed or a stop added - Then the start stays the only stop`() {
        runTest {
            val viewModel = createViewModel()
            testDispatcher.scheduler.advanceUntilIdle()
            val start = viewModel.uiState.value.stops.single()

            viewModel.onEvent(RoutePlannerUiEvents.WaypointRemoved(start.id))
            viewModel.onEvent(RoutePlannerUiEvents.AddEmptyStopClicked)
            viewModel.onEvent(RoutePlannerUiEvents.RoundTripClicked)

            viewModel.uiState.value.stops shouldBe listOf(start)
        }
    }

    @Test
    fun `Given a failing round trip - When it is planned - Then the round trip failure is logged`() {
        runTest {
            routePlannerRepository.result = NetworkResult.Error(NetworkError.NOT_FOUND)
            createViewModel()

            testDispatcher.scheduler.advanceUntilIdle()

            analyticsService.loggedEvents shouldContain AnalyticsEvent.RoundTripFailed
            analyticsService.loggedEvents shouldNotContain AnalyticsEvent.RoutePlanFailed
        }
    }

    @Test
    fun `Given a planned round trip - When it is saved - Then both ends are named after the start`() {
        runTest {
            val viewModel = createViewModel()
            testDispatcher.scheduler.advanceUntilIdle()
            viewModel.onEvent(RoutePlannerUiEvents.SearchPlaceAdded(place("Dobogókő")))
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.onEvent(RoutePlannerUiEvents.SaveRouteClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            verifySuspend {
                gpxRepository.saveRoutePlan(any(), listOf("Dobogókő", "Dobogókő"), RoutePlannerProfile.ON_TRAILS, any())
            }
            analyticsService.loggedEvents shouldContain AnalyticsEvent.RoundTripSaved(RouteProfile.ON_TRAILS)
        }
    }

    @Test
    fun `Given a restored wizard session - When the wizard opens - Then the same loop is requested again`() {
        runTest {
            val roundTrip = RoundTripOptions(distanceKm = 17, seed = 42, heading = 90)
            val session = RoutePlannerSession(
                routeProfile = RoutePlannerProfile.BIKE,
                waypoints = listOf(PLACE_LOCATION.toRoutePlannerWaypoint(), RoutePlannerWaypoint()),
                mode = RoutePlannerMode.ROUND_TRIP_WIZARD,
                roundTrip = roundTrip,
            )
            val viewModel = createViewModel(restoredSession = session)

            testDispatcher.scheduler.advanceUntilIdle()

            routePlannerRepository.lastRequest?.roundTrip shouldBe roundTrip
            viewModel.uiState.value.session shouldBe session
        }
    }

    private fun place(name: String, location: Location = PLACE_LOCATION): Place =
        Place(
            osmId = name,
            location = location,
            name = name,
            placeSource = PlaceSource.SEARCH_AUTOCOMPLETE,
            address = null,
            placeCategory = PlaceCategory.PEAK,
            osmType = OsmType.NODE,
        )

    companion object {
        private const val RANDOM_SEED = 1
        private const val SAVED_GPX_URI = "gpx/routeplanner/Dobogoko roundtrip.gpx"
        private val MY_LOCATION = Location(47.5, 19.0)
        private val PLACE_LOCATION = Location(47.6, 18.9)
        private val ROUND_TRIP_RESPONSE = GraphhopperRouteResponse(
            paths = listOf(
                GraphhopperPath(
                    distance = 10200.0,
                    time = 3_600_000,
                    ascend = 240.0,
                    descend = 240.0,
                    points = GraphhopperPoints(
                        coordinates = listOf(listOf(19.0, 47.5, 300.0), listOf(19.05, 47.55, 420.0), listOf(19.0, 47.5, 300.0)),
                    ),
                    snappedWaypoints = GraphhopperPoints(
                        coordinates = listOf(listOf(19.0, 47.5), listOf(19.05, 47.55), listOf(19.0, 47.5)),
                    ),
                ),
            ),
        )
    }
}
