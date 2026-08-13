package hu.mostoha.mobile.kmp.huki.features.placefinder

import app.cash.turbine.test
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.model.analytics.AnalyticsEvent
import hu.mostoha.mobile.kmp.huki.model.domain.Destination
import hu.mostoha.mobile.kmp.huki.model.domain.DestinationType
import hu.mostoha.mobile.kmp.huki.model.domain.GpxFileItem
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.OsmType
import hu.mostoha.mobile.kmp.huki.model.domain.Place
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceSource
import hu.mostoha.mobile.kmp.huki.model.mapper.toInfoViewData
import hu.mostoha.mobile.kmp.huki.model.network.LocationIqPlace
import hu.mostoha.mobile.kmp.huki.model.network.NetworkError
import hu.mostoha.mobile.kmp.huki.model.network.NetworkResult
import hu.mostoha.mobile.kmp.huki.repository.DestinationRepository
import hu.mostoha.mobile.kmp.huki.repository.GeocodingRepository
import hu.mostoha.mobile.kmp.huki.repository.GpxRepository
import hu.mostoha.mobile.kmp.huki.repository.PlaceHistoryRepository
import hu.mostoha.mobile.kmp.huki.service.FakeAnalyticsService
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
        every { searchDestinations(any(), any()) } returns emptyList()
    }
    private val gpxRepository = mock<GpxRepository> {
        everySuspend { getRecentGpxFiles(any()) } returns emptyList()
    }
    private val placeHistoryRepository = mock<PlaceHistoryRepository> {
        everySuspend { getRecentPlaces(any()) } returns emptyList()
        everySuspend { searchPlaces(any(), any()) } returns emptyList()
    }
    private val analyticsService = FakeAnalyticsService()

    private lateinit var placeFinderViewModel: PlaceFinderViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        placeFinderViewModel = PlaceFinderViewModel(
            geocodingRepository = geocodingRepository,
            locationMonitoringService = locationMonitoringService,
            destinationRepository = destinationRepository,
            gpxRepository = gpxRepository,
            placeHistoryRepository = placeHistoryRepository,
            analyticsService = analyticsService,
            defaultDispatcher = testDispatcher,
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
                placeHistoryRepository = placeHistoryRepository,
                analyticsService = analyticsService,
                defaultDispatcher = testDispatcher,
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
                placeHistoryRepository = placeHistoryRepository,
                analyticsService = analyticsService,
                defaultDispatcher = testDispatcher,
            )
            testDispatcher.scheduler.runCurrent()

            viewModel.uiState.value.recentGpxFiles shouldBe recentGpxFiles
        }
    }

    @Test
    fun `Given recent places from repository, When view model init, Then uiState exposes them`() {
        runTest {
            val recentPlaces = listOf(
                Place(
                    osmId = "1",
                    name = "Dobogókő",
                    placeSource = PlaceSource.SEARCH_AUTOCOMPLETE,
                    address = "Pilis Mountains, Hungary",
                    location = Location(47.7181, 18.8948),
                    osmType = OsmType.NODE,
                ),
            )
            everySuspend { placeHistoryRepository.getRecentPlaces(any()) } returns recentPlaces

            val viewModel = PlaceFinderViewModel(
                geocodingRepository = geocodingRepository,
                locationMonitoringService = locationMonitoringService,
                destinationRepository = destinationRepository,
                gpxRepository = gpxRepository,
                placeHistoryRepository = placeHistoryRepository,
                analyticsService = analyticsService,
                defaultDispatcher = testDispatcher,
            )
            testDispatcher.scheduler.runCurrent()

            viewModel.uiState.value.recentPlaces shouldBe recentPlaces
        }
    }

    @Test
    fun `Given known last location, When recent places are loaded, Then they show distance from that location`() {
        runTest {
            val userLocation = Location(47.4979, 19.0402)
            val placeLocation = Location(46.2530, 20.1414)
            val expectedDistance = DistanceFormatter.formatDistance(
                userLocation.distanceBetween(placeLocation),
            )
            everySuspend { placeHistoryRepository.getRecentPlaces(any()) } returns listOf(
                Place(
                    osmId = "1",
                    name = "Szeged",
                    placeSource = PlaceSource.SEARCH_AUTOCOMPLETE,
                    address = "Hungary",
                    location = placeLocation,
                    osmType = OsmType.NODE,
                ),
            )

            val viewModel = PlaceFinderViewModel(
                geocodingRepository = geocodingRepository,
                locationMonitoringService = locationMonitoringService(lastKnownLocation = userLocation),
                destinationRepository = destinationRepository,
                gpxRepository = gpxRepository,
                placeHistoryRepository = placeHistoryRepository,
                analyticsService = analyticsService,
                defaultDispatcher = testDispatcher,
            )
            testDispatcher.scheduler.runCurrent()

            viewModel.uiState.value.recentPlaces.single().distance shouldBe expectedDistance
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
                            osmId = "osm-budapest-id",
                            name = "Budapest",
                            placeSource = PlaceSource.SEARCH_AUTOCOMPLETE,
                            address = "Hungary",
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
            val expectedDistance = DistanceFormatter.formatDistance(
                userLocation.distanceBetween(placeLocation),
            )

            val viewModel = PlaceFinderViewModel(
                geocodingRepository = geocodingRepository,
                locationMonitoringService = locationMonitoringService(lastKnownLocation = userLocation),
                destinationRepository = destinationRepository,
                gpxRepository = gpxRepository,
                placeHistoryRepository = placeHistoryRepository,
                analyticsService = analyticsService,
                defaultDispatcher = testDispatcher,
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
                placeHistoryRepository = placeHistoryRepository,
                analyticsService = analyticsService,
                defaultDispatcher = testDispatcher,
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
            } returns NetworkResult.Error(NetworkError.UNKNOWN)

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
                    error = NetworkError.UNKNOWN.toInfoViewData(),
                )
                analyticsService.loggedEvents shouldBe listOf(AnalyticsEvent.SearchFailed)
            }
        }
    }

    @Test
    fun `Given valid search text, When autocomplete has no internet, Then search no internet event is logged`() {
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
                analyticsService.loggedEvents shouldBe listOf(AnalyticsEvent.SearchNoInternet)
            }
        }
    }

    @Test
    fun `Given valid search text, When autocomplete is rate limited, Then search rate limited event is logged`() {
        runTest {
            everySuspend {
                geocodingRepository.autocomplete("Balaton")
            } returns NetworkResult.Error(NetworkError.RATE_LIMITED)

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
                    error = NetworkError.RATE_LIMITED.toInfoViewData(),
                )
                analyticsService.loggedEvents shouldBe listOf(AnalyticsEvent.SearchRateLimited)
            }
        }
    }

    @Test
    fun `Given valid search text, When autocomplete returns empty, Then search empty event is logged`() {
        runTest {
            everySuspend {
                geocodingRepository.autocomplete("Nowhere")
            } returns NetworkResult.Success(emptyList())

            placeFinderViewModel.uiState.test {
                awaitItem() shouldBe PlaceFinderUiState.Default

                placeFinderViewModel.onEvent(PlaceFinderUiEvents.SearchTextChanged("Nowhere"))

                awaitItem() shouldBe PlaceFinderUiState(
                    searchText = "Nowhere",
                    isLoading = true,
                )

                testDispatcher.scheduler.advanceTimeBy(800)
                testDispatcher.scheduler.advanceUntilIdle()

                awaitItem() shouldBe PlaceFinderUiState(
                    searchText = "Nowhere",
                    isLoading = false,
                    places = emptyList(),
                    error = null,
                )
                analyticsService.loggedEvents shouldBe listOf(AnalyticsEvent.SearchEmpty)
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
                        osmId = "osm-matra-id",
                        name = "Matra, Hungary",
                        placeSource = PlaceSource.SEARCH_AUTOCOMPLETE,
                        address = "Hungary",
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
                            osmId = "osm-budapest-id",
                            name = "Budapest, Hungary",
                            placeSource = PlaceSource.SEARCH_AUTOCOMPLETE,
                            address = null,
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
                placeHistoryRepository = placeHistoryRepository,
                analyticsService = analyticsService,
                defaultDispatcher = testDispatcher,
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
                            osmId = "osm-balaton-id",
                            name = "Lake Balaton, Hungary",
                            placeSource = PlaceSource.SEARCH_AUTOCOMPLETE,
                            address = null,
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
                placeHistoryRepository = placeHistoryRepository,
                analyticsService = analyticsService,
                defaultDispatcher = testDispatcher,
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

    @Test
    fun `Given completed search, When same search text re-emitted, Then autocomplete does not run again`() {
        runTest {
            var autocompleteCallCount = 0
            placeFinderViewModel = PlaceFinderViewModel(
                geocodingRepository = object : GeocodingRepository {
                    override suspend fun autocomplete(searchText: String): NetworkResult<List<LocationIqPlace>> {
                        autocompleteCallCount += 1
                        return NetworkResult.Success(
                            listOf(locationIqPlace(placeId = "dob-id", lat = 47.7, lon = 18.8, displayName = "Dob")),
                        )
                    }

                    override suspend fun reverseGeocode(location: Location): NetworkResult<LocationIqPlace?> {
                        error("Not used in this test")
                    }
                },
                locationMonitoringService = locationMonitoringService,
                destinationRepository = destinationRepository,
                gpxRepository = gpxRepository,
                placeHistoryRepository = placeHistoryRepository,
                analyticsService = analyticsService,
                defaultDispatcher = testDispatcher,
            )
            testDispatcher.scheduler.runCurrent()

            placeFinderViewModel.onEvent(PlaceFinderUiEvents.SearchTextChanged("Dob"))
            testDispatcher.scheduler.advanceTimeBy(1200)
            testDispatcher.scheduler.advanceUntilIdle()
            autocompleteCallCount shouldBe 1

            placeFinderViewModel.uiState.test {
                awaitItem() shouldBe PlaceFinderUiState(
                    searchText = "Dob",
                    isLoading = false,
                    places = placeFinderViewModel.uiState.value.places,
                )

                placeFinderViewModel.onEvent(PlaceFinderUiEvents.SearchTextChanged("Dob"))
                testDispatcher.scheduler.advanceTimeBy(1200)
                testDispatcher.scheduler.advanceUntilIdle()

                expectNoEvents()
                autocompleteCallCount shouldBe 1
            }
        }
    }

    @Test
    fun `Given local matches, When two characters typed, Then local groups populate instantly and online stays empty`() {
        runTest {
            val recentPlace = Place(
                osmId = "r1",
                name = "Dobogókő",
                placeSource = PlaceSource.SEARCH_AUTOCOMPLETE,
                location = Location(47.7181, 18.8948),
            )
            val destination = Destination(
                osmId = "d1",
                name = "Dobogó-tető",
                town = "Pilisszentkereszt",
                type = DestinationType.PEAK,
                location = Location(47.72, 18.89),
                description = SharedRes.strings.destinations_type_peak,
                popularity = 8,
            )
            everySuspend { placeHistoryRepository.searchPlaces("Do", 5) } returns listOf(recentPlace)
            every { destinationRepository.searchDestinations("Do", 20) } returns listOf(destination)

            placeFinderViewModel.onEvent(PlaceFinderUiEvents.SearchTextChanged("Do"))
            testDispatcher.scheduler.runCurrent()

            val state = placeFinderViewModel.uiState.value
            state.searchRecentPlaces shouldBe listOf(recentPlace)
            state.searchDestinations shouldBe listOf(destination)
            state.places shouldBe emptyList()
            state.isLoading shouldBe false
        }
    }

    @Test
    fun `Given three character query, When debounce elapses, Then online results merge with local groups`() {
        runTest {
            val recentPlace = Place(
                osmId = "r1",
                name = "Pilis",
                placeSource = PlaceSource.SEARCH_AUTOCOMPLETE,
                location = Location(47.6, 18.9),
            )
            everySuspend { placeHistoryRepository.searchPlaces("Pil", 5) } returns listOf(recentPlace)
            everySuspend {
                geocodingRepository.autocomplete("Pil")
            } returns NetworkResult.Success(
                listOf(locationIqPlace(placeId = "pilis-id", lat = 47.6, lon = 18.9, displayName = "Pilis, Hungary")),
            )

            placeFinderViewModel.onEvent(PlaceFinderUiEvents.SearchTextChanged("Pil"))
            testDispatcher.scheduler.runCurrent()

            placeFinderViewModel.uiState.value.searchRecentPlaces shouldBe listOf(recentPlace)
            placeFinderViewModel.uiState.value.places shouldBe emptyList()

            testDispatcher.scheduler.advanceTimeBy(800)
            testDispatcher.scheduler.advanceUntilIdle()

            val state = placeFinderViewModel.uiState.value
            state.places.map { it.name } shouldBe listOf("Pilis, Hungary")
            state.searchRecentPlaces shouldBe listOf(recentPlace)
        }
    }

    @Test
    fun `Given local matches, When online fails, Then local groups remain and error is set`() {
        runTest {
            val recentPlace = Place(
                osmId = "r1",
                name = "Bükk",
                placeSource = PlaceSource.SEARCH_AUTOCOMPLETE,
                location = Location(48.0, 20.5),
            )
            everySuspend { placeHistoryRepository.searchPlaces("Bük", 5) } returns listOf(recentPlace)
            everySuspend {
                geocodingRepository.autocomplete("Bük")
            } returns NetworkResult.Error(NetworkError.NO_INTERNET)

            placeFinderViewModel.onEvent(PlaceFinderUiEvents.SearchTextChanged("Bük"))
            testDispatcher.scheduler.advanceTimeBy(800)
            testDispatcher.scheduler.advanceUntilIdle()

            val state = placeFinderViewModel.uiState.value
            state.searchRecentPlaces shouldBe listOf(recentPlace)
            state.places shouldBe emptyList()
            state.error shouldBe NetworkError.NO_INTERNET.toInfoViewData()
        }
    }

    @Test
    fun `Given local matches, When query cleared, Then local groups are cleared`() {
        runTest {
            val recentPlace = Place(
                osmId = "r1",
                name = "Mátra",
                placeSource = PlaceSource.SEARCH_AUTOCOMPLETE,
                location = Location(47.8, 20.0),
            )
            everySuspend { placeHistoryRepository.searchPlaces("Mát", 5) } returns listOf(recentPlace)

            placeFinderViewModel.onEvent(PlaceFinderUiEvents.SearchTextChanged("Mát"))
            testDispatcher.scheduler.runCurrent()
            placeFinderViewModel.uiState.value.searchRecentPlaces shouldBe listOf(recentPlace)

            placeFinderViewModel.onEvent(PlaceFinderUiEvents.SearchTextChanged(""))
            testDispatcher.scheduler.runCurrent()

            placeFinderViewModel.uiState.value.searchRecentPlaces shouldBe emptyList()
            placeFinderViewModel.uiState.value.searchDestinations shouldBe emptyList()
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
