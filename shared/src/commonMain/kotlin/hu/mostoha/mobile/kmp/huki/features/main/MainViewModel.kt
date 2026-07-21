package hu.mostoha.mobile.kmp.huki.features.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.location.LOCATION
import hu.mostoha.mobile.huki.shared.SharedRes
import hu.mostoha.mobile.kmp.huki.features.map.MapUiEffects
import hu.mostoha.mobile.kmp.huki.logger.trimLongLists
import hu.mostoha.mobile.kmp.huki.model.analytics.AnalyticsEvent
import hu.mostoha.mobile.kmp.huki.model.analytics.Layer
import hu.mostoha.mobile.kmp.huki.model.analytics.Screen
import hu.mostoha.mobile.kmp.huki.model.domain.Alert
import hu.mostoha.mobile.kmp.huki.model.domain.BaseLayer
import hu.mostoha.mobile.kmp.huki.model.domain.CameraTarget
import hu.mostoha.mobile.kmp.huki.model.domain.ContentPadding
import hu.mostoha.mobile.kmp.huki.model.domain.Destination
import hu.mostoha.mobile.kmp.huki.model.domain.DistanceInfoWindowData
import hu.mostoha.mobile.kmp.huki.model.domain.DomainException
import hu.mostoha.mobile.kmp.huki.model.domain.GpxMapsNavigationType
import hu.mostoha.mobile.kmp.huki.model.domain.GpxWaypoint
import hu.mostoha.mobile.kmp.huki.model.domain.MyLocationStatus
import hu.mostoha.mobile.kmp.huki.model.domain.OsmType
import hu.mostoha.mobile.kmp.huki.model.domain.Place
import hu.mostoha.mobile.kmp.huki.model.domain.Sheet
import hu.mostoha.mobile.kmp.huki.model.domain.WaypointType
import hu.mostoha.mobile.kmp.huki.model.domain.toLocations
import hu.mostoha.mobile.kmp.huki.model.mapper.toLayer
import hu.mostoha.mobile.kmp.huki.model.mapper.toMyLocationMode
import hu.mostoha.mobile.kmp.huki.model.mapper.toScreen
import hu.mostoha.mobile.kmp.huki.repository.DestinationRepository
import hu.mostoha.mobile.kmp.huki.repository.GpxRepository
import hu.mostoha.mobile.kmp.huki.repository.PlaceHistoryRepository
import hu.mostoha.mobile.kmp.huki.repository.SettingsRepository
import hu.mostoha.mobile.kmp.huki.repository.WhatsNewRepository
import hu.mostoha.mobile.kmp.huki.service.AnalyticsService
import hu.mostoha.mobile.kmp.huki.service.LocationMonitoringService
import hu.mostoha.mobile.kmp.huki.service.locations
import hu.mostoha.mobile.kmp.huki.util.MapConstants.PLACE_DEFAULT_CAMERA_ZOOM
import hu.mostoha.mobile.kmp.huki.util.formatter.DistanceFormatter
import hu.mostoha.mobile.kmp.huki.util.formatter.TravelTimeFormatter
import hu.mostoha.mobile.kmp.huki.util.routeProgressTo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    val permissionsController: PermissionsController,
    val gpxRepository: GpxRepository,
    private val placeHistoryRepository: PlaceHistoryRepository,
    private val destinationRepository: DestinationRepository,
    private val locationMonitoringService: LocationMonitoringService,
    private val settingsRepository: SettingsRepository,
    private val whatsNewRepository: WhatsNewRepository,
    private val analyticsService: AnalyticsService,
    private val defaultDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState.Default)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _mainUiEffects = Channel<MainUiEffects>(Channel.BUFFERED)
    val mainUiEffects: Flow<MainUiEffects> = _mainUiEffects.receiveAsFlow()

    private val _mapUiEffects = Channel<MapUiEffects>(Channel.BUFFERED)
    val mapUiEffects: Flow<MapUiEffects> = _mapUiEffects.receiveAsFlow()

    private val selectedWaypoint = MutableStateFlow<GpxWaypoint?>(null)

    init {
        initLogging()
        initMyLocation()
        initDistanceMonitoring()
        initWhatsNew()
        observeSettings()
        observeSheetViews()
    }

    fun onEvent(event: MainUiEvents) {
        Logger.d { "MainEvent: $event" }
        when (event) {
            // General events
            MainUiEvents.SheetDismissed -> hideSheet()
            MainUiEvents.AlertDismissed -> dismissAlert()
            // Search events
            MainUiEvents.SearchClicked -> showSearch()
            is MainUiEvents.SearchPlaceSelected -> showSearchPlace(event.place)
            is MainUiEvents.SearchRecentPlaceSelected -> showRecentPlace(event.place)
            is MainUiEvents.SearchDestinationSelected -> showSearchDestination(event.destination)
            is MainUiEvents.SearchResultPlaceHistorySelected -> showSearchResultPlaceHistory(event.place)
            is MainUiEvents.SearchResultDestinationSelected -> showSearchResultDestination(event.destination)
            is MainUiEvents.DestinationSelected -> showDestinationById(event.osmId)
            is MainUiEvents.HistoryPlaceSelected -> showHistoryPlace(event.osmType, event.osmId)
            // My location events
            MainUiEvents.MyLocationClicked -> enableMyLocation()
            MainUiEvents.MyLocationReceived -> updateMyLocation()
            MainUiEvents.FollowingDisabled -> disableFollowing()
            MainUiEvents.CompassClicked -> resetCameraToNorth()
            MainUiEvents.ZoomInClicked -> zoom(zoomIn = true)
            MainUiEvents.ZoomOutClicked -> zoom(zoomIn = false)
            // Layers events
            MainUiEvents.LayersClicked -> showSheet(Sheet.Layers)
            is MainUiEvents.BaseLayerSelected -> selectBaseLayer(event.baseLayer)
            MainUiEvents.HikingLayerSelected -> toggleHikingLayer()
            // GPX events
            MainUiEvents.GpxLayerSelected -> switchGpxLayer()
            MainUiEvents.GpxStartNavigationClicked -> startGpxNavigation()
            is MainUiEvents.GpxMapsNavigationClicked -> openMapsNavigation(event.type)
            MainUiEvents.GpxCloseClicked -> closeGpx()
            is MainUiEvents.GpxFileSelected -> importGpx(event.uri, AnalyticsEvent.GpxImported(event.source))
            is MainUiEvents.GpxFileReopened -> importGpx(event.uri, AnalyticsEvent.HistoryGpxSelected)
            MainUiEvents.GpxRouteVisibilityToggled -> toggleGpxRouteVisibility()
            MainUiEvents.GpxDistancesVisibilityToggled -> toggleAllDistancesVisibility()
            MainUiEvents.GpxOverviewClicked -> showGpxOverview()
            is MainUiEvents.GpxWaypointClicked -> selectWaypoint(event.waypoint)
            MainUiEvents.DistanceInfoWindowDismissed -> dismissDistanceInfoWindow()
        }
    }

    private fun showSearch() {
        analyticsService.logEvent(AnalyticsEvent.SearchOpened)
        showSheet(Sheet.Search)
    }

    private fun showSheet(sheet: Sheet) {
        _uiState.update { it.copy(sheet = sheet) }
    }

    private fun hideSheet() {
        _uiState.update { it.copy(sheet = null) }
    }

    private fun dismissAlert() {
        viewModelScope.launch {
            _uiState.update { uiState ->
                uiState.copy(alert = null)
            }
        }
    }

    private fun showSearchPlace(place: Place) {
        analyticsService.logEvent(AnalyticsEvent.SearchPlaceSelected)
        showPlace(place)
    }

    private fun showRecentPlace(place: Place) {
        analyticsService.logEvent(AnalyticsEvent.HistoryPlaceSelected)
        showPlace(place)
    }

    private fun showSearchResultPlaceHistory(place: Place) {
        analyticsService.logEvent(AnalyticsEvent.SearchPlaceHistorySelected)
        showPlace(place)
    }

    private fun showPlace(place: Place) {
        viewModelScope.launch {
            hideSheet()
            val boundingBox = place.boundingBox
            sendEffect(
                if (boundingBox != null) {
                    MapUiEffects.UpdateCamera(
                        target = CameraTarget.Bounds(
                            locations = boundingBox.toLocations(),
                            maxZoom = PLACE_DEFAULT_CAMERA_ZOOM,
                        ),
                    )
                } else {
                    MapUiEffects.UpdateCamera(
                        target = CameraTarget.Center(place.location, zoom = PLACE_DEFAULT_CAMERA_ZOOM),
                    )
                },
            )
        }
        viewModelScope.launch {
            placeHistoryRepository.recordVisit(place)
        }
    }

    private fun showHistoryPlace(osmType: OsmType, osmId: String) {
        analyticsService.logEvent(AnalyticsEvent.HistoryPlaceSelected)
        viewModelScope.launch {
            val place = placeHistoryRepository.getPlace(osmType, osmId) ?: return@launch
            showPlace(place)
        }
    }

    private fun showSearchDestination(destination: Destination) {
        analyticsService.logEvent(AnalyticsEvent.DestinationSelected(destination.name))
        showDestination(destination)
    }

    private fun showSearchResultDestination(destination: Destination) {
        analyticsService.logEvent(AnalyticsEvent.SearchDestinationSelected(destination.name))
        showDestination(destination)
    }

    private fun showDestination(destination: Destination) {
        viewModelScope.launch {
            hideSheet()
            sendEffect(
                MapUiEffects.UpdateCamera(
                    target = CameraTarget.Center(destination.location, zoom = PLACE_DEFAULT_CAMERA_ZOOM),
                ),
            )
        }
        viewModelScope.launch {
            placeHistoryRepository.recordVisit(destination)
        }
    }

    private fun showDestinationById(osmId: String) {
        val destination = destinationRepository.requireDestination(osmId)
        analyticsService.logEvent(AnalyticsEvent.DestinationSelected(destination.name))
        showDestination(destination)
    }

    private fun enableMyLocation() {
        viewModelScope.launch {
            withLocationPermission {
                val newStatus = when (uiState.value.myLocationState.myLocationStatus) {
                    MyLocationStatus.Default -> MyLocationStatus.Following
                    MyLocationStatus.Following -> MyLocationStatus.FollowingLiveCompass
                    MyLocationStatus.FollowingLiveCompass -> MyLocationStatus.Following
                    MyLocationStatus.NotAvailable -> MyLocationStatus.Following
                }
                newStatus.toMyLocationMode()?.let {
                    analyticsService.logEvent(AnalyticsEvent.MyLocationFollowed(it))
                }
                _uiState.update { uiState ->
                    uiState.copy(
                        myLocationState = uiState.myLocationState.copy(
                            permissionState = PermissionState.Granted,
                            myLocationStatus = newStatus,
                        ),
                        isMyLocationLoading = !uiState.myLocationState.hasLocationFix,
                        isSearchBarVisible = shouldShowSearchBar(uiState.mapUiState.gpxLayerVisible, newStatus),
                    )
                }
                sendEffect(MapUiEffects.ShowMyLocation(newStatus, animated = true))
            }
        }
    }

    private fun updateMyLocation() {
        _uiState.update {
            it.copy(
                myLocationState = it.myLocationState.copy(hasLocationFix = true),
                isMyLocationLoading = false,
            )
        }
    }

    private fun disableFollowing() {
        _uiState.update {
            it.copy(
                myLocationState = it.myLocationState.copy(myLocationStatus = MyLocationStatus.Default),
                isSearchBarVisible = shouldShowSearchBar(it.mapUiState.gpxLayerVisible, MyLocationStatus.Default),
            )
        }
    }

    private fun zoom(zoomIn: Boolean) {
        viewModelScope.launch {
            sendEffect(MapUiEffects.Zoom(zoomIn))
        }
    }

    /**
     * Recenters north-up on the puck while following, otherwise only rotates the camera back to north,
     * so browsing the map away from the current location is not interrupted.
     */
    private fun resetCameraToNorth() {
        viewModelScope.launch {
            when (_uiState.value.myLocationState.myLocationStatus) {
                MyLocationStatus.Following, MyLocationStatus.FollowingLiveCompass -> {
                    val newStatus = MyLocationStatus.Following
                    _uiState.update { uiState ->
                        uiState.copy(
                            myLocationState = uiState.myLocationState.copy(myLocationStatus = newStatus),
                            isSearchBarVisible = shouldShowSearchBar(uiState.mapUiState.gpxLayerVisible, newStatus),
                        )
                    }
                    sendEffect(MapUiEffects.ShowMyLocation(newStatus, animated = true))
                }
                MyLocationStatus.Default, MyLocationStatus.NotAvailable -> sendEffect(MapUiEffects.ResetBearing)
            }
        }
    }

    private fun initMyLocation() {
        viewModelScope.launch {
            val permissionState = permissionsController.getPermissionState(Permission.LOCATION)
            val myLocationStatus = if (permissionState == PermissionState.Granted) {
                MyLocationStatus.Following
            } else {
                MyLocationStatus.NotAvailable
            }
            _uiState.update { uiState ->
                uiState.copy(
                    myLocationState = uiState.myLocationState.copy(
                        permissionState = permissionState,
                        myLocationStatus = myLocationStatus,
                    ),
                    isMyLocationLoading = myLocationStatus != MyLocationStatus.NotAvailable &&
                        !uiState.myLocationState.hasLocationFix,
                )
            }
            sendEffect(MapUiEffects.ShowMyLocation(myLocationStatus, animated = false))
        }
    }

    private fun initWhatsNew() {
        viewModelScope.launch {
            if (whatsNewRepository.shouldShowWhatsNew()) {
                showSheet(Sheet.WhatsNew(whatsNewRepository.currentWhatsNew))
                whatsNewRepository.markCurrentWhatsNewSeen()
            }
        }
    }

    private fun observeSheetViews() {
        uiState
            .map { it.sheet.toScreen() }
            .distinctUntilChanged()
            .filter { it != Screen.MAP }
            .onEach { analyticsService.logEvent(AnalyticsEvent.ScreenView(it)) }
            .launchIn(viewModelScope)
    }

    private fun observeSettings() {
        settingsRepository.settings
            .map { it.mapZoomControlsVisible }
            .distinctUntilChanged()
            .onEach { alwaysVisible ->
                _uiState.update { it.copy(mapZoomControlsAlwaysVisible = alwaysVisible) }
            }
            .launchIn(viewModelScope)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun initDistanceMonitoring() {
        val allDistancesVisible = uiState
            .map { it.mapUiState.allDistancesVisible }
            .distinctUntilChanged()
        combine(selectedWaypoint, allDistancesVisible) { selected, showAll ->
            val gpxDetails = uiState.value.mapUiState.gpxDetails
            when {
                gpxDetails == null -> emptyList()
                showAll -> gpxDetails.waypoints
                selected != null -> listOf(selected)
                else -> emptyList()
            }
        }
            .flatMapLatest { waypoints ->
                val gpxDetails = uiState.value.mapUiState.gpxDetails
                if (waypoints.isEmpty() || gpxDetails == null) {
                    flowOf(emptyList())
                } else {
                    val isRoundTripRoute = gpxDetails.waypoints.any { it.type == WaypointType.ROUND_TRIP }
                    locationMonitoringService.locations().map { location ->
                        waypoints.map { waypoint ->
                            val progress = gpxDetails.locations.routeProgressTo(
                                from = location,
                                to = waypoint.location,
                                isRoundTrip = isRoundTripRoute,
                            )
                            DistanceInfoWindowData(
                                location = waypoint.location,
                                distance = DistanceFormatter.formatDistance(progress.distance),
                                travelTime = TravelTimeFormatter.formatTravelTime(progress.travelTime),
                            )
                        }
                    }
                }
            }
            .flowOn(defaultDispatcher)
            .onEach { data -> _uiState.updateMapUiState { it.copy(distanceInfoWindows = data) } }
            .launchIn(viewModelScope)
    }

    private suspend fun withLocationPermission(onGranted: suspend () -> Unit) {
        if (permissionsController.getPermissionState(Permission.LOCATION) == PermissionState.Granted) {
            onGranted()
            return
        }
        runCatching { permissionsController.providePermission(Permission.LOCATION) }
            .onSuccess { onGranted() }
            .onFailure { exception ->
                _uiState.updateMyLocationState { uiState ->
                    uiState.copy(
                        permissionState = when (exception) {
                            is DeniedAlwaysException -> PermissionState.DeniedAlways
                            is DeniedException -> PermissionState.Denied
                            else -> PermissionState.NotDetermined
                        },
                    )
                }
                when (exception) {
                    is DeniedAlwaysException -> {
                        analyticsService.logEvent(AnalyticsEvent.LocationPermissionDenied(deniedAlways = true))
                        sendEffect(MainUiEffects.NavigateToAppSettings)
                    }
                    is DeniedException -> {
                        analyticsService.logEvent(AnalyticsEvent.LocationPermissionDenied(deniedAlways = false))
                    }
                }
            }
    }

    private fun shouldShowSearchBar(gpxLayerVisible: Boolean, myLocationStatus: MyLocationStatus): Boolean =
        !gpxLayerVisible && myLocationStatus != MyLocationStatus.FollowingLiveCompass

    private fun selectBaseLayer(baseLayer: BaseLayer) {
        analyticsService.logEvent(AnalyticsEvent.LayerSelected(baseLayer.toLayer()))
        _uiState.updateMapUiState {
            it.copy(baseLayer = baseLayer)
        }
    }

    private fun toggleHikingLayer() {
        analyticsService.logEvent(AnalyticsEvent.LayerSelected(Layer.HIKING))
        _uiState.updateMapUiState {
            it.copy(hikingLayerVisible = it.hikingLayerVisible.not())
        }
    }

    private fun switchGpxLayer() {
        val gpxDetails = uiState.value.mapUiState.gpxDetails
        if (gpxDetails == null) {
            showGpxFilePicker()
        } else {
            _uiState.update {
                val gpxLayerVisible = it.mapUiState.gpxLayerVisible.not()
                it.copy(
                    mapUiState = it.mapUiState.copy(gpxLayerVisible = gpxLayerVisible),
                    isSearchBarVisible = shouldShowSearchBar(gpxLayerVisible, it.myLocationState.myLocationStatus),
                )
            }
        }
    }

    private fun startGpxNavigation() {
        analyticsService.logEvent(AnalyticsEvent.GpxNavigationStarted)
        viewModelScope.launch {
            hideSheet()
            withLocationPermission {
                val targetStatus = MyLocationStatus.FollowingLiveCompass
                _uiState.update {
                    it.copy(
                        myLocationState = it.myLocationState.copy(
                            permissionState = PermissionState.Granted,
                            myLocationStatus = targetStatus,
                        ),
                        isMyLocationLoading = !it.myLocationState.hasLocationFix,
                        isSearchBarVisible = shouldShowSearchBar(it.mapUiState.gpxLayerVisible, targetStatus),
                    )
                }
                sendEffect(MapUiEffects.ShowMyLocation(targetStatus, animated = true))
            }
        }
    }

    private fun openMapsNavigation(type: GpxMapsNavigationType) {
        analyticsService.logEvent(AnalyticsEvent.GpxMapsNavigationOpened)
        val gpxDetails = _uiState.value.mapUiState.gpxDetails ?: return
        val targetLocation = when (type) {
            GpxMapsNavigationType.START ->
                gpxDetails.waypoints
                    .first { it.type == WaypointType.START || it.type == WaypointType.ROUND_TRIP }
                    .location
            GpxMapsNavigationType.END ->
                gpxDetails.waypoints
                    .first { it.type == WaypointType.END }
                    .location
        }
        viewModelScope.launch {
            sendEffect(MainUiEffects.OpenMapsNavigation(targetLocation))
        }
    }

    private fun closeGpx() {
        analyticsService.logEvent(AnalyticsEvent.GpxClosed)
        selectedWaypoint.value = null
        viewModelScope.launch {
            _uiState.update { uiState ->
                uiState.copy(
                    mapUiState = uiState.mapUiState.copy(
                        gpxDetails = null,
                        gpxLayerVisible = false,
                        gpxRouteVisible = true,
                        allDistancesVisible = false,
                        distanceInfoWindows = emptyList(),
                    ),
                    sheet = null,
                    isSearchBarVisible = shouldShowSearchBar(
                        gpxLayerVisible = false,
                        myLocationStatus = uiState.myLocationState.myLocationStatus,
                    ),
                )
            }
        }
    }

    private fun importGpx(uri: String, analyticsEvent: AnalyticsEvent) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isGpxLoading = true,
                    alert = null,
                )
            }
            runCatching { gpxRepository.readGpxFile(uri) }
                .onSuccess { gpxDetails ->
                    analyticsService.logEvent(analyticsEvent)
                    selectedWaypoint.value = null
                    _uiState.update { uiState ->
                        uiState.copy(
                            mapUiState = uiState.mapUiState.copy(
                                gpxDetails = gpxDetails,
                                gpxLayerVisible = true,
                                gpxRouteVisible = true,
                                allDistancesVisible = false,
                                distanceInfoWindows = emptyList(),
                            ),
                            sheet = Sheet.Gpx(gpxDetails),
                            alert = null,
                            isGpxLoading = false,
                            isSearchBarVisible = shouldShowSearchBar(
                                gpxLayerVisible = true,
                                myLocationStatus = uiState.myLocationState.myLocationStatus,
                            ),
                        )
                    }
                    sendEffect(
                        MapUiEffects.UpdateCamera(
                            target = CameraTarget.Bounds(gpxDetails.bounds),
                            contentPadding = ContentPadding.MAP_GPX,
                        ),
                    )
                }
                .onFailure { exception ->
                    Logger.e(exception) { "Failed to import GPX file." }
                    analyticsService.logEvent(AnalyticsEvent.GpxImportFailed)
                    _uiState.update { uiState ->
                        uiState.copy(
                            alert = Alert(
                                title = SharedRes.strings.gpx_import_error_title,
                                message = if (exception is DomainException) {
                                    exception.stringResource
                                } else {
                                    SharedRes.strings.error_unknown
                                },
                            ),
                            isGpxLoading = false,
                        )
                    }
                }
        }
    }

    private fun toggleGpxRouteVisibility() {
        analyticsService.logEvent(AnalyticsEvent.GpxRouteVisibilityToggled)
        _uiState.updateMapUiState {
            it.copy(gpxRouteVisible = it.gpxRouteVisible.not())
        }
    }

    private fun toggleAllDistancesVisibility() {
        analyticsService.logEvent(AnalyticsEvent.GpxDistancesToggled)
        selectedWaypoint.value = null
        _uiState.updateMapUiState {
            it.copy(allDistancesVisible = it.allDistancesVisible.not())
        }
    }

    private fun showGpxOverview() {
        analyticsService.logEvent(AnalyticsEvent.GpxOverviewClicked)
        viewModelScope.launch {
            val gpxDetails = uiState.value.mapUiState.gpxDetails ?: return@launch
            sendEffect(
                MapUiEffects.UpdateCamera(
                    target = CameraTarget.Bounds(gpxDetails.bounds),
                    contentPadding = ContentPadding.MAP_GPX,
                ),
            )
        }
    }

    private fun selectWaypoint(waypoint: GpxWaypoint) {
        selectedWaypoint.value = waypoint
    }

    private fun dismissDistanceInfoWindow() {
        selectedWaypoint.value = null
        _uiState.updateMapUiState { it.copy(allDistancesVisible = false) }
    }

    private fun showGpxFilePicker() {
        viewModelScope.launch {
            hideSheet()
            sendEffect(MainUiEffects.ShowGpxFilePicker)
        }
    }

    private suspend fun sendEffect(uiEffect: UiEffect) {
        Logger.d { "UiEffect: ${uiEffect.toString().trimLongLists()}" }
        when (uiEffect) {
            is MainUiEffects -> _mainUiEffects.send(uiEffect)
            is MapUiEffects -> _mapUiEffects.send(uiEffect)
        }
    }

    private fun initLogging() {
        uiState
            .onEach { Logger.d { "MainState: ${it.toString().trimLongLists()}" } }
            .launchIn(viewModelScope)
    }
}
