package hu.mostoha.mobile.kmp.huki.features.main

import hu.mostoha.mobile.kmp.huki.model.domain.CameraPosition

sealed interface MainUiEffects {
    data class MoveCamera(val cameraPosition: CameraPosition) : MainUiEffects
}
