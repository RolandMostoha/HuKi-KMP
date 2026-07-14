package hu.mostoha.mobile.kmp.huki.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import hu.mostoha.mobile.kmp.huki.logger.trimLongLists
import hu.mostoha.mobile.kmp.huki.repository.SettingsRepository
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

class SettingsViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState.Default)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _uiEffects = Channel<SettingsUiEffects>(Channel.BUFFERED)
    val uiEffects: Flow<SettingsUiEffects> = _uiEffects.receiveAsFlow()

    init {
        initLogging()
        observeSettings()
    }

    fun onEvent(event: SettingsUiEvents) {
        Logger.d { "SettingsEvent: $event" }
        when (event) {
            SettingsUiEvents.BackClicked -> sendEffect(SettingsUiEffects.NavigateBack)
            is SettingsUiEvents.MapZoomControlsToggled -> viewModelScope.launch {
                settingsRepository.setMapZoomControlsVisible(event.visible)
            }
        }
    }

    private fun observeSettings() {
        settingsRepository.settings
            .onEach { settings ->
                _uiState.update { it.copy(mapZoomControlsVisible = settings.mapZoomControlsVisible) }
            }
            .launchIn(viewModelScope)
    }

    private fun sendEffect(uiEffect: SettingsUiEffects) {
        viewModelScope.launch {
            Logger.d { "UiEffect: ${uiEffect.toString().trimLongLists()}" }
            _uiEffects.send(uiEffect)
        }
    }

    private fun initLogging() {
        uiState
            .onEach { Logger.d { "SettingsState: ${it.toString().trimLongLists()}" } }
            .launchIn(viewModelScope)
    }
}
