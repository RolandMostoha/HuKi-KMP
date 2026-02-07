package hu.mostoha.mobile.kmp.huki.features.main

import hu.mostoha.mobile.kmp.huki.model.domain.Location
import hu.mostoha.mobile.kmp.huki.model.domain.MyLocationStatus

sealed interface MainUiEffects {
    data class UpdateCamera(
        val location: Location? = null,
        val zoom: Double? = null,
        val bearing: Double? = null,
        val pitch: Double? = null,
    ) : MainUiEffects

    data class ShowMyLocation(
        val myLocationStatus: MyLocationStatus,
        val animated: Boolean,
    ) : MainUiEffects

    data object NavigateToAppSettings : MainUiEffects
}
