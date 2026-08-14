package hu.mostoha.mobile.kmp.huki.features.gpxcollection

import hu.mostoha.mobile.kmp.huki.features.main.UiEffect

sealed interface GpxCollectionUiEffects : UiEffect {
    data object NavigateBack : GpxCollectionUiEffects
    data object NavigateToTutorial : GpxCollectionUiEffects
    data class OpenGpx(val fileUri: String) : GpxCollectionUiEffects
    data class ShareGpx(
        val fileUri: String,
        val fileName: String,
    ) : GpxCollectionUiEffects
}
