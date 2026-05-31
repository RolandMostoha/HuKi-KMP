package hu.mostoha.mobile.kmp.huki.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.logger.trimLongLists
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState.Default)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _settingsUiEffects = Channel<SettingsUiEffects>(Channel.BUFFERED)
    val settingsUiEffects: Flow<SettingsUiEffects> = _settingsUiEffects.receiveAsFlow()

    init {
        initLogging()
    }

    fun onEvent(event: SettingsUiEvents) {
        Logger.d { "SettingsEvent: $event" }
        when (event) {
            SettingsUiEvents.BackClicked -> sendEffect(SettingsUiEffects.NavigateBack)
            SettingsUiEvents.EmailClicked ->
                sendEffect(
                    SettingsUiEffects.SendEmail(
                        emailRes = SharedRes.strings.settings_contact_email,
                        subjectRes = SharedRes.strings.settings_email_subject,
                    ),
                )
            SettingsUiEvents.FacebookClicked ->
                sendEffect(SettingsUiEffects.OpenUrl(SharedRes.strings.settings_facebook_url))
            SettingsUiEvents.GithubClicked ->
                sendEffect(SettingsUiEffects.OpenUrl(SharedRes.strings.settings_github_url))
            SettingsUiEvents.LocationIqClicked ->
                sendEffect(SettingsUiEffects.NavigateToLocationIq)
        }
    }

    private fun sendEffect(uiEffect: SettingsUiEffects) {
        viewModelScope.launch {
            Logger.d { "UiEffect: ${uiEffect.toString().trimLongLists()}" }
            _settingsUiEffects.send(uiEffect)
        }
    }

    private fun initLogging() {
        uiState
            .onEach { Logger.d { "SettingsState: ${it.toString().trimLongLists()}" } }
            .launchIn(viewModelScope)
    }
}
