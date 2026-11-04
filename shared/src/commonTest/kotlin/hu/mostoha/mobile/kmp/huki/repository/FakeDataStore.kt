package hu.mostoha.mobile.kmp.huki.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import okio.IOException

class FakeDataStore(initial: Preferences = emptyPreferences()) : DataStore<Preferences> {
    private val state = MutableStateFlow(initial)
    override val data: Flow<Preferences> = state
    var writeError: IOException? = null

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        writeError?.let { throw it }
        val updated = transform(state.value)
        state.value = updated
        return updated
    }
}
