package hu.mostoha.mobile.kmp.huki.features.main

import hu.mostoha.mobile.kmp.huki.features.map.MapUiState
import hu.mostoha.mobile.kmp.huki.model.domain.MyLocationState

data class MainUiState(
    val mapUiState: MapUiState = MapUiState.Default,
    val myLocationState: MyLocationState = MyLocationState.Default,
) {
    companion object {
        val Default = MainUiState()
    }
}
