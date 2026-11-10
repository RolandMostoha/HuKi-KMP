package hu.mostoha.mobile.kmp.huki.model.domain

import kotlin.time.Instant

/**
 * [lastPurchaseTimes] holds the last purchase of one-time animals and the start of active subscriptions.
 */
data class SupporterState(
    val activeSubscriptions: Set<SupportAnimal> = emptySet(),
    val oneTimePurchaseCounts: Map<SupportAnimal, Int> = emptyMap(),
    val chosenAnimal: SupportAnimal? = null,
    val scheduledSubscription: ScheduledSubscription? = null,
    val lastPurchaseTimes: Map<SupportAnimal, Instant> = emptyMap(),
) {
    val ownedAnimals: List<SupportAnimal>
        get() = SupportAnimal.entries.filter { it in activeSubscriptions || it in oneTimePurchaseCounts }

    /**
     * The chosen animal while it's owned, otherwise an active subscription wins over one-time
     * purchases, the highest level within each.
     */
    val supporterAnimal: SupportAnimal?
        get() = chosenAnimal?.takeIf { it in ownedAnimals }
            ?: activeSubscriptions.minByOrNull { it.supportLevel }
            ?: oneTimePurchaseCounts.keys.minByOrNull { it.supportLevel }
}
