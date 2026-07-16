package hu.mostoha.mobile.kmp.huki.features.main

import hu.mostoha.mobile.kmp.huki.model.domain.Location

interface UiEffect

sealed interface MainUiEffects : UiEffect {
    data object NavigateToAppSettings : MainUiEffects
    data object ShowGpxFilePicker : MainUiEffects
    data class OpenMapsNavigation(val location: Location) : MainUiEffects
}
