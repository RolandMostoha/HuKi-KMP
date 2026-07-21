package hu.mostoha.mobile.kmp.huki.features.destinations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.location.LOCATION
import hu.mostoha.mobile.kmp.huki.logger.trimLongLists
import hu.mostoha.mobile.kmp.huki.model.analytics.AnalyticsEvent
import hu.mostoha.mobile.kmp.huki.model.analytics.Screen
import hu.mostoha.mobile.kmp.huki.model.domain.Destination
import hu.mostoha.mobile.kmp.huki.model.domain.DestinationListItem
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.mapper.toDestinationListItem
import hu.mostoha.mobile.kmp.huki.repository.DestinationRepository
import hu.mostoha.mobile.kmp.huki.service.AnalyticsService
import hu.mostoha.mobile.kmp.huki.service.LocationMonitoringService
import hu.mostoha.mobile.kmp.huki.util.distanceBetween
import hu.mostoha.mobile.kmp.huki.util.formatter.DistanceFormatter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.seconds

class DestinationsViewModel(
    private val destinationRepository: DestinationRepository,
    private val locationMonitoringService: LocationMonitoringService,
    private val defaultDispatcher: CoroutineDispatcher,
    val permissionsController: PermissionsController,
    private val analyticsService: AnalyticsService,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DestinationsUiState.Default)
    val uiState: StateFlow<DestinationsUiState> = _uiState.asStateFlow()

    private val _uiEffects = Channel<DestinationsUiEffects>(Channel.BUFFERED)
    val uiEffects: Flow<DestinationsUiEffects> = _uiEffects.receiveAsFlow()

    init {
        analyticsService.logEvent(AnalyticsEvent.ScreenView(Screen.DESTINATIONS))
        initLogging()
        loadDestinations()
    }

    fun onEvent(event: DestinationsUiEvents) {
        Logger.d { "DestinationsEvent: $event" }
        when (event) {
            DestinationsUiEvents.BackClicked -> sendEffect(DestinationsUiEffects.NavigateBack)
            is DestinationsUiEvents.TabSelected -> _uiState.update { it.copy(selectedTab = event.tab) }
            DestinationsUiEvents.GrantLocationClicked -> requestLocationPermission()
            DestinationsUiEvents.RetryNearbyClicked -> viewModelScope.launch { refreshNearby() }
        }
    }

    private fun loadDestinations() {
        viewModelScope.launch {
            withContext(defaultDispatcher) {
                val popular = destinationRepository.getPopularDestinations()
                val landscapes = destinationRepository.getLandscapes()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        destinationCount = popular.size,
                        popularItems = popular.map { destination -> destination.toDestinationListItem() },
                        landscapes = landscapes,
                    )
                }
            }
            refreshNearby()
        }
    }

    private suspend fun refreshNearby() {
        val isGranted = permissionsController.getPermissionState(Permission.LOCATION) == PermissionState.Granted
        if (!isGranted) {
            _uiState.update { it.copy(nearbyState = NearbyState.PermissionRequired) }
            return
        }
        loadNearby()
    }

    private suspend fun loadNearby() {
        _uiState.update { it.copy(nearbyState = NearbyState.Loading) }
        val location = locationMonitoringService.lastKnownLocation()
            ?: withTimeoutOrNull(LOCATION_FIX_TIMEOUT) { locationMonitoringService.locationUpdates.first() }

        if (location != null) {
            val destinations = withContext(defaultDispatcher) { location.toNearbyItems() }
            _uiState.update { it.copy(nearbyState = NearbyState.Loaded(destinations), userLocation = location) }
        } else {
            _uiState.update { it.copy(nearbyState = NearbyState.LocationUnavailable) }
        }
    }

    fun distanceText(destination: Destination): String? =
        _uiState.value.userLocation?.let { destination.distanceTextFrom(it) }

    private fun Location.toNearbyItems(): List<DestinationListItem> =
        destinationRepository.getNearbyDestinations(this).map { destination ->
            destination.toDestinationListItem(distance = destination.distanceTextFrom(this))
        }

    private fun Destination.distanceTextFrom(location: Location): String =
        DistanceFormatter.formatDistance(this.location.distanceBetween(location))

    private fun requestLocationPermission() {
        viewModelScope.launch {
            runCatching { permissionsController.providePermission(Permission.LOCATION) }
                .onSuccess { loadNearby() }
                .onFailure { exception ->
                    when (exception) {
                        is DeniedAlwaysException -> {
                            analyticsService.logEvent(AnalyticsEvent.LocationPermissionDenied(deniedAlways = true))
                            sendEffect(DestinationsUiEffects.NavigateToAppSettings)
                        }
                        is DeniedException -> {
                            analyticsService.logEvent(AnalyticsEvent.LocationPermissionDenied(deniedAlways = false))
                        }
                    }
                }
        }
    }

    private fun sendEffect(uiEffect: DestinationsUiEffects) {
        viewModelScope.launch {
            Logger.d { "UiEffect: ${uiEffect.toString().trimLongLists()}" }
            _uiEffects.send(uiEffect)
        }
    }

    private fun initLogging() {
        uiState
            .onEach { Logger.d { "DestinationsState: ${it.toString().trimLongLists()}" } }
            .flowOn(defaultDispatcher)
            .launchIn(viewModelScope)
    }

    private companion object {
        val LOCATION_FIX_TIMEOUT = 5.seconds
    }
}
