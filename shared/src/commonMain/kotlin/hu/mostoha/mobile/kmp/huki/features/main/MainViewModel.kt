package hu.mostoha.mobile.kmp.huki.features.main

import androidx.lifecycle.ViewModel
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState.Default)

    @NativeCoroutinesState
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    fun onEvent(event: MainUiEvents) {
        when (event) {
            MainUiEvents.MyLocationClicked -> _uiState.update {
                it.copy(
                    mapUiState = it.mapUiState.copy(
                        latitude = 47.716808,
                        longitude = 18.895073,
                        zoomLevel = 13.0,
                    ),
                )
            }
        }
    }
}
