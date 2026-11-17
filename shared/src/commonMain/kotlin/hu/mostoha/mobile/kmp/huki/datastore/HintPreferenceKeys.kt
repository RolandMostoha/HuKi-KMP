package hu.mostoha.mobile.kmp.huki.datastore

import androidx.datastore.preferences.core.stringSetPreferencesKey

internal object HintPreferenceKeys {
    val DISMISSED_HINTS = stringSetPreferencesKey("dismissed_hints")
}
