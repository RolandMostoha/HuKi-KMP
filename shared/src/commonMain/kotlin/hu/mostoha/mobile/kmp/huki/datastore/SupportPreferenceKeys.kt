package hu.mostoha.mobile.kmp.huki.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey

/**
 * The purchase keys match the legacy HuKi-Android app, so its records can be migrated.
 */
internal object SupportPreferenceKeys {
    val PURCHASED_ONE_TIME_PRODUCTS = stringSetPreferencesKey("support_purchased_one_time_products")
    val RECORDED_PURCHASE_TOKENS = stringSetPreferencesKey("support_recorded_purchase_tokens")
    val CHOSEN_ANIMAL = stringPreferencesKey("support_chosen_animal")
    val LEGACY_MIGRATED = booleanPreferencesKey("support_legacy_migrated")
}
