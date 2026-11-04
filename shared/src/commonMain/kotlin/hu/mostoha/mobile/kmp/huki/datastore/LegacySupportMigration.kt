package hu.mostoha.mobile.kmp.huki.datastore

import androidx.datastore.core.DataMigration
import androidx.datastore.preferences.core.Preferences
import co.touchlab.kermit.Logger
import hu.mostoha.mobile.kmp.huki.model.domain.OneTimePurchaseRecord
import hu.mostoha.mobile.kmp.huki.model.mapper.toDataStoreEntries
import hu.mostoha.mobile.kmp.huki.model.mapper.toOneTimePurchaseRecords

/**
 * Moves the one-time support purchases of the legacy HuKi-Android app, which this app updates in place.
 * An unreadable legacy file is skipped for good, so it can never block reading the settings.
 */
class LegacySupportMigration(
    private val readLegacyPreferences: suspend () -> Preferences?,
) : DataMigration<Preferences> {

    override suspend fun shouldMigrate(currentData: Preferences): Boolean =
        currentData[SupportPreferenceKeys.LEGACY_MIGRATED] != true

    override suspend fun migrate(currentData: Preferences): Preferences {
        val legacy = try {
            readLegacyPreferences()
        } catch (exception: Exception) {
            Logger.e(exception) { "Support: Failed to read the legacy purchases." }
            null
        }
        val migrated = currentData.toMutablePreferences()
        if (legacy != null) {
            migrated[SupportPreferenceKeys.RECORDED_PURCHASE_TOKENS] =
                currentData[SupportPreferenceKeys.RECORDED_PURCHASE_TOKENS].orEmpty() +
                legacy[SupportPreferenceKeys.RECORDED_PURCHASE_TOKENS].orEmpty()
            migrated[SupportPreferenceKeys.PURCHASED_ONE_TIME_PRODUCTS] = mergeRecords(
                currentData[SupportPreferenceKeys.PURCHASED_ONE_TIME_PRODUCTS].orEmpty().toOneTimePurchaseRecords() +
                    legacy[SupportPreferenceKeys.PURCHASED_ONE_TIME_PRODUCTS].orEmpty().toOneTimePurchaseRecords(),
            ).toDataStoreEntries()
        }
        migrated[SupportPreferenceKeys.LEGACY_MIGRATED] = true
        return migrated.toPreferences()
    }

    override suspend fun cleanUp() = Unit

    private fun mergeRecords(records: List<OneTimePurchaseRecord>): List<OneTimePurchaseRecord> =
        records.groupBy { it.productId }.map { (productId, productRecords) ->
            OneTimePurchaseRecord(
                productId = productId,
                count = productRecords.sumOf { it.count },
                lastPurchaseTimeMillis = productRecords.maxOf { it.lastPurchaseTimeMillis },
            )
        }
}
