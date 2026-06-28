package hu.mostoha.mobile.kmp.huki.features.placehistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import hu.mostoha.mobile.kmp.huki.logger.trimLongLists
import hu.mostoha.mobile.kmp.huki.model.domain.OsmType
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceHistoryHeader
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceHistoryItem
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceHistorySection
import hu.mostoha.mobile.kmp.huki.repository.PlaceHistoryRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

class PlaceHistoryViewModel(
    private val placeHistoryRepository: PlaceHistoryRepository,
    private val clock: Clock,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlaceHistoryUiState.Default)
    val uiState: StateFlow<PlaceHistoryUiState> = _uiState.asStateFlow()

    private val _uiEffects = Channel<PlaceHistoryUiEffects>(Channel.BUFFERED)
    val uiEffects: Flow<PlaceHistoryUiEffects> = _uiEffects.receiveAsFlow()

    init {
        initLogging()
        loadPlaceHistory()
    }

    fun onEvent(event: PlaceHistoryUiEvents) {
        Logger.d { "PlaceHistoryEvent: $event" }
        when (event) {
            PlaceHistoryUiEvents.BackClicked -> sendEffect(PlaceHistoryUiEffects.NavigateBack)
            is PlaceHistoryUiEvents.PlaceClicked -> sendEffect(
                PlaceHistoryUiEffects.OpenPlace(
                    osmType = event.place.osmType ?: OsmType.NODE,
                    osmId = event.place.osmId,
                ),
            )
        }
    }

    private fun loadPlaceHistory() {
        viewModelScope.launch {
            val items = placeHistoryRepository.getPlaceHistory()
            _uiState.update { uiState ->
                uiState.copy(
                    isLoading = false,
                    placeCount = items.size,
                    sections = items.groupIntoSections(),
                )
            }
        }
    }

    private fun List<PlaceHistoryItem>.groupIntoSections(): List<PlaceHistorySection> {
        val timeZone = TimeZone.currentSystemDefault()
        val today = clock.now().toLocalDateTime(timeZone).date
        val yesterday = today.minus(DatePeriod(days = 1))

        return this
            .sortedByDescending { it.lastVisited }
            .groupBy { it.lastVisited.toLocalDateTime(timeZone).date }
            .map { (date, items) ->
                val header = when (date) {
                    today -> PlaceHistoryHeader.Today
                    yesterday -> PlaceHistoryHeader.Yesterday
                    else -> PlaceHistoryHeader.Date(date.formatAsLabel())
                }
                PlaceHistorySection(header = header, items = items)
            }
    }

    private fun LocalDate.formatAsLabel(): String = toString().replace('-', '.')

    private fun sendEffect(uiEffect: PlaceHistoryUiEffects) {
        viewModelScope.launch {
            Logger.d { "UiEffect: ${uiEffect.toString().trimLongLists()}" }
            _uiEffects.send(uiEffect)
        }
    }

    private fun initLogging() {
        uiState
            .onEach { Logger.d { "PlaceHistoryState: ${it.toString().trimLongLists()}" } }
            .launchIn(viewModelScope)
    }
}
