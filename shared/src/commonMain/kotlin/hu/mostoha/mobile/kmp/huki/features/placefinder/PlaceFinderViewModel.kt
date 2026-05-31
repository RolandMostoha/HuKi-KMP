package hu.mostoha.mobile.kmp.huki.features.placefinder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.toPlaceSearchResult
import hu.mostoha.mobile.kmp.huki.model.network.NetworkResult
import hu.mostoha.mobile.kmp.huki.network.toInfoViewData
import hu.mostoha.mobile.kmp.huki.repository.GeocodingRepository
import kotlinx.coroutines.FlowPreview
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

@OptIn(FlowPreview::class)
class PlaceFinderViewModel(private val geocodingRepository: GeocodingRepository) : ViewModel() {
    private companion object {
        const val AUTOCOMPLETE_DEBOUNCE_MILLIS = 800L
        const val MIN_CHARACTERS = 3

        // TODO Replace with the user's current location from location services.
        val FAKE_USER_LOCATION = Location(latitude = 47.7168079, longitude = 18.8950729)
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
                .debounce(AUTOCOMPLETE_DEBOUNCE_MILLIS)
                .filter { it.length >= MIN_CHARACTERS }
                .collectLatest { query -> searchPlaces(query) }
        }
    }

    fun onEvent(event: PlaceFinderUiEvents) {
        when (event) {
            is PlaceFinderUiEvents.SearchTextChanged -> onSearchTextChanged(event.searchText)
            PlaceFinderUiEvents.RetryClicked -> onRetryClicked()
        }
    }

    private fun onSearchTextChanged(searchText: String) {
        val trimmedSearchText = searchText.trim()
        val isTooShort = trimmedSearchText.length < MIN_CHARACTERS

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
        if (trimmedSearchText.length < MIN_CHARACTERS) return

        _uiState.update { uiState ->
            uiState.copy(isLoading = true, error = null)
        }

        searchQueries.tryEmit(trimmedSearchText)
    }

    private suspend fun searchPlaces(query: String) {
        when (val result = geocodingRepository.autocomplete(query)) {
            is NetworkResult.Success -> _uiState.update { uiState ->
                uiState.copy(
                    isLoading = false,
                    places = result.data.map { it.toPlaceSearchResult(FAKE_USER_LOCATION) },
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
