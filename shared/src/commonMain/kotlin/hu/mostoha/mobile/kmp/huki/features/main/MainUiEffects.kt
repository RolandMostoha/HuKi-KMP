package hu.mostoha.mobile.kmp.huki.features.main

interface UiEffect

sealed interface MainUiEffects : UiEffect {
    data object NavigateToAppSettings : MainUiEffects
    data class ShowLayersBottomSheet(val show: Boolean) : MainUiEffects
    data object ShowGpxFilePicker : MainUiEffects
}
