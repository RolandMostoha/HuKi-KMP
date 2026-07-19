package hu.mostoha.mobile.kmp.huki.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import hu.mostoha.mobile.kmp.huki.WhatsNewContent
import hu.mostoha.mobile.kmp.huki.datastore.SettingsPreferenceKeys
import hu.mostoha.mobile.kmp.huki.model.domain.WhatsNew
import hu.mostoha.mobile.kmp.huki.model.mapper.toCurrentWhatsNew
import hu.mostoha.mobile.kmp.huki.model.mapper.toWhatsNewHistory
import hu.mostoha.mobile.kmp.huki.util.FeatureFlags
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import okio.IOException

class DefaultWhatsNewRepository(private val dataStore: DataStore<Preferences>) : WhatsNewRepository {
    override val currentWhatsNew: WhatsNew = WhatsNewContent.toCurrentWhatsNew()

    override val whatsNewHistory: List<WhatsNew> = WhatsNewContent.toWhatsNewHistory()

    override suspend fun shouldShowWhatsNew(): Boolean {
        if (FeatureFlags.ALWAYS_SHOW_WHATSNEW) {
            return true
        }
        val lastSeenVersion = dataStore.data
            .catch { exception ->
                if (exception is IOException) emit(emptyPreferences()) else throw exception
            }
            .map { preferences -> preferences[SettingsPreferenceKeys.WHATS_NEW_LAST_SEEN_VERSION] }
            .first()
        return lastSeenVersion != WhatsNewContent.currentVersion
    }

    override suspend fun markCurrentWhatsNewSeen() {
        dataStore.edit { preferences ->
            preferences[SettingsPreferenceKeys.WHATS_NEW_LAST_SEEN_VERSION] = WhatsNewContent.currentVersion
        }
    }
}
