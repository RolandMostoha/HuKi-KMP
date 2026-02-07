package hu.mostoha.mobile.kmp.huki.features.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(val permissionsController: PermissionsController) : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState.Default)

    @NativeCoroutinesState
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<MainUiEffects>()

    @NativeCoroutines
    val uiEffect: Flow<MainUiEffects> = _uiEffect.receiveAsFlow()

    init {
        initLogging()
        initMyLocation()
    }

    fun onEvent(event: MainUiEvents) {
        Logger.d { "MainEvent: $event" }
        when (event) {
            MainUiEvents.MyLocationClicked -> enableMyLocation()
            MainUiEvents.FollowingDisabled -> _uiState.update { uiState ->
                uiState.copy(
                    myLocationState = uiState.myLocationState.copy(
                        myLocationStatus = MyLocationStatus.Default,
                    ),
                )
            }
        }
    }

    private fun sendEffect(effect: MainUiEffects) {
        viewModelScope.launch {
            Logger.d { "MainEffect: $effect" }
            _uiEffect.send(effect)
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
                        )
                    }
                    sendEffect(MainUiEffects.ShowMyLocation(newStatus, animated = true))
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
                _uiState.update { uiState ->
                    uiState.copy(
                        myLocationState = uiState.myLocationState.copy(
                            permissionState = when (exception) {
                                is DeniedAlwaysException -> PermissionState.DeniedAlways
                                is DeniedException -> PermissionState.Denied
                                else -> PermissionState.NotDetermined
                            },
                        ),
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
                )
            }
            sendEffect(MainUiEffects.ShowMyLocation(myLocationStatus, animated = false))
        }
    }

    private fun initLogging() {
        uiState
            .onEach { Logger.d { "MainState: $it" } }
            .launchIn(viewModelScope)
    }
}
