package hu.mostoha.mobile.kmp.huki.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

internal object SettingsPreferenceKeys {
    val MAP_ZOOM_CONTROLS_VISIBLE = booleanPreferencesKey("map_zoom_controls_visible")
    val MAP_ROTATION_ENABLED = booleanPreferencesKey("map_rotation_enabled")
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val BASE_LAYER = stringPreferencesKey("base_layer")
    val MAP_LABEL_SCALE_FOLLOW_SYSTEM = booleanPreferencesKey("map_label_scale_follow_system")
    val MAP_LABEL_SCALE_FACTOR = floatPreferencesKey("map_label_scale_factor")
    val GPX_DISTANCES_VISIBLE = booleanPreferencesKey("gpx_distances_visible")
    val WHATS_NEW_LAST_SEEN_VERSION = stringPreferencesKey("whats_new_last_seen_version")
}
