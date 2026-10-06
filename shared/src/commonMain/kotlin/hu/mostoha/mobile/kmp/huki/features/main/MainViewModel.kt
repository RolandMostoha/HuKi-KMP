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
import hu.mostoha.mobile.kmp.huki.features.map.OktUiState
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
import hu.mostoha.mobile.kmp.huki.model.domain.HikeRecommendation
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.MyLocationStatus
import hu.mostoha.mobile.kmp.huki.model.domain.NonGpxFileException
import hu.mostoha.mobile.kmp.huki.model.domain.OktInfoWindowData
import hu.mostoha.mobile.kmp.huki.model.domain.OktMarker
import hu.mostoha.mobile.kmp.huki.model.domain.OktTrail
import hu.mostoha.mobile.kmp.huki.model.domain.OktType
import hu.mostoha.mobile.kmp.huki.model.domain.OsmType
import hu.mostoha.mobile.kmp.huki.model.domain.Place
import hu.mostoha.mobile.kmp.huki.model.domain.PlaceDetails
import hu.mostoha.mobile.kmp.huki.model.domain.RoutePlan
import hu.mostoha.mobile.kmp.huki.model.domain.RouteProgress
import hu.mostoha.mobile.kmp.huki.model.domain.Sheet
import hu.mostoha.mobile.kmp.huki.model.domain.WaypointType
import hu.mostoha.mobile.kmp.huki.model.domain.toLocations
import hu.mostoha.mobile.kmp.huki.model.mapper.toBlueTrail
import hu.mostoha.mobile.kmp.huki.model.mapper.toHikeRecommender
import hu.mostoha.mobile.kmp.huki.model.mapper.toInfoWindowTitle
import hu.mostoha.mobile.kmp.huki.model.mapper.toLayer
import hu.mostoha.mobile.kmp.huki.model.mapper.toMyLocationMode
import hu.mostoha.mobile.kmp.huki.model.mapper.toOktLine
import hu.mostoha.mobile.kmp.huki.model.mapper.toOktMarkers
import hu.mostoha.mobile.kmp.huki.model.mapper.toOktSectionItem
import hu.mostoha.mobile.kmp.huki.model.mapper.toPlace
import hu.mostoha.mobile.kmp.huki.model.mapper.toReverseGeocodedPlace
import hu.mostoha.mobile.kmp.huki.model.mapper.toScreen
import hu.mostoha.mobile.kmp.huki.model.mapper.toUrl
import hu.mostoha.mobile.kmp.huki.model.mapper.withDistanceFrom
import hu.mostoha.mobile.kmp.huki.model.mapper.withoutGpx
import hu.mostoha.mobile.kmp.huki.model.network.NetworkResult
import hu.mostoha.mobile.kmp.huki.repository.DestinationRepository
import hu.mostoha.mobile.kmp.huki.repository.GeocodingRepository
import hu.mostoha.mobile.kmp.huki.repository.GpxRepository
import hu.mostoha.mobile.kmp.huki.repository.MapCameraStore
import hu.mostoha.mobile.kmp.huki.repository.OktRepository
import hu.mostoha.mobile.kmp.huki.repository.PlaceHistoryRepository
import hu.mostoha.mobile.kmp.huki.repository.SettingsRepository
import hu.mostoha.mobile.kmp.huki.repository.WhatsNewRepository
import hu.mostoha.mobile.kmp.huki.service.AnalyticsService
import hu.mostoha.mobile.kmp.huki.service.CrashlyticsService
import hu.mostoha.mobile.kmp.huki.service.LocationMonitoringService
import hu.mostoha.mobile.kmp.huki.service.locations
import hu.mostoha.mobile.kmp.huki.util.AppLaunchConfig
import hu.mostoha.mobile.kmp.huki.util.MapConstants.PLACE_DEFAULT_CAMERA_ZOOM
import hu.mostoha.mobile.kmp.huki.util.TrackPosition
import hu.mostoha.mobile.kmp.huki.util.distanceBetween
import hu.mostoha.mobile.kmp.huki.util.formatter.DistanceFormatter
import hu.mostoha.mobile.kmp.huki.util.formatter.TravelTimeFormatter
import hu.mostoha.mobile.kmp.huki.util.nearestIndexTo
import hu.mostoha.mobile.kmp.huki.util.routeProgressTo
import kotlinx.coroutines.CancellationException
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
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.maplibre.spatialk.units.extensions.inMeters
import org.maplibre.spatialk.units.extensions.meters
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
    private val oktRepository: OktRepository,
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

    private var oktTrail: OktTrail? = null
    private var oktJob: Job? = null
    private var oktLocationJob: Job? = null
    private var oktUserPosition: TrackPosition? = null

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
            // Discover events
            MainUiEvents.DiscoverClicked -> showSheet(Sheet.Discover)
            is MainUiEvents.HikeRecommendationClicked -> openHikeRecommendation(event.recommendation)
            MainUiEvents.DiscoverBrowseDestinationsClicked -> browseDestinations()
            MainUiEvents.HikeRecommendationsInfoClicked -> logHikeRecommendationsInfoOpened()
            is OktUiEvents -> onOktEvent(event)
            // Map events
            is MainUiEvents.MapCameraChanged -> mapCameraStore.update(event.cameraPosition)
            // Place Details events
            is MainUiEvents.MapLongClicked -> handleMapLongClick(event.location)
            MainUiEvents.PlaceDetailsCloseClicked -> closePlaceDetails()
            MainUiEvents.PlaceDetailsRoutePlanClicked -> planRoute()
            MainUiEvents.PlaceDetailsMapsNavigationClicked -> openPlaceMapsNavigation()
            // Route Planner events
            MainUiEvents.RoutePlannerClicked -> showRoutePlanner(place = null)
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

    private fun onOktEvent(event: OktUiEvents) {
        when (event) {
            is OktUiEvents.OktTypeClicked -> openOkt(event.type)
            OktUiEvents.OktInfoClicked -> analyticsService.logEvent(AnalyticsEvent.OktInfoClicked)
            is OktUiEvents.OktSectionClicked -> selectOktSection(event.sectionId)
            is OktUiEvents.OktSectionStartClicked -> startOktSection(event.sectionId)
            is OktUiEvents.OktSectionWebsiteClicked -> openOktSectionWebsite(event.sectionId)
            is OktUiEvents.OktSectionReverseClicked -> reverseOktSection(event.sectionId)
            OktUiEvents.OktResumeClicked -> resumeOkt()
            is OktUiEvents.OktMarkerClicked -> selectOktMarker(event.marker)
            is OktUiEvents.OktLineClicked -> selectNearestOktSection(event.location)
            OktUiEvents.OktInfoWindowDismissed -> dismissOktInfoWindow()
            OktUiEvents.OktInfoWindowPlaceDetailsClicked -> showOktMarkerPlaceDetails()
            OktUiEvents.OktCloseClicked -> closeOkt()
        }
    }

    private fun showSearch() {
        analyticsService.logEvent(AnalyticsEvent.SearchOpened)
        showSheet(Sheet.Search)
    }

    private fun openHikeRecommendation(recommendation: HikeRecommendation) {
        analyticsService.logEvent(AnalyticsEvent.HikeRecommendationSelected(recommendation.toHikeRecommender()))
        viewModelScope.launch {
            sendEffect(MainUiEffects.OpenUrl(recommendation.toUrl(mapCameraStore.cameraPosition.location)))
        }
    }

    private fun logHikeRecommendationsInfoOpened() {
        analyticsService.logEvent(AnalyticsEvent.HikeRecommendationsInfoOpened)
    }

    private fun browseDestinations() {
        analyticsService.logEvent(AnalyticsEvent.DiscoverBrowseDestinationsClicked)
        hideSheet()
        viewModelScope.launch {
            sendEffect(MainUiEffects.NavigateToDestinations)
        }
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
        if (swipedSheet == Sheet.Okt) {
            refitOktCamera(ContentPadding.MAP_OKT_STARTED)
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

    private fun showPlaceDetails(location: Location, source: PlaceDetailsSource = PlaceDetailsSource.LONG_TAP) {
        analyticsService.logEvent(AnalyticsEvent.PlaceDetailsOpened(source))
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
        showRoutePlanner((placeDetails as? PlaceDetails.PlaceLoaded)?.place)
    }

    private fun showRoutePlanner(place: Place?) {
        selectedWaypoint.value = null
        cancelPlaceDetailsLoad()
        _uiState.update { uiState ->
            uiState.copy(
                sheet = Sheet.RoutePlanner(place),
                mapUiState = uiState.mapUiState
                    .withoutGpx()
                    .copy(
                        placeDetails = null,
                        routePlan = null,
                        routePlanWaypoints = emptyList(),
                    ),
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
            .distinctUntilChanged()
            .onEach { settings ->
                _uiState.update {
                    it.copy(
                        mapZoomControlsAlwaysVisible = settings.mapZoomControlsVisible,
                        themeMode = settings.themeMode,
                        mapUiState = it.mapUiState.copy(baseLayer = settings.baseLayer),
                    )
                }
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
        viewModelScope.launch {
            settingsRepository.setBaseLayer(baseLayer)
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
                    mapUiState = uiState.mapUiState
                        .withoutGpx()
                        .copy(placeDetails = null),
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
                    cancelOktJobs()
                    oktTrail = null
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
                                okt = null,
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

    private fun openOkt(type: OktType) {
        analyticsService.logEvent(AnalyticsEvent.OktOpened(type.toBlueTrail()))
        selectedWaypoint.value = null
        cancelPlaceDetailsLoad()
        cancelOktJobs()
        oktJob = viewModelScope.launch {
            _uiState.update { it.copy(sheet = null, isOktLoading = true, alert = null) }
            try {
                showOktTrail(oktRepository.getOktTrail(type))
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                onOktLoadFailed(exception)
            }
        }
    }

    private suspend fun showOktTrail(trail: OktTrail) {
        val fullTrail = trail.sections.first { it.section.id == trail.type.fullTrailId }
        val (sections, baseLine) = withContext(defaultDispatcher) {
            trail.sections.map { it.toOktSectionItem(trail.type) } to
                trail.baseLine.toOktLine("okt_base_${trail.type.name}")
        }
        oktTrail = trail
        observeOktUserPosition(trail)
        _uiState.update { uiState ->
            uiState.copy(
                mapUiState = uiState.mapUiState
                    .withoutGpx()
                    .copy(
                        placeDetails = null,
                        routePlan = null,
                        routePlanWaypoints = emptyList(),
                        okt = OktUiState(
                            type = trail.type,
                            sections = sections,
                            selectedSectionId = fullTrail.section.id,
                            baseLine = baseLine,
                            selectedLine = baseLine,
                            markers = fullTrail.toOktMarkers(),
                        ),
                    ),
                sheet = Sheet.Okt,
                isOktLoading = false,
            )
        }
        fitCameraTo(baseLine.bounds, ContentPadding.MAP_OKT)
    }

    private fun onOktLoadFailed(exception: Exception) {
        Logger.e(exception) { "Okt: failed to load the trail." }
        analyticsService.logEvent(AnalyticsEvent.OktLoadFailed)
        crashlyticsService.recordException(exception)
        _uiState.update {
            it.copy(
                alert = Alert(
                    title = SharedRes.strings.okt_header_title,
                    message = SharedRes.strings.okt_loading_error,
                ),
                isOktLoading = false,
            )
        }
    }

    private fun selectOktSection(sectionId: String) {
        analyticsService.logEvent(AnalyticsEvent.OktSectionSelected(sectionId))
        showOktSection(sectionId, ContentPadding.MAP_OKT)
    }

    private fun startOktSection(sectionId: String) {
        analyticsService.logEvent(AnalyticsEvent.OktSectionStarted(sectionId))
        hideSheet()
        showOktSection(sectionId, ContentPadding.MAP_OKT_STARTED)
    }

    private fun showOktSection(sectionId: String, contentPadding: ContentPadding) {
        val trail = oktTrail ?: return
        val okt = _uiState.value.mapUiState.okt ?: return
        val geometry = trail.sections.firstOrNull { it.section.id == sectionId } ?: return
        oktJob?.cancel()
        oktJob = viewModelScope.launch {
            val line = when (sectionId) {
                okt.selectedSectionId -> okt.selectedLine
                trail.type.fullTrailId -> okt.baseLine
                else -> withContext(defaultDispatcher) { geometry.locations.toOktLine("okt_section_$sectionId") }
            }
            _uiState.updateOktUiState {
                it.copy(
                    selectedSectionId = sectionId,
                    selectedLine = line,
                    markers = geometry.toOktMarkers(),
                    selectedMarkerId = null,
                    infoWindow = null,
                )
            }
            fitCameraTo(line.bounds, contentPadding)
        }
    }

    private fun reverseOktSection(sectionId: String) {
        val okt = _uiState.value.mapUiState.okt ?: return
        analyticsService.logEvent(AnalyticsEvent.OktSectionReversed(sectionId))
        _uiState.updateOktUiState {
            val reversedSectionIds = if (it.isReversed(sectionId)) {
                it.reversedSectionIds - sectionId
            } else {
                it.reversedSectionIds + sectionId
            }
            it.copy(reversedSectionIds = reversedSectionIds)
        }
        if (okt.selectedSectionId != sectionId) {
            showOktSection(sectionId, ContentPadding.MAP_OKT)
        }
    }

    private fun openOktSectionWebsite(sectionId: String) {
        val section = _uiState.value.mapUiState.okt?.sections?.firstOrNull { it.id == sectionId } ?: return
        analyticsService.logEvent(AnalyticsEvent.OktSectionLinkClicked(sectionId))
        viewModelScope.launch {
            sendEffect(MainUiEffects.OpenUrl(section.websiteUrl))
        }
    }

    private fun resumeOkt() {
        showSheet(Sheet.Okt)
        refitOktCamera(ContentPadding.MAP_OKT)
    }

    private fun refitOktCamera(contentPadding: ContentPadding) {
        val okt = _uiState.value.mapUiState.okt ?: return
        viewModelScope.launch {
            fitCameraTo(okt.selectedLine.bounds, contentPadding)
        }
    }

    private fun selectNearestOktSection(location: Location) {
        val trail = oktTrail ?: return
        analyticsService.logEvent(AnalyticsEvent.OktLineClicked)
        viewModelScope.launch {
            val nearest = withContext(defaultDispatcher) {
                trail.sections
                    .filter { it.section.id != trail.type.fullTrailId }
                    .minByOrNull { geometry ->
                        val nearestLocation = geometry.locations[geometry.locations.nearestIndexTo(location)]
                        nearestLocation.distanceBetween(location).inMeters
                    }
            } ?: return@launch
            val contentPadding = if (_uiState.value.sheet == Sheet.Okt) {
                ContentPadding.MAP_OKT
            } else {
                ContentPadding.MAP_OKT_STARTED
            }
            showOktSection(nearest.section.id, contentPadding)
        }
    }

    private fun selectOktMarker(marker: OktMarker) {
        analyticsService.logEvent(AnalyticsEvent.OktStampClicked)
        val progress = oktProgressTo(marker)
        _uiState.updateOktUiState {
            it.copy(
                selectedMarkerId = marker.id,
                infoWindow = OktInfoWindowData(marker = marker, title = marker.toInfoWindowTitle())
                    .withProgress(progress),
            )
        }
        viewModelScope.launch {
            sendEffect(
                MapUiEffects.UpdateCamera(
                    target = CameraTarget.Center(marker.location),
                    contentPadding = if (_uiState.value.sheet == Sheet.Okt) {
                        ContentPadding.MAP_OKT
                    } else {
                        ContentPadding.MAP_OKT_STARTED
                    },
                ),
            )
        }
    }

    private fun observeOktUserPosition(trail: OktTrail) {
        oktLocationJob?.cancel()
        oktLocationJob = locationMonitoringService.locations()
            .distinctUntilChanged { old, new -> old.distanceBetween(new) < OKT_USER_POSITION_MIN_MOVE }
            .map { location ->
                trail.progressIndex.positionOf(location).takeIf { it.offTrackDistance <= OKT_ON_TRAIL_THRESHOLD }
            }
            .flowOn(defaultDispatcher)
            .onEach { position ->
                oktUserPosition = position
                refreshOktInfoWindowProgress()
            }
            .launchIn(viewModelScope)
    }

    private fun refreshOktInfoWindowProgress() {
        val infoWindow = _uiState.value.mapUiState.okt?.infoWindow?.takeIf { it.distance != null } ?: return
        val progress = oktProgressTo(infoWindow.marker) ?: return
        _uiState.updateOktUiState { okt ->
            okt.copy(
                infoWindow = okt.infoWindow?.takeIf {
                    it.marker.id == infoWindow.marker.id
                }?.withProgress(progress),
            )
        }
    }

    private fun OktInfoWindowData.withProgress(progress: RouteProgress?): OktInfoWindowData =
        copy(
            distance = progress?.let { DistanceFormatter.formatDistance(it.distance) },
            travelTime = progress?.let { TravelTimeFormatter.formatTravelTime(it.travelTime) },
        )

    private fun oktProgressTo(marker: OktMarker): RouteProgress? {
        val trail = oktTrail ?: return null
        val userPosition = oktUserPosition ?: return null
        val markerPosition = trail.markerPositions[marker.id] ?: return null
        return trail.progressIndex.progress(from = userPosition, to = markerPosition)
    }

    private fun dismissOktInfoWindow() {
        _uiState.updateOktUiState { it.copy(selectedMarkerId = null, infoWindow = null) }
    }

    private fun showOktMarkerPlaceDetails() {
        val marker = _uiState.value.mapUiState.okt?.infoWindow?.marker ?: return
        dismissOktInfoWindow()
        showPlaceDetails(marker.location, PlaceDetailsSource.OKT_STAMP)
    }

    private fun closeOkt() {
        analyticsService.logEvent(AnalyticsEvent.OktClosed)
        cancelOktJobs()
        oktTrail = null
        _uiState.update { uiState ->
            uiState.copy(
                mapUiState = uiState.mapUiState.copy(okt = null),
                sheet = uiState.sheet.takeUnless { it == Sheet.Okt },
                isOktLoading = false,
            )
        }
    }

    private fun cancelOktJobs() {
        oktJob?.cancel()
        oktJob = null
        oktLocationJob?.cancel()
        oktLocationJob = null
        oktUserPosition = null
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
        val OKT_ON_TRAIL_THRESHOLD = 500.meters
        val OKT_USER_POSITION_MIN_MOVE = 50.meters
    }
}
