package hu.mostoha.mobile.kmp.huki.features.gpxcollection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import hu.mostoha.mobile.kmp.huki.logger.trimLongLists
import hu.mostoha.mobile.kmp.huki.model.analytics.AnalyticsEvent
import hu.mostoha.mobile.kmp.huki.model.analytics.GpxShareSource
import hu.mostoha.mobile.kmp.huki.model.analytics.Screen
import hu.mostoha.mobile.kmp.huki.model.domain.GpxCollectionSortMode
import hu.mostoha.mobile.kmp.huki.model.domain.GpxFileItem
import hu.mostoha.mobile.kmp.huki.model.domain.GpxFileSection
import hu.mostoha.mobile.kmp.huki.repository.GpxRepository
import hu.mostoha.mobile.kmp.huki.service.AnalyticsService
import hu.mostoha.mobile.kmp.huki.service.logScreenView
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
import kotlinx.datetime.TimeZone
import kotlin.time.Clock

class GpxCollectionViewModel(
    private val gpxRepository: GpxRepository,
    private val analyticsService: AnalyticsService,
    private val clock: Clock,
) : ViewModel() {

    private var files: List<GpxFileItem> = emptyList()

    private val _uiState = MutableStateFlow(GpxCollectionUiState.Default)
    val uiState: StateFlow<GpxCollectionUiState> = _uiState.asStateFlow()

    private val _uiEffects = Channel<GpxCollectionUiEffects>(Channel.BUFFERED)
    val uiEffects: Flow<GpxCollectionUiEffects> = _uiEffects.receiveAsFlow()

    init {
        initLogging()
        loadGpxFiles()
    }

    fun onEvent(event: GpxCollectionUiEvents) {
        Logger.d { "GpxCollectionEvent: $event" }
        when (event) {
            GpxCollectionUiEvents.ScreenViewed -> analyticsService.logScreenView(Screen.GPX_HISTORY)
            GpxCollectionUiEvents.BackClicked -> sendEffect(GpxCollectionUiEffects.NavigateBack)
            GpxCollectionUiEvents.HelpClicked -> openTutorial()
            is GpxCollectionUiEvents.SortModeSelected -> selectSortMode(event.sortMode)
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

    private fun selectSortMode(sortMode: GpxCollectionSortMode) {
        if (sortMode == _uiState.value.sortMode) return

        analyticsService.logEvent(AnalyticsEvent.GpxCollectionSorted(sortMode))
        _uiState.update { uiState ->
            uiState.copy(sortMode = sortMode, sections = files.toSections(sortMode))
        }
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
        files = gpxRepository.getGpxFiles()
        _uiState.update { uiState ->
            uiState.copy(
                isLoading = false,
                fileCount = files.size,
                sections = files.toSections(uiState.sortMode),
                pendingDelete = null,
            )
        }
    }

    private fun List<GpxFileItem>.toSections(sortMode: GpxCollectionSortMode): List<GpxFileSection> =
        toGpxFileSections(sortMode = sortMode, now = clock.now(), timeZone = TimeZone.currentSystemDefault())

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
