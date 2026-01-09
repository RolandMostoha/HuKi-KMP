package hu.mostoha.mobile.kmp.huki.features.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import hu.mostoha.mobile.kmp.huki.model.domain.CameraPosition
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState.Default)

    @NativeCoroutinesState
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<MainUiEffects>()

    @NativeCoroutines
    val uiEffect: Flow<MainUiEffects> = _uiEffect.receiveAsFlow()

    fun onEvent(event: MainUiEvents) {
        when (event) {
            MainUiEvents.MyLocationClicked -> {
                val cameraPosition = CameraPosition(
                    latitude = 47.716808,
                    longitude = 18.895073,
                    zoom = 13.0,
                )
                _uiState.update { uiState ->
                    uiState.copy(
                        mapUiState = uiState.mapUiState.copy(cameraPosition = cameraPosition),
                    )
                }
                viewModelScope.launch {
                    _uiEffect.send(MainUiEffects.MoveCamera(cameraPosition))
                }
            }
        }
    }
}
