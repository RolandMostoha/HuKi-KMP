package hu.mostoha.mobile.kmp.huki.features.gpxcollection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import hu.mostoha.mobile.kmp.huki.logger.trimLongLists
import hu.mostoha.mobile.kmp.huki.model.analytics.AnalyticsEvent
import hu.mostoha.mobile.kmp.huki.model.analytics.GpxShareSource
import hu.mostoha.mobile.kmp.huki.model.analytics.Screen
import hu.mostoha.mobile.kmp.huki.model.domain.GpxFileHeader
import hu.mostoha.mobile.kmp.huki.model.domain.GpxFileItem
import hu.mostoha.mobile.kmp.huki.model.domain.GpxFileSection
import hu.mostoha.mobile.kmp.huki.repository.GpxRepository
import hu.mostoha.mobile.kmp.huki.service.AnalyticsService
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

class GpxCollectionViewModel(
    private val gpxRepository: GpxRepository,
    private val analyticsService: AnalyticsService,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GpxCollectionUiState.Default)
    val uiState: StateFlow<GpxCollectionUiState> = _uiState.asStateFlow()

    private val _uiEffects = Channel<GpxCollectionUiEffects>(Channel.BUFFERED)
    val uiEffects: Flow<GpxCollectionUiEffects> = _uiEffects.receiveAsFlow()

    init {
        analyticsService.logEvent(AnalyticsEvent.ScreenView(Screen.GPX_HISTORY))
        initLogging()
        loadGpxFiles()
    }

    fun onEvent(event: GpxCollectionUiEvents) {
        Logger.d { "GpxCollectionEvent: $event" }
        when (event) {
            GpxCollectionUiEvents.BackClicked -> sendEffect(GpxCollectionUiEffects.NavigateBack)
            GpxCollectionUiEvents.HelpClicked -> openTutorial()
            is GpxCollectionUiEvents.FileClicked -> sendEffect(GpxCollectionUiEffects.OpenGpx(event.file.fileUri))
            is GpxCollectionUiEvents.ShareClicked -> shareGpx(event.file)
            is GpxCollectionUiEvents.DeleteClicked -> _uiState.update { it.copy(pendingDelete = event.file) }
            GpxCollectionUiEvents.DeleteDismissed -> _uiState.update { it.copy(pendingDelete = null) }
            GpxCollectionUiEvents.DeleteConfirmed -> deletePendingFile()
        }
    }

    private fun shareGpx(file: GpxFileItem) {
        analyticsService.logEvent(AnalyticsEvent.GpxShared(GpxShareSource.COLLECTION))
        sendEffect(GpxCollectionUiEffects.ShareGpx(fileUri = file.fileUri, fileName = file.fileName))
    }

    private fun openTutorial() {
        sendEffect(GpxCollectionUiEffects.NavigateToTutorial)
    }

    private fun loadGpxFiles() {
        viewModelScope.launch { refreshFiles() }
    }

    private fun deletePendingFile() {
        val file = _uiState.value.pendingDelete ?: return
        analyticsService.logEvent(AnalyticsEvent.GpxDeleted)
        viewModelScope.launch {
            gpxRepository.deleteGpxFile(file.fileUri, file.trackId)
            refreshFiles()
        }
    }

    private suspend fun refreshFiles() {
        val files = gpxRepository.getGpxFiles()
        _uiState.update { uiState ->
            uiState.copy(
                isLoading = false,
                fileCount = files.size,
                sections = files.groupIntoSections(),
                pendingDelete = null,
            )
        }
    }

    private fun List<GpxFileItem>.groupIntoSections(): List<GpxFileSection> {
        val timeZone = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(timeZone).date
        val yesterday = today.minus(DatePeriod(days = 1))

        return this
            .sortedByDescending { it.lastOpened ?: it.lastModified }
            .groupBy { (it.lastOpened ?: it.lastModified).toLocalDateTime(timeZone).date }
            .map { (date, files) ->
                val header = when (date) {
                    today -> GpxFileHeader.Today
                    yesterday -> GpxFileHeader.Yesterday
                    else -> GpxFileHeader.Date(date.formatAsLabel())
                }
                GpxFileSection(header = header, files = files)
            }
    }

    private fun LocalDate.formatAsLabel(): String = toString().replace('-', '.')

    private fun sendEffect(uiEffect: GpxCollectionUiEffects) {
        viewModelScope.launch {
            Logger.d { "UiEffect: ${uiEffect.toString().trimLongLists()}" }
            _uiEffects.send(uiEffect)
        }
    }

    private fun initLogging() {
        uiState
            .onEach { Logger.d { "GpxCollectionState: ${it.toString().trimLongLists()}" } }
            .launchIn(viewModelScope)
    }
}
