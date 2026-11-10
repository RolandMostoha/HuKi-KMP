package hu.mostoha.mobile.kmp.huki.features.map

import hu.mostoha.mobile.kmp.huki.features.main.UiEffect
import hu.mostoha.mobile.kmp.huki.model.domain.CameraTarget
import hu.mostoha.mobile.kmp.huki.model.domain.ContentPadding
import hu.mostoha.mobile.kmp.huki.model.domain.MyLocationStatus
import hu.mostoha.mobile.kmp.huki.util.AnimationConstants

sealed interface MapUiEffects : UiEffect {
    data class UpdateCamera(
        val target: CameraTarget,
        val bearing: Double? = null,
        val pitch: Double? = null,
        val contentPadding: ContentPadding? = null,
        val animated: Boolean = true,
        val durationMs: Long = AnimationConstants.MAP_CAMERA_ANIM_DURATION.inWholeMilliseconds,
    ) : MapUiEffects

    data class ShowMyLocation(
        val myLocationStatus: MyLocationStatus,
        val maxDurationMs: Long,
    ) : MapUiEffects

    data class Zoom(val zoomIn: Boolean) : MapUiEffects

    data object ResetBearing : MapUiEffects
}
