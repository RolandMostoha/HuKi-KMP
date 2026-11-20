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
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlannerSession
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
import hu.mostoha.mobile.kmp.huki.util.RoutePlanConstants
import hu.mostoha.mobile.kmp.huki.util.distanceBetween
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.Uuid

class RoutePlannerViewModel(
    private val locationMonitoringService: LocationMonitoringService,
    private val routePlannerRepository: RoutePlannerRepository,
    private val geocodingRepository: GeocodingRepository,
    private val placeHistoryRepository: PlaceHistoryRepository,
    private val gpxRepository: GpxRepository,
    private val analyticsService: AnalyticsService,
    private val crashlyticsService: CrashlyticsService,
    initialStops: List<RoutePlannerWaypoint> = emptyList(),
    restoredSession: RoutePlannerSession? = null,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoutePlannerUiState.Default)
    val uiState: StateFlow<RoutePlannerUiState> = _uiState.asStateFlow()

    private val _uiEffects = Channel<RoutePlannerUiEffects>(Channel.BUFFERED)
    val uiEffects: Flow<RoutePlannerUiEffects> = _uiEffects.receiveAsFlow()

    /**
     * Replays the latest request, since seeded stops can form one before the collector subscribes.
     */
    private val routePlanRequests = MutableSharedFlow<RoutePlanRequest>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    init {
        if (restoredSession != null) {
            restoreSession(restoredSession)
        } else {
            // Seeded before my location loads, so no state ever has my location as the only stop
            initialStops.forEach { addWaypoint(it, guardRange = true) }
        }
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
            RoutePlannerUiEvents.AddEmptyStopClicked -> addEmptyStop()
            is RoutePlannerUiEvents.StopClicked -> toggleStopSelection(event.waypointId)
            is RoutePlannerUiEvents.StopMoveClicked -> startStopMove()
            is RoutePlannerUiEvents.StopDragged -> moveStop(event.from, event.to)
            is RoutePlannerUiEvents.StopMarkerClicked -> selectStopAt(event.location)
            RoutePlannerUiEvents.StopDeselected -> deselectStop()
            is RoutePlannerUiEvents.StopReplaceClicked -> showStopReplaceSearch(event.waypointId)
            is RoutePlannerUiEvents.StopRenameClicked -> showStopRename(event.waypointId)
            is RoutePlannerUiEvents.StopRenamed -> renameStop(event.waypointId, event.name, event.comment)
            RoutePlannerUiEvents.StopRenameDismissed -> _uiState.update { it.copy(renamingStopId = null) }
            is RoutePlannerUiEvents.AddStopFromSearchClicked -> showWaypointSearch(event.waypointId)
            RoutePlannerUiEvents.WaypointSearchDismissed -> hideWaypointSearch()
            is RoutePlannerUiEvents.SearchPlaceAdded -> addSearchPlace(event.place)
            is RoutePlannerUiEvents.SearchDestinationAdded -> addSearchDestination(event.destination)
            RoutePlannerUiEvents.MyLocationAdded -> addMyLocation()
            is RoutePlannerUiEvents.PickOnMapClicked -> pickOnMap(event.waypointId)
            RoutePlannerUiEvents.RoundTripClicked -> addRoundTrip()
            RoutePlannerUiEvents.RetryClicked -> retryRoutePlan()
            RoutePlannerUiEvents.SaveRouteClicked -> saveRoutePlan()
            RoutePlannerUiEvents.CloseClicked -> sendEffect(RoutePlannerUiEffects.Close)
            RoutePlannerUiEvents.GraphhopperAttributionClicked -> openGraphhopper()
        }
    }

    private fun openGraphhopper() {
        sendEffect(RoutePlannerUiEffects.OpenUrl(RoutePlanConstants.GRAPHHOPPER_URL))
    }

    private fun restoreSession(session: RoutePlannerSession) {
        _uiState.update { it.copy(routeProfile = session.routeProfile, waypoints = session.waypoints) }
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
        val waypoint = location.toRoutePlannerWaypoint()
        if (!addWaypoint(waypoint)) {
            return
        }

        analyticsService.logEvent(AnalyticsEvent.RoutePlanWaypointLongTapped)

        _uiState.update { it.copy(isMovingStop = false) }

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

    private fun addEmptyStop() {
        if (!_uiState.value.isAddStopEnabled) return

        analyticsService.logEvent(AnalyticsEvent.RoutePlanEmptyStopAdded)

        // The new empty stop takes the highlight, so only one card is open at a time
        _uiState.update { uiState ->
            val stops = uiState.stops
            // A round trip keeps returning to its start, so the new stop goes before the mirrored end
            val updated = if (uiState.isRoundTrip) {
                stops.toMutableList().apply { add(lastIndex, RoutePlannerWaypoint()) }
            } else {
                stops + RoutePlannerWaypoint()
            }
            uiState.withStops(updated).copy(
                selectedStopId = null,
                waypointSearchTargetId = null,
                isPickingOnMap = false,
            )
        }
    }

    /**
     * Clicking the collapsed empty stop clears the selection, which opens the empty stop again.
     */
    private fun toggleStopSelection(waypointId: String, logsSelection: Boolean = true) {
        val uiState = _uiState.value
        val isSelected = uiState.selectedStop?.id == waypointId
        val isEmptyStop = uiState.stops.any { it.id == waypointId && it.isEmpty }
        if (logsSelection && !isSelected && !isEmptyStop) {
            analyticsService.logEvent(AnalyticsEvent.RoutePlanStopSelected)
        }

        _uiState.update { it.copy(selectedStopId = if (isSelected) null else waypointId) }
    }

    /**
     * A planned route snaps its markers away from the stops' own locations, so the nearest stop is the clicked one.
     */
    private fun selectStopAt(location: Location) {
        val stop = _uiState.value.stops
            .mapNotNull { stop -> stop.location?.let { stop to it.distanceBetween(location) } }
            .minByOrNull { it.second }
            ?.first
            ?: return

        analyticsService.logEvent(AnalyticsEvent.RoutePlanStopMarkerClicked)

        toggleStopSelection(stop.id, logsSelection = false)
    }

    /**
     * Moving needs the selected stop, so a move in progress ends with it. An abandoned pick also drops its target, so a
     * later long tap adds a stop instead of replacing the one the pick was for.
     */
    private fun deselectStop() {
        _uiState.update {
            it.copy(selectedStopId = null, isMovingStop = false, isPickingOnMap = false, waypointSearchTargetId = null)
        }
    }

    private fun startStopMove() {
        analyticsService.logEvent(AnalyticsEvent.RoutePlanStopMoveClicked)

        _uiState.update { it.copy(isMovingStop = true, isPickingOnMap = false, waypointSearchTargetId = null) }

        sendEffect(RoutePlannerUiEffects.MinimizeSheet)
    }

    /**
     * Moves the stop under the dragged marker, which a planned route snaps away from the stop, so the nearest one is
     * taken. A round trip's shared start and end move together.
     */
    private fun moveStop(from: Location, to: Location) {
        val stops = _uiState.value.stops
        val target = stops
            .mapNotNull { stop -> stop.location?.let { stop to it.distanceBetween(from) } }
            .minByOrNull { it.second }
            ?.first
            ?: return

        analyticsService.logEvent(AnalyticsEvent.RoutePlanStopDragged)

        val targetIds = _uiState.value.mirroredStopIds(target.id)
        val movedIds = mutableSetOf<String>()
        val moved = stops.map { stop ->
            if (stop.id !in targetIds) return@map stop
            // My location's id belongs to the live location fix, which must not pull the moved stop back
            val id = if (stop.id == RoutePlannerUiState.MY_LOCATION_WAYPOINT_ID) Uuid.random().toString() else stop.id
            movedIds += id
            to.toRoutePlannerWaypoint().copy(id = id, customName = stop.customName, comment = stop.comment)
        }

        // A finished move also ends the selection, so the dropped marker stops pulsing
        _uiState.update { uiState ->
            uiState.withStops(moved).copy(isMovingStop = false, selectedStopId = null)
        }

        viewModelScope.launch {
            val result = geocodingRepository.reverseGeocode(to)
            val place = (result as? NetworkResult.Success)?.data?.toReverseGeocodedPlace(to) ?: return@launch

            _uiState.update { uiState ->
                uiState.copy(
                    waypoints = uiState.waypoints.map { waypoint ->
                        if (waypoint.id in movedIds && waypoint.location == to) {
                            waypoint.copy(name = StringDesc.Raw(place.name), placeName = place.name)
                        } else {
                            waypoint
                        }
                    },
                )
            }
        }
    }

    private fun showStopReplaceSearch(waypointId: String) {
        analyticsService.logEvent(AnalyticsEvent.RoutePlanStopReplaceClicked)

        _uiState.update {
            it.copy(
                isWaypointSearchVisible = true,
                waypointSearchTargetId = waypointId,
                isPickingOnMap = false,
                isMovingStop = false,
            )
        }
    }

    private fun showStopRename(waypointId: String) {
        analyticsService.logEvent(AnalyticsEvent.RoutePlanStopRenameClicked)

        _uiState.update { it.copy(renamingStopId = waypointId) }
    }

    /**
     * A blank name gives the stop its resolved name back.
     */
    private fun renameStop(waypointId: String, name: String, comment: String) {
        analyticsService.logEvent(AnalyticsEvent.RoutePlanStopRenamed)

        _uiState.update { uiState ->
            val renamedIds = uiState.mirroredStopIds(waypointId)
            uiState.copy(
                waypoints = uiState.waypoints.map { waypoint ->
                    if (waypoint.id in renamedIds) {
                        waypoint.copy(
                            customName = name.trim().ifEmpty { null },
                            comment = comment.trim().ifEmpty { null },
                        )
                    } else {
                        waypoint
                    }
                },
                renamingStopId = null,
            )
        }
    }

    /**
     * A round trip's last stop mirrors its start, so a change to either end goes to both.
     */
    private fun RoutePlannerUiState.mirroredStopIds(waypointId: String): Set<String> {
        val ends = setOfNotNull(stops.firstOrNull()?.id, stops.lastOrNull()?.id)
        return if (isRoundTrip && waypointId in ends) ends else setOf(waypointId)
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

    private fun pickOnMap(waypointId: String?) {
        analyticsService.logEvent(AnalyticsEvent.RoutePlanPickOnMapClicked)

        _uiState.update {
            it.copy(
                isWaypointSearchVisible = false,
                waypointSearchTargetId = waypointId ?: it.waypointSearchTargetId,
                isPickingOnMap = true,
                isMovingStop = false,
            )
        }

        sendEffect(RoutePlannerUiEffects.MinimizeSheet)
    }

    private fun addWaypoint(waypoint: RoutePlannerWaypoint, guardRange: Boolean = false): Boolean {
        var added = false

        _uiState.update { uiState ->
            added = false

            val stops = uiState.stops
            // The target is an empty stop to fill or a filled one to replace
            val targetIndex = stops
                .indexOfFirst { it.id == uiState.waypointSearchTargetId }
                .takeIf { it != -1 }
                ?: stops.indexOfFirst { it.isEmpty }
            if (targetIndex == -1 && uiState.isMaxStopsReached) {
                return@update uiState
            }

            added = true

            val updated = when {
                targetIndex == -1 -> stops + waypoint
                uiState.isRoundTrip && (targetIndex == 0 || targetIndex == stops.lastIndex) -> {
                    // Replacing either end of a round trip moves both, so the route still returns to its start
                    val otherEnd = if (targetIndex == 0) stops.lastIndex else 0
                    stops.toMutableList().apply {
                        set(targetIndex, waypoint)
                        set(otherEnd, waypoint.copy(id = stops[otherEnd].id))
                    }
                }
                else -> stops.toMutableList().apply { set(targetIndex, waypoint) }
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
        val current = _uiState.value
        val stop = current.stops.firstOrNull { it.id == id } ?: return
        // An empty stop at the minimum would only be replaced by another empty one
        if (stop.isEmpty && !current.isEmptyStopRemovable) {
            return
        }

        analyticsService.logEvent(AnalyticsEvent.RoutePlanWaypointRemoved)

        _uiState.update { uiState ->
            val stops = uiState.stops
            // Removing either end of a round trip drops its mirrored end, so the route keeps its start
            val removedId = if (uiState.isRoundTrip && id == stops.first().id) stops.last().id else id
            val index = stops.indexOfFirst { it.id == removedId }
            if (index == -1) {
                return@update uiState
            }

            // Below the minimum the row itself has to stay, so it falls back to the empty "pick a place" state.
            val updated = if (stops.size > RoutePlanConstants.MIN_WAYPOINT_COUNT) {
                stops.filterNot { it.id == removedId }
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
        val routedStops = uiState.stops.filter { it.location != null }
        val stopNames = routedStops.map { it.gpxName }
        val stopComments = routedStops.map { it.comment }

        viewModelScope.launch {
            runCatching { gpxRepository.saveRoutePlan(routePlan, stopNames, uiState.routeProfile, stopComments) }
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
    }

    private suspend fun loadRoutePlan(request: RoutePlanRequest) {
        if (request.waypoints.size < RoutePlanConstants.MIN_WAYPOINT_COUNT) {
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
