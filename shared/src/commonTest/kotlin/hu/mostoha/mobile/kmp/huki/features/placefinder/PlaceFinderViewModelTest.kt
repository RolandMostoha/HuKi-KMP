package hu.mostoha.mobile.kmp.huki.features.placefinder

import app.cash.turbine.test
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.domain.Destination
import hu.mostoha.mobile.kmp.huki.model.domain.DestinationType
import hu.mostoha.mobile.kmp.huki.model.domain.GpxFileItem
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.OsmType
import hu.mostoha.mobile.kmp.huki.model.domain.Place
import hu.mostoha.mobile.kmp.huki.model.network.LocationIqPlace
import hu.mostoha.mobile.kmp.huki.model.network.NetworkError
import hu.mostoha.mobile.kmp.huki.model.network.NetworkResult
import hu.mostoha.mobile.kmp.huki.network.toInfoViewData
import hu.mostoha.mobile.kmp.huki.repository.DestinationRepository
import hu.mostoha.mobile.kmp.huki.repository.GeocodingRepository
import hu.mostoha.mobile.kmp.huki.repository.GpxRepository
import hu.mostoha.mobile.kmp.huki.service.LocationMonitoringService
import hu.mostoha.mobile.kmp.huki.util.distanceBetween
import hu.mostoha.mobile.kmp.huki.util.formatter.DistanceFormatter
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.maplibre.spatialk.units.extensions.kilometers
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

@OptIn(ExperimentalCoroutinesApi::class)
class PlaceFinderViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val geocodingRepository = mock<GeocodingRepository>()
    private val locationMonitoringService = noOpLocationMonitoringService()
    private val destinationRepository = mock<DestinationRepository> {
        every { getTopDestinations(any()) } returns emptyList()
    }
    private val gpxRepository = mock<GpxRepository> {
        everySuspend { getRecentGpxFiles(any()) } returns emptyList()
    }

    private lateinit var placeFinderViewModel: PlaceFinderViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        placeFinderViewModel = PlaceFinderViewModel(
            geocodingRepository = geocodingRepository,
            locationMonitoringService = locationMonitoringService,
            destinationRepository = destinationRepository,
            gpxRepository = gpxRepository,
        )
        testDispatcher.scheduler.runCurrent()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Given view model init, When observed, Then uiState is default`() {
        runTest {
            placeFinderViewModel.uiState.value shouldBe PlaceFinderUiState.Default
        }
    }

    @Test
    fun `Given top destinations from repository, When view model init, Then uiState exposes them`() {
        runTest {
            val topDestinations = listOf(
                Destination(
                    osmId = "1",
                    name = "Kékestető",
                    town = "Mátraszentimre",
                    type = DestinationType.HIGHEST_PEAK,
                    location = Location(47.8721, 20.0102),
                    description = SharedRes.strings.destinations_type_peak,
                    popularity = 10,
                ),
            )
            every { destinationRepository.getTopDestinations(any()) } returns topDestinations

            val viewModel = PlaceFinderViewModel(
                geocodingRepository = geocodingRepository,
                locationMonitoringService = locationMonitoringService,
                destinationRepository = destinationRepository,
                gpxRepository = gpxRepository,
            )
            testDispatcher.scheduler.runCurrent()

            viewModel.uiState.value.topDestinations shouldBe topDestinations
        }
    }

    @Test
    fun `Given recent gpx files from repository, When view model init, Then uiState exposes them`() {
        runTest {
            val recentGpxFiles = listOf(gpxFileItem("okt15.gpx"), gpxFileItem("pilis.gpx"))
            everySuspend { gpxRepository.getRecentGpxFiles(any()) } returns recentGpxFiles

            val viewModel = PlaceFinderViewModel(
                geocodingRepository = geocodingRepository,
                locationMonitoringService = locationMonitoringService,
                destinationRepository = destinationRepository,
                gpxRepository = gpxRepository,
            )
            testDispatcher.scheduler.runCurrent()

            viewModel.uiState.value.recentGpxFiles shouldBe recentGpxFiles
        }
    }

    private fun gpxFileItem(fileName: String): GpxFileItem =
        GpxFileItem(
            fileName = fileName,
            fileUri = "uri/$fileName",
            trackId = "track-$fileName",
            title = "Title of $fileName",
            totalDistance = 10.kilometers,
            travelTime = 2.hours,
            incline = 100,
            decline = 100,
            lastModified = Clock.System.now(),
            lastOpened = Clock.System.now(),
        )

    @Test
    fun `Given valid search text, When autocomplete succeeds, Then uiState shows loading then mapped places`() {
        runTest {
            val networkPlace = locationIqPlace(
                placeId = "budapest-id",
                lat = 47.4979,
                lon = 19.0402,
                displayName = "Budapest, Hungary",
                displayPlace = "Budapest",
                displayAddress = "Hungary",
            )

            everySuspend {
                geocodingRepository.autocomplete("Budapest")
            } returns NetworkResult.Success(listOf(networkPlace))

            placeFinderViewModel.uiState.test {
                awaitItem() shouldBe PlaceFinderUiState.Default

                placeFinderViewModel.onEvent(PlaceFinderUiEvents.SearchTextChanged("  Budapest  "))

                awaitItem() shouldBe PlaceFinderUiState(
                    searchText = "  Budapest  ",
                    isLoading = true,
                )

                testDispatcher.scheduler.advanceTimeBy(800)
                testDispatcher.scheduler.advanceUntilIdle()

                val state = awaitItem()
                state shouldBe PlaceFinderUiState(
                    searchText = "  Budapest  ",
                    isLoading = false,
                    places = listOf(
                        Place(
                            id = "budapest-id",
                            title = "Budapest",
                            subtitle = "Hungary",
                            location = Location(47.4979, 19.0402),
                            osmType = OsmType.RELATION,
                            distance = state.places.single().distance,
                        ),
                    ),
                )
            }
        }
    }

    @Test
    fun `Given known last location, When autocomplete succeeds, Then places show distance from that location`() {
        runTest {
            val userLocation = Location(47.4979, 19.0402)
            val placeLocation = Location(46.2530, 20.1414)
            val expectedDistance = DistanceFormatter.formatRoundedDistance(
                userLocation.distanceBetween(placeLocation),
            )

            val viewModel = PlaceFinderViewModel(
                geocodingRepository = geocodingRepository,
                locationMonitoringService = locationMonitoringService(lastKnownLocation = userLocation),
                destinationRepository = destinationRepository,
                gpxRepository = gpxRepository,
            )
            testDispatcher.scheduler.runCurrent()

            everySuspend {
                geocodingRepository.autocomplete("Szeged")
            } returns NetworkResult.Success(
                listOf(
                    locationIqPlace(
                        placeId = "szeged-id",
                        lat = placeLocation.latitude,
                        lon = placeLocation.longitude,
                        displayName = "Szeged, Hungary",
                        displayAddress = "Hungary",
                    ),
                ),
            )

            viewModel.uiState.test {
                awaitItem()

                viewModel.onEvent(PlaceFinderUiEvents.SearchTextChanged("Szeged"))
                awaitItem().isLoading shouldBe true

                testDispatcher.scheduler.advanceTimeBy(800)
                testDispatcher.scheduler.advanceUntilIdle()

                awaitItem().places.single().distance shouldBe expectedDistance
            }
        }
    }

    @Test
    fun `Given no last location, When autocomplete succeeds, Then places have no distance`() {
        runTest {
            everySuspend {
                geocodingRepository.autocomplete("Szeged")
            } returns NetworkResult.Success(
                listOf(
                    locationIqPlace(
                        placeId = "szeged-id",
                        lat = 46.2530,
                        lon = 20.1414,
                        displayName = "Szeged, Hungary",
                        displayAddress = "Hungary",
                    ),
                ),
            )

            placeFinderViewModel.uiState.test {
                awaitItem()

                placeFinderViewModel.onEvent(PlaceFinderUiEvents.SearchTextChanged("Szeged"))
                awaitItem().isLoading shouldBe true

                testDispatcher.scheduler.advanceTimeBy(800)
                testDispatcher.scheduler.advanceUntilIdle()

                awaitItem().places.single().distance shouldBe null
            }
        }
    }

    @Test
    fun `Given last location never returns, When timeout elapses, Then search still completes without distance`() {
        runTest {
            val viewModel = PlaceFinderViewModel(
                geocodingRepository = geocodingRepository,
                locationMonitoringService = hangingLocationMonitoringService(),
                destinationRepository = destinationRepository,
                gpxRepository = gpxRepository,
            )
            testDispatcher.scheduler.runCurrent()

            everySuspend {
                geocodingRepository.autocomplete("Szeged")
            } returns NetworkResult.Success(
                listOf(
                    locationIqPlace(
                        placeId = "szeged-id",
                        lat = 46.2530,
                        lon = 20.1414,
                        displayName = "Szeged, Hungary",
                        displayAddress = "Hungary",
                    ),
                ),
            )

            viewModel.uiState.test {
                awaitItem()

                viewModel.onEvent(PlaceFinderUiEvents.SearchTextChanged("Szeged"))
                awaitItem().isLoading shouldBe true

                testDispatcher.scheduler.advanceTimeBy(800)
                testDispatcher.scheduler.advanceUntilIdle()

                val state = awaitItem()
                state.isLoading shouldBe false
                state.places.single().distance shouldBe null
            }
        }
    }

    @Test
    fun `Given valid search text, When autocomplete fails, Then uiState has error and empty places`() {
        runTest {
            everySuspend {
                geocodingRepository.autocomplete("Balaton")
            } returns NetworkResult.Error(NetworkError.NO_INTERNET)

            placeFinderViewModel.uiState.test {
                awaitItem() shouldBe PlaceFinderUiState.Default

                placeFinderViewModel.onEvent(PlaceFinderUiEvents.SearchTextChanged("Balaton"))

                awaitItem() shouldBe PlaceFinderUiState(
                    searchText = "Balaton",
                    isLoading = true,
                )

                testDispatcher.scheduler.advanceTimeBy(800)
                testDispatcher.scheduler.advanceUntilIdle()

                awaitItem() shouldBe PlaceFinderUiState(
                    searchText = "Balaton",
                    isLoading = false,
                    places = emptyList(),
                    error = NetworkError.NO_INTERNET.toInfoViewData(),
                )
            }
        }
    }

    @Test
    fun `Given successful results, When search text becomes shorter than minimum, Then uiState clears the state`() {
        runTest {
            everySuspend {
                geocodingRepository.autocomplete("Matra")
            } returns NetworkResult.Success(
                listOf(
                    locationIqPlace(
                        placeId = "matra-id",
                        lat = 47.8721,
                        lon = 20.0324,
                        displayName = "Matra, Hungary",
                        displayAddress = "Hungary",
                    ),
                ),
            )

            placeFinderViewModel.uiState.test {
                awaitItem()

                placeFinderViewModel.onEvent(PlaceFinderUiEvents.SearchTextChanged("Matra"))
                awaitItem().isLoading shouldBe true

                testDispatcher.scheduler.advanceTimeBy(800)
                testDispatcher.scheduler.advanceUntilIdle()

                val places = awaitItem().places
                places shouldBe listOf(
                    Place(
                        id = "matra-id",
                        title = "Matra, Hungary",
                        subtitle = "Hungary",
                        location = Location(47.8721, 20.0324),
                        osmType = OsmType.RELATION,
                        distance = places.single().distance,
                    ),
                )

                placeFinderViewModel.onEvent(PlaceFinderUiEvents.SearchTextChanged("  ma "))

                awaitItem() shouldBe PlaceFinderUiState(
                    searchText = "  ma ",
                    isLoading = false,
                    places = emptyList(),
                    error = null,
                )

                testDispatcher.scheduler.advanceUntilIdle()
            }
        }
    }

    @Test
    fun `Given quick consecutive searches, When debounce expires once, Then only the latest query is sent`() {
        runTest {
            everySuspend {
                geocodingRepository.autocomplete("Budapest")
            } returns NetworkResult.Success(
                listOf(
                    locationIqPlace(
                        placeId = "budapest-id",
                        lat = 47.4979,
                        lon = 19.0402,
                        displayName = "Budapest, Hungary",
                    ),
                ),
            )

            placeFinderViewModel.uiState.test {
                awaitItem()

                placeFinderViewModel.onEvent(PlaceFinderUiEvents.SearchTextChanged("Bud"))
                awaitItem() shouldBe PlaceFinderUiState(
                    searchText = "Bud",
                    isLoading = true,
                )

                testDispatcher.scheduler.advanceTimeBy(400)

                placeFinderViewModel.onEvent(PlaceFinderUiEvents.SearchTextChanged("Budapest"))
                awaitItem() shouldBe PlaceFinderUiState(
                    searchText = "Budapest",
                    isLoading = true,
                )

                testDispatcher.scheduler.advanceTimeBy(800)
                testDispatcher.scheduler.advanceUntilIdle()

                val state = awaitItem()
                state shouldBe PlaceFinderUiState(
                    searchText = "Budapest",
                    isLoading = false,
                    places = listOf(
                        Place(
                            id = "budapest-id",
                            title = "Budapest, Hungary",
                            subtitle = null,
                            location = Location(47.4979, 19.0402),
                            osmType = OsmType.RELATION,
                            distance = state.places.single().distance,
                        ),
                    ),
                    error = null,
                )
            }
        }
    }

    @Test
    fun `Given failed search, When retry, Then autocomplete runs again after debounce and clears the error`() {
        runTest {
            var autocompleteCallCount = 0
            placeFinderViewModel = PlaceFinderViewModel(
                geocodingRepository = object : GeocodingRepository {
                    override suspend fun autocomplete(searchText: String): NetworkResult<List<LocationIqPlace>> {
                        autocompleteCallCount += 1
                        return if (autocompleteCallCount == 1) {
                            NetworkResult.Error(NetworkError.NO_INTERNET)
                        } else {
                            NetworkResult.Success(
                                listOf(
                                    locationIqPlace(
                                        placeId = "balaton-id",
                                        lat = 46.8797,
                                        lon = 17.8864,
                                        displayName = "Lake Balaton, Hungary",
                                    ),
                                ),
                            )
                        }
                    }

                    override suspend fun reverseGeocode(location: Location): NetworkResult<LocationIqPlace?> {
                        error("Not used in this test")
                    }
                },
                locationMonitoringService = locationMonitoringService,
                destinationRepository = destinationRepository,
                gpxRepository = gpxRepository,
            )
            testDispatcher.scheduler.runCurrent()

            placeFinderViewModel.uiState.test {
                awaitItem() shouldBe PlaceFinderUiState.Default

                placeFinderViewModel.onEvent(PlaceFinderUiEvents.SearchTextChanged("Balaton"))
                awaitItem() shouldBe PlaceFinderUiState(
                    searchText = "Balaton",
                    isLoading = true,
                )

                testDispatcher.scheduler.advanceTimeBy(800)
                testDispatcher.scheduler.advanceUntilIdle()

                awaitItem() shouldBe PlaceFinderUiState(
                    searchText = "Balaton",
                    isLoading = false,
                    places = emptyList(),
                    error = NetworkError.NO_INTERNET.toInfoViewData(),
                )

                placeFinderViewModel.onEvent(PlaceFinderUiEvents.RetryClicked)

                awaitItem() shouldBe PlaceFinderUiState(
                    searchText = "Balaton",
                    isLoading = true,
                    places = emptyList(),
                    error = null,
                )

                testDispatcher.scheduler.advanceTimeBy(800)
                testDispatcher.scheduler.advanceUntilIdle()

                val state = awaitItem()
                state shouldBe PlaceFinderUiState(
                    searchText = "Balaton",
                    isLoading = false,
                    places = listOf(
                        Place(
                            id = "balaton-id",
                            title = "Lake Balaton, Hungary",
                            subtitle = null,
                            location = Location(46.8797, 17.8864),
                            osmType = OsmType.RELATION,
                            distance = state.places.single().distance,
                        ),
                    ),
                    error = null,
                )

                autocompleteCallCount shouldBe 2
            }
        }
    }

    @Test
    fun `Given retry tapped repeatedly within debounce, When debounce expires, Then autocomplete runs only once`() {
        runTest {
            var autocompleteCallCount = 0
            placeFinderViewModel = PlaceFinderViewModel(
                geocodingRepository = object : GeocodingRepository {
                    override suspend fun autocomplete(searchText: String): NetworkResult<List<LocationIqPlace>> {
                        autocompleteCallCount += 1
                        return NetworkResult.Error(NetworkError.NO_INTERNET)
                    }

                    override suspend fun reverseGeocode(location: Location): NetworkResult<LocationIqPlace?> {
                        error("Not used in this test")
                    }
                },
                locationMonitoringService = locationMonitoringService,
                destinationRepository = destinationRepository,
                gpxRepository = gpxRepository,
            )
            testDispatcher.scheduler.runCurrent()

            placeFinderViewModel.uiState.test {
                awaitItem()

                placeFinderViewModel.onEvent(PlaceFinderUiEvents.SearchTextChanged("Balaton"))
                awaitItem()
                testDispatcher.scheduler.advanceTimeBy(800)
                testDispatcher.scheduler.advanceUntilIdle()
                awaitItem()

                repeat(5) {
                    placeFinderViewModel.onEvent(PlaceFinderUiEvents.RetryClicked)
                    testDispatcher.scheduler.advanceTimeBy(100)
                }
                awaitItem()

                testDispatcher.scheduler.advanceTimeBy(800)
                testDispatcher.scheduler.advanceUntilIdle()
                awaitItem()

                autocompleteCallCount shouldBe 2
            }
        }
    }

    private fun noOpLocationMonitoringService() = locationMonitoringService(lastKnownLocation = null)

    private fun hangingLocationMonitoringService() =
        object : LocationMonitoringService {
            override val locationUpdates: SharedFlow<Location> = MutableSharedFlow()
            override suspend fun lastKnownLocation(): Location? = awaitCancellation()
        }

    private fun locationMonitoringService(lastKnownLocation: Location?) =
        object : LocationMonitoringService {
            override val locationUpdates: SharedFlow<Location> = MutableSharedFlow()
            override suspend fun lastKnownLocation(): Location? = lastKnownLocation
        }

    private fun locationIqPlace(
        placeId: String,
        lat: Double,
        lon: Double,
        displayName: String,
        displayPlace: String? = null,
        displayAddress: String? = null,
    ) = LocationIqPlace(
        placeId = placeId,
        osmId = "osm-$placeId",
        osmType = "relation",
        licence = "licence",
        lat = lat,
        lon = lon,
        displayName = displayName,
        displayPlace = displayPlace,
        displayAddress = displayAddress,
    )
}
