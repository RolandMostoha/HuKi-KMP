package hu.mostoha.mobile.kmp.huki.datastore

import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

const val SETTINGS_DATA_STORE_FILE_NAME = "huki.settings.preferences_pb"

fun createDataStore(
    migrations: List<DataMigration<Preferences>> = emptyList(),
    producePath: () -> String,
): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        migrations = migrations,
        produceFile = { producePath().toPath() },
    )
