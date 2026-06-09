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
            MainUiEvents.FollowingDisabled -> _uiState.updateMyLocationState {
                it.copy(myLocationStatus = MyLocationStatus.Default)
            }
            MainUiEvents.LayersClicked -> showSheet(Sheet.Layers)
            is MainUiEvents.BaseLayerSelected -> _uiState.updateMapUiState {
                it.copy(baseLayer = event.baseLayer)
            }
            MainUiEvents.HikingLayerSelected -> _uiState.updateMapUiState {
                it.copy(hikingLayerVisible = it.hikingLayerVisible.not())
            }
            MainUiEvents.GpxLayerSelected -> switchGpxLayer()
            MainUiEvents.GpxStartNavigationClicked -> startGpxNavigation()
            is MainUiEvents.GpxRouteClicked -> showSheet(Sheet.Gpx(event.gpxDetails))
            is MainUiEvents.GpxFileSelected -> importGpx(event.uri)
            MainUiEvents.AlertDismissed -> dismissAlert()
            MainUiEvents.GpxCloseClicked -> closeGpx()
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
            when (val permissionState = permissionsController.getPermissionState(Permission.LOCATION)) {
                PermissionState.Granted -> {
                    val lastStatus = uiState.value.myLocationState.myLocationStatus
                    val newStatus = when (lastStatus) {
                        MyLocationStatus.Default -> MyLocationStatus.Following
                        MyLocationStatus.Following -> MyLocationStatus.FollowingLiveCompass
                        MyLocationStatus.FollowingLiveCompass -> MyLocationStatus.Following
                        MyLocationStatus.NotAvailable -> MyLocationStatus.Following
                    }
                    _uiState.update { uiState ->
                        uiState.copy(
                            myLocationState = uiState.myLocationState.copy(
                                permissionState = permissionState,
                                myLocationStatus = newStatus,
                            ),
                            isMyLocationLoading = !uiState.myLocationState.hasLocationFix,
                        )
                    }
                    sendEffect(MapUiEffects.ShowMyLocation(newStatus, animated = true))
                }
                else -> requestLocationPermission()
            }
        }
    }

    private fun requestLocationPermission() {
        viewModelScope.launch {
            runCatching {
                permissionsController.providePermission(Permission.LOCATION)
                enableMyLocation()
            }.onFailure { exception ->
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
            _uiState.updateMapUiState {
                it.copy(gpxLayerVisible = it.gpxLayerVisible.not())
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
                            ),
                            sheet = Sheet.Gpx(gpxDetails),
                            alert = null,
                            isGpxLoading = false,
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
            val targetStatus = MyLocationStatus.FollowingLiveCompass
            _uiState.update {
                it.copy(
                    myLocationState = it.myLocationState.copy(
                        myLocationStatus = targetStatus,
                    ),
                    isMyLocationLoading = !it.myLocationState.hasLocationFix,
                    sheet = null,
                )
            }
            sendEffect(MapUiEffects.ShowMyLocation(targetStatus, animated = true))
        }
    }

    private fun closeGpx() {
        viewModelScope.launch {
            _uiState.update { uiState ->
                uiState.copy(
                    mapUiState = uiState.mapUiState.copy(
                        gpxDetails = null,
                        gpxLayerVisible = false,
                    ),
                    sheet = null,
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
