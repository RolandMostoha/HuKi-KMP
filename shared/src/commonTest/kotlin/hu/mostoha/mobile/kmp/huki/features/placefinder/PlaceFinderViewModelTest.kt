package hu.mostoha.mobile.kmp.huki.features.placefinder

import app.cash.turbine.test
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.Place
import hu.mostoha.mobile.kmp.huki.model.network.LocationIqPlace
import hu.mostoha.mobile.kmp.huki.model.network.NetworkError
import hu.mostoha.mobile.kmp.huki.model.network.NetworkResult
import hu.mostoha.mobile.kmp.huki.network.toInfoViewData
import hu.mostoha.mobile.kmp.huki.repository.GeocodingRepository
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlaceFinderViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val geocodingRepository = mock<GeocodingRepository>()

    private lateinit var placeFinderViewModel: PlaceFinderViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        placeFinderViewModel = PlaceFinderViewModel(
            geocodingRepository = geocodingRepository,
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

                awaitItem() shouldBe PlaceFinderUiState(
                    searchText = "  Budapest  ",
                    isLoading = false,
                    places = listOf(
                        Place(
                            id = "budapest-id",
                            title = "Budapest",
                            subtitle = "Hungary",
                            location = Location(47.4979, 19.0402),
                        ),
                    ),
                )
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

                awaitItem().places shouldBe listOf(
                    Place(
                        id = "matra-id",
                        title = "Matra, Hungary",
                        subtitle = "Hungary",
                        location = Location(47.8721, 20.0324),
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

                awaitItem() shouldBe PlaceFinderUiState(
                    searchText = "Budapest",
                    isLoading = false,
                    places = listOf(
                        Place(
                            id = "budapest-id",
                            title = "Budapest, Hungary",
                            subtitle = null,
                            location = Location(47.4979, 19.0402),
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

                awaitItem() shouldBe PlaceFinderUiState(
                    searchText = "Balaton",
                    isLoading = false,
                    places = listOf(
                        Place(
                            id = "balaton-id",
                            title = "Lake Balaton, Hungary",
                            subtitle = null,
                            location = Location(46.8797, 17.8864),
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
