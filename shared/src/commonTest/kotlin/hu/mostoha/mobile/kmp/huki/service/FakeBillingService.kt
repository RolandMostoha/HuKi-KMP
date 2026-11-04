package hu.mostoha.mobile.kmp.huki.service

import hu.mostoha.mobile.kmp.huki.model.domain.BillingEntitlements
import hu.mostoha.mobile.kmp.huki.model.domain.BillingProduct
import hu.mostoha.mobile.kmp.huki.model.domain.BillingPurchase
import hu.mostoha.mobile.kmp.huki.model.domain.BillingPurchaseResult
import hu.mostoha.mobile.kmp.huki.model.domain.BillingScheduledSubscription

class FakeBillingService : BillingService {
    var products: List<BillingProduct> = emptyList()
    var errorMessage: String? = null
    var purchaseResult: BillingPurchaseResult = BillingPurchaseResult.Cancelled
    var entitlements: List<BillingPurchase> = emptyList()
    var entitlementsErrorMessage: String? = null
    var scheduledSubscription: BillingScheduledSubscription? = null

    /**
     * A successful purchase schedules this subscription instead of becoming an active entitlement.
     */
    var scheduledAfterPurchase: BillingScheduledSubscription? = null
    val requestedProductIds = mutableListOf<List<String>>()
    val purchasedProductIds = mutableListOf<String>()
    val consumedPurchaseTokens = mutableListOf<String>()
    var isPurchaseHeld = false
    private var heldPurchase: (() -> Unit)? = null
    private var purchaseListener: ((BillingPurchase) -> Unit)? = null

    override fun fetchProducts(
        productIds: List<String>,
        onSuccess: (List<BillingProduct>) -> Unit,
        onError: (String) -> Unit,
    ) {
        requestedProductIds += productIds
        errorMessage?.let(onError) ?: onSuccess(products.filter { it.productId in productIds })
    }

    override fun purchase(productId: String, onResult: (BillingPurchaseResult) -> Unit) {
        purchasedProductIds += productId
        val complete = {
            (purchaseResult as? BillingPurchaseResult.Success)?.let { result ->
                val scheduled = scheduledAfterPurchase
                if (scheduled != null) scheduledSubscription = scheduled else entitlements = entitlements + result.purchase
            }
            onResult(purchaseResult)
        }
        if (isPurchaseHeld) heldPurchase = complete else complete()
    }

    fun completeHeldPurchase() {
        heldPurchase?.invoke()
        heldPurchase = null
    }

    override fun fetchEntitlements(onSuccess: (BillingEntitlements) -> Unit, onError: (String) -> Unit) {
        entitlementsErrorMessage?.let(onError) ?: onSuccess(BillingEntitlements(entitlements, scheduledSubscription))
    }

    override fun consumePurchase(purchaseToken: String) {
        consumedPurchaseTokens += purchaseToken
    }

    override fun setPurchaseListener(listener: (BillingPurchase) -> Unit) {
        purchaseListener = listener
    }

    fun completePurchaseOutsideFlow(purchase: BillingPurchase) {
        entitlements = entitlements + purchase
        purchaseListener?.invoke(purchase)
    }
}
