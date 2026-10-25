package hu.mostoha.mobile.kmp.huki.datastore

import androidx.datastore.preferences.core.longPreferencesKey

internal object RoutingPreferenceKeys {
    val GRAPHHOPPER_RESERVE_REACHED_UNTIL = longPreferencesKey("routing_graphhopper_reserve_reached_until")
    val GRAPHHOPPER_BLOCKED_UNTIL = longPreferencesKey("routing_graphhopper_blocked_until")
}
