package hu.mostoha.mobile.kmp.huki.features.destinations

import app.cash.turbine.test
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.location.LOCATION
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.analytics.AnalyticsEvent
import hu.mostoha.mobile.kmp.huki.model.analytics.Screen
import hu.mostoha.mobile.kmp.huki.model.domain.Destination
import hu.mostoha.mobile.kmp.huki.model.domain.DestinationType
import hu.mostoha.mobile.kmp.huki.model.domain.Landscape
import hu.mostoha.mobile.kmp.huki.model.domain.LandscapeType
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.OsmType
import hu.mostoha.mobile.kmp.huki.repository.DestinationRepository
import hu.mostoha.mobile.kmp.huki.service.FakeAnalyticsService
import hu.mostoha.mobile.kmp.huki.service.LocationMonitoringService
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
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
class DestinationsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val destinationRepository = mock<DestinationRepository>()
    private val locationMonitoringService = mock<LocationMonitoringService>()
    private val permissionsController = mock<PermissionsController>()
    private val analyticsService = FakeAnalyticsService()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { destinationRepository.getPopularDestinations() } returns POPULAR
        every { destinationRepository.getLandscapes() } returns emptyList()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): DestinationsViewModel {
        val viewModel = DestinationsViewModel(
            destinationRepository,
            locationMonitoringService,
            testDispatcher,
            permissionsController,
            analyticsService,
        )
        testDispatcher.scheduler.runCurrent()
        return viewModel
    }

    private fun stubPermissionState(state: PermissionState) {
        everySuspend { permissionsController.getPermissionState(Permission.LOCATION) } returns state
    }

    @Test
    fun `Given view model init, When created, Then destinations screen view is logged`() {
        stubPermissionState(PermissionState.NotDetermined)

        runTest {
            createViewModel()

            analyticsService.screenViews shouldBe listOf(AnalyticsEvent.ScreenView(Screen.DESTINATIONS))
        }
    }

    @Test
    fun `Given popular destinations, When view model init, Then popular items and count are set`() {
        stubPermissionState(PermissionState.NotDetermined)

        runTest {
            val viewModel = createViewModel()

            val uiState = viewModel.uiState.value

            uiState.isLoading shouldBe false
            uiState.destinationCount shouldBe POPULAR.size
            uiState.popularItems.map { it.destination } shouldBe POPULAR
            uiState.popularItems.all { it.distance == null } shouldBe true
        }
    }

    @Test
    fun `Given landscapes, When view model init, Then landscapes is populated`() {
        stubPermissionState(PermissionState.NotDetermined)
        val landscape = landscape("bukk", POPULAR)
        every { destinationRepository.getLandscapes() } returns listOf(landscape)

        runTest {
            val viewModel = createViewModel()

            viewModel.uiState.value.landscapes shouldBe listOf(landscape)
        }
    }

    @Test
    fun `Given location permission not granted, When view model init, Then nearby state requires permission`() {
        stubPermissionState(PermissionState.NotDetermined)

        runTest {
            val viewModel = createViewModel()

            viewModel.uiState.value.nearbyState shouldBe NearbyState.PermissionRequired
        }
    }

    @Test
    fun `Given permission granted and a location, When view model init, Then nearby items are loaded with distances`() {
        val location = Location(47.0, 19.0)
        stubPermissionState(PermissionState.Granted)
        everySuspend { locationMonitoringService.lastKnownLocation() } returns location
        every { destinationRepository.getNearbyDestinations(location) } returns POPULAR

        runTest {
            val viewModel = createViewModel()

            val nearbyState = viewModel.uiState.value.nearbyState
            nearbyState.shouldBeInstanceOf<NearbyState.Loaded>()
            nearbyState.items.map { it.destination } shouldBe POPULAR
            nearbyState.items.all { it.distance != null } shouldBe true
        }
    }

    @Test
    fun `Given user location available, When distanceText for a destination at that location, Then zero distance`() {
        val location = Location(47.0, 19.0)
        val destination = destination("1", "Kékestető", popularity = 10).copy(location = location)
        stubPermissionState(PermissionState.Granted)
        everySuspend { locationMonitoringService.lastKnownLocation() } returns location
        every { destinationRepository.getNearbyDestinations(location) } returns POPULAR

        runTest {
            val viewModel = createViewModel()

            viewModel.distanceText(destination) shouldBe "0 m"
        }
    }

    @Test
    fun `Given no user location, When distanceText, Then null is returned`() {
        stubPermissionState(PermissionState.NotDetermined)

        runTest {
            val viewModel = createViewModel()

            viewModel.distanceText(POPULAR.first()) shouldBe null
        }
    }

    @Test
    fun `Given granted but no location fix, When init, Then nearby state is location unavailable`() {
        stubPermissionState(PermissionState.Granted)
        everySuspend { locationMonitoringService.lastKnownLocation() } returns null
        every { locationMonitoringService.locationUpdates } returns MutableSharedFlow()

        runTest {
            val viewModel = createViewModel()
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.uiState.value.nearbyState shouldBe NearbyState.LocationUnavailable
        }
    }

    @Test
    fun `Given a tab, When TabSelected event, Then selected tab is updated`() {
        stubPermissionState(PermissionState.NotDetermined)

        runTest {
            val viewModel = createViewModel()

            viewModel.onEvent(DestinationsUiEvents.TabSelected(DestinationsTab.NEARBY))

            viewModel.uiState.value.selectedTab shouldBe DestinationsTab.NEARBY
        }
    }

    @Test
    fun `Given default state, When BackClicked event, Then NavigateBack effect is emitted`() {
        stubPermissionState(PermissionState.NotDetermined)

        runTest {
            val viewModel = createViewModel()

            viewModel.uiEffects.test {
                viewModel.onEvent(DestinationsUiEvents.BackClicked)

                awaitItem() shouldBe DestinationsUiEffects.NavigateBack
            }
        }
    }

    @Test
    fun `Given permission required, When permission is granted, Then nearby items are loaded`() {
        val location = Location(47.0, 19.0)
        stubPermissionState(PermissionState.NotDetermined)
        everySuspend { permissionsController.providePermission(Permission.LOCATION) } returns Unit
        everySuspend { locationMonitoringService.lastKnownLocation() } returns location
        every { destinationRepository.getNearbyDestinations(location) } returns POPULAR

        runTest {
            val viewModel = createViewModel()
            viewModel.uiState.value.nearbyState shouldBe NearbyState.PermissionRequired

            viewModel.onEvent(DestinationsUiEvents.GrantLocationClicked)
            testDispatcher.scheduler.runCurrent()

            viewModel.uiState.value.nearbyState.shouldBeInstanceOf<NearbyState.Loaded>()
        }
    }

    @Test
    fun `Given permission denied always, When permission requested, Then NavigateToAppSettings effect is emitted`() {
        stubPermissionState(PermissionState.NotDetermined)
        val deniedAlways = DeniedAlwaysException(Permission.LOCATION)
        everySuspend { permissionsController.providePermission(Permission.LOCATION) } throws deniedAlways

        runTest {
            val viewModel = createViewModel()

            viewModel.uiEffects.test {
                viewModel.onEvent(DestinationsUiEvents.GrantLocationClicked)

                awaitItem() shouldBe DestinationsUiEffects.NavigateToAppSettings
            }

            analyticsService.loggedEvents shouldBe listOf(
                AnalyticsEvent.LocationPermissionDenied(deniedAlways = true),
            )
        }
    }

    @Test
    fun `Given permission denied, When permission requested, Then denied analytics event is logged`() {
        stubPermissionState(PermissionState.NotDetermined)
        everySuspend {
            permissionsController.providePermission(Permission.LOCATION)
        } throws DeniedException(Permission.LOCATION)

        runTest {
            val viewModel = createViewModel()

            viewModel.onEvent(DestinationsUiEvents.GrantLocationClicked)
            testDispatcher.scheduler.runCurrent()

            analyticsService.loggedEvents shouldBe listOf(
                AnalyticsEvent.LocationPermissionDenied(deniedAlways = false),
            )
        }
    }

    private companion object {
        val POPULAR = listOf(
            destination("1", "Kékestető", popularity = 10),
            destination("2", "Dobogókő", popularity = 8),
        )

        fun landscape(osmId: String, destinations: List<Destination>): Landscape =
            Landscape(
                osmId = osmId,
                osmType = OsmType.WAY,
                nameRes = SharedRes.strings.landscape_bukk,
                landscapeType = LandscapeType.MOUNTAIN_HIGH,
                center = Location(47.0, 19.0),
                destinations = destinations,
                areaTags = emptyMap(),
            )

        fun destination(osmId: String, name: String, popularity: Int): Destination =
            Destination(
                osmId = osmId,
                name = name,
                town = "Town $osmId",
                type = DestinationType.PEAK,
                location = Location(47.0 + osmId.toInt() / 100.0, 19.0),
                description = SharedRes.strings.bukk_fatyol_vizeses,
                popularity = popularity,
            )
    }
}
