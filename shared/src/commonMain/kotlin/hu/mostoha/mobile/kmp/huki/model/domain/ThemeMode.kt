package hu.mostoha.mobile.kmp.huki.model.domain

import dev.icerock.moko.resources.StringResource
import hu.mostoha.mobile.huki.shared.SharedRes

enum class ThemeMode(val title: StringResource) {
    SYSTEM(title = SharedRes.strings.settings_theme_system),
    LIGHT(title = SharedRes.strings.settings_theme_light),
    DARK(title = SharedRes.strings.settings_theme_dark),
}
