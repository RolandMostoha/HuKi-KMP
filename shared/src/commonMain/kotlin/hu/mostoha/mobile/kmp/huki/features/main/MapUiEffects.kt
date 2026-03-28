package hu.mostoha.mobile.kmp.huki.features.main

import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.MyLocationStatus

sealed interface MapUiEffects : UiEffect {
    data class UpdateCamera(
        val location: Location? = null,
        val zoom: Double? = null,
        val bearing: Double? = null,
        val pitch: Double? = null,
    ) : MapUiEffects

    data class ShowMyLocation(
        val myLocationStatus: MyLocationStatus,
        val animated: Boolean,
    ) : MapUiEffects
}
