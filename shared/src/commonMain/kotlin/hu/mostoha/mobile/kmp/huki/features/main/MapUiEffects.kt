package hu.mostoha.mobile.kmp.huki.features.main

import hu.mostoha.mobile.kmp.huki.model.domain.ContentPadding
import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.MyLocationStatus

sealed interface MapUiEffects : UiEffect {
    data class UpdateCamera(
        val bounds: List<Location>,
        val zoom: Double? = null,
        val bearing: Double? = null,
        val pitch: Double? = null,
        val contentPadding: ContentPadding? = null,
    ) : MapUiEffects

    data class ShowMyLocation(
        val myLocationStatus: MyLocationStatus,
        val animated: Boolean,
    ) : MapUiEffects
}
