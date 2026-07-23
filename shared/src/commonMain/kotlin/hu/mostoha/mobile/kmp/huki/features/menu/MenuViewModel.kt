package hu.mostoha.mobile.kmp.huki.features.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.logger.trimLongLists
import hu.mostoha.mobile.kmp.huki.model.analytics.AnalyticsEvent
import hu.mostoha.mobile.kmp.huki.model.analytics.MenuLink
import hu.mostoha.mobile.kmp.huki.model.analytics.Screen
import hu.mostoha.mobile.kmp.huki.service.AnalyticsService
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class MenuViewModel(private val analyticsService: AnalyticsService) : ViewModel() {
    private val _uiState = MutableStateFlow(MenuUiState.Default)
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    private val _menuUiEffects = Channel<MenuUiEffects>(Channel.BUFFERED)
    val menuUiEffects: Flow<MenuUiEffects> = _menuUiEffects.receiveAsFlow()

    init {
        analyticsService.logEvent(AnalyticsEvent.ScreenView(Screen.MENU))
        initLogging()
    }

    fun onEvent(event: MenuUiEvents) {
        Logger.d { "MenuEvent: $event" }
        when (event) {
            MenuUiEvents.BackClicked -> sendEffect(MenuUiEffects.NavigateBack)
            MenuUiEvents.SettingsClicked -> sendEffect(MenuUiEffects.NavigateToSettings)
            MenuUiEvents.DestinationsClicked -> sendEffect(MenuUiEffects.NavigateToDestinations)
            MenuUiEvents.PlaceHistoryClicked -> sendEffect(MenuUiEffects.NavigateToPlaceHistory)
            MenuUiEvents.GpxCollectionClicked -> sendEffect(MenuUiEffects.NavigateToGpxCollection)
            MenuUiEvents.EmailClicked -> openEmail()
            MenuUiEvents.FacebookClicked -> openFacebook()
            MenuUiEvents.GithubClicked -> openGithub()
            MenuUiEvents.PrivacyPolicyClicked -> openPrivacyPolicy()
            MenuUiEvents.LocationIqClicked -> sendEffect(MenuUiEffects.NavigateToLocationIq)
        }
    }

    private fun openEmail() {
        analyticsService.logEvent(AnalyticsEvent.MenuLinkClicked(MenuLink.EMAIL))
        sendEffect(
            MenuUiEffects.SendEmail(
                emailRes = SharedRes.strings.menu_contact_email,
                subjectRes = SharedRes.strings.menu_email_subject,
            ),
        )
    }

    private fun openFacebook() {
        analyticsService.logEvent(AnalyticsEvent.MenuLinkClicked(MenuLink.FACEBOOK))
        sendEffect(MenuUiEffects.OpenUrl(SharedRes.strings.menu_facebook_url))
    }

    private fun openGithub() {
        analyticsService.logEvent(AnalyticsEvent.MenuLinkClicked(MenuLink.GITHUB))
        sendEffect(MenuUiEffects.OpenUrl(SharedRes.strings.menu_github_url))
    }

    private fun openPrivacyPolicy() {
        analyticsService.logEvent(AnalyticsEvent.MenuLinkClicked(MenuLink.PRIVACY_POLICY))
        sendEffect(MenuUiEffects.OpenUrl(SharedRes.strings.menu_privacy_policy_url))
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
