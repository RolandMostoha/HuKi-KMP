package hu.mostoha.mobile.kmp.huki.model.domain

import kotlin.time.Instant

/**
 * [purchaseToken] is unique per purchase: the Play purchase token or the App Store transaction ID.
 * [quantity] is above 1 when a one-time product is bought in multiple quantities in one purchase.
 * [purchaseTimeMillis] of a subscription is its start, renewals don't change it.
 */
data class BillingPurchase(
    val productId: String,
    val purchaseToken: String,
    val purchaseTimeMillis: Long,
    val quantity: Int,
)

/**
 * A subscription bought to replace the current one, but starting only at its renewal, e.g. an App Store downgrade.
 */
data class BillingScheduledSubscription(
    val productId: String,
    val startTimeMillis: Long,
)

/**
 * [purchases] are the active subscriptions plus the one-time purchases the store still knows about.
 */
data class BillingEntitlements(
    val purchases: List<BillingPurchase>,
    val scheduledSubscription: BillingScheduledSubscription? = null,
)

sealed interface BillingPurchaseResult {
    data class Success(val purchase: BillingPurchase) : BillingPurchaseResult

    /**
     * The subscription replaces the current one only from [startTime], it isn't active until then.
     */
    data class Scheduled(val startTime: Instant) : BillingPurchaseResult

    /**
     * Waiting for an approval (Ask to Buy, slow payment method), it completes through the purchase listener.
     */
    data object Pending : BillingPurchaseResult

    data object Cancelled : BillingPurchaseResult

    data class Failed(val message: String) : BillingPurchaseResult
}
