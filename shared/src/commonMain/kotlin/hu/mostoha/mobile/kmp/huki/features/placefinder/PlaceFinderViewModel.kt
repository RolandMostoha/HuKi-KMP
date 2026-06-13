package hu.mostoha.mobile.kmp.huki.features.placefinder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hu.mostoha.mobile.kmp.huki.model.domain.toPlaceSearchResult
import hu.mostoha.mobile.kmp.huki.model.network.NetworkResult
import hu.mostoha.mobile.kmp.huki.network.toInfoViewData
import hu.mostoha.mobile.kmp.huki.repository.GeocodingRepository
import hu.mostoha.mobile.kmp.huki.service.LocationMonitoringService
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
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(FlowPreview::class)
class PlaceFinderViewModel(
    private val geocodingRepository: GeocodingRepository,
    private val locationMonitoringService: LocationMonitoringService,
) : ViewModel() {
    private companion object {
        private val AUTOCOMPLETE_DEBOUNCE = 800.milliseconds
        private val AUTOCOMPLETE_LOCATION_TIMEOUT = 2.seconds
        private const val AUTOCOMPLETE_MIN_CHARACTERS = 3
    }

    private val _uiState = MutableStateFlow(PlaceFinderUiState.Default)
    val uiState: StateFlow<PlaceFinderUiState> = _uiState.asStateFlow()

    private val searchQueries = MutableSharedFlow<String>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    init {
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
