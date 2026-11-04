package hu.mostoha.mobile.kmp.huki.model.mapper

import androidx.datastore.preferences.core.Preferences
import hu.mostoha.mobile.kmp.huki.datastore.SupportPreferenceKeys
import hu.mostoha.mobile.kmp.huki.model.domain.BillingScheduledSubscription
import hu.mostoha.mobile.kmp.huki.model.domain.OneTimePurchaseRecord
import hu.mostoha.mobile.kmp.huki.model.domain.ScheduledSubscription
import hu.mostoha.mobile.kmp.huki.model.domain.SupportAnimal
import hu.mostoha.mobile.kmp.huki.model.domain.SupporterState
import kotlin.time.Instant

/**
 * Same format as the legacy HuKi-Android app ("productId;count;lastPurchaseTimeMillis"), so its records can be migrated.
 */
private const val ENTRY_SEPARATOR = ";"

fun String.toOneTimePurchaseRecord(): OneTimePurchaseRecord? {
    val parts = split(ENTRY_SEPARATOR)
    val productId = parts.getOrNull(0)?.takeIf { it.isNotBlank() } ?: return null
    val count = parts.getOrNull(1)?.toIntOrNull()?.takeIf { it > 0 } ?: return null
    val lastPurchaseTimeMillis = parts.getOrNull(2)?.toLongOrNull()?.takeIf { it >= 0 } ?: return null
    return OneTimePurchaseRecord(productId, count, lastPurchaseTimeMillis)
}

fun Set<String>.toOneTimePurchaseRecords(): List<OneTimePurchaseRecord> = mapNotNull { it.toOneTimePurchaseRecord() }

fun OneTimePurchaseRecord.toDataStoreEntry(): String =
    listOf(productId, count, lastPurchaseTimeMillis).joinToString(ENTRY_SEPARATOR)

fun List<OneTimePurchaseRecord>.toDataStoreEntries(): Set<String> = map { it.toDataStoreEntry() }.toSet()

fun BillingScheduledSubscription.toScheduledSubscription(): ScheduledSubscription? =
    SupportAnimal.fromProductId(productId)?.let { animal ->
        ScheduledSubscription(animal = animal, startTime = Instant.fromEpochMilliseconds(startTimeMillis))
    }

/**
 * The stored part of the state, the store reports the subscriptions.
 */
fun Preferences.toSupporterState(): SupporterState {
    val records = this[SupportPreferenceKeys.PURCHASED_ONE_TIME_PRODUCTS].orEmpty()
        .toOneTimePurchaseRecords()
        .mapNotNull { record -> SupportAnimal.fromProductId(record.productId)?.let { it to record } }
    return SupporterState(
        oneTimePurchaseCounts = records.associate { (animal, record) -> animal to record.count },
        chosenAnimal = this[SupportPreferenceKeys.CHOSEN_ANIMAL]?.let { SupportAnimal.fromProductId(it) },
        lastPurchaseTimes = records.associate { (animal, record) ->
            animal to Instant.fromEpochMilliseconds(record.lastPurchaseTimeMillis)
        },
    )
}
