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
import hu.mostoha.mobile.kmp.huki.model.domain.MyLocationStatus
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class MainViewModel(val permissionsController: PermissionsController) : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState.Default)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _mainUiEffects = Channel<MainUiEffects>()
    val mainUiEffects: Flow<MainUiEffects> = _mainUiEffects.receiveAsFlow()

    private val _mapUiEffects = Channel<MapUiEffects>()
    val mapUiEffects: Flow<MapUiEffects> = _mapUiEffects.receiveAsFlow()

    init {
        initLogging()
        initMyLocation()
    }

    fun onEvent(event: MainUiEvents) {
        Logger.d { "MainEvent: $event" }
        when (event) {
            MainUiEvents.MyLocationClicked -> enableMyLocation()
            MainUiEvents.FollowingDisabled -> _uiState.updateMyLocationState { uiState ->
                uiState.copy(myLocationStatus = MyLocationStatus.Default)
            }
            MainUiEvents.LayersClicked -> sendEffect(MainUiEffects.ShowLayersBottomSheet)
            is MainUiEvents.BaseLayerSelected -> _uiState.updateMapUiState { uiState ->
                uiState.copy(baseLayer = event.baseLayer)
            }
            MainUiEvents.HikingLayerSelected -> _uiState.updateMapUiState { uiState ->
                uiState.copy(hikingLayerVisible = uiState.hikingLayerVisible.not())
            }
            MainUiEvents.GpxLayerSelected -> {
                // TODO
            }
        }
    }

    private fun sendEffect(uiEffect: UiEffect) {
        viewModelScope.launch {
            Logger.d { "UiEffect: $uiEffect" }
            when (uiEffect) {
                is MainUiEffects -> _mainUiEffects.send(uiEffect)
                is MapUiEffects -> _mapUiEffects.send(uiEffect)
            }
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

    private fun initLogging() {
        uiState
            .onEach { Logger.d { "MainState: $it" } }
            .launchIn(viewModelScope)
    }
}
