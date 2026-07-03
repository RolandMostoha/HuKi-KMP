package hu.mostoha.mobile.kmp.huki.features.placefinder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hu.mostoha.mobile.kmp.huki.model.mapper.toInfoViewData
import hu.mostoha.mobile.kmp.huki.model.mapper.toPlaceSearchResult
import hu.mostoha.mobile.kmp.huki.model.network.NetworkResult
import hu.mostoha.mobile.kmp.huki.repository.DestinationRepository
import hu.mostoha.mobile.kmp.huki.repository.GeocodingRepository
import hu.mostoha.mobile.kmp.huki.repository.GpxRepository
import hu.mostoha.mobile.kmp.huki.repository.PlaceHistoryRepository
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
    private val defaultDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private companion object {
        private val AUTOCOMPLETE_DEBOUNCE = 800.milliseconds
        private val AUTOCOMPLETE_LOCATION_TIMEOUT = 2.seconds
        private const val AUTOCOMPLETE_MIN_CHARACTERS = 3
        private const val RECENT_GPX_LIMIT = 3
        private const val RECENT_PLACES_LIMIT = 3
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
            val location = withTimeoutOrNull(AUTOCOMPLETE_LOCATION_TIMEOUT) {
                locationMonitoringService.lastKnownLocation()
            }
            val destinations = withContext(defaultDispatcher) {
                destinationRepository.getTopDestinations(location)
            }
            _uiState.update { uiState -> uiState.copy(topDestinations = destinations) }
        }
    }

    private fun loadRecentPlaces() {
        viewModelScope.launch {
            val recentPlaces = placeHistoryRepository.getRecentPlaces(RECENT_PLACES_LIMIT)
            _uiState.update { uiState -> uiState.copy(recentPlaces = recentPlaces) }
        }
    }

    private fun loadRecentGpxFiles() {
        viewModelScope.launch {
            val recentGpxFiles = gpxRepository.getRecentGpxFiles(RECENT_GPX_LIMIT)
            _uiState.update { uiState -> uiState.copy(recentGpxFiles = recentGpxFiles) }
        }
    }

    private fun onSearchTextChanged(searchText: String) {
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

    private suspend fun searchPlaces(query: String) {
        val currentLocation = withTimeoutOrNull(AUTOCOMPLETE_LOCATION_TIMEOUT) {
            locationMonitoringService.lastKnownLocation()
        }

        when (val result = geocodingRepository.autocomplete(query)) {
            is NetworkResult.Success -> _uiState.update { uiState ->
                uiState.copy(
                    isLoading = false,
                    places = result.data.map { it.toPlaceSearchResult(currentLocation) },
                    error = null,
                )
            }
            is NetworkResult.Error -> _uiState.update { uiState ->
                uiState.copy(
                    isLoading = false,
                    places = emptyList(),
                    error = result.error.toInfoViewData(),
                )
            }
        }
    }
}
