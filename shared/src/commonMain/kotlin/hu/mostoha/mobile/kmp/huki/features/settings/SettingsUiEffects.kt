package hu.mostoha.mobile.kmp.huki.features.settings

import hu.mostoha.mobile.kmp.huki.features.main.UiEffect

sealed interface SettingsUiEffects : UiEffect {
    data object NavigateBack : SettingsUiEffects
}
