package hu.mostoha.mobile.kmp.huki.features.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.logger.trimLongLists
import hu.mostoha.mobile.kmp.huki.model.analytics.AnalyticsEvent
import hu.mostoha.mobile.kmp.huki.model.analytics.MenuLink
import hu.mostoha.mobile.kmp.huki.model.analytics.Screen
import hu.mostoha.mobile.kmp.huki.repository.SupporterRepository
import hu.mostoha.mobile.kmp.huki.repository.supporterAnimal
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

class MenuViewModel(
    private val analyticsService: AnalyticsService,
    private val supporterRepository: SupporterRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MenuUiState.Default)
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    private val _menuUiEffects = Channel<MenuUiEffects>(Channel.BUFFERED)
    val menuUiEffects: Flow<MenuUiEffects> = _menuUiEffects.receiveAsFlow()

    init {
        initLogging()
        observeSupporter()
    }

    fun onEvent(event: MenuUiEvents) {
        Logger.d { "MenuEvent: $event" }
        when (event) {
            MenuUiEvents.ScreenViewed -> analyticsService.logScreenView(Screen.MENU)
            MenuUiEvents.BackClicked -> sendEffect(MenuUiEffects.NavigateBack)
            MenuUiEvents.SettingsClicked -> sendEffect(MenuUiEffects.NavigateToSettings)
            MenuUiEvents.RoutePlannerClicked -> openRoutePlanner()
            MenuUiEvents.DiscoverClicked -> sendEffect(MenuUiEffects.NavigateToDiscover)
            MenuUiEvents.PlaceHistoryClicked -> sendEffect(MenuUiEffects.NavigateToPlaceHistory)
            MenuUiEvents.GpxCollectionClicked -> sendEffect(MenuUiEffects.NavigateToGpxCollection)
            MenuUiEvents.GpxGuideClicked -> sendEffect(MenuUiEffects.NavigateToGpxGuide)
            MenuUiEvents.TrailSymbolsGuideClicked -> sendEffect(MenuUiEffects.NavigateToTrailSymbolsGuide)
            MenuUiEvents.QuickGuideClicked -> sendEffect(MenuUiEffects.NavigateToQuickGuide)
            MenuUiEvents.EmailClicked -> openEmail()
            MenuUiEvents.FacebookClicked -> openFacebook()
            MenuUiEvents.GithubClicked -> openGithub()
            MenuUiEvents.PrivacyPolicyClicked -> openPrivacyPolicy()
            MenuUiEvents.LocationIqClicked -> sendEffect(MenuUiEffects.NavigateToLocationIq)
            MenuUiEvents.GraphhopperClicked -> openGraphhopper()
            MenuUiEvents.SupportClicked -> sendEffect(MenuUiEffects.NavigateToSupport)
        }
    }

    private fun openRoutePlanner() {
        analyticsService.logEvent(AnalyticsEvent.MenuRoutePlanClicked)
        sendEffect(MenuUiEffects.NavigateToRoutePlanner)
    }

    private fun openEmail() {
        analyticsService.logEvent(AnalyticsEvent.MenuLinkClicked(MenuLink.EMAIL))
        sendEffect(MenuUiEffects.SendEmail(SharedRes.strings.menu_contact_email))
    }

    private fun openFacebook() {
        analyticsService.logEvent(AnalyticsEvent.MenuLinkClicked(MenuLink.FACEBOOK))
        sendEffect(MenuUiEffects.OpenUrl(SharedRes.strings.menu_facebook_url))
    }

    private fun openGithub() {
        analyticsService.logEvent(AnalyticsEvent.MenuLinkClicked(MenuLink.GITHUB))
        sendEffect(MenuUiEffects.OpenUrl(SharedRes.strings.menu_github_url))
    }

    private fun openGraphhopper() {
        analyticsService.logEvent(AnalyticsEvent.MenuLinkClicked(MenuLink.GRAPHHOPPER))
        sendEffect(MenuUiEffects.OpenUrl(SharedRes.strings.menu_graphhopper_url))
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

    private fun observeSupporter() {
        supporterRepository.supporterAnimal
            .onEach { animal -> _uiState.update { it.copy(supporterAnimal = animal) } }
            .launchIn(viewModelScope)
    }

    private fun initLogging() {
        uiState
            .onEach { Logger.d { "MenuState: ${it.toString().trimLongLists()}" } }
            .launchIn(viewModelScope)
    }
}
