package hu.mostoha.mobile.kmp.huki.features.map

import hu.mostoha.mobile.kmp.huki.features.main.UiEffect
import hu.mostoha.mobile.kmp.huki.model.domain.CameraTarget
import hu.mostoha.mobile.kmp.huki.model.domain.ContentPadding
import hu.mostoha.mobile.kmp.huki.model.domain.MyLocationStatus

sealed interface MapUiEffects : UiEffect {
    data class UpdateCamera(
        val target: CameraTarget,
        val bearing: Double? = null,
        val pitch: Double? = null,
        val contentPadding: ContentPadding? = null,
    ) : MapUiEffects

    data class ShowMyLocation(
        val myLocationStatus: MyLocationStatus,
        val animated: Boolean,
    ) : MapUiEffects

    data class Zoom(val zoomIn: Boolean) : MapUiEffects

    data object ResetBearing : MapUiEffects
}
