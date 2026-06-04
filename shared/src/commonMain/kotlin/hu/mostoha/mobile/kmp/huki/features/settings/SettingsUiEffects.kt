package hu.mostoha.mobile.kmp.huki.features.settings

import dev.icerock.moko.resources.StringResource
import hu.mostoha.mobile.kmp.huki.features.main.UiEffect

sealed interface SettingsUiEffects : UiEffect {
    data object NavigateBack : SettingsUiEffects
    data object NavigateToLocationIq : SettingsUiEffects
    data class OpenUrl(val urlRes: StringResource) : SettingsUiEffects
    data class SendEmail(
        val emailRes: StringResource,
        val subjectRes: StringResource,
    ) : SettingsUiEffects
}
