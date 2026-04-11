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
import hu.mostoha.mobile.kmp.huki.model.domain.Alert
import hu.mostoha.mobile.kmp.huki.model.domain.DomainException
import hu.mostoha.mobile.kmp.huki.logger.trimLongLists
import hu.mostoha.mobile.kmp.huki.model.domain.MyLocationStatus
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
            MainUiEvents.FollowingDisabled -> _uiState.updateMyLocationState {
                it.copy(myLocationStatus = MyLocationStatus.Default)
            }
            MainUiEvents.LayersClicked -> showLayersBottomSheet(true)
            MainUiEvents.LayersDismissed -> showLayersBottomSheet(false)
            is MainUiEvents.BaseLayerSelected -> _uiState.updateMapUiState {
                it.copy(baseLayer = event.baseLayer)
            }
            MainUiEvents.HikingLayerSelected -> _uiState.updateMapUiState {
                it.copy(hikingLayerVisible = it.hikingLayerVisible.not())
            }
            MainUiEvents.GpxLayerSelected -> handleGpxLayerSelected()
            MainUiEvents.GpxStartNavigationClicked -> startGpxNavigation()
            MainUiEvents.GpxRouteClicked -> showDetailsBottomSheet()
            is MainUiEvents.GpxFileSelected -> importGpx(event.uri)
            MainUiEvents.AlertDismissed -> dismissAlert()
            MainUiEvents.GpxCloseClicked -> closeGpx()
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
                    _uiState.updateMyLocationState { uiState ->
                        uiState.copy(
                            permissionState = permissionState,
                            myLocationStatus = newStatus,
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
            _uiState.updateMyLocationState { uiState ->
                uiState.copy(
                    permissionState = permissionState,
                    myLocationStatus = myLocationStatus,
                )
            }
            sendEffect(MapUiEffects.ShowMyLocation(myLocationStatus, animated = false))
        }
    }

    private fun showLayersBottomSheet(show: Boolean) {
        viewModelScope.launch {
            sendEffect(MainUiEffects.ShowLayersBottomSheet(show))
        }
    }

    private fun showGpxFilePicker() {
        viewModelScope.launch {
            sendEffect(MainUiEffects.ShowLayersBottomSheet(show = false))
            sendEffect(MainUiEffects.ShowGpxFilePicker)
        }
    }

    private fun handleGpxLayerSelected() {
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
                    isLoading = true,
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
                            alert = null,
                            isLoading = false,
                        )
                    }
                    sendEffect(
                        MapUiEffects.UpdateCamera(
                            bounds = gpxDetails.bounds,
                            contentPadding = SharedDimens.GPX_CONTENT_PADDING,
                        ),
                    )
                    sendEffect(MainUiEffects.ShowDetailsBottomSheet(show = true))
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
                            isLoading = false,
                        )
                    }
                }
        }
    }

    private fun startGpxNavigation() {
        viewModelScope.launch {
            val targetStatus = MyLocationStatus.FollowingLiveCompass
            _uiState.updateMyLocationState { uiState ->
                uiState.copy(myLocationStatus = targetStatus)
            }
            sendEffect(MapUiEffects.ShowMyLocation(targetStatus, animated = true))
            sendEffect(MainUiEffects.ShowDetailsBottomSheet(show = false))
        }
    }

    private fun showDetailsBottomSheet() {
        viewModelScope.launch {
            sendEffect(MainUiEffects.ShowDetailsBottomSheet(show = true))
        }
    }

    private fun closeGpx() {
        viewModelScope.launch {
            sendEffect(MainUiEffects.ShowDetailsBottomSheet(show = false))
            _uiState.updateMapUiState { uiState ->
                uiState.copy(
                    gpxDetails = null,
                    gpxLayerVisible = false,
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
        Logger.d { "UiEffect: $uiEffect" }
        when (uiEffect) {
            is MainUiEffects -> _mainUiEffects.send(uiEffect)
            is MapUiEffects -> _mapUiEffects.send(uiEffect)
        }
    }

    private fun initLogging() {
        uiState
            .onEach { Logger.d { "MainState: ${it.trimLongLists()}" } }
            .launchIn(viewModelScope)
    }
}
