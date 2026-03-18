package hu.mostoha.mobile.kmp.huki.features.main

interface UiEffect

sealed interface MainUiEffects : UiEffect {
    data object NavigateToAppSettings : MainUiEffects

    data object ShowLayersBottomSheet : MainUiEffects
}
