package hu.mostoha.mobile.kmp.huki.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

internal object SettingsPreferenceKeys {
    val MAP_ZOOM_CONTROLS_VISIBLE = booleanPreferencesKey("map_zoom_controls_visible")
    val WHATS_NEW_LAST_SEEN_VERSION = stringPreferencesKey("whats_new_last_seen_version")
}
