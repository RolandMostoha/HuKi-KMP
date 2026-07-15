package hu.mostoha.mobile.kmp.huki.model.mapper

import androidx.datastore.preferences.core.Preferences
import hu.mostoha.mobile.kmp.huki.datastore.SettingsPreferenceKeys
import hu.mostoha.mobile.kmp.huki.model.domain.UserPreferences

fun Preferences.toUserPreferences(): UserPreferences =
    UserPreferences(
        mapZoomControlsVisible = this[SettingsPreferenceKeys.MAP_ZOOM_CONTROLS_VISIBLE]
            ?: UserPreferences.DEFAULTS.mapZoomControlsVisible,
    )
