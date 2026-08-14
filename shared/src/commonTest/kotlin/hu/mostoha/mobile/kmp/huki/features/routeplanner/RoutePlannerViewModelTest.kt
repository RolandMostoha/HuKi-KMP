package hu.mostoha.mobile.kmp.huki.features.routeplanner

import app.cash.turbine.Event
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import dev.icerock.moko.resources.desc.RawStringDesc
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.exactly
import dev.mokkery.verifySuspend
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.analytics.AnalyticsEvent
import hu.mostoha.mobile.kmp.huki.model.analytics.RouteProfile
import hu.mostoha.mobile.kmp.huki.model.domain.Destination
import hu.mostoha.mobile.kmp.huki.model.domain.DestinationType
import hu.mostoha.mobile.kmp.huki.model.domain.GpxWaypoint
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.OsmType
import hu.mostoha.mobile.kmp.huki.model.domain.Place
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceCategory
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceSource
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlannerProfile
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlannerWaypoint
import hu.mostoha.mobile.kmp.huki.model.domain.WaypointType
import hu.mostoha.mobile.kmp.huki.model.mapper.toInfoViewData
import hu.mostoha.mobile.kmp.huki.model.mapper.toRoutePlanInfoViewData
import hu.mostoha.mobile.kmp.huki.model.network.GraphhopperPath
import hu.mostoha.mobile.kmp.huki.model.network.GraphhopperPoints
import hu.mostoha.mobile.kmp.huki.model.network.GraphhopperRouteResponse
import hu.mostoha.mobile.kmp.huki.model.network.LocationIqPlace
import hu.mostoha.mobile.kmp.huki.model.network.NetworkError
import hu.mostoha.mobile.kmp.huki.model.network.NetworkResult
import hu.mostoha.mobile.kmp.huki.repository.GeocodingRepository
import hu.mostoha.mobile.kmp.huki.repository.GpxRepository
import hu.mostoha.mobile.kmp.huki.repository.PlaceHistoryRepository
import hu.mostoha.mobile.kmp.huki.repository.RoutePlannerRepository
import hu.mostoha.mobile.kmp.huki.service.FakeAnalyticsService
import hu.mostoha.mobile.kmp.huki.service.FakeCrashlyticsService
import hu.mostoha.mobile.kmp.huki.service.LocationMonitoringService
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RoutePlannerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val locationMonitoringService = mock<LocationMonitoringService>()
    private val routePlannerRepository = mock<RoutePlannerRepository>()
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
        everySuspend { routePlannerRepository.getRoutePlan(any(), any()) } returns
            NetworkResult.Success(ROUTE_RESPONSE)
        everySuspend { geocodingRepository.reverseGeocode(any()) } returns NetworkResult.Success(null)
        everySuspend { placeHistoryRepository.recordVisit(any()) } returns Unit
        everySuspend { gpxRepository.saveRoutePlan(any(), any(), any()) } returns SAVED_GPX_URI
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): RoutePlannerViewModel {
        val viewModel = RoutePlannerViewModel(
            locationMonitoringService,
            routePlannerRepository,
            geocodingRepository,
            placeHistoryRepository,
            gpxRepository,
            analyticsService,
            crashlyticsService,
        )
        testDispatcher.scheduler.runCurrent()
        return viewModel
    }

    private suspend fun ReceiveTurbine<RoutePlannerUiEffects>.emittedEffects(): List<RoutePlannerUiEffects> = cancelAndConsumeRemainingEvents().mapNotNull { (it as? Event.Item)?.value }

    private fun List<RoutePlannerWaypoint>.names(): List<String?> =
        map { waypoint ->
            when (val name = waypoint.name) {
                null -> null
                is RawStringDesc -> name.string
                else -> MY_LOCATION_NAME
            }
        }

    private fun createViewModelWithMaxStops(): RoutePlannerViewModel {
        val viewModel = createViewModel()
        repeat(RoutePlannerUiState.MAX_WAYPOINT_COUNT - 1) { index ->
            viewModel.onEvent(RoutePlannerUiEvents.PlaceAdded(place("Stop $index")))
        }
        return viewModel
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

    private fun destination(name: String): Destination =
        Destination(
            osmId = name,
            name = name,
            town = "Pilisszentkereszt",
            type = DestinationType.WATERFALL,
            location = DESTINATION_LOCATION,
            description = SharedRes.strings.route_planner_title,
            popularity = 5,
        )

    private fun locationIqPlace(name: String): LocationIqPlace =
        LocationIqPlace(
            placeId = name,
            osmId = name,
            osmType = "node",
            licence = "",
            lat = 47.12345,
            lon = 18.98765,
            displayName = name,
            displayPlace = name,
        )

    @Test
    fun `Given default state, When view model init, Then my location and an empty stop are shown`() {
        runTest {
            val viewModel = createViewModel()

            val uiState = viewModel.uiState.value

            uiState.routeProfile shouldBe RoutePlannerProfile.ON_TRAILS
            uiState.waypoints.size shouldBe 3
            uiState.stops.size shouldBe 2
            uiState.stops.first().location shouldBe MY_LOCATION
            uiState.stops.last().location shouldBe null
            uiState.isRoundTrip shouldBe false
            uiState.isRoundTripEnabled shouldBe true
            uiState.isSaveEnabled shouldBe false
        }
    }

    @Test
    fun `Given no known location, When view model init, Then the my location stop stays empty`() {
        runTest {
            everySuspend { locationMonitoringService.lastKnownLocation() } returns null

            val viewModel = createViewModel()

            val myLocationStop = viewModel.uiState.value.stops.first()
            myLocationStop.name shouldNotBe null
            myLocationStop.location shouldBe null
        }
    }

    @Test
    fun `Given the planner is opened with a place, When place added, Then it follows my location`() {
        runTest {
            val viewModel = createViewModel()

            viewModel.onEvent(RoutePlannerUiEvents.PlaceAdded(place("Dobogókő")))

            viewModel.uiState.value.stops.names() shouldBe listOf(MY_LOCATION_NAME, "Dobogókő")
        }
    }

    @Test
    fun `Given my location is far away, When the planner is opened with a place, Then the start stop is emptied`() {
        runTest {
            val viewModel = createViewModel()

            viewModel.onEvent(RoutePlannerUiEvents.PlaceAdded(place("Dobogókő", FAR_PLACE_LOCATION)))

            val stops = viewModel.uiState.value.stops
            stops.names() shouldBe listOf(null, "Dobogókő")
            stops.first().location.shouldBeNull()
        }
    }

    @Test
    fun `Given a far place added before the fix, When the first fix arrives, Then the start stop stays empty`() {
        runTest {
            everySuspend { locationMonitoringService.lastKnownLocation() } returns null
            val viewModel = createViewModel()
            viewModel.onEvent(RoutePlannerUiEvents.PlaceAdded(place("Dobogókő", FAR_PLACE_LOCATION)))

            locationUpdates.emit(MY_LOCATION)
            testDispatcher.scheduler.advanceUntilIdle()

            val stops = viewModel.uiState.value.stops
            stops.names() shouldBe listOf(null, "Dobogókő")
            stops.first().location.shouldBeNull()
        }
    }

    @Test
    fun `Given my location is far away, When my location is added explicitly, Then it is kept`() {
        runTest {
            val viewModel = createViewModel()
            viewModel.onEvent(RoutePlannerUiEvents.PlaceAdded(place("Dobogókő", FAR_PLACE_LOCATION)))

            viewModel.onEvent(RoutePlannerUiEvents.MyLocationAdded)
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.uiState.value.stops.first().location shouldBe MY_LOCATION
        }
    }

    @Test
    fun `Given shortest route profile selected, When event received, Then profile is updated`() {
        runTest {
            val viewModel = createViewModel()

            viewModel.onEvent(RoutePlannerUiEvents.ProfileSelected(RoutePlannerProfile.SHORTEST_ROUTE))

            viewModel.uiState.value.routeProfile shouldBe RoutePlannerProfile.SHORTEST_ROUTE
        }
    }

    @Test
    fun `Given empty stops, When place added twice, Then my location is kept and stops are appended`() {
        runTest {
            val viewModel = createViewModel()

            viewModel.onEvent(RoutePlannerUiEvents.PlaceAdded(place("Dobogókő")))
            viewModel.onEvent(RoutePlannerUiEvents.PlaceAdded(place("Ram-hegy")))

            val uiState = viewModel.uiState.value

            uiState.stops.names() shouldBe listOf(MY_LOCATION_NAME, "Dobogókő", "Ram-hegy")
            uiState.waypoints.last().location shouldBe null
        }
    }

    @Test
    fun `Given an empty stop, When location added, Then it is filled with the formatted coordinates`() {
        runTest {
            val viewModel = createViewModel()

            viewModel.onEvent(RoutePlannerUiEvents.LocationAdded(Location(47.12345, 18.98765)))

            val uiState = viewModel.uiState.value
            uiState.stops.names() shouldBe listOf(MY_LOCATION_NAME, "(47.12345, 18.98765)")
            uiState.stops.last().location shouldBe Location(47.12345, 18.98765)
            analyticsService.loggedEvents shouldContain AnalyticsEvent.RoutePlanWaypointLongTapped
        }
    }

    @Test
    fun `Given a geocoded location, When location added, Then the coordinates are replaced by the place name`() {
        runTest {
            val location = Location(47.12345, 18.98765)
            everySuspend { geocodingRepository.reverseGeocode(location) } returns
                NetworkResult.Success(locationIqPlace("Dobogókő"))
            val viewModel = createViewModel()

            viewModel.onEvent(RoutePlannerUiEvents.LocationAdded(location))
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.uiState.value.stops.names() shouldBe listOf(MY_LOCATION_NAME, "Dobogókő")
        }
    }

    @Test
    fun `Given a failing geocode, When location added, Then the coordinates are kept`() {
        runTest {
            everySuspend { geocodingRepository.reverseGeocode(any()) } returns
                NetworkResult.Error(NetworkError.NO_INTERNET)
            val viewModel = createViewModel()

            viewModel.onEvent(RoutePlannerUiEvents.LocationAdded(Location(47.12345, 18.98765)))
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.uiState.value.stops.names() shouldBe listOf(MY_LOCATION_NAME, "(47.12345, 18.98765)")
        }
    }

    @Test
    fun `Given a pending geocode, When the waypoint is removed, Then the resolved name is dropped`() {
        runTest {
            val location = Location(47.12345, 18.98765)
            everySuspend { geocodingRepository.reverseGeocode(location) } returns
                NetworkResult.Success(locationIqPlace("Dobogókő"))
            val viewModel = createViewModel()
            viewModel.onEvent(RoutePlannerUiEvents.LocationAdded(location))
            val addedStop = viewModel.uiState.value.stops.last()

            viewModel.onEvent(RoutePlannerUiEvents.WaypointRemoved(addedStop.id))
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.uiState.value.stops.names() shouldBe listOf(MY_LOCATION_NAME, null)
        }
    }

    @Test
    fun `Given all stops filled, When location added, Then it is appended as a new stop`() {
        runTest {
            val viewModel = createViewModel()
            viewModel.onEvent(RoutePlannerUiEvents.PlaceAdded(place("Dobogókő")))

            viewModel.onEvent(RoutePlannerUiEvents.LocationAdded(Location(47.12345, 18.98765)))

            val uiState = viewModel.uiState.value
            uiState.stops.names() shouldBe listOf(MY_LOCATION_NAME, "Dobogókő", "(47.12345, 18.98765)")
            uiState.waypoints.last().location shouldBe null
        }
    }

    @Test
    fun `Given all stops filled, When place added, Then it is appended as a new stop`() {
        runTest {
            val viewModel = createViewModel()

            viewModel.onEvent(RoutePlannerUiEvents.PlaceAdded(place("Dobogókő")))
            viewModel.onEvent(RoutePlannerUiEvents.PlaceAdded(place("Ram-hegy")))
            viewModel.onEvent(RoutePlannerUiEvents.PlaceAdded(place("Prédikálószék")))

            val uiState = viewModel.uiState.value

            uiState.stops.names() shouldBe listOf(MY_LOCATION_NAME, "Dobogókő", "Ram-hegy", "Prédikálószék")
            uiState.waypoints.last().location shouldBe null
        }
    }

    @Test
    fun `Given the maximum stops, When a place is added, Then it is not appended`() {
        runTest {
            val viewModel = createViewModelWithMaxStops()

            viewModel.onEvent(RoutePlannerUiEvents.PlaceAdded(place("One too many")))

            val uiState = viewModel.uiState.value
            uiState.stops.size shouldBe RoutePlannerUiState.MAX_WAYPOINT_COUNT
            uiState.stops.names() shouldNotContain "One too many"
        }
    }

    @Test
    fun `Given the maximum stops, When a location is long tapped, Then it is not appended`() {
        runTest {
            val viewModel = createViewModelWithMaxStops()

            viewModel.onEvent(RoutePlannerUiEvents.LocationAdded(Location(47.12345, 18.98765)))

            viewModel.uiState.value.stops.size shouldBe RoutePlannerUiState.MAX_WAYPOINT_COUNT
            analyticsService.loggedEvents shouldNotContain AnalyticsEvent.RoutePlanWaypointLongTapped
        }
    }

    @Test
    fun `Given the maximum stops, When the state is read, Then the round trip is disabled`() {
        runTest {
            val viewModel = createViewModelWithMaxStops()

            val uiState = viewModel.uiState.value

            uiState.isMaxStopsReached shouldBe true
            uiState.isRoundTripEnabled shouldBe false
        }
    }

    @Test
    fun `Given three waypoints, When last one moved to front, Then order is updated`() {
        runTest {
            val viewModel = createViewModel()
            viewModel.onEvent(RoutePlannerUiEvents.PlaceAdded(place("A")))
            viewModel.onEvent(RoutePlannerUiEvents.PlaceAdded(place("B")))
            viewModel.onEvent(RoutePlannerUiEvents.PlaceAdded(place("C")))

            viewModel.onEvent(RoutePlannerUiEvents.WaypointMoved(fromIndex = 3, toIndex = 0))

            viewModel.uiState.value.stops.names() shouldBe listOf("C", MY_LOCATION_NAME, "A", "B")
        }
    }

    @Test
    fun `Given invalid indexes, When waypoint moved, Then order is unchanged`() {
        runTest {
            val viewModel = createViewModel()
            val originalWaypoints = viewModel.uiState.value.waypoints

            viewModel.onEvent(RoutePlannerUiEvents.WaypointMoved(fromIndex = 0, toIndex = 5))

            viewModel.uiState.value.waypoints shouldBe originalWaypoints
        }
    }

    @Test
    fun `Given a third stop, When it is removed, Then the minimum stops remain`() {
        runTest {
            val viewModel = createViewModel()
            viewModel.onEvent(RoutePlannerUiEvents.PlaceAdded(place("Dobogókő")))
            viewModel.onEvent(RoutePlannerUiEvents.PlaceAdded(place("Ram-hegy")))
            viewModel.onEvent(RoutePlannerUiEvents.PlaceAdded(place("Prédikálószék")))
            val addedStop = viewModel.uiState.value.stops.last()

            viewModel.onEvent(RoutePlannerUiEvents.WaypointRemoved(addedStop.id))

            viewModel.uiState.value.stops.names() shouldBe listOf(MY_LOCATION_NAME, "Dobogókő", "Ram-hegy")
            analyticsService.loggedEvents shouldContain AnalyticsEvent.RoutePlanWaypointRemoved
        }
    }

    @Test
    fun `Given minimum stops, When my location is removed, Then the row is kept but emptied`() {
        runTest {
            val viewModel = createViewModel()
            val firstStop = viewModel.uiState.value.stops.first()

            viewModel.onEvent(RoutePlannerUiEvents.WaypointRemoved(firstStop.id))

            val uiState = viewModel.uiState.value
            uiState.stops.size shouldBe 2
            uiState.stops.first().isEmpty shouldBe true
            uiState.isRoundTripEnabled shouldBe false
            analyticsService.loggedEvents shouldContain AnalyticsEvent.RoutePlanWaypointRemoved
        }
    }

    @Test
    fun `Given an emptied start stop, When a place is added, Then it fills the start stop`() {
        runTest {
            val viewModel = createViewModel()
            val firstStop = viewModel.uiState.value.stops.first()
            viewModel.onEvent(RoutePlannerUiEvents.WaypointRemoved(firstStop.id))

            viewModel.onEvent(RoutePlannerUiEvents.PlaceAdded(place("Dobogókő")))

            viewModel.uiState.value.stops.names() shouldBe listOf("Dobogókő", null)
        }
    }

    @Test
    fun `Given default state, When add stop from search clicked, Then the waypoint search is shown`() {
        runTest {
            val viewModel = createViewModel()

            viewModel.onEvent(RoutePlannerUiEvents.AddStopFromSearchClicked())

            viewModel.uiState.value.isWaypointSearchVisible shouldBe true
            analyticsService.loggedEvents shouldContain AnalyticsEvent.RoutePlanAddStopFromSearchClicked
        }
    }

    @Test
    fun `Given the waypoint search is shown, When it is dismissed, Then it is hidden`() {
        runTest {
            val viewModel = createViewModel()
            viewModel.onEvent(RoutePlannerUiEvents.AddStopFromSearchClicked())

            viewModel.onEvent(RoutePlannerUiEvents.WaypointSearchDismissed)

            viewModel.uiState.value.isWaypointSearchVisible shouldBe false
        }
    }

    @Test
    fun `Given the waypoint search is shown, When a place is added, Then the search is hidden`() {
        runTest {
            val viewModel = createViewModel()
            viewModel.onEvent(RoutePlannerUiEvents.AddStopFromSearchClicked())

            viewModel.onEvent(RoutePlannerUiEvents.PlaceAdded(place("Dobogókő")))

            viewModel.uiState.value.isWaypointSearchVisible shouldBe false
            viewModel.uiState.value.stops.names() shouldBe listOf(MY_LOCATION_NAME, "Dobogókő")
        }
    }

    @Test
    fun `Given the waypoint search is shown, When a location is long tapped, Then the search is hidden`() {
        runTest {
            val viewModel = createViewModel()
            viewModel.onEvent(RoutePlannerUiEvents.AddStopFromSearchClicked())

            viewModel.onEvent(RoutePlannerUiEvents.LocationAdded(Location(47.5, 18.5)))

            viewModel.uiState.value.isWaypointSearchVisible shouldBe false
            viewModel.uiState.value.stops.last().location shouldBe Location(47.5, 18.5)
        }
    }

    @Test
    fun `Given the waypoint search, When a search place is added, Then it is a stop and a recorded visit`() {
        runTest {
            val viewModel = createViewModel()
            viewModel.onEvent(RoutePlannerUiEvents.AddStopFromSearchClicked())
            val place = place("Dobogókő")

            viewModel.onEvent(RoutePlannerUiEvents.SearchPlaceAdded(place))
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.uiState.value.stops.names() shouldBe listOf(MY_LOCATION_NAME, "Dobogókő")
            viewModel.uiState.value.isWaypointSearchVisible shouldBe false
            analyticsService.loggedEvents shouldContain AnalyticsEvent.RoutePlanStopAddedFromSearch
            verifySuspend(exactly(1)) { placeHistoryRepository.recordVisit(place) }
        }
    }

    @Test
    fun `Given the waypoint search, When a destination is added, Then its location becomes a stop`() {
        runTest {
            val viewModel = createViewModel()
            viewModel.onEvent(RoutePlannerUiEvents.AddStopFromSearchClicked())

            viewModel.onEvent(RoutePlannerUiEvents.SearchDestinationAdded(destination("Rám-szakadék")))

            viewModel.uiState.value.stops.names() shouldBe listOf(MY_LOCATION_NAME, "Rám-szakadék")
            viewModel.uiState.value.stops.last().location shouldBe DESTINATION_LOCATION
            viewModel.uiState.value.isWaypointSearchVisible shouldBe false
        }
    }

    @Test
    fun `Given a known location, When my location is added, Then it becomes a stop`() {
        runTest {
            val viewModel = createViewModel()
            viewModel.onEvent(RoutePlannerUiEvents.AddStopFromSearchClicked())

            viewModel.onEvent(RoutePlannerUiEvents.MyLocationAdded)
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.uiState.value.myLocation shouldBe MY_LOCATION
            viewModel.uiState.value.stops.last().location shouldBe MY_LOCATION
            viewModel.uiState.value.isWaypointSearchVisible shouldBe false
            analyticsService.loggedEvents shouldContain AnalyticsEvent.RoutePlanMyLocationAdded
        }
    }

    @Test
    fun `Given no known location, When my location is added, Then nothing changes`() {
        runTest {
            everySuspend { locationMonitoringService.lastKnownLocation() } returns null
            val viewModel = createViewModel()
            viewModel.onEvent(RoutePlannerUiEvents.AddStopFromSearchClicked())

            viewModel.onEvent(RoutePlannerUiEvents.MyLocationAdded)
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.uiState.value.myLocation.shouldBeNull()
            viewModel.uiState.value.stops.names() shouldBe listOf(MY_LOCATION_NAME, null)
            viewModel.uiState.value.isWaypointSearchVisible shouldBe true
            analyticsService.loggedEvents shouldNotContain AnalyticsEvent.RoutePlanMyLocationAdded
        }
    }

    @Test
    fun `Given the waypoint search, When pick on map is clicked, Then the sheet is minimized`() {
        runTest {
            val viewModel = createViewModel()
            viewModel.onEvent(RoutePlannerUiEvents.AddStopFromSearchClicked())

            viewModel.uiEffects.test {
                viewModel.onEvent(RoutePlannerUiEvents.PickOnMapClicked)
                testDispatcher.scheduler.advanceUntilIdle()

                expectMostRecentItem() shouldBe RoutePlannerUiEffects.MinimizeSheet
                viewModel.uiState.value.isWaypointSearchVisible shouldBe false
                analyticsService.loggedEvents shouldContain AnalyticsEvent.RoutePlanPickOnMapClicked
            }
        }
    }

    @Test
    fun `Given two empty stops, When the second one is targeted, Then the search fills that stop`() {
        runTest {
            val viewModel = createViewModel()
            // Below the minimum a removal empties the row instead of dropping it, so both stops end up empty.
            viewModel.uiState.value.stops.forEach {
                viewModel.onEvent(RoutePlannerUiEvents.WaypointRemoved(it.id))
            }
            val secondStop = viewModel.uiState.value.stops.last()
            secondStop.isEmpty shouldBe true

            viewModel.onEvent(RoutePlannerUiEvents.AddStopFromSearchClicked(waypointId = secondStop.id))
            viewModel.onEvent(RoutePlannerUiEvents.SearchPlaceAdded(place("Dobogókő")))

            viewModel.uiState.value.stops.names() shouldBe listOf(null, "Dobogókő")
        }
    }

    @Test
    fun `Given an empty stop, When no waypoint is targeted, Then the search fills the first empty stop`() {
        runTest {
            val viewModel = createViewModel()

            viewModel.onEvent(RoutePlannerUiEvents.AddStopFromSearchClicked())
            viewModel.onEvent(RoutePlannerUiEvents.SearchPlaceAdded(place("Dobogókő")))

            viewModel.uiState.value.stops.names() shouldBe listOf(MY_LOCATION_NAME, "Dobogókő")
        }
    }

    @Test
    fun `Given a moved user, When my location is added, Then the current fix is used`() {
        runTest {
            val viewModel = createViewModel()
            val movedLocation = Location(47.9, 19.4)
            everySuspend { locationMonitoringService.lastKnownLocation() } returns movedLocation

            viewModel.onEvent(RoutePlannerUiEvents.MyLocationAdded)
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.uiState.value.stops.last().location shouldBe movedLocation
        }
    }

    @Test
    fun `Given a targeted stop, When pick on map is clicked, Then the target survives the dismissal`() {
        runTest {
            val viewModel = createViewModel()
            val targetStop = viewModel.uiState.value.stops.last()
            viewModel.onEvent(RoutePlannerUiEvents.AddStopFromSearchClicked(waypointId = targetStop.id))

            viewModel.onEvent(RoutePlannerUiEvents.PickOnMapClicked)
            // The sheet's own dismissal callback fires right after pick on map closed it.
            viewModel.onEvent(RoutePlannerUiEvents.WaypointSearchDismissed)

            viewModel.uiState.value.waypointSearchTargetId shouldBe targetStop.id
            viewModel.uiState.value.isPickingOnMap shouldBe true
        }
    }

    @Test
    fun `Given a pick is pending, When a location is long tapped, Then the sheet is expanded again`() {
        runTest {
            val viewModel = createViewModel()
            viewModel.onEvent(RoutePlannerUiEvents.AddStopFromSearchClicked())
            viewModel.onEvent(RoutePlannerUiEvents.PickOnMapClicked)

            viewModel.uiEffects.test {
                viewModel.onEvent(RoutePlannerUiEvents.LocationAdded(Location(47.5, 18.5)))
                testDispatcher.scheduler.advanceUntilIdle()

                // Adding a waypoint also emits RoutePlanUpdated, and the order between them is not fixed.
                emittedEffects() shouldContain RoutePlannerUiEffects.ExpandSheet
                viewModel.uiState.value.isPickingOnMap shouldBe false
            }
        }
    }

    @Test
    fun `Given no pick is pending, When a location is long tapped, Then the sheet is left alone`() {
        runTest {
            val viewModel = createViewModel()

            viewModel.uiEffects.test {
                viewModel.onEvent(RoutePlannerUiEvents.LocationAdded(Location(47.5, 18.5)))
                testDispatcher.scheduler.advanceUntilIdle()

                emittedEffects() shouldNotContain RoutePlannerUiEffects.ExpandSheet
            }
        }
    }

    @Test
    fun `Given an empty stop, When it is removed, Then nothing changes`() {
        runTest {
            val viewModel = createViewModel()
            val emptyStop = viewModel.uiState.value.stops.last()

            viewModel.onEvent(RoutePlannerUiEvents.WaypointRemoved(emptyStop.id))

            viewModel.uiState.value.stops.names() shouldBe listOf(MY_LOCATION_NAME, null)
            analyticsService.loggedEvents shouldNotContain AnalyticsEvent.RoutePlanWaypointRemoved
        }
    }

    @Test
    fun `Given a planned route, When a stop is emptied, Then the route plan is cleared`() {
        runTest {
            val viewModel = createViewModel()
            viewModel.onEvent(RoutePlannerUiEvents.PlaceAdded(place("Dobogókő")))
            testDispatcher.scheduler.advanceUntilIdle()
            viewModel.uiState.value.routePlan shouldNotBe null
            val firstStop = viewModel.uiState.value.stops.first()

            viewModel.onEvent(RoutePlannerUiEvents.WaypointRemoved(firstStop.id))
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.uiState.value.routePlan.shouldBeNull()
        }
    }

    @Test
    fun `Given a round trip, When markers are built, Then the repeated stop is not marked twice`() {
        runTest {
            val viewModel = createViewModel()
            viewModel.onEvent(RoutePlannerUiEvents.PlaceAdded(place("Dobogókő")))
            viewModel.onEvent(
                RoutePlannerUiEvents.PlaceAdded(place("Ram-hegy", location = Location(47.7, 18.8))),
            )

            viewModel.onEvent(RoutePlannerUiEvents.RoundTripClicked)

            val markers = viewModel.uiState.value.routePlanMarkers
            markers.map { it.location } shouldBe markers.map { it.location }.distinct()
            markers.first().type shouldBe WaypointType.ROUND_TRIP
        }
    }

    @Test
    fun `Given a route, When round trip clicked, Then the first stop is repeated at the end`() {
        runTest {
            val viewModel = createViewModel()
            viewModel.onEvent(RoutePlannerUiEvents.PlaceAdded(place("Dobogókő")))
            viewModel.onEvent(RoutePlannerUiEvents.PlaceAdded(place("Ram-hegy")))

            viewModel.onEvent(RoutePlannerUiEvents.RoundTripClicked)

            val uiState = viewModel.uiState.value
            uiState.isRoundTrip shouldBe true
            uiState.isRoundTripEnabled shouldBe false
            uiState.stops.names() shouldBe listOf(MY_LOCATION_NAME, "Dobogókő", "Ram-hegy", MY_LOCATION_NAME)
            uiState.stops.last().location shouldBe MY_LOCATION
        }
    }

    @Test
    fun `Given a route, When stop types are read, Then the first stop is a start`() {
        runTest {
            val viewModel = createViewModel()
            viewModel.onEvent(RoutePlannerUiEvents.PlaceAdded(place("Dobogókő")))

            val uiState = viewModel.uiState.value

            uiState.stopType(0) shouldBe WaypointType.START
            uiState.stopType(uiState.stops.lastIndex) shouldBe WaypointType.END
        }
    }

    @Test
    fun `Given a round trip, When stop types are read, Then the first stop is a start`() {
        runTest {
            val viewModel = createViewModel()
            viewModel.onEvent(RoutePlannerUiEvents.PlaceAdded(place("Dobogókő")))
            viewModel.onEvent(RoutePlannerUiEvents.PlaceAdded(place("Ram-hegy")))

            viewModel.onEvent(RoutePlannerUiEvents.RoundTripClicked)

            val uiState = viewModel.uiState.value
            uiState.stopType(0) shouldBe WaypointType.START
            uiState.stopType(1) shouldBe WaypointType.INTERMEDIATE
            uiState.stopType(uiState.stops.lastIndex) shouldBe WaypointType.END
        }
    }

    @Test
    fun `Given a round trip, When round trip clicked again, Then the route is unchanged`() {
        runTest {
            val viewModel = createViewModel()
            viewModel.onEvent(RoutePlannerUiEvents.PlaceAdded(place("Dobogókő")))
            viewModel.onEvent(RoutePlannerUiEvents.RoundTripClicked)
            val roundTripStops = viewModel.uiState.value.stops

            viewModel.onEvent(RoutePlannerUiEvents.RoundTripClicked)

            viewModel.uiState.value.stops shouldBe roundTripStops
        }
    }

    @Test
    fun `Given no known location, When round trip clicked, Then no stop is repeated`() {
        runTest {
            everySuspend { locationMonitoringService.lastKnownLocation() } returns null
            val viewModel = createViewModel()

            viewModel.onEvent(RoutePlannerUiEvents.RoundTripClicked)

            val uiState = viewModel.uiState.value
            uiState.isRoundTrip shouldBe false
            uiState.isRoundTripEnabled shouldBe false
            uiState.stops.size shouldBe 2
        }
    }

    @Test
    fun `Given no known location, When the first fix arrives, Then the stop fills and the route is planned`() {
        runTest {
            everySuspend { locationMonitoringService.lastKnownLocation() } returns null
            val viewModel = createViewModel()
            viewModel.onEvent(RoutePlannerUiEvents.PlaceAdded(place("Dobogókő")))
            testDispatcher.scheduler.advanceUntilIdle()
            viewModel.uiState.value.routePlan.shouldBeNull()

            locationUpdates.emit(MY_LOCATION)
            testDispatcher.scheduler.advanceUntilIdle()

            val uiState = viewModel.uiState.value
            uiState.stops.first().location shouldBe MY_LOCATION
            uiState.routePlan shouldNotBe null
        }
    }

    @Test
    fun `Given a single waypoint, When view model init, Then no route is planned`() {
        runTest {
            val viewModel = createViewModel()

            testDispatcher.scheduler.advanceUntilIdle()

            val uiState = viewModel.uiState.value
            uiState.routePlan.shouldBeNull()
            uiState.isRoutePlanLoading shouldBe false
            uiState.isSaveEnabled shouldBe false
        }
    }

    @Test
    fun `Given two waypoints, When place added, Then the route plan and its stats are shown`() {
        runTest {
            val viewModel = createViewModel()

            viewModel.onEvent(RoutePlannerUiEvents.PlaceAdded(place("Dobogókő")))
            testDispatcher.scheduler.advanceUntilIdle()

            val uiState = viewModel.uiState.value
            uiState.isRoutePlanLoading shouldBe false
            uiState.routePlanError.shouldBeNull()
            uiState.routePlan?.locations?.size shouldBe 2
            uiState.routeStats?.incline shouldBe 240
            uiState.routeStats?.decline shouldBe 120
            uiState.isSaveEnabled shouldBe true
            analyticsService.loggedEvents shouldContain AnalyticsEvent.RoutePlanCreated(RouteProfile.ON_TRAILS)
        }
    }

    @Test
    fun `Given a planned route, When save clicked, Then the plan is saved and opened as a GPX`() {
        runTest {
            val viewModel = createViewModel()
            viewModel.onEvent(RoutePlannerUiEvents.PlaceAdded(place("Dobogókő")))
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.uiEffects.test {
                viewModel.onEvent(RoutePlannerUiEvents.SaveRouteClicked)
                testDispatcher.scheduler.advanceUntilIdle()

                emittedEffects() shouldContain RoutePlannerUiEffects.RoutePlanSaved(SAVED_GPX_URI)
            }
            // My location has no geocoded place, so only the searched stop carries a name.
            verifySuspend(exactly(1)) {
                gpxRepository.saveRoutePlan(any(), listOf(null, "Dobogókő"), RoutePlannerProfile.ON_TRAILS)
            }
            analyticsService.loggedEvents shouldContain AnalyticsEvent.RoutePlanSaved(RouteProfile.ON_TRAILS)
        }
    }

    @Test
    fun `Given a geocoded my location, When save clicked, Then its place name is used for the GPX naming`() {
        runTest {
            everySuspend { geocodingRepository.reverseGeocode(any()) } returns
                NetworkResult.Success(locationIqPlace("Téry Ödön út"))
            val viewModel = createViewModel()
            viewModel.onEvent(RoutePlannerUiEvents.PlaceAdded(place("Dobogókő")))
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.onEvent(RoutePlannerUiEvents.SaveRouteClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.uiState.value.stops.first().placeName shouldBe "Téry Ödön út"
            verifySuspend(exactly(1)) {
                gpxRepository.saveRoutePlan(
                    any(),
                    listOf("Téry Ödön út", "Dobogókő"),
                    RoutePlannerProfile.ON_TRAILS,
                )
            }
        }
    }

    @Test
    fun `Given a geocoded my location, When resolved, Then the row label stays the my location placeholder`() {
        runTest {
            everySuspend { geocodingRepository.reverseGeocode(any()) } returns
                NetworkResult.Success(locationIqPlace("Téry Ödön út"))
            val viewModel = createViewModel()

            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.uiState.value.stops.names().first() shouldBe MY_LOCATION_NAME
        }
    }

    @Test
    fun `Given a re-plan in flight, When save clicked, Then the stale plan is not saved`() {
        runTest {
            val viewModel = createViewModel()
            viewModel.onEvent(RoutePlannerUiEvents.PlaceAdded(place("Dobogókő")))
            testDispatcher.scheduler.advanceUntilIdle()

            // A third stop keeps the previous plan on screen while the new one loads.
            viewModel.onEvent(RoutePlannerUiEvents.SearchPlaceAdded(place("Rám-szakadék", DESTINATION_LOCATION)))
            testDispatcher.scheduler.advanceTimeBy(100)

            viewModel.uiState.value.isRoutePlanLoading shouldBe true
            viewModel.uiState.value.routePlan shouldNotBe null
            viewModel.uiState.value.isSaveEnabled shouldBe false

            viewModel.onEvent(RoutePlannerUiEvents.SaveRouteClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            verifySuspend(exactly(0)) { gpxRepository.saveRoutePlan(any(), any(), any()) }
        }
    }

    @Test
    fun `Given no planned route, When save clicked, Then nothing is saved`() {
        runTest {
            val viewModel = createViewModel()
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.onEvent(RoutePlannerUiEvents.SaveRouteClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            verifySuspend(exactly(0)) { gpxRepository.saveRoutePlan(any(), any(), any()) }
        }
    }

    @Test
    fun `Given a failing storage, When save clicked, Then the save error is shown and the plan is kept`() {
        runTest {
            everySuspend { gpxRepository.saveRoutePlan(any(), any(), any()) } throws IllegalStateException("disk full")
            val viewModel = createViewModel()
            viewModel.onEvent(RoutePlannerUiEvents.PlaceAdded(place("Dobogókő")))
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.uiEffects.test {
                viewModel.onEvent(RoutePlannerUiEvents.SaveRouteClicked)
                testDispatcher.scheduler.advanceUntilIdle()

                emittedEffects() shouldContain RoutePlannerUiEffects.RoutePlanSaveFailed
            }
            viewModel.uiState.value.routePlan shouldNotBe null
            analyticsService.loggedEvents shouldContain AnalyticsEvent.RoutePlanSaveFailed
        }
    }

    @Test
    fun `Given a planned route, When profile selected, Then the route is planned with the new profile`() {
        runTest {
            val viewModel = createViewModel()
            viewModel.onEvent(RoutePlannerUiEvents.PlaceAdded(place("Dobogókő")))
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.onEvent(RoutePlannerUiEvents.ProfileSelected(RoutePlannerProfile.BIKE))
            testDispatcher.scheduler.advanceUntilIdle()

            analyticsService.loggedEvents shouldContain AnalyticsEvent.RoutePlanCreated(RouteProfile.BIKE)
        }
    }

    @Test
    fun `Given a planned route, When profile switched rapidly, Then only the last profile is requested`() {
        runTest {
            val viewModel = createViewModel()
            viewModel.onEvent(RoutePlannerUiEvents.PlaceAdded(place("Dobogókő")))
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.onEvent(RoutePlannerUiEvents.ProfileSelected(RoutePlannerProfile.SHORTEST_ROUTE))
            testDispatcher.scheduler.advanceTimeBy(100)
            viewModel.uiState.value.isRoutePlanLoading shouldBe true
            viewModel.onEvent(RoutePlannerUiEvents.ProfileSelected(RoutePlannerProfile.BIKE))
            testDispatcher.scheduler.advanceUntilIdle()

            verifySuspend(exactly(1)) { routePlannerRepository.getRoutePlan(RoutePlannerProfile.BIKE, any()) }
            verifySuspend(exactly(0)) {
                routePlannerRepository.getRoutePlan(RoutePlannerProfile.SHORTEST_ROUTE, any())
            }
        }
    }

    @Test
    fun `Given no internet, When route is planned, Then the error is shown`() {
        runTest {
            everySuspend { routePlannerRepository.getRoutePlan(any(), any()) } returns
                NetworkResult.Error(NetworkError.NO_INTERNET)
            val viewModel = createViewModel()

            viewModel.onEvent(RoutePlannerUiEvents.PlaceAdded(place("Dobogókő")))
            testDispatcher.scheduler.advanceUntilIdle()

            val uiState = viewModel.uiState.value
            uiState.isRoutePlanLoading shouldBe false
            uiState.routePlan.shouldBeNull()
            uiState.routePlanError shouldBe NetworkError.NO_INTERNET.toInfoViewData()
            uiState.isSaveEnabled shouldBe false
            analyticsService.loggedEvents shouldContain AnalyticsEvent.RoutePlanNoInternet
        }
    }

    @Test
    fun `Given rate limited response, When route is planned, Then the daily limit error is shown`() {
        runTest {
            everySuspend { routePlannerRepository.getRoutePlan(any(), any()) } returns
                NetworkResult.Error(NetworkError.RATE_LIMITED)
            val viewModel = createViewModel()

            viewModel.onEvent(RoutePlannerUiEvents.PlaceAdded(place("Dobogókő")))
            testDispatcher.scheduler.advanceUntilIdle()

            val uiState = viewModel.uiState.value
            uiState.isRoutePlanLoading shouldBe false
            uiState.routePlan.shouldBeNull()
            uiState.routePlanError shouldBe NetworkError.RATE_LIMITED.toRoutePlanInfoViewData()
            uiState.routePlanError?.title shouldBe SharedRes.strings.route_planner_daily_limit_error_title
            uiState.isStopListVisible shouldBe false
            uiState.isSaveButtonVisible shouldBe false
            uiState.isSaveEnabled shouldBe false
            analyticsService.loggedEvents shouldContain AnalyticsEvent.RoutePlanDailyLimitReached
        }
    }

    @Test
    fun `Given an empty route response, When route is planned, Then the not found error is shown`() {
        runTest {
            everySuspend { routePlannerRepository.getRoutePlan(any(), any()) } returns
                NetworkResult.Success(GraphhopperRouteResponse())
            val viewModel = createViewModel()

            viewModel.onEvent(RoutePlannerUiEvents.PlaceAdded(place("Dobogókő")))
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.uiState.value.routePlanError shouldBe NetworkError.NOT_FOUND.toInfoViewData()
        }
    }

    @Test
    fun `Given a failed route plan, When retry clicked, Then the route is planned again`() {
        runTest {
            everySuspend { routePlannerRepository.getRoutePlan(any(), any()) } returns
                NetworkResult.Error(NetworkError.NO_INTERNET)
            val viewModel = createViewModel()
            viewModel.onEvent(RoutePlannerUiEvents.PlaceAdded(place("Dobogókő")))
            testDispatcher.scheduler.advanceUntilIdle()
            everySuspend { routePlannerRepository.getRoutePlan(any(), any()) } returns
                NetworkResult.Success(ROUTE_RESPONSE)

            viewModel.onEvent(RoutePlannerUiEvents.RetryClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            val uiState = viewModel.uiState.value
            uiState.routePlanError.shouldBeNull()
            uiState.routePlan shouldNotBe null
        }
    }

    @Test
    fun `Given two waypoints, When the route is planned, Then it is emitted for the map`() {
        runTest {
            val viewModel = createViewModel()

            viewModel.uiEffects.test {
                viewModel.onEvent(RoutePlannerUiEvents.PlaceAdded(place("Dobogókő")))
                testDispatcher.scheduler.advanceUntilIdle()

                val effect = expectMostRecentItem() as RoutePlannerUiEffects.RoutePlanUpdated
                effect.routePlan shouldBe viewModel.uiState.value.routePlan
                effect.markers shouldBe viewModel.uiState.value.routePlanMarkers
            }
        }
    }

    @Test
    fun `Given a single waypoint, When it is added, Then it is emitted for the map without a plan`() {
        runTest {
            everySuspend { locationMonitoringService.lastKnownLocation() } returns null
            val viewModel = createViewModel()

            viewModel.uiEffects.test {
                viewModel.onEvent(RoutePlannerUiEvents.PlaceAdded(place("Dobogókő")))
                testDispatcher.scheduler.advanceUntilIdle()

                val effect = expectMostRecentItem() as RoutePlannerUiEffects.RoutePlanUpdated
                effect.routePlan.shouldBeNull()
                effect.markers shouldBe listOf(GpxWaypoint(PLACE_LOCATION, WaypointType.END))
            }
        }
    }

    @Test
    fun `Given my location is far away, When the planner is opened with a place, Then the end marker is emitted`() {
        runTest {
            val viewModel = createViewModel()

            viewModel.uiEffects.test {
                viewModel.onEvent(RoutePlannerUiEvents.PlaceAdded(place("Dobogoko", FAR_PLACE_LOCATION)))
                testDispatcher.scheduler.advanceUntilIdle()

                val effect = expectMostRecentItem() as RoutePlannerUiEffects.RoutePlanUpdated
                effect.routePlan.shouldBeNull()
                effect.markers shouldBe listOf(GpxWaypoint(FAR_PLACE_LOCATION, WaypointType.END))
            }
        }
    }

    @Test
    fun `Given the planner is open, When close clicked, Then Close effect is emitted`() {
        runTest {
            val viewModel = createViewModel()

            viewModel.uiEffects.test {
                viewModel.onEvent(RoutePlannerUiEvents.CloseClicked)
                testDispatcher.scheduler.advanceUntilIdle()

                expectMostRecentItem() shouldBe RoutePlannerUiEffects.Close
            }
        }
    }

    companion object {
        private val MY_LOCATION = Location(47.5, 19.0)
        private val PLACE_LOCATION = Location(47.6, 18.9)
        private val FAR_PLACE_LOCATION = Location(46.2, 18.0)
        private val DESTINATION_LOCATION = Location(47.7, 18.9)
        private const val MY_LOCATION_NAME = "MY_LOCATION_RESOURCE"
        private const val SAVED_GPX_URI = "gpx/routeplanner/Dobogoko - Ram-szakadek 2026-08-19.gpx"
        private val ROUTE_RESPONSE = GraphhopperRouteResponse(
            paths = listOf(
                GraphhopperPath(
                    distance = 8500.0,
                    time = 3_600_000,
                    ascend = 240.0,
                    descend = 120.0,
                    points = GraphhopperPoints(
                        coordinates = listOf(listOf(19.0, 47.5, 300.0), listOf(18.0, 47.0, 420.0)),
                    ),
                    snappedWaypoints = GraphhopperPoints(
                        coordinates = listOf(listOf(19.0, 47.5), listOf(18.0, 47.0)),
                    ),
                ),
            ),
        )
    }
}
