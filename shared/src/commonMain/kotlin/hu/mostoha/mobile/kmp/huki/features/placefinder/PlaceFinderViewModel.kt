package hu.mostoha.mobile.kmp.huki.features.placefinder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hu.mostoha.mobile.kmp.huki.model.analytics.AnalyticsEvent
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.mapper.toInfoViewData
import hu.mostoha.mobile.kmp.huki.model.mapper.toPlaceSearchResult
import hu.mostoha.mobile.kmp.huki.model.mapper.withDistanceFrom
import hu.mostoha.mobile.kmp.huki.model.network.NetworkError
import hu.mostoha.mobile.kmp.huki.model.network.NetworkResult
import hu.mostoha.mobile.kmp.huki.repository.DestinationRepository
import hu.mostoha.mobile.kmp.huki.repository.GeocodingRepository
import hu.mostoha.mobile.kmp.huki.repository.GpxRepository
import hu.mostoha.mobile.kmp.huki.repository.PlaceHistoryRepository
import hu.mostoha.mobile.kmp.huki.service.AnalyticsService
import hu.mostoha.mobile.kmp.huki.service.LocationMonitoringService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(FlowPreview::class)
class PlaceFinderViewModel(
    private val geocodingRepository: GeocodingRepository,
    private val locationMonitoringService: LocationMonitoringService,
    private val destinationRepository: DestinationRepository,
    private val gpxRepository: GpxRepository,
    private val placeHistoryRepository: PlaceHistoryRepository,
    private val analyticsService: AnalyticsService,
    private val defaultDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private companion object {
        private val AUTOCOMPLETE_DEBOUNCE = 1100.milliseconds
        private val AUTOCOMPLETE_LOCATION_TIMEOUT = 2.seconds
        private const val AUTOCOMPLETE_MIN_CHARACTERS = 3
        private const val RECENT_GPX_LIMIT = 3
        private const val RECENT_PLACES_LIMIT = 3
        private const val SEARCH_RECENT_PLACES_LIMIT = 5
        private const val SEARCH_DESTINATIONS_LIMIT = 20
    }

    private val _uiState = MutableStateFlow(PlaceFinderUiState.Default)
    val uiState: StateFlow<PlaceFinderUiState> = _uiState.asStateFlow()

    private val searchQueries = MutableSharedFlow<String>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    init {
        loadTopDestinations()
        loadRecentPlaces()
        loadRecentGpxFiles()

        viewModelScope.launch {
            searchQueries.collectLatest { query -> searchLocal(query) }
        }

        viewModelScope.launch {
            searchQueries
                .debounce(AUTOCOMPLETE_DEBOUNCE)
                .filter { it.length >= AUTOCOMPLETE_MIN_CHARACTERS }
                .collectLatest { query -> searchPlaces(query) }
        }
    }

    fun onEvent(event: PlaceFinderUiEvents) {
        when (event) {
            is PlaceFinderUiEvents.SearchTextChanged -> onSearchTextChanged(event.searchText)
            PlaceFinderUiEvents.RetryClicked -> onRetryClicked()
        }
    }

    fun clear() {
        viewModelScope.cancel()
    }

    private fun loadTopDestinations() {
        viewModelScope.launch {
            val location = lastKnownLocation()
            val destinations = withContext(defaultDispatcher) {
                destinationRepository.getTopDestinations(location)
            }
            _uiState.update { uiState -> uiState.copy(topDestinations = destinations) }
        }
    }

    private fun loadRecentPlaces() {
        viewModelScope.launch {
            val places = placeHistoryRepository.getRecentPlaces(RECENT_PLACES_LIMIT)
            val userLocation = lastKnownLocation()
            val recentPlaces = places.map { it.withDistanceFrom(userLocation) }
            _uiState.update { uiState -> uiState.copy(recentPlaces = recentPlaces) }
        }
    }

    private suspend fun lastKnownLocation(): Location? =
        withTimeoutOrNull(AUTOCOMPLETE_LOCATION_TIMEOUT) {
            locationMonitoringService.lastKnownLocation()
        }

    private fun loadRecentGpxFiles() {
        viewModelScope.launch {
            val recentGpxFiles = gpxRepository.getRecentGpxFiles(RECENT_GPX_LIMIT)
            _uiState.update { uiState -> uiState.copy(recentGpxFiles = recentGpxFiles) }
        }
    }

    private fun onSearchTextChanged(searchText: String) {
        if (searchText == _uiState.value.searchText) return

        val trimmedSearchText = searchText.trim()
        val isTooShort = trimmedSearchText.length < AUTOCOMPLETE_MIN_CHARACTERS

        _uiState.update { uiState ->
            uiState.copy(
                searchText = searchText,
                isLoading = !isTooShort,
                places = if (isTooShort) emptyList() else uiState.places,
                error = null,
            )
        }

        searchQueries.tryEmit(trimmedSearchText)
    }

    private fun onRetryClicked() {
        val trimmedSearchText = _uiState.value.searchText.trim()
        if (trimmedSearchText.length < AUTOCOMPLETE_MIN_CHARACTERS) return

        _uiState.update { uiState ->
            uiState.copy(isLoading = true, error = null)
        }

        searchQueries.tryEmit(trimmedSearchText)
    }

    private suspend fun searchLocal(query: String) {
        if (query.isBlank()) {
            _uiState.update { uiState ->
                uiState.copy(searchRecentPlaces = emptyList(), searchDestinations = emptyList())
            }
            return
        }
        val places = placeHistoryRepository.searchPlaces(query, SEARCH_RECENT_PLACES_LIMIT)
        val userLocation = lastKnownLocation()
        val recentPlaces = places.map { it.withDistanceFrom(userLocation) }
        val destinations = withContext(defaultDispatcher) {
            destinationRepository.searchDestinations(query, SEARCH_DESTINATIONS_LIMIT)
        }
        _uiState.update { uiState ->
            uiState.copy(
                searchRecentPlaces = recentPlaces,
                searchDestinations = destinations,
            )
        }
    }

    private suspend fun searchPlaces(query: String) {
        val currentLocation = lastKnownLocation()

        when (val result = geocodingRepository.autocomplete(query)) {
            is NetworkResult.Success -> {
                if (result.data.isEmpty()) {
                    analyticsService.logEvent(AnalyticsEvent.SearchEmpty)
                }
                _uiState.update { uiState ->
                    uiState.copy(
                        isLoading = false,
                        places = result.data.map { it.toPlaceSearchResult(currentLocation) },
                        error = null,
                    )
                }
            }
            is NetworkResult.Error -> {
                val event = when (result.error) {
                    NetworkError.RATE_LIMITED -> AnalyticsEvent.SearchRateLimited
                    NetworkError.NO_INTERNET -> AnalyticsEvent.SearchNoInternet
                    else -> AnalyticsEvent.SearchFailed
                }
                analyticsService.logEvent(event)
                _uiState.update { uiState ->
                    uiState.copy(
                        isLoading = false,
                        places = emptyList(),
                        error = result.error.toInfoViewData(),
                    )
                }
            }
        }
    }
}
