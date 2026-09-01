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
import hu.mostoha.mobile.kmp.huki.model.analytics.GpxShareSource
import hu.mostoha.mobile.kmp.huki.model.analytics.Layer
import hu.mostoha.mobile.kmp.huki.model.analytics.PlaceDetailsSource
import hu.mostoha.mobile.kmp.huki.model.analytics.Screen
import hu.mostoha.mobile.kmp.huki.model.domain.Alert
import hu.mostoha.mobile.kmp.huki.model.domain.BaseLayer
import hu.mostoha.mobile.kmp.huki.model.domain.CameraTarget
import hu.mostoha.mobile.kmp.huki.model.domain.ContentPadding
import hu.mostoha.mobile.kmp.huki.model.domain.Destination
import hu.mostoha.mobile.kmp.huki.model.domain.DistanceInfoWindowData
import hu.mostoha.mobile.kmp.huki.model.domain.DomainException
import hu.mostoha.mobile.kmp.huki.model.domain.EmptyGpxContentException
import hu.mostoha.mobile.kmp.huki.model.domain.GpxMapsNavigationType
import hu.mostoha.mobile.kmp.huki.model.domain.GpxWaypoint
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.MyLocationStatus
import hu.mostoha.mobile.kmp.huki.model.domain.NonGpxFileException
import hu.mostoha.mobile.kmp.huki.model.domain.OsmType
import hu.mostoha.mobile.kmp.huki.model.domain.Place
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceDetails
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlan
import hu.mostoha.mobile.kmp.huki.model.domain.Sheet
import hu.mostoha.mobile.kmp.huki.model.domain.WaypointType
import hu.mostoha.mobile.kmp.huki.model.domain.toLocations
import hu.mostoha.mobile.kmp.huki.model.mapper.toLayer
import hu.mostoha.mobile.kmp.huki.model.mapper.toMyLocationMode
import hu.mostoha.mobile.kmp.huki.model.mapper.toPlace
import hu.mostoha.mobile.kmp.huki.model.mapper.toReverseGeocodedPlace
import hu.mostoha.mobile.kmp.huki.model.mapper.toScreen
import hu.mostoha.mobile.kmp.huki.model.mapper.withDistanceFrom
import hu.mostoha.mobile.kmp.huki.model.network.NetworkResult
import hu.mostoha.mobile.kmp.huki.repository.DestinationRepository
import hu.mostoha.mobile.kmp.huki.repository.GeocodingRepository
import hu.mostoha.mobile.kmp.huki.repository.GpxRepository
import hu.mostoha.mobile.kmp.huki.repository.MapCameraStore
import hu.mostoha.mobile.kmp.huki.repository.PlaceHistoryRepository
import hu.mostoha.mobile.kmp.huki.repository.SettingsRepository
import hu.mostoha.mobile.kmp.huki.repository.WhatsNewRepository
import hu.mostoha.mobile.kmp.huki.service.AnalyticsService
import hu.mostoha.mobile.kmp.huki.service.CrashlyticsService
import hu.mostoha.mobile.kmp.huki.service.LocationMonitoringService
import hu.mostoha.mobile.kmp.huki.service.locations
import hu.mostoha.mobile.kmp.huki.util.AppLaunchConfig
import hu.mostoha.mobile.kmp.huki.util.MapConstants.PLACE_DEFAULT_CAMERA_ZOOM
import hu.mostoha.mobile.kmp.huki.util.distanceBetween
import hu.mostoha.mobile.kmp.huki.util.formatter.DistanceFormatter
import hu.mostoha.mobile.kmp.huki.util.formatter.TravelTimeFormatter
import hu.mostoha.mobile.kmp.huki.util.routeProgressTo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
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
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.seconds

class MainViewModel(
    val permissionsController: PermissionsController,
    val gpxRepository: GpxRepository,
    private val placeHistoryRepository: PlaceHistoryRepository,
    private val destinationRepository: DestinationRepository,
    private val geocodingRepository: GeocodingRepository,
    private val locationMonitoringService: LocationMonitoringService,
    private val settingsRepository: SettingsRepository,
    private val mapCameraStore: MapCameraStore,
    private val whatsNewRepository: WhatsNewRepository,
    private val analyticsService: AnalyticsService,
    private val crashlyticsService: CrashlyticsService,
    private val defaultDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState.Default)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _mainUiEffects = Channel<MainUiEffects>(Channel.BUFFERED)
    val mainUiEffects: Flow<MainUiEffects> = _mainUiEffects.receiveAsFlow()

    private val _mapUiEffects = Channel<MapUiEffects>(Channel.BUFFERED)
    val mapUiEffects: Flow<MapUiEffects> = _mapUiEffects.receiveAsFlow()

    private val selectedWaypoint = MutableStateFlow<GpxWaypoint?>(null)

    private var placeDetailsJob: Job? = null

    init {
        initLogging()
        initMyLocation()
        initDistanceMonitoring()
        initWhatsNew()
        initLaunchArgGpx()
        observeSettings()
        observeSheetViews()
    }

    fun onEvent(event: MainUiEvents) {
        logEventChanges(event)
        when (event) {
            // General events
            MainUiEvents.SheetDismissed -> hideSheet()
            is MainUiEvents.SheetSwipeDismissed -> hideSheet(event.sheet)
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
            // Map events
            is MainUiEvents.MapCameraChanged -> mapCameraStore.update(event.cameraPosition)
            // Place Details events
            is MainUiEvents.MapLongClicked -> handleMapLongClick(event.location)
            MainUiEvents.PlaceDetailsCloseClicked -> closePlaceDetails()
            MainUiEvents.PlaceDetailsRoutePlanClicked -> planRoute()
            MainUiEvents.PlaceDetailsMapsNavigationClicked -> openPlaceMapsNavigation()
            // Route Planner events
            MainUiEvents.RoutePlannerClicked -> showSheet(Sheet.RoutePlanner(place = null))
            is MainUiEvents.RoutePlanUpdated -> showRoutePlan(event.routePlan, event.markers)
            // My location events
            MainUiEvents.MyLocationClicked -> enableMyLocation()
            MainUiEvents.MyLocationLongClicked -> enableMyLocation(MyLocationStatus.FollowingLiveCompass)
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
            MainUiEvents.GpxShareClicked -> shareGpx()
            is MainUiEvents.GpxFileSelected -> importGpx(event.uri, AnalyticsEvent.GpxImported(event.source))
            is MainUiEvents.GpxFileReopened -> importGpx(event.uri, AnalyticsEvent.HistoryGpxSelected)
            is MainUiEvents.RoutePlanGpxSaved -> importGpx(event.uri, analyticsEvent = null)
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
        cancelPlaceDetailsLoad()
        _uiState.update { uiState ->
            uiState.copy(
                sheet = sheet,
                mapUiState = uiState.mapUiState.copy(
                    placeDetails = null,
                    routePlan = null,
                    routePlanWaypoints = emptyList(),
                ),
            )
        }
    }

    private fun hideSheet(swipedSheet: Sheet? = null) {
        if (swipedSheet != null && _uiState.value.sheet != swipedSheet) return
        cancelPlaceDetailsLoad()
        _uiState.update { uiState ->
            uiState.copy(
                sheet = null,
                mapUiState = uiState.mapUiState.copy(
                    placeDetails = null,
                    routePlan = null,
                    routePlanWaypoints = emptyList(),
                ),
            )
        }
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
        showPlace(place, PlaceDetailsSource.SEARCH)
    }

    private fun showRecentPlace(place: Place) {
        analyticsService.logEvent(AnalyticsEvent.HistoryPlaceSelected)
        showPlace(place, PlaceDetailsSource.HISTORY)
    }

    private fun showSearchResultPlaceHistory(place: Place) {
        analyticsService.logEvent(AnalyticsEvent.SearchPlaceHistorySelected)
        showPlace(place, PlaceDetailsSource.HISTORY)
    }

    private fun showPlace(place: Place, source: PlaceDetailsSource) {
        analyticsService.logEvent(AnalyticsEvent.PlaceDetailsOpened(source))
        showPlaceDetailsSheet(PlaceDetails.PlaceLoaded(place))
        fillPlaceDistance(place)
        viewModelScope.launch {
            val boundingBox = place.boundingBox
            sendEffect(
                if (boundingBox != null) {
                    MapUiEffects.UpdateCamera(
                        target = CameraTarget.Bounds(
                            locations = boundingBox.toLocations(),
                            maxZoom = PLACE_DEFAULT_CAMERA_ZOOM,
                        ),
                        contentPadding = ContentPadding.MAP_PLACE_DETAILS,
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
            showPlace(place, PlaceDetailsSource.HISTORY)
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
        showPlace(destination.toPlace(), PlaceDetailsSource.DESTINATION)
    }

    private fun showDestinationById(osmId: String) {
        val destination = destinationRepository.requireDestination(osmId)
        analyticsService.logEvent(AnalyticsEvent.DestinationSelected(destination.name))
        showDestination(destination)
    }

    private fun handleMapLongClick(location: Location) {
        when (_uiState.value.sheet) {
            is Sheet.RoutePlanner -> viewModelScope.launch {
                sendEffect(MainUiEffects.RoutePlannerLocationPicked(location))
            }
            else -> showPlaceDetails(location)
        }
    }

    private fun showPlaceDetails(location: Location) {
        analyticsService.logEvent(AnalyticsEvent.PlaceDetailsOpened(PlaceDetailsSource.LONG_TAP))
        showPlaceDetailsSheet(PlaceDetails.Loading(location))

        launchPlaceDetailsLoad {
            val result = geocodingRepository.reverseGeocode(location)
            val userLocation = withTimeoutOrNull(PLACE_DETAILS_LOCATION_TIMEOUT) {
                locationMonitoringService.lastKnownLocation()
            }
            val place = (result as? NetworkResult.Success)?.data?.toReverseGeocodedPlace(location, userLocation)
            if (place == null) {
                analyticsService.logEvent(AnalyticsEvent.PlaceDetailsUnresolved)
            }
            val placeDetails = place?.let { PlaceDetails.PlaceLoaded(it) }
                ?: PlaceDetails.Unresolved(
                    location = location,
                    distance = userLocation?.let {
                        DistanceFormatter.formatDistance(it.distanceBetween(location))
                    },
                )
            _uiState.updateMapUiState { it.copy(placeDetails = placeDetails) }
            place?.let { placeHistoryRepository.recordVisit(it) }
        }
    }

    private fun fillPlaceDistance(place: Place) {
        if (place.distance != null) return

        launchPlaceDetailsLoad {
            val userLocation = withTimeoutOrNull(PLACE_DETAILS_LOCATION_TIMEOUT) {
                locationMonitoringService.lastKnownLocation()
            } ?: return@launchPlaceDetailsLoad
            _uiState.updateMapUiState {
                it.copy(placeDetails = PlaceDetails.PlaceLoaded(place.withDistanceFrom(userLocation)))
            }
        }
    }

    private fun showPlaceDetailsSheet(placeDetails: PlaceDetails) {
        cancelPlaceDetailsLoad()
        _uiState.update { uiState ->
            uiState.copy(
                mapUiState = uiState.mapUiState.copy(placeDetails = placeDetails),
                sheet = Sheet.PlaceDetails,
            )
        }
    }

    private fun launchPlaceDetailsLoad(block: suspend () -> Unit) {
        cancelPlaceDetailsLoad()
        placeDetailsJob = viewModelScope.launch { block() }
    }

    private fun cancelPlaceDetailsLoad() {
        placeDetailsJob?.cancel()
        placeDetailsJob = null
    }

    private fun closePlaceDetails() {
        analyticsService.logEvent(AnalyticsEvent.PlaceDetailsClosed)
        hideSheet()
    }

    private fun openPlaceMapsNavigation() {
        val placeDetails = _uiState.value.mapUiState.placeDetails ?: return
        analyticsService.logEvent(AnalyticsEvent.PlaceDetailsMapsNavigationOpened)
        viewModelScope.launch {
            sendEffect(MainUiEffects.OpenMapsNavigation(placeDetails.location))
        }
    }

    private fun planRoute() {
        val placeDetails = _uiState.value.mapUiState.placeDetails ?: return
        analyticsService.logEvent(AnalyticsEvent.PlaceDetailsRoutePlanClicked)
        val place = (placeDetails as? PlaceDetails.PlaceLoaded)?.place
        cancelPlaceDetailsLoad()
        _uiState.update { uiState ->
            uiState.copy(
                sheet = Sheet.RoutePlanner(place),
                mapUiState = uiState.mapUiState.copy(placeDetails = null),
            )
        }
    }

    private fun showRoutePlan(routePlan: RoutePlan?, markers: List<GpxWaypoint>) {
        val mapUiState = _uiState.value.mapUiState
        val isNewPlan = mapUiState.routePlan != routePlan
        val hasNewStops = mapUiState.routePlanWaypoints != markers
        _uiState.updateMapUiState { it.copy(routePlan = routePlan, routePlanWaypoints = markers) }

        if (routePlan != null) {
            if (!isNewPlan) return
            viewModelScope.launch {
                fitCameraTo(routePlan.locations + routePlan.waypoints, ContentPadding.MAP_ROUTE_PLANNER)
            }
            return
        }

        if (!hasNewStops) return
        val stopLocations = markers.map { it.location }.ifEmpty { return }
        viewModelScope.launch {
            sendEffect(
                MapUiEffects.UpdateCamera(
                    target = CameraTarget.Bounds(stopLocations, maxZoom = PLACE_DEFAULT_CAMERA_ZOOM),
                    contentPadding = ContentPadding.MAP_ROUTE_PLANNER,
                ),
            )
        }
    }

    private suspend fun fitCameraTo(bounds: List<Location>, contentPadding: ContentPadding) {
        sendEffect(MapUiEffects.UpdateCamera(CameraTarget.Bounds(bounds), contentPadding = contentPadding))
    }

    private fun enableMyLocation(targetStatus: MyLocationStatus? = null) {
        viewModelScope.launch {
            withLocationPermission {
                val newStatus = targetStatus ?: when (uiState.value.myLocationState.myLocationStatus) {
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
        _uiState.updateMyLocationState {
            it.copy(myLocationStatus = MyLocationStatus.Default)
        }
    }

    private fun zoom(zoomIn: Boolean) {
        viewModelScope.launch {
            sendEffect(MapUiEffects.Zoom(zoomIn))
        }
    }

    private fun resetCameraToNorth() {
        viewModelScope.launch {
            when (_uiState.value.myLocationState.myLocationStatus) {
                MyLocationStatus.Following, MyLocationStatus.FollowingLiveCompass -> {
                    val newStatus = MyLocationStatus.Following
                    _uiState.updateMyLocationState { it.copy(myLocationStatus = newStatus) }
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

    private fun initLaunchArgGpx() {
        val path = AppLaunchConfig.importGpxPath ?: return
        importGpx(path, null)
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
            .onSuccess {
                analyticsService.logEvent(AnalyticsEvent.LocationPermissionGranted)
                onGranted()
            }
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
            _uiState.updateMapUiState {
                it.copy(gpxLayerVisible = it.gpxLayerVisible.not())
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

    private fun shareGpx() {
        val gpxDetails = _uiState.value.mapUiState.gpxDetails ?: return
        analyticsService.logEvent(AnalyticsEvent.GpxShared(GpxShareSource.DETAILS))
        viewModelScope.launch {
            sendEffect(MainUiEffects.ShareGpxFile(fileUri = gpxDetails.fileUri, fileName = gpxDetails.fileName))
        }
    }

    private fun closeGpx() {
        analyticsService.logEvent(AnalyticsEvent.GpxClosed)
        selectedWaypoint.value = null
        cancelPlaceDetailsLoad()
        viewModelScope.launch {
            _uiState.update { uiState ->
                uiState.copy(
                    mapUiState = uiState.mapUiState.copy(
                        gpxDetails = null,
                        placeDetails = null,
                        gpxLayerVisible = false,
                        gpxRouteVisible = true,
                        allDistancesVisible = false,
                        distanceInfoWindows = emptyList(),
                    ),
                    sheet = null,
                )
            }
        }
    }

    private fun importGpx(uri: String, analyticsEvent: AnalyticsEvent?) {
        cancelPlaceDetailsLoad()
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isGpxLoading = true,
                    alert = null,
                )
            }
            runCatching { gpxRepository.readGpxFile(uri) }
                .onSuccess { gpxDetails ->
                    analyticsEvent?.let { analyticsService.logEvent(it) }
                    selectedWaypoint.value = null
                    _uiState.update { uiState ->
                        uiState.copy(
                            mapUiState = uiState.mapUiState.copy(
                                gpxDetails = gpxDetails,
                                placeDetails = null,
                                routePlan = null,
                                routePlanWaypoints = emptyList(),
                                gpxLayerVisible = true,
                                gpxRouteVisible = true,
                                allDistancesVisible = false,
                                distanceInfoWindows = emptyList(),
                            ),
                            sheet = Sheet.Gpx(gpxDetails),
                            alert = null,
                            isGpxLoading = false,
                        )
                    }
                    fitCameraTo(gpxDetails.bounds, ContentPadding.MAP_GPX)
                }
                .onFailure { exception -> onGpxImportFailed(exception) }
        }
    }

    private fun onGpxImportFailed(exception: Throwable) {
        Logger.e(exception) { "Failed to import GPX file." }
        analyticsService.logEvent(AnalyticsEvent.GpxImportFailed)
        if (exception !is NonGpxFileException && exception !is EmptyGpxContentException) {
            crashlyticsService.recordException(exception)
        }
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
            fitCameraTo(gpxDetails.bounds, ContentPadding.MAP_GPX)
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

    private fun logEventChanges(event: MainUiEvents) {
        // Camera changes arrive on every frame of a pan
        if (event !is MainUiEvents.MapCameraChanged) {
            Logger.d { "MainEvent: $event" }
        }
    }

    private companion object {
        val PLACE_DETAILS_LOCATION_TIMEOUT = 2.seconds
    }
}
