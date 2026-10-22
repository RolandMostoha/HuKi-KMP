package hu.mostoha.mobile.kmp.huki.features.destinations

import hu.mostoha.mobile.kmp.huki.features.main.UiEffect

sealed interface DestinationsUiEffects : UiEffect {
    data object NavigateBack : DestinationsUiEffects
    data object NavigateToAppSettings : DestinationsUiEffects
    data class OpenUrl(val url: String) : DestinationsUiEffects
}
