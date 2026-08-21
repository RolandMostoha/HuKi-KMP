package hu.mostoha.mobile.kmp.huki.features.menu

import dev.icerock.moko.resources.StringResource
import hu.mostoha.mobile.kmp.huki.features.main.UiEffect

sealed interface MenuUiEffects : UiEffect {
    data object NavigateBack : MenuUiEffects
    data object NavigateToSettings : MenuUiEffects
    data object NavigateToRoutePlanner : MenuUiEffects
    data object NavigateToDestinations : MenuUiEffects
    data object NavigateToPlaceHistory : MenuUiEffects
    data object NavigateToGpxCollection : MenuUiEffects
    data object NavigateToGpxGuide : MenuUiEffects
    data object NavigateToTrailSymbolsGuide : MenuUiEffects
    data object NavigateToLocationIq : MenuUiEffects
    data class OpenUrl(val urlRes: StringResource) : MenuUiEffects
    data class SendEmail(
        val emailRes: StringResource,
        val subjectRes: StringResource,
    ) : MenuUiEffects
}
