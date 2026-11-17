package hu.mostoha.mobile.kmp.huki.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import co.touchlab.kermit.Logger
import hu.mostoha.mobile.kmp.huki.datastore.HintPreferenceKeys
import hu.mostoha.mobile.kmp.huki.model.domain.Hint
import hu.mostoha.mobile.kmp.huki.util.FeatureFlags
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import okio.IOException

class DefaultHintRepository(
    private val dataStore: DataStore<Preferences>,
    private val alwaysShowHints: Boolean = FeatureFlags.DEBUG_ALWAYS_SHOW_HINTS,
) : HintRepository {
    /**
     * Holds the dismissals of the current process while the stored ones are ignored.
     */
    private val sessionDismissedHints = MutableStateFlow(emptySet<Hint>())

    override val dismissedHints: Flow<Set<Hint>> = if (alwaysShowHints) {
        sessionDismissedHints
    } else {
        dataStore.data
            .catch { exception ->
                if (exception is IOException) emit(emptyPreferences()) else throw exception
            }
            .map { preferences ->
                val names = preferences[HintPreferenceKeys.DISMISSED_HINTS].orEmpty()
                Hint.entries.filterTo(mutableSetOf()) { it.name in names }
            }
    }

    override suspend fun dismissHint(hint: Hint) {
        if (alwaysShowHints) {
            sessionDismissedHints.value += hint
            return
        }
        try {
            dataStore.edit { preferences ->
                preferences[HintPreferenceKeys.DISMISSED_HINTS] =
                    preferences[HintPreferenceKeys.DISMISSED_HINTS].orEmpty() + hint.name
            }
        } catch (exception: IOException) {
            Logger.e(exception) { "Hints: Failed to store the dismissed hint $hint." }
        }
    }
}
