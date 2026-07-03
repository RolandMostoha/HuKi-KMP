package hu.mostoha.mobile.kmp.huki.features.menu

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

class MenuViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MenuUiState.Default)
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    private val _menuUiEffects = Channel<MenuUiEffects>(Channel.BUFFERED)
    val menuUiEffects: Flow<MenuUiEffects> = _menuUiEffects.receiveAsFlow()

    init {
        initLogging()
    }

    fun onEvent(event: MenuUiEvents) {
        Logger.d { "MenuEvent: $event" }
        when (event) {
            MenuUiEvents.BackClicked -> sendEffect(MenuUiEffects.NavigateBack)
            MenuUiEvents.DestinationsClicked -> sendEffect(MenuUiEffects.NavigateToDestinations)
            MenuUiEvents.PlaceHistoryClicked -> sendEffect(MenuUiEffects.NavigateToPlaceHistory)
            MenuUiEvents.GpxCollectionClicked -> sendEffect(MenuUiEffects.NavigateToGpxCollection)
            MenuUiEvents.EmailClicked ->
                sendEffect(
                    MenuUiEffects.SendEmail(
                        emailRes = SharedRes.strings.menu_contact_email,
                        subjectRes = SharedRes.strings.menu_email_subject,
                    ),
                )
            MenuUiEvents.FacebookClicked ->
                sendEffect(MenuUiEffects.OpenUrl(SharedRes.strings.menu_facebook_url))
            MenuUiEvents.GithubClicked ->
                sendEffect(MenuUiEffects.OpenUrl(SharedRes.strings.menu_github_url))
            MenuUiEvents.LocationIqClicked ->
                sendEffect(MenuUiEffects.NavigateToLocationIq)
        }
    }

    private fun sendEffect(uiEffect: MenuUiEffects) {
        viewModelScope.launch {
            Logger.d { "UiEffect: ${uiEffect.toString().trimLongLists()}" }
            _menuUiEffects.send(uiEffect)
        }
    }

    private fun initLogging() {
        uiState
            .onEach { Logger.d { "MenuState: ${it.toString().trimLongLists()}" } }
            .launchIn(viewModelScope)
    }
}
