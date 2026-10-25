package hu.mostoha.mobile.kmp.huki.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import hu.mostoha.mobile.kmp.huki.datastore.RoutingPreferenceKeys
import hu.mostoha.mobile.kmp.huki.model.domain.RoutingQuotaState
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import okio.IOException
import kotlin.time.Instant

class DefaultRoutingQuotaStore(private val dataStore: DataStore<Preferences>) : RoutingQuotaStore {
    override suspend fun getQuotaState(now: Instant): RoutingQuotaState {
        val preferences = dataStore.data
            .catch { exception ->
                if (exception is IOException) emit(emptyPreferences()) else throw exception
            }
            .first()
        val reserveReachedUntil = preferences[RoutingPreferenceKeys.GRAPHHOPPER_RESERVE_REACHED_UNTIL] ?: 0L
        val blockedUntil = preferences[RoutingPreferenceKeys.GRAPHHOPPER_BLOCKED_UNTIL] ?: 0L
        val nowMillis = now.toEpochMilliseconds()

        return RoutingQuotaState(
            isReserveReached = nowMillis < reserveReachedUntil,
            isBlocked = nowMillis < blockedUntil,
        )
    }

    override suspend fun setReserveReachedUntil(until: Instant) {
        dataStore.edit { preferences ->
            preferences[RoutingPreferenceKeys.GRAPHHOPPER_RESERVE_REACHED_UNTIL] = until.toEpochMilliseconds()
        }
    }

    override suspend fun setBlockedUntil(until: Instant) {
        dataStore.edit { preferences ->
            preferences[RoutingPreferenceKeys.GRAPHHOPPER_BLOCKED_UNTIL] = until.toEpochMilliseconds()
        }
    }
}
