package hu.mostoha.mobile.kmp.huki.features.support

import hu.mostoha.mobile.kmp.huki.model.domain.ProductPrice
import hu.mostoha.mobile.kmp.huki.model.domain.SupportAnimal
import kotlin.time.Instant

/**
 * [celebrationId] increases with every completed purchase, the supporter card celebrates it.
 */
data class SupportUiState(
    val tiers: List<SupportTier> = SupportAnimal.entries.map { SupportTier(animal = it, price = null) },
    val pricesState: SupportPricesState = SupportPricesState.LOADING,
    val supporterAnimal: SupportAnimal? = null,
    val celebrationId: Int = 0,
    val supporterChoices: List<SupporterChoice> = emptyList(),
    val purchasingAnimal: SupportAnimal? = null,
    val purchaseMessage: SupportPurchaseMessage? = null,
    val alreadyPurchasedAnimal: SupportAnimal? = null,
    val scheduledDialogStartTime: Instant? = null,
    val isRestoring: Boolean = false,
    val restoreMessage: SupportRestoreMessage? = null,
) {
    companion object {
        val Default = SupportUiState()
    }
}

/**
 * [isActive] marks an owned subscription, which can't be bought again. [scheduledStartTime] marks a subscription
 * bought to replace the current one, starting at its renewal.
 */
data class SupportTier(
    val animal: SupportAnimal,
    val price: ProductPrice?,
    val isActive: Boolean = false,
    val scheduledStartTime: Instant? = null,
)

/**
 * An owned animal that can be shown as the supporter animal, [purchaseCount] is 1 for subscriptions.
 */
data class SupporterChoice(
    val animal: SupportAnimal,
    val purchaseCount: Int,
)

enum class SupportPricesState {
    LOADING,
    LOADED,
    UNAVAILABLE,
}

enum class SupportPurchaseMessage {
    PENDING,
    SCHEDULED,
    FAILED,
}

enum class SupportRestoreMessage {
    RESTORED,
    NOTHING_TO_RESTORE,
    FAILED,
}
