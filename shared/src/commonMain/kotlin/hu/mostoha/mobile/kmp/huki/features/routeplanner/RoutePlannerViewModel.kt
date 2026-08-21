package hu.mostoha.mobile.kmp.huki.features.routeplanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import dev.icerock.moko.resources.desc.Raw
import dev.icerock.moko.resources.desc.StringDesc
import hu.mostoha.mobile.kmp.huki.logger.trimLongLists
import hu.mostoha.mobile.kmp.huki.model.analytics.AnalyticsEvent
import hu.mostoha.mobile.kmp.huki.model.domain.Destination
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.Place
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlanRequest
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlannerProfile
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlannerWaypoint
import hu.mostoha.mobile.kmp.huki.model.mapper.toMyLocationWaypoint
import hu.mostoha.mobile.kmp.huki.model.mapper.toPlace
import hu.mostoha.mobile.kmp.huki.model.mapper.toReverseGeocodedPlace
import hu.mostoha.mobile.kmp.huki.model.mapper.toRoutePlan
import hu.mostoha.mobile.kmp.huki.model.mapper.toRoutePlanInfoViewData
import hu.mostoha.mobile.kmp.huki.model.mapper.toRoutePlannerWaypoint
import hu.mostoha.mobile.kmp.huki.model.mapper.toRouteProfile
import hu.mostoha.mobile.kmp.huki.model.network.NetworkError
import hu.mostoha.mobile.kmp.huki.model.network.NetworkResult
import hu.mostoha.mobile.kmp.huki.repository.GeocodingRepository
import hu.mostoha.mobile.kmp.huki.repository.GpxRepository
import hu.mostoha.mobile.kmp.huki.repository.PlaceHistoryRepository
import hu.mostoha.mobile.kmp.huki.repository.RoutePlannerRepository
import hu.mostoha.mobile.kmp.huki.service.AnalyticsService
import hu.mostoha.mobile.kmp.huki.service.CrashlyticsService
import hu.mostoha.mobile.kmp.huki.service.LocationMonitoringService
import hu.mostoha.mobile.kmp.huki.service.locations
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class RoutePlannerViewModel(
    private val locationMonitoringService: LocationMonitoringService,
    private val routePlannerRepository: RoutePlannerRepository,
    private val geocodingRepository: GeocodingRepository,
    private val placeHistoryRepository: PlaceHistoryRepository,
    private val gpxRepository: GpxRepository,
    private val analyticsService: AnalyticsService,
    private val crashlyticsService: CrashlyticsService,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoutePlannerUiState.Default)
    val uiState: StateFlow<RoutePlannerUiState> = _uiState.asStateFlow()

    private val _uiEffects = Channel<RoutePlannerUiEffects>(Channel.BUFFERED)
    val uiEffects: Flow<RoutePlannerUiEffects> = _uiEffects.receiveAsFlow()

    private val routePlanRequests = MutableSharedFlow<RoutePlanRequest>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    init {
        initLogging()
        observeRoutePlanRequests()
        loadMyLocation()
    }

    fun onEvent(event: RoutePlannerUiEvents) {
        Logger.d { "RoutePlannerEvent: $event" }
        when (event) {
            is RoutePlannerUiEvents.ProfileSelected -> selectProfile(event.routeProfile)
            is RoutePlannerUiEvents.PlaceAdded -> addWaypoint(event.place.toRoutePlannerWaypoint(), guardRange = true)
            is RoutePlannerUiEvents.LocationAdded -> addLongTappedLocation(event.location)
            is RoutePlannerUiEvents.WaypointRemoved -> removeWaypoint(event.id)
            is RoutePlannerUiEvents.WaypointMoved -> moveWaypoint(event.fromIndex, event.toIndex)
            is RoutePlannerUiEvents.AddStopFromSearchClicked -> showWaypointSearch(event.waypointId)
            RoutePlannerUiEvents.WaypointSearchDismissed -> hideWaypointSearch()
            is RoutePlannerUiEvents.SearchPlaceAdded -> addSearchPlace(event.place)
            is RoutePlannerUiEvents.SearchDestinationAdded -> addSearchDestination(event.destination)
            RoutePlannerUiEvents.MyLocationAdded -> addMyLocation()
            RoutePlannerUiEvents.PickOnMapClicked -> pickOnMap()
            RoutePlannerUiEvents.RoundTripClicked -> addRoundTrip()
            RoutePlannerUiEvents.RetryClicked -> retryRoutePlan()
            RoutePlannerUiEvents.SaveRouteClicked -> saveRoutePlan()
            RoutePlannerUiEvents.CloseClicked -> sendEffect(RoutePlannerUiEffects.Close)
        }
    }

    fun clear() {
        viewModelScope.cancel()
    }

    private fun selectProfile(routeProfile: RoutePlannerProfile) {
        _uiState.update { it.copy(routeProfile = routeProfile) }
    }

    private fun loadMyLocation() {
        viewModelScope.launch {
            val location = locationMonitoringService.locations().first()

            _uiState.update { uiState ->
                uiState.copy(
                    waypoints = uiState.waypoints.map { waypoint ->
                        if (waypoint.id == RoutePlannerUiState.MY_LOCATION_WAYPOINT_ID && waypoint.location == null) {
                            waypoint.copy(location = location)
                        } else {
                            waypoint
                        }
                    },
                    myLocation = location,
                ).withMyLocationRangeGuard()
            }

            resolvePlaceName(RoutePlannerUiState.MY_LOCATION_WAYPOINT_ID, location)
        }
    }

    private fun addLongTappedLocation(location: Location) {
        val wasPickingOnMap = _uiState.value.isPickingOnMap
        val waypoint = location.toRoutePlannerWaypoint()
        if (!addWaypoint(waypoint)) {
            return
        }

        analyticsService.logEvent(AnalyticsEvent.RoutePlanWaypointLongTapped)

        if (wasPickingOnMap) {
            sendEffect(RoutePlannerUiEffects.ExpandSheet)
        }

        viewModelScope.launch {
            val result = geocodingRepository.reverseGeocode(location)
            val place = (result as? NetworkResult.Success)?.data?.toReverseGeocodedPlace(location) ?: return@launch

            _uiState.update { uiState ->
                uiState.copy(
                    waypoints = uiState.waypoints.map { current ->
                        if (current.location == location && current.name == waypoint.name) {
                            current.copy(name = StringDesc.Raw(place.name), placeName = place.name)
                        } else {
                            current
                        }
                    },
                )
            }
        }
    }

    private fun showWaypointSearch(waypointId: String?) {
        analyticsService.logEvent(AnalyticsEvent.RoutePlanAddStopFromSearchClicked)

        _uiState.update {
            it.copy(isWaypointSearchVisible = true, waypointSearchTargetId = waypointId, isPickingOnMap = false)
        }
    }

    private fun hideWaypointSearch() {
        if (_uiState.value.isPickingOnMap) return

        _uiState.update { it.copy(isWaypointSearchVisible = false, waypointSearchTargetId = null) }
    }

    private fun addSearchPlace(place: Place) {
        analyticsService.logEvent(AnalyticsEvent.RoutePlanStopAddedFromSearch)

        addWaypoint(place.toRoutePlannerWaypoint())

        viewModelScope.launch {
            placeHistoryRepository.recordVisit(place)
        }
    }

    private fun addSearchDestination(destination: Destination) {
        addSearchPlace(destination.toPlace())
    }

    private fun addMyLocation() {
        viewModelScope.launch {
            val location = locationMonitoringService.lastKnownLocation() ?: _uiState.value.myLocation ?: return@launch

            analyticsService.logEvent(AnalyticsEvent.RoutePlanMyLocationAdded)

            val waypoint = location.toMyLocationWaypoint()
            if (addWaypoint(waypoint)) {
                resolvePlaceName(waypoint.id, location)
            }
        }
    }

    private suspend fun resolvePlaceName(waypointId: String, location: Location) {
        val result = geocodingRepository.reverseGeocode(location)
        val place = (result as? NetworkResult.Success)?.data?.toReverseGeocodedPlace(location) ?: return

        _uiState.update { uiState ->
            uiState.copy(
                waypoints = uiState.waypoints.map { waypoint ->
                    if (waypoint.id == waypointId) waypoint.copy(placeName = place.name) else waypoint
                },
            )
        }
    }

    private fun pickOnMap() {
        analyticsService.logEvent(AnalyticsEvent.RoutePlanPickOnMapClicked)

        _uiState.update { it.copy(isWaypointSearchVisible = false, isPickingOnMap = true) }

        sendEffect(RoutePlannerUiEffects.MinimizeSheet)
    }

    private fun addWaypoint(waypoint: RoutePlannerWaypoint, guardRange: Boolean = false): Boolean {
        var added = false

        _uiState.update { uiState ->
            added = false

            val stops = uiState.stops
            val targetIndex = stops
                .indexOfFirst { it.id == uiState.waypointSearchTargetId && it.isEmpty }
                .takeIf { it != -1 }
                ?: stops.indexOfFirst { it.isEmpty }
            if (targetIndex == -1 && uiState.isMaxStopsReached) {
                return@update uiState
            }

            added = true

            val updated = if (targetIndex == -1) {
                stops + waypoint
            } else {
                stops.toMutableList().apply { set(targetIndex, waypoint) }
            }

            uiState.withStops(updated).copy(
                isWaypointSearchVisible = false,
                waypointSearchTargetId = null,
                isPickingOnMap = false,
            ).let { if (guardRange) it.withMyLocationRangeGuard() else it }
        }

        return added
    }

    private fun removeWaypoint(id: String) {
        if (_uiState.value.stops.none { it.id == id && !it.isEmpty }) {
            return
        }

        analyticsService.logEvent(AnalyticsEvent.RoutePlanWaypointRemoved)

        _uiState.update { uiState ->
            val stops = uiState.stops
            val index = stops.indexOfFirst { it.id == id }
            if (index == -1) {
                return@update uiState
            }

            // Below the minimum the row itself has to stay, so it falls back to the empty "pick a place" state.
            val updated = if (stops.size > RoutePlannerUiState.MIN_WAYPOINT_COUNT) {
                stops.filterNot { it.id == id }
            } else {
                stops.toMutableList().apply { set(index, RoutePlannerWaypoint()) }
            }

            uiState.withStops(updated)
        }
    }

    private fun moveWaypoint(fromIndex: Int, toIndex: Int) {
        _uiState.update { uiState ->
            val stops = uiState.stops
            if (fromIndex !in stops.indices || toIndex !in stops.indices || fromIndex == toIndex) {
                return@update uiState
            }

            val reordered = stops.toMutableList().apply { add(toIndex, removeAt(fromIndex)) }

            uiState.withStops(reordered)
        }
    }

    private fun addRoundTrip() {
        _uiState.update { uiState ->
            val first = uiState.stops.firstOrNull()
            if (!uiState.isRoundTripEnabled || first == null) {
                uiState
            } else {
                uiState.withStops(uiState.stops + first.copy(id = Uuid.random().toString()))
            }
        }
    }

    private fun saveRoutePlan() {
        val uiState = _uiState.value
        val routePlan = uiState.routePlan
        if (!uiState.isSaveEnabled || routePlan == null) return
        val stopPlaceNames = uiState.stops.filter { it.location != null }.map { it.placeName }

        viewModelScope.launch {
            runCatching { gpxRepository.saveRoutePlan(routePlan, stopPlaceNames, uiState.routeProfile) }
                .onSuccess { fileUri ->
                    analyticsService.logEvent(AnalyticsEvent.RoutePlanSaved(uiState.routeProfile.toRouteProfile()))
                    sendEffect(RoutePlannerUiEffects.RoutePlanSaved(fileUri))
                }
                .onFailure { throwable ->
                    Logger.e(throwable) { "RoutePlan: failed to save the plan as GPX" }
                    analyticsService.logEvent(AnalyticsEvent.RoutePlanSaveFailed)
                    crashlyticsService.recordException(throwable)
                    sendEffect(RoutePlannerUiEffects.RoutePlanSaveFailed)
                }
        }
    }

    private fun retryRoutePlan() {
        routePlanRequests.tryEmit(_uiState.value.routePlanRequest)
    }

    private fun observeRoutePlanRequests() {
        uiState
            .map { it.routePlanRequest }
            .distinctUntilChanged()
            .onEach { routePlanRequests.tryEmit(it) }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            routePlanRequests.collectLatest { request -> loadRoutePlan(request) }
        }

        uiState
            .map { it.routePlan to it.routePlanMarkers }
            .distinctUntilChanged()
            .drop(1)
            .onEach { (routePlan, markers) ->
                sendEffect(RoutePlannerUiEffects.RoutePlanUpdated(routePlan, markers))
            }
            .launchIn(viewModelScope)
    }

    private suspend fun loadRoutePlan(request: RoutePlanRequest) {
        if (request.waypoints.size < RoutePlannerUiState.MIN_WAYPOINT_COUNT) {
            _uiState.update { it.copy(routePlan = null, isRoutePlanLoading = false, routePlanError = null) }
            return
        }

        _uiState.update { it.copy(isRoutePlanLoading = true, routePlanError = null) }

        delay(ROUTE_PLAN_DEBOUNCE)

        when (val result = routePlannerRepository.getRoutePlan(request.routeProfile, request.waypoints)) {
            is NetworkResult.Success -> {
                val routePlan = result.data.toRoutePlan()
                if (routePlan == null) {
                    showRoutePlanError(NetworkError.NOT_FOUND)
                    return
                }
                analyticsService.logEvent(AnalyticsEvent.RoutePlanCreated(request.routeProfile.toRouteProfile()))
                _uiState.update {
                    it.copy(routePlan = routePlan, isRoutePlanLoading = false, routePlanError = null)
                }
            }
            is NetworkResult.Error -> showRoutePlanError(result.error)
        }
    }

    private fun showRoutePlanError(error: NetworkError) {
        val event = when (error) {
            NetworkError.NO_INTERNET -> AnalyticsEvent.RoutePlanNoInternet
            NetworkError.RATE_LIMITED -> AnalyticsEvent.RoutePlanDailyLimitReached
            else -> AnalyticsEvent.RoutePlanFailed
        }
        analyticsService.logEvent(event)

        _uiState.update {
            it.copy(routePlan = null, isRoutePlanLoading = false, routePlanError = error.toRoutePlanInfoViewData())
        }
    }

    private fun sendEffect(uiEffect: RoutePlannerUiEffects) {
        viewModelScope.launch {
            Logger.d { "UiEffect: ${uiEffect.toString().trimLongLists()}" }
            _uiEffects.send(uiEffect)
        }
    }

    private fun initLogging() {
        uiState
            .onEach { Logger.d { "RoutePlannerState: ${it.toString().trimLongLists()}" } }
            .launchIn(viewModelScope)
    }

    companion object {
        private val ROUTE_PLAN_DEBOUNCE = 300.milliseconds
    }
}
