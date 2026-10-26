package hu.mostoha.mobile.kmp.huki.model.mapper

import androidx.datastore.preferences.core.Preferences
import hu.mostoha.mobile.kmp.huki.datastore.SettingsPreferenceKeys
import hu.mostoha.mobile.kmp.huki.model.domain.BaseLayer
import hu.mostoha.mobile.kmp.huki.model.domain.MapLabelScale
import hu.mostoha.mobile.kmp.huki.model.domain.ThemeMode
import hu.mostoha.mobile.kmp.huki.model.domain.UserPreferences

fun Preferences.toUserPreferences(): UserPreferences =
    UserPreferences(
        mapZoomControlsVisible = this[SettingsPreferenceKeys.MAP_ZOOM_CONTROLS_VISIBLE]
            ?: UserPreferences.DEFAULTS.mapZoomControlsVisible,
        mapRotationEnabled = this[SettingsPreferenceKeys.MAP_ROTATION_ENABLED]
            ?: UserPreferences.DEFAULTS.mapRotationEnabled,
        themeMode = this[SettingsPreferenceKeys.THEME_MODE].toThemeMode(),
        baseLayer = this[SettingsPreferenceKeys.BASE_LAYER].toBaseLayer(),
        mapLabelScale = MapLabelScale(
            followSystem = this[SettingsPreferenceKeys.MAP_LABEL_SCALE_FOLLOW_SYSTEM]
                ?: UserPreferences.DEFAULTS.mapLabelScale.followSystem,
            factor = this[SettingsPreferenceKeys.MAP_LABEL_SCALE_FACTOR]?.let(MapLabelScale::coerceFactor)
                ?: UserPreferences.DEFAULTS.mapLabelScale.factor,
        ),
    )

fun String?.toThemeMode(): ThemeMode =
    ThemeMode.entries.firstOrNull { it.name == this } ?: UserPreferences.DEFAULTS.themeMode

fun String?.toBaseLayer(): BaseLayer =
    BaseLayer.entries.firstOrNull { it.name == this } ?: UserPreferences.DEFAULTS.baseLayer
