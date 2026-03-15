package hu.mostoha.mobile.kmp.huki.features.main

import hu.mostoha.mobile.kmp.huki.features.map.MapUiState
import hu.mostoha.mobile.kmp.huki.model.domain.MyLocationState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

data class MainUiState(
    val mapUiState: MapUiState = MapUiState.Default,
    val myLocationState: MyLocationState = MyLocationState.Default,
) {
    companion object {
        val Default = MainUiState()
    }
}

/**
 * Convenience function for updating nested UI states.
 */
fun MutableStateFlow<MainUiState>.updateMapUiState(reducer: (MapUiState) -> MapUiState) {
    this.update { uiState ->
        uiState.copy(
            mapUiState = reducer(uiState.mapUiState),
        )
    }
}

/**
 * Convenience function for updating nested UI states.
 */
fun MutableStateFlow<MainUiState>.updateMyLocationState(reducer: (MyLocationState) -> MyLocationState) {
    this.update { uiState ->
        uiState.copy(
            myLocationState = reducer(uiState.myLocationState),
        )
    }
}
