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
import hu.mostoha.mobile.kmp.huki.model.domain.Alert
import hu.mostoha.mobile.kmp.huki.model.domain.DomainException
import hu.mostoha.mobile.kmp.huki.model.domain.MyLocationStatus
import hu.mostoha.mobile.kmp.huki.model.domain.Place
import hu.mostoha.mobile.kmp.huki.model.domain.Sheet
import hu.mostoha.mobile.kmp.huki.repository.GpxRepository
import hu.mostoha.mobile.kmp.huki.theme.SharedDimens
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

@Suppress("TooManyFunctions")
class MainViewModel(
    val permissionsController: PermissionsController,
    val gpxRepository: GpxRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState.Default)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _mainUiEffects = Channel<MainUiEffects>(Channel.BUFFERED)
    val mainUiEffects: Flow<MainUiEffects> = _mainUiEffects.receiveAsFlow()

    private val _mapUiEffects = Channel<MapUiEffects>(Channel.BUFFERED)
    val mapUiEffects: Flow<MapUiEffects> = _mapUiEffects.receiveAsFlow()

    init {
        initLogging()
        initMyLocation()
    }

    fun onEvent(event: MainUiEvents) {
        Logger.d { "MainEvent: $event" }
        when (event) {
            MainUiEvents.MyLocationClicked -> enableMyLocation()
            MainUiEvents.MyLocationReceived -> _uiState.update {
                it.copy(
                    myLocationState = it.myLocationState.copy(hasLocationFix = true),
                    isMyLocationLoading = false,
                )
            }
            MainUiEvents.FollowingDisabled -> _uiState.update {
                it.copy(
                    myLocationState = it.myLocationState.copy(myLocationStatus = MyLocationStatus.Default),
                    isSearchBarVisible = shouldShowSearchBar(it.mapUiState.gpxLayerVisible, MyLocationStatus.Default),
                )
            }
            MainUiEvents.CompassClicked -> resetCameraToNorth()
            MainUiEvents.LayersClicked -> showSheet(Sheet.Layers)
            is MainUiEvents.BaseLayerSelected -> _uiState.updateMapUiState {
                it.copy(baseLayer = event.baseLayer)
            }
            MainUiEvents.HikingLayerSelected -> _uiState.updateMapUiState {
                it.copy(hikingLayerVisible = it.hikingLayerVisible.not())
            }
            MainUiEvents.GpxLayerSelected -> switchGpxLayer()
            MainUiEvents.GpxStartNavigationClicked -> startGpxNavigation()
            is MainUiEvents.GpxFileSelected -> importGpx(event.uri)
            MainUiEvents.AlertDismissed -> dismissAlert()
            MainUiEvents.GpxCloseClicked -> closeGpx()
            MainUiEvents.GpxRouteVisibilityToggled -> _uiState.updateMapUiState {
                it.copy(gpxRouteVisible = it.gpxRouteVisible.not())
            }
            MainUiEvents.GpxOverviewClicked -> showGpxOverview()
            MainUiEvents.SheetDismissed -> hideSheet()
            MainUiEvents.SearchClicked -> showSheet(Sheet.Search)
            is MainUiEvents.SearchPlaceSelected -> showPlace(event.place)
        }
    }

    private fun showPlace(place: Place) {
        viewModelScope.launch {
            hideSheet()
            sendEffect(
                MapUiEffects.UpdateCamera(
                    bounds = listOf(place.location),
                    zoom = 16.0,
                ),
            )
        }
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

    private fun resetCameraToNorth() {
        viewModelScope.launch {
            val newStatus = MyLocationStatus.Following
            _uiState.update { uiState ->
                uiState.copy(
                    myLocationState = uiState.myLocationState.copy(myLocationStatus = newStatus),
                    isSearchBarVisible = shouldShowSearchBar(uiState.mapUiState.gpxLayerVisible, newStatus),
                )
            }
            sendEffect(MapUiEffects.ShowMyLocation(newStatus, animated = true))
        }
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
                if (exception is DeniedAlwaysException) {
                    sendEffect(MainUiEffects.NavigateToAppSettings)
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

    private fun shouldShowSearchBar(gpxLayerVisible: Boolean, myLocationStatus: MyLocationStatus): Boolean =
        !gpxLayerVisible && myLocationStatus != MyLocationStatus.FollowingLiveCompass

    private fun showSheet(sheet: Sheet) {
        _uiState.update { it.copy(sheet = sheet) }
    }

    private fun hideSheet() {
        _uiState.update { it.copy(sheet = null) }
    }

    private fun showGpxFilePicker() {
        viewModelScope.launch {
            hideSheet()
            sendEffect(MainUiEffects.ShowGpxFilePicker)
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

    private fun importGpx(uri: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isGpxLoading = true,
                    alert = null,
                )
            }
            runCatching { gpxRepository.readGpxFile(uri) }
                .onSuccess { gpxDetails ->
                    _uiState.update { uiState ->
                        uiState.copy(
                            mapUiState = uiState.mapUiState.copy(
                                gpxDetails = gpxDetails,
                                gpxLayerVisible = true,
                                gpxRouteVisible = true,
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
                            bounds = gpxDetails.bounds,
                            contentPadding = SharedDimens.GPX_CONTENT_PADDING,
                        ),
                    )
                }
                .onFailure { exception ->
                    Logger.e(exception) { "Failed to import GPX file." }
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

    private fun startGpxNavigation() {
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

    private fun showGpxOverview() {
        viewModelScope.launch {
            val gpxDetails = uiState.value.mapUiState.gpxDetails ?: return@launch
            sendEffect(
                MapUiEffects.UpdateCamera(
                    bounds = gpxDetails.bounds,
                    contentPadding = SharedDimens.GPX_CONTENT_PADDING,
                ),
            )
        }
    }

    private fun closeGpx() {
        viewModelScope.launch {
            _uiState.update { uiState ->
                uiState.copy(
                    mapUiState = uiState.mapUiState.copy(
                        gpxDetails = null,
                        gpxLayerVisible = false,
                        gpxRouteVisible = true,
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

    private fun dismissAlert() {
        viewModelScope.launch {
            _uiState.update { uiState ->
                uiState.copy(alert = null)
            }
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
