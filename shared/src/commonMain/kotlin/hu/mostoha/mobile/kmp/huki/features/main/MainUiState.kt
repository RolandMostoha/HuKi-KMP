package hu.mostoha.mobile.kmp.huki.features.main

import hu.mostoha.mobile.kmp.huki.features.map.MapUiState

data class MainUiState(val mapUiState: MapUiState = MapUiState.Default) {
    companion object {
        val Default = MainUiState()
    }
}
