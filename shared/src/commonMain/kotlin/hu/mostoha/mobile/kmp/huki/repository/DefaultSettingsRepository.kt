package hu.mostoha.mobile.kmp.huki.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import hu.mostoha.mobile.kmp.huki.datastore.SettingsPreferenceKeys
import hu.mostoha.mobile.kmp.huki.model.domain.BaseLayer
import hu.mostoha.mobile.kmp.huki.model.domain.MapLabelScale
import hu.mostoha.mobile.kmp.huki.model.domain.ThemeMode
import hu.mostoha.mobile.kmp.huki.model.domain.UserPreferences
import hu.mostoha.mobile.kmp.huki.model.mapper.toUserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import okio.IOException

class DefaultSettingsRepository(private val dataStore: DataStore<Preferences>) : SettingsRepository {
    override val settings: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences -> preferences.toUserPreferences() }

    override suspend fun setMapZoomControlsVisible(visible: Boolean) {
        dataStore.edit { preferences ->
            preferences[SettingsPreferenceKeys.MAP_ZOOM_CONTROLS_VISIBLE] = visible
        }
    }

    override suspend fun setMapRotationEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SettingsPreferenceKeys.MAP_ROTATION_ENABLED] = enabled
        }
    }

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[SettingsPreferenceKeys.THEME_MODE] = themeMode.name
        }
    }

    override suspend fun setBaseLayer(baseLayer: BaseLayer) {
        dataStore.edit { preferences ->
            preferences[SettingsPreferenceKeys.BASE_LAYER] = baseLayer.name
        }
    }

    override suspend fun setMapLabelScale(mapLabelScale: MapLabelScale) {
        dataStore.edit { preferences ->
            preferences[SettingsPreferenceKeys.MAP_LABEL_SCALE_FOLLOW_SYSTEM] = mapLabelScale.followSystem
            preferences[SettingsPreferenceKeys.MAP_LABEL_SCALE_FACTOR] = mapLabelScale.factor
        }
    }

    override suspend fun setGpxDistancesVisible(visible: Boolean) {
        dataStore.edit { preferences ->
            preferences[SettingsPreferenceKeys.GPX_DISTANCES_VISIBLE] = visible
        }
    }

    override suspend fun setGpxSlopeColorsVisible(visible: Boolean) {
        dataStore.edit { preferences ->
            preferences[SettingsPreferenceKeys.GPX_SLOPE_COLORS_VISIBLE] = visible
        }
    }
}
