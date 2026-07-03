package hu.mostoha.mobile.kmp.huki.features.destinations

import hu.mostoha.mobile.kmp.huki.features.main.UiEffect

sealed interface DestinationsUiEffects : UiEffect {
    data object NavigateBack : DestinationsUiEffects
    data object NavigateToAppSettings : DestinationsUiEffects
}
